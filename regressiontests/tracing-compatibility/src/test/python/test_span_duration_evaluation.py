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

import copy
import json
import unittest
from pathlib import Path

from span_duration_evaluation import ReplayBuffer, key, new_orphans, oracle, synthetic_span as span
from run_span_duration_evaluation import assert_same_spans


class OracleTest(unittest.TestCase):
    def test_sanitized_committed_manifest(self):
        # given: a stable synthetic graph with independently declared expectations
        fixture = json.loads((Path(__file__).parent / "fixtures/closed-reference.json").read_text())
        graph = fixture["spans"]
        # when
        output = oracle(graph, fixture["threshold_ns"], closed=True).spans
        # then
        self.assertEqual(fixture["expected_retained"], [s["span_id"] for s in output])
        self.assertEqual(set(fixture["expected_pruned"]), {s["span_id"] for s in graph} - {s["span_id"] for s in output})
        self.assertFalse(new_orphans(graph, output))

    def test_trivial_subtree_pruned_but_root_and_boundary_retained(self):
        # given: a trivial subtree and a span exactly on the threshold
        graph = [span("root"), span("p", "root", duration=1), span("q", "p", duration=1), span("boundary", "root", duration=10)]
        # when
        output = oracle(graph, 10, closed=True).spans
        # then
        self.assertEqual(["root", "boundary"], [s["span_id"] for s in output])
        self.assertFalse(new_orphans(graph, output))

    def test_slow_and_failed_descendants_retain_short_ancestors(self):
        # given
        graph = [span("r", duration=1), span("p", "r", duration=1),
                 span("slow", "p", duration=20), span("failed", "p", error=True), span("trivial", "r", duration=1)]
        # when
        output = oracle(graph, 10, closed=True).spans
        # then
        self.assertEqual({"r", "p", "slow", "failed"}, {s["span_id"] for s in output})
        self.assertFalse(new_orphans(graph, output))

    def test_exception_event_without_error_status_and_unknown_data_retained(self):
        # given
        graph = [span("r"), span("exception", "r", events=[{"name": "exception"}]),
                 span("invalid", "r"), span("unknown", "r", error=None)]
        graph[2]["end_ns"] = -1
        # when
        output = oracle(graph, 10_000_000, closed=True).spans
        # then
        self.assertEqual(tuple(graph), output)

    def test_external_parent_and_metadata_not_rewritten(self):
        # given: an external parent and opaque captured metadata/links
        graph = [span("r", "outside", name="HTTP", attributes={"tenant": "fixture"},
                      events=[{"name": "event"}], links=[{"trace_id": "other", "span_id": "linked"}], wire="AA=="),
                 span("child", "r")]
        before = copy.deepcopy(graph)
        # when
        output = oracle(graph, 10_000_000, closed=True).spans
        # then
        self.assertEqual(before, graph)
        self.assertEqual((graph[0],), output)
        self.assertIs(graph[0], output[0])
        self.assertEqual("outside", output[0]["parent_id"])
        self.assertFalse(new_orphans(graph, output))

    def test_incomplete_duplicate_and_cyclic_graphs_fail_open(self):
        # given
        graphs = [[span("r"), span("c", "r")], [span("r"), span("r")],
                  [span("r"), span("a", "b"), span("b", "a")]]
        # when / then: uncertainty or malformed topology never justifies pruning
        self.assertEqual(tuple(graphs[0]), oracle(graphs[0], 10_000_000, closed=False).spans)
        for graph in graphs[1:]:
            result = oracle(graph, 10_000_000, closed=True)
            self.assertEqual("malformed", result.reason)
            self.assertEqual(tuple(graph), result.spans)

    def test_child_before_parent_and_cross_trace_ids_are_independent(self):
        # given
        graph = [span("c", "r", trace="a", duration=20), span("r", trace="a", duration=1),
                 span("r", trace="b", duration=1), span("c", "r", trace="b", duration=1)]
        # when
        output = oracle(graph, 10, closed=True).spans
        # then
        self.assertEqual({("a", "c"), ("a", "r"), ("b", "r")}, {key(s) for s in output})
        self.assertFalse(new_orphans(graph, output))

    def test_zero_and_negative_thresholds(self):
        # given
        graph = [span("r"), span("c", "r")]
        # when / then
        self.assertEqual(tuple(graph), oracle(graph, 0, closed=True).spans)
        with self.assertRaises(ValueError):
            oracle(graph, -1, closed=True)


class CorpusComparisonTest(unittest.TestCase):
    def test_cross_trace_batch_order_does_not_change_graph_equality(self):
        # given: identical payloads in different trace-batch order
        graph = [span("a", trace="a"), span("b", trace="b")]
        # when / then
        assert_same_spans(graph, list(reversed(graph)))

    def test_missing_changed_and_duplicate_payloads_are_rejected(self):
        # given
        graph = [span("a", trace="a"), span("b", trace="b")]
        changed = [dict(graph[0], error=True), graph[1]]
        # when / then: ignoring order must not hide real corruption
        for actual in [graph[:1], changed, [graph[0], graph[0], graph[1]]]:
            with self.assertRaises(AssertionError):
                assert_same_spans(graph, actual)


class ReplayTest(unittest.TestCase):
    def test_trusted_fixture_close_matches_oracle(self):
        # given: out-of-order input, with a manifest provided by the test driver
        graph = [span("q", "p"), span("root"), span("p", "root")]
        received = []
        buffer = ReplayBuffer(received.extend)
        # when
        for s in graph:
            buffer.feed(s)
        buffer.close_fixture("trace", ["root", "p", "q"])
        # then
        self.assertEqual(oracle(graph, buffer.threshold, closed=True).spans, tuple(received))
        self.assertEqual(0, buffer.pending_spans)
        self.assertFalse(new_orphans(graph, received))

    def test_timeout_is_retention_not_assumed_completion(self):
        # given
        graph = [span("root"), span("p", "root"), span("late", "p", error=True)]
        received = []
        buffer = ReplayBuffer(received.extend, max_age=10)
        # when: a useful child arrives after the wait window
        buffer.feed(graph[0]); buffer.feed(graph[1])
        buffer.advance(11)
        buffer.feed(graph[2], 12)
        # then
        self.assertEqual(graph, received)
        self.assertTrue(buffer.uncertain)
        self.assertEqual(0, buffer.pruned)
        self.assertFalse(new_orphans(graph, received))

    def test_unsafe_window_negative_control_orphans_late_child(self):
        # given: a deliberately unsafe candidate, not an available production option
        graph = [span("root"), span("p", "root"), span("late", "p", error=True)]
        received = []
        buffer = ReplayBuffer(received.extend, max_age=10, unsafe_window=True)
        # when
        buffer.feed(graph[0]); buffer.feed(graph[1]); buffer.advance(11)
        buffer.feed(graph[2], 12)
        # then: the test succeeds by detecting/rejecting the candidate's claim
        self.assertEqual({("trace", "late")}, new_orphans(graph, received))
        self.assertEqual(1, buffer.pruned)

    def test_closed_manifest_cannot_protect_against_unexpected_later_child(self):
        # given: even an apparently trusted close can be false in a live system
        graph = [span("root"), span("p", "root"), span("unexpected", "p", error=True)]
        received = []
        buffer = ReplayBuffer(received.extend)
        # when
        buffer.feed(graph[0]); buffer.feed(graph[1])
        buffer.close_fixture("trace", ["root", "p"])
        buffer.feed(graph[2])
        # then: noticing the late arrival cannot restore an already pruned parent
        self.assertEqual({("trace", "unexpected")}, new_orphans(graph, received))
        self.assertGreater(buffer.reasons["late-after-close"], 0)

    def test_expired_decision_cache_does_not_recover_pruned_parent(self):
        # given
        graph = [span("root"), span("p", "root"), span("late", "p", error=True)]
        received = []
        buffer = ReplayBuffer(received.extend, decision_ttl=10)
        # when
        buffer.feed(graph[0]); buffer.feed(graph[1]); buffer.close_fixture("trace", ["root", "p"])
        buffer.advance(11); buffer.feed(graph[2], 12); buffer.shutdown()
        # then
        self.assertEqual({("trace", "late")}, new_orphans(graph, received))
        self.assertEqual(1, buffer.reasons["decision-expired"])

    def test_trace_span_and_byte_pressure_retain_all_with_bounded_state(self):
        for limits in [dict(max_traces=1), dict(max_spans=1), dict(max_bytes=1)]:
            with self.subTest(limits=limits):
                # given
                graph = [span("a", trace="a"), span("b", trace="b")]
                received = []
                buffer = ReplayBuffer(received.extend, **limits)
                # when
                for s in graph:
                    buffer.feed(s)
                buffer.shutdown()
                # then
                self.assertEqual(graph, received)
                self.assertLessEqual(buffer.peak_spans, buffer.max_spans)
                self.assertLessEqual(buffer.peak_traces, buffer.max_traces)
                self.assertLessEqual(buffer.peak_bytes, buffer.max_bytes)
                self.assertEqual(0, buffer.pending_spans)
                self.assertEqual(0, buffer.pruned)

    def test_decision_eviction_is_bounded_not_a_late_span_guarantee(self):
        # given
        received = []
        buffer = ReplayBuffer(received.extend, max_decisions=2)
        # when
        for t in range(10):
            trace = str(t)
            buffer.feed(span("r", trace=trace))
            buffer.close_fixture(trace, ["r"])
        # then
        self.assertEqual(2, len(buffer.decisions))
        self.assertEqual(2, buffer.peak_decisions)
        self.assertEqual(8, buffer.reasons["decision-evicted"])

    def test_manifest_mismatch_and_shutdown_retain_pending_input(self):
        # given
        graph = [span("r"), span("p", "r")]
        for close in [True, False]:
            received = []
            buffer = ReplayBuffer(received.extend)
            # when
            for s in graph:
                buffer.feed(s)
            if close:
                buffer.close_fixture("trace", ["r", "p", "missing"])
            buffer.shutdown()
            # then
            self.assertEqual(graph, received)
            self.assertEqual(0, buffer.pending_spans)
            self.assertEqual(0, buffer.pruned)

    def test_exporter_and_backpressure_failures_are_not_counted_as_pruning(self):
        for failure in [OSError("export failed"), TimeoutError("backpressure deadline")]:
            # given: synchronous bounded failure, with no unbounded retry queue
            def sink(_):
                raise failure
            buffer = ReplayBuffer(sink)
            # when
            buffer.feed(span("r")); buffer.feed(span("p", "r")); buffer.shutdown()
            # then
            self.assertEqual(2, buffer.delivery_failed)
            self.assertEqual(0, buffer.pruned)
            self.assertEqual(0, buffer.pending_spans)
            self.assertTrue(buffer.uncertain)

    def test_replay_clock_and_limits_are_checked(self):
        # given
        buffer = ReplayBuffer(lambda _: None)
        buffer.advance(10)
        # when / then
        with self.assertRaises(ValueError):
            buffer.feed(span("r"), 9)
        with self.assertRaises(ValueError):
            ReplayBuffer(lambda _: None, max_spans=0)


if __name__ == "__main__":
    unittest.main()
