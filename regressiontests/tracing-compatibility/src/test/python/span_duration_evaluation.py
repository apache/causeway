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

"""Offline reference and bounded replay experiment. NOT a live tracing component."""

import base64
import json
from collections import Counter, OrderedDict
from dataclasses import dataclass


def key(span):
    return span.get("trace_id"), span.get("span_id")


def parent_key(span):
    return span.get("trace_id"), span.get("parent_id")


def new_orphans(before, after):
    """Only report edges whose parent existed in the input and was removed."""
    original = {key(s) for s in before}
    retained = {key(s) for s in after}
    return {key(s) for s in after if parent_key(s) in original - retained}


def model_bytes(spans):
    return len(json.dumps(list(spans), sort_keys=True, separators=(",", ":")).encode())


def span_wire_bytes(spans):
    """Exact sum of captured Span message payloads, NOT complete OTLP request bytes."""
    return sum(len(base64.b64decode(s["wire"])) for s in spans)


@dataclass(frozen=True)
class Decision:
    spans: tuple
    reason: str


def retain(spans, reason):
    return Decision(tuple(spans), reason)


def oracle(spans, threshold_ns, *, closed):
    """Prune only explicitly closed datasets. Never mutate a retained span."""
    if threshold_ns < 0:
        raise ValueError("threshold must be non-negative")
    spans = tuple(spans)
    if not closed:
        return retain(spans, "incomplete")
    indexed = {key(s): s for s in spans}
    if len(indexed) != len(spans) or any(not t or not i for t, i in indexed):
        return retain(spans, "malformed")

    # Validate every ancestry chain, including otherwise uninteresting cycles.
    visited = set()
    for start in indexed:
        path = set()
        node = start
        while node in indexed and node not in visited:
            if node in path:
                return retain(spans, "malformed")
            path.add(node)
            node = parent_key(indexed[node])
        visited.update(path)

    keep = set()
    for k, s in indexed.items():
        start, end = s.get("start_ns"), s.get("end_ns")
        valid = type(start) is int and type(end) is int and 0 <= start <= end
        useful = (parent_key(s) not in indexed or s.get("error") is not False
                  or not valid or end - start >= threshold_ns
                  or any(e.get("name") == "exception" for e in s.get("events", [])))
        if useful:
            node = k
            while node in indexed and node not in keep:
                keep.add(node)
                node = parent_key(indexed[node])
    return Decision(tuple(s for s in spans if key(s) in keep), "closed-manifest")


class ReplayBuffer:
    """Finite replay state; pruning requires a trusted fixture-only close event.

    unsafe_window is an intentionally incorrect negative control. Neither mode is
    a production processor. Even a trusted close cannot make an unexpected later
    child safe after its parent was already pruned; tests expose that limitation.
    """

    def __init__(self, sink, *, threshold_ns=10_000_000, max_traces=8,
                 max_spans=256, max_bytes=1_048_576, max_age=100,
                 max_decisions=8, decision_ttl=100, unsafe_window=False):
        if min(max_traces, max_spans, max_bytes, max_age, max_decisions, decision_ttl) <= 0:
            raise ValueError("bounds must be positive")
        if threshold_ns < 0:
            raise ValueError("threshold must be non-negative")
        self.sink = sink
        self.threshold = threshold_ns
        self.max_traces, self.max_spans, self.max_bytes = max_traces, max_spans, max_bytes
        self.max_age, self.max_decisions, self.decision_ttl = max_age, max_decisions, decision_ttl
        self.unsafe_window = unsafe_window
        self.pending = OrderedDict()
        self.decisions = OrderedDict()
        self.pending_spans = self.pending_bytes = 0
        self.peak_spans = self.peak_traces = self.peak_bytes = self.peak_decisions = 0
        self.uncertain = False
        self.now = 0
        self.reasons = Counter()
        self.delivery_failed = 0
        self.pruned = 0

    def _emit(self, spans, reason):
        self.reasons[reason] += 1
        try:
            self.sink(tuple(spans))
        except (OSError, TimeoutError):
            # No hidden/unbounded retry queue. Model existing delivery failure,
            # separately from intentional pruning, and retain future input.
            self.delivery_failed += len(spans)
            self.uncertain = True

    def _take(self, trace_id):
        _, spans, size = self.pending.pop(trace_id)
        self.pending_spans -= len(spans)
        self.pending_bytes -= size
        return spans

    def _fallback(self, reason):
        self.uncertain = True
        while self.pending:
            self._emit(self._take(next(iter(self.pending))), reason)

    def advance(self, now):
        if now < self.now:
            raise ValueError("replay clock must be monotonic")
        self.now = now
        for trace, when in list(self.decisions.items()):
            if now - when >= self.decision_ttl:
                del self.decisions[trace]
                self.reasons["decision-expired"] += 1
        expired = [t for t, (start, _, _) in self.pending.items() if now - start >= self.max_age]
        if expired and not self.unsafe_window:
            self._fallback("timeout-retain")
        else:
            for trace in expired:
                self._decide(trace, "UNSAFE-window")

    def feed(self, span, now=0):
        self.advance(now)
        trace = span.get("trace_id")
        if trace in self.decisions:
            # Detection is not a repair: an earlier pruned parent cannot be
            # recovered from this bounded decision cache.
            self._fallback("late-after-close")
            self._emit((span,), "late-after-close")
            return
        size = model_bytes((span,))
        if (self.uncertain or not trace or size > self.max_bytes):
            self._fallback("uncertain-retain")
            self._emit((span,), "pass-through")
            return
        if ((trace not in self.pending and len(self.pending) >= self.max_traces)
                or self.pending_spans >= self.max_spans
                or self.pending_bytes + size > self.max_bytes):
            # Eviction/pressure releases all pending data and latches the session
            # into pass-through, rather than forgetting decisions and pruning.
            self._fallback("pressure-retain")
            self._emit((span,), "pass-through")
            return
        if trace not in self.pending:
            self.pending[trace] = [now, [], 0]
        self.pending[trace][1].append(span)
        self.pending[trace][2] += size
        self.pending_spans += 1
        self.pending_bytes += size
        self.peak_spans = max(self.peak_spans, self.pending_spans)
        self.peak_traces = max(self.peak_traces, len(self.pending))
        self.peak_bytes = max(self.peak_bytes, self.pending_bytes)

    def _decide(self, trace, reason):
        spans = self._take(trace)
        decision = oracle(spans, self.threshold, closed=True)
        self.pruned += len(spans) - len(decision.spans)
        self._emit(decision.spans, reason + ":" + decision.reason)
        self.decisions[trace] = self.now
        while len(self.decisions) > self.max_decisions:
            self.decisions.popitem(last=False)
            self.reasons["decision-evicted"] += 1
        self.peak_decisions = max(self.peak_decisions, len(self.decisions))

    def close_fixture(self, trace, expected_ids):
        """External fixture manifest, NOT an available distributed close signal."""
        if trace not in self.pending:
            return
        spans = self.pending[trace][1]
        actual = {s.get("span_id") for s in spans}
        if (self.uncertain or len(expected_ids) > self.max_spans
                or len(actual) != len(spans) or actual != set(expected_ids)):
            self._fallback("manifest-mismatch-retain")
            return
        self._decide(trace, "trusted-fixture")

    def shutdown(self):
        self._fallback("shutdown-retain")


def synthetic_span(i, parent="", *, trace="trace", duration=1_000_000, error=False, **metadata):
    return dict(trace_id=trace, span_id=str(i), parent_id=str(parent),
                start_ns=0, end_ns=duration, error=error, **metadata)
