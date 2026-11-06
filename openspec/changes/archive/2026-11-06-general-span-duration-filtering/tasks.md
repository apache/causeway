## 1. Baseline and architecture comparison

- [x] 1.1 Inventory the current Boot/Micrometer/OTel/agent versions, SDK/export ownership and tracing fixture entry points; establish the retain-all baseline and start `evaluation.adoc` with reproducible environment/commands.
- [x] 1.2 Compare JVM processor/exporter, Collector and backend/UI locations using current version-identified documentation/source; distinguish individual-span pruning from whole-trace sampling and display-only collapsing, and record early rejection reasons.

## 2. Graph model and reference oracle

- [x] 2.1 Add isolated test graph/capture helpers with trace/span/parent IDs, timing, error/exception signals, metadata/links and explicit closed-fixture manifests; keep generated captures under ignored `target/span-duration-evaluation/` and sanitize committed fixtures.
- [x] 2.2 Implement the offline retention-first oracle with root/slow/error ancestor closure, strict below-threshold eligibility, conservative unknown handling and no reparenting or retained-metadata changes.
- [x] 2.3 Add given/when/then conformance tests for trivial subtrees, short-parent/slow-child and failed-child cases, exact thresholds, exception events without ERROR status, invalid timing, external parents and preserved metadata/links.

## 3. Real exported-trace corpus

- [x] 3.1 Capture unfiltered real Boot-managed and agent-managed framework/HTTP/JDBC traces using the existing test infrastructure without installing another SDK/export owner or modifying production profiles.
- [x] 3.2 Add controlled async/remote ancestry and arrival-order fixture cases with bounded waits and expected-span manifests; identify which further scenarios are exercised only synthetically and do not claim fixture closure proves distributed completeness.
- [x] 3.3 Apply the same oracle and graph assertions to both ownership-mode captures; record topology/instrumentation differences and verify no newly orphaned retained edges or lost error/slow ancestry.

## 4. Isolated candidate and resource-safety experiments

- [x] 4.1 Select and justify an individual-span prototype after the comparison, declaring pending trace/span bounds, decision age, decision-cache limits and completeness assumptions before running it; keep dependencies/configuration opt-in and test/experimental only.
- [x] 4.2 Implement the isolated prototype/replay runner and compare it with the offline oracle on explicitly closed cases, recording eligibility, retention and uncertainty/fallback decisions.
- [x] 4.3 Exercise out-of-order/late descendants, children ending after parents, remote/partial graphs, concurrent traces, timeout, overflow and eviction; verify bounded pending/cache state and reject any claimed safety based solely on a wait window or unbounded tombstones.
- [x] 4.4 Exercise bounded shutdown, exporter failure and backpressure behavior; distinguish filtering-induced loss from upstream/delivery failures and report unsupported cases instead of silently pruning uncertain input.

## 5. Measurements and decision evidence

- [x] 5.1 Define repeatable small/deep/high-concurrency workloads, test thresholds and warmup/repetition settings; compare the prototype with retain-all and separately labelled sampling/display controls where available.
- [x] 5.2 Measure retained/pruned span counts and serialized bytes at named pipeline boundaries, distinguishing actual measurements from storage estimates, UI effects and savings not established by offline transformation.
- [x] 5.3 Record processing time, peak pending state and allocation/CPU where practical with environment/limits; avoid machine-dependent CI performance thresholds or general production claims from fixture timing.
- [x] 5.4 Complete `evaluation.adoc` with correctness/resource results, an explicit go/conditional-go/no-go recommendation, supported scope and late-trace justification, unresolved limitations and any separate production-change prerequisites.

## 6. Baseline protection and documentation

- [x] 6.1 Run the new conformance/corpus experiments and existing Boot/agent tracing compatibility regressions, confirming unchanged production duration retention, explicit discard semantics and single-owner behavior.
- [x] 6.2 Document how to reproduce the investigation, distinguish experimental settings from supported configuration, and update the retained backlog note with the outcome without enabling production filtering or adding a production BOM/profile dependency.

## 7. Retire disposable research tooling

- [x] 7.1 Remove the Python prototype, tests, synthetic fixture and README after recording results; retain their complete Git checkpoint and historical reproduction instructions, verify no live references remain, and leave Java fixtures/production regressions unchanged.
