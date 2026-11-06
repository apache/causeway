## 1. Internal diagnostics and loader

- [x] 1.1 Add thread-local retained frames, safe caller/state snapshots, best-effort identity-deduplicated overflow reporting and cleanup with focused helper tests.
- [x] 1.2 Wrap v4's common nullable-load path without changing existing decisions, and test nested failures, current state, phase downgrade, substitution/filtering/null and subsequent cleanup.

## 2. Deterministic traversal and real regression

- [x] 2.1 Sort the shared mixin registry stream by fully qualified class name and verify insertion-order independence.
- [x] 2.2 Add a representative cyclic mixed-in collection/action-element graph and repeated real metamodel bootstrap assertions for navigation synthesis and explicit order.

## 3. Documentation and validation

- [x] 3.1 Document diagnostics, unchanged failure semantics, limitations and ordering, and update the roadmap without including 4061.
- [x] 3.2 Run focused tests, representative/domain-model regressions, a non-gating overhead sample, documentation rendering and strict validation; record versions, results and limitations.
