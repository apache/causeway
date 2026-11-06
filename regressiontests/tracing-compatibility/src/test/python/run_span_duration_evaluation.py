# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""Reproducible offline measurements, never a Collector/exporter implementation."""

import argparse
import json
import platform
import statistics
import time
import tracemalloc
from collections import Counter, defaultdict
from pathlib import Path

from span_duration_evaluation import ReplayBuffer, key, model_bytes, new_orphans, oracle, span_wire_bytes, synthetic_span

THRESHOLD_NS = 10_000_000  # Experiment parameter, NOT a production default.
CORPUS_BOUNDS = dict(max_traces=128, max_spans=4096, max_bytes=16_777_216,
                     max_age=100, max_decisions=128, decision_ttl=100)
WARMUPS = 3
REPETITIONS = 10


def replay(graph, bounds=None):
    received = []
    buffer = ReplayBuffer(received.extend, threshold_ns=THRESHOLD_NS, **(bounds or {}))
    groups = defaultdict(list)
    for span in graph:
        buffer.feed(span)
        groups[span["trace_id"]].append(span["span_id"])
    for trace, manifest in groups.items():
        buffer.close_fixture(trace, manifest)
    buffer.shutdown()
    return tuple(received), buffer


def assert_safety(graph, output):
    assert not new_orphans(graph, output), "Filtering introduced a missing parent"
    by_id = {key(s): s for s in graph}
    for span in output:
        assert span == by_id[key(span)], "Retained metadata changed"
    protected = {key(s) for s in graph if s["error"] or s["end_ns"] - s["start_ns"] >= THRESHOLD_NS}
    assert protected <= {key(s) for s in output}, "Slow/error data lost"


def assert_same_spans(expected, actual):
    """OTLP batches across traces have no required relative ordering."""
    expected_by_id = {key(s): s for s in expected}
    actual_by_id = {key(s): s for s in actual}
    assert len(expected_by_id) == len(expected), "Duplicate expected span identity"
    assert len(actual_by_id) == len(actual), "Duplicate replayed span identity"
    assert expected_by_id == actual_by_id, "Replay differs from oracle identities/payloads"


def corpus(directory):
    records = []
    modes = set()
    for file in sorted(directory.glob("*.json")):
        capture = json.loads(file.read_text())
        assert capture["format"] == "causeway-duration-corpus-v1"
        graph = capture["spans"]
        if capture["mode"] not in ("boot", "agent") or not graph:
            continue
        assert capture["closed"] is True
        assert Counter(capture["observed_manifest"]) == Counter(":".join(key(s)) for s in graph)
        assert len({key(s) for s in graph}) == len(graph), "Duplicate captured span identity"
        output = oracle(graph, THRESHOLD_NS, closed=True)
        assert output.reason == "closed-manifest"
        replayed, buffer = replay(graph, CORPUS_BOUNDS)
        assert_same_spans(output.spans, replayed)
        assert_safety(graph, output.spans)
        assert_safety(graph, replayed)
        modes.add(capture["mode"])
        records.append(dict(file=file.name, mode=capture["mode"], fixture=capture["fixture"],
                            input_spans=len(graph), retained_spans=len(output.spans),
                            input_span_message_bytes=span_wire_bytes(graph),
                            retained_span_message_bytes=span_wire_bytes(output.spans),
                            error_spans=sum(s["error"] for s in graph),
                            server_spans=sum(s["kind"] == "SPAN_KIND_SERVER" for s in graph),
                            sql_named_spans=sum(s["name"].startswith(("SELECT", "INSERT", "UPDATE", "DELETE")) for s in graph),
                            fixture_jdbc_spans=sum(s["name"] == "fixture.jdbc" for s in graph),
                            external_parent_edges=sum(bool(s["parent_id"]) and
                                (s["trace_id"], s["parent_id"]) not in {key(x) for x in graph} for s in graph),
                            peak_spans=buffer.peak_spans, peak_bytes=buffer.peak_bytes,
                            new_orphans=0))
    assert modes == {"boot", "agent"}, "Both ownership-mode captures are required"
    totals = {}
    for mode in sorted(modes):
        rows = [r for r in records if r["mode"] == mode]
        totals[mode] = {name: sum(r[name] for r in rows) for name in
                       ["input_spans", "retained_spans", "input_span_message_bytes", "retained_span_message_bytes",
                        "error_spans", "server_spans", "sql_named_spans", "fixture_jdbc_spans", "external_parent_edges"]}
        totals[mode]["captures"] = len(rows)
        totals[mode]["new_orphans"] = 0
    return dict(boundary="offline regenerated OTLP Span messages at receiver; excludes request/resource/scope overhead",
                closure="observed dataset manifest plus successful fixture tests, NOT distributed completeness",
                totals=totals, captures=records)


def workloads():
    small = [synthetic_span("root")] + [synthetic_span(str(i), "root") for i in range(31)]
    deep = [synthetic_span(str(i), "" if i == 0 else str(i - 1)) for i in range(1024)]
    deep_error = [dict(s) for s in deep]
    deep_error[-1]["error"] = True
    concurrent = [synthetic_span(str(i), "" if i == 0 else "0", trace=str(t))
                  for i in range(32) for t in range(64)]
    return dict(small=small, deep=deep, deep_error=deep_error, concurrent=concurrent)


def measure(fn):
    for _ in range(WARMUPS):
        fn()
    wall, cpu = [], []
    for _ in range(REPETITIONS):
        started, process = time.perf_counter_ns(), time.process_time_ns()
        fn()
        wall.append(time.perf_counter_ns() - started)
        cpu.append(time.process_time_ns() - process)
    # Separate traced-allocation run to avoid contaminating measured timings.
    tracemalloc.start()
    fn()
    _, peak = tracemalloc.get_traced_memory()
    tracemalloc.stop()
    return dict(median_elapsed_ns=int(statistics.median(wall)),
                median_cpu_ns=int(statistics.median(cpu)), peak_traced_python_bytes=peak)


def benchmark():
    rows = []
    for name, graph in workloads().items():
        filtered = oracle(graph, THRESHOLD_NS, closed=True).spans
        replayed, bounded = replay(graph)
        within, configured = replay(graph, CORPUS_BOUNDS)
        assert_same_spans(filtered, within)
        assert_safety(graph, filtered)
        assert_safety(graph, replayed)
        rows.append(dict(workload=name, input_spans=len(graph), retained_spans=len(filtered),
                         input_json_model_bytes=model_bytes(graph), retained_json_model_bytes=model_bytes(filtered),
                         default_replay_retained_spans=len(replayed),
                         default_replay_reasons=dict(bounded.reasons),
                         default_peak_spans=bounded.peak_spans, default_peak_traces=bounded.peak_traces,
                         default_peak_pending_model_bytes=bounded.peak_bytes,
                         corpus_preset_peak_spans=configured.peak_spans,
                         retain_all=measure(lambda: tuple(graph)),
                         oracle=measure(lambda: oracle(graph, THRESHOLD_NS, closed=True)),
                         default_replay=measure(lambda: replay(graph))))
    return dict(warmups=WARMUPS, repetitions=REPETITIONS, threshold_ns=THRESHOLD_NS,
                default_bounds=dict(max_traces=8, max_spans=256, max_bytes=1_048_576,
                                    max_age=100, max_decisions=8, decision_ttl=100),
                corpus_bounds=CORPUS_BOUNDS, workloads=rows)


def controls():
    graph = [synthetic_span("root"), synthetic_span("p", "root"),
             synthetic_span("late-error", "p", error=True)]
    results = {}
    for name, unsafe, false_manifest, expire in [("retention_timeout", False, False, False),
            ("unsafe_wait_window", True, False, False), ("unexpected_after_close", False, True, False),
            ("expired_close_cache", False, True, True)]:
        received = []
        buffer = ReplayBuffer(received.extend, max_age=10, decision_ttl=10 if expire else 100, unsafe_window=unsafe)
        buffer.feed(graph[0]); buffer.feed(graph[1])
        if false_manifest:
            buffer.close_fixture("trace", ["root", "p"])
        buffer.advance(11)
        buffer.feed(graph[2], 12)
        buffer.shutdown()
        results[name] = dict(new_orphans=len(new_orphans(graph, received)),
                             pruned=buffer.pruned, reasons=dict(buffer.reasons),
                             cache_expiry_expected=expire)
    assert results["retention_timeout"]["new_orphans"] == 0
    assert all(results[name]["new_orphans"] == 1 for name in results if name != "retention_timeout")
    # Model controls only: entire traces vs UI projection, not real integrations.
    closed = [synthetic_span("r"), synthetic_span("p", "r")]
    results["whole_fast_trace_sampling_model"] = dict(input_spans=2, retained_spans=0, whole_traces_dropped=1)
    results["ui_projection_model"] = dict(stored_spans=2,
            visible_spans=len(oracle(closed, THRESHOLD_NS, closed=True).spans), export_bytes_saved=0)
    return results


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--captures", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    result = dict(format="causeway-duration-evaluation-v1", python=platform.python_version(),
                  platform=platform.platform(), corpus=corpus(args.captures),
                  benchmark=benchmark(), controls=controls(),
                  recommendation="NO-GO for unrestricted live pruning; conditional offline closed-dataset feasibility only")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(dict(output=str(args.output), recommendation=result["recommendation"],
                         corpus=result["corpus"]["totals"]), indent=2))


if __name__ == "__main__":
    main()
