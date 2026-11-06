## Context

The previous JPA-only threshold was removed because independently exported JDBC descendants could survive their discarded parent.
Current observation-policy requirements retain successful and failed JPA observations independently of duration, subject to ordinary sampling/export and existing explicit discard semantics.
Boot-managed and Java-agent-managed traces already have supported single-owner integrations and an exported-trace fixture in `regressiontests/tracing-compatibility`.
This investigation uses that baseline rather than installing another SDK or changing its policy.

The research question is individual-span pruning across framework and automatic spans, not simply whole-trace sampling.
Duration is available only after a span ends; a parent's end does not establish that async or remote descendants have finished or arrived.
A finite waiting window likewise does not prove distributed trace completeness.

## Goals / Non-Goals

**Goals:**

- Establish a precise ancestry/error-retention oracle and reproducible corpus spanning both ownership modes.
- Compare processing locations and demonstrate at least one isolated individual-span pruning candidate against that oracle.
- Expose limits around incomplete/late traces, bounded state, failure handling, overhead and measurable savings.
- Produce a go/no-go decision with supported scope, prerequisites and an explicit production follow-up boundary.

**Non-Goals:**

- No enabled production filtering, public threshold/configuration, global duration sampler, exporter replacement or second SDK.
- No claim that dropping whole traces or hiding spans in a UI is equivalent to pruning individual spans.
- No guarantee that buffering recovers spans already lost upstream through sampling, transport or exporter failure.
- No silent reparenting, fabricated roots, identity changes or restoration of the old JPA threshold.

## Decisions

### 1. Establish the baseline and compare locations before choosing production architecture

Inventory the exact Boot/Micrometer/OTel/agent/Collector versions and supported extension points when the investigation is implemented.
Compare JVM processor/exporter, Collector and backend/UI approaches using the same matrix: span visibility, distributed completeness, ancestry guarantees, ownership/activation, buffering/backpressure, deployment cost and savings boundary.
JVM-local visibility cannot be assumed to include remote descendants; a Collector cannot be assumed to receive every span or prove completion merely by waiting.
Include whole-trace tail sampling and UI-only collapsing as separately labelled controls, not substitutes for the individual-span goal.
An approach may be rejected by source/documentation analysis without implementing an otherwise costly production plugin.

### 2. Build an offline reference oracle over explicitly closed test graphs

Use a test-only duration parameter, with exact-boundary behavior documented as retaining spans whose duration is equal to or above the threshold.
Keep roots, useful slow spans, spans with recorded error/exception signals and their ancestor closure within the captured graph.
Only shorter successful spans outside that closure are eligible for bottom-up pruning; unknown or invalid timing/error/completeness data is retained conservatively.
Preserve retained trace/span/parent IDs, names, attributes, events and links without reparenting.
Compare edges against the input so a pre-existing external parent is not confused with a newly orphaned retained child.

The oracle is a correctness reference, not a production algorithm.
Expected-span manifests and completed fixture processes may close an offline test graph, but those test-only signals MUST NOT be presented as a general distributed completeness mechanism.
If an online approach relies on an assumption that cannot be justified outside the fixture, record conditional feasibility or no-go rather than claiming production safety.

### 3. Exercise realistic inputs without changing the active export pipeline

Add isolated test/experimental fixtures around the existing tracing-compatibility module, extracting shared test helpers only where needed.
Capture real Boot/agent exports containing Causeway, HTTP and JDBC spans and retain a sanitized input/output comparison for the evaluated policy.
Use synthetic clock-controlled graphs for exact duration boundaries, child-before-parent arrival, children ending after parents, late remote children, missing parents, sampled/partial graphs and error signals without an ERROR status.
Include multiple concurrent trace IDs and linked spans, distinguishing parent ancestry from cross-trace links.
Any experimental dependency/configuration is pinned and opt-in inside the investigation harness, not the production BOM or application profiles.

### 4. Make bounded-state behavior and uncertainty observable

Declare maximum pending traces/spans, decision age and any decision-cache lifetime before testing a buffering candidate.
Exercise overflow, eviction, timeout, late arrivals after a decision, shutdown and exporter/backpressure failures with deterministic clocks and bounded waits.
An uncertain or incomplete filtering decision must retain/pass through the affected input rather than silently prune it; normal upstream/exporter failure remains a separate loss category.
Prove that any state/tombstones needed to protect late descendants are bounded, or record that the candidate fails feasibility.
If a decision cannot be reversed after export, demonstrate how later arrivals remain safe; otherwise mark that scope unsupported.
Failure to meet those conditions is a valid investigation result and must not be disguised by silently enlarging the buffer/window.

### 5. Report correctness, savings and operational cost separately

Record span counts and serialized bytes at clearly named stages, distinguishing application-to-Collector traffic, Collector-to-backend export, storage estimates and UI visibility.
An offline transformation proves graph behavior but does not by itself demonstrate application export or storage savings.
Measure elapsed processing time, allocation/CPU where practical and peak pending state for declared small, deep and high-concurrency workloads against a retain-all baseline.
Use repeatable runs with recorded warmup/environment; do not introduce machine-dependent performance pass/fail thresholds or claim production gains from fixture timing.
Produce `evaluation.md` in this change with the comparison matrix, runnable commands, correctness/resource results, measured limits and recommendation.
Keep generated captures/results under the module's ignored `target/span-duration-evaluation/`, using sanitized deterministic manifests for committed fixtures.

### 6. End with a decision, not an implicit production rollout

A go recommendation requires demonstrated individual-span pruning, no new orphan edges, retained error/slow-span ancestry, justified late/incomplete-trace behavior, bounded resources and Boot/agent evidence for a stated scope.
It must identify the production integration/deployment design and unresolved limitations requiring explicit follow-up approval.
Conditional go must enumerate the conditions and cannot claim unsupported distributed/async cases.
No-go must document which invariants failed and retain the existing unfiltered behavior, optionally recommending separately scoped UI collapsing or trace sampling.
All outcomes preserve current production configuration and exports in this change.

## Risks / Trade-offs

- Unknown distributed completion → separate closed-world oracle evidence from live guarantees and allow conditional/no-go outcomes.
- Retaining ancestors reduces pruning savings → report that cost instead of breaking trace identity to improve numbers.
- Error signals vary between instrumentation libraries → include exception events and conservative unknown handling, with supported signals documented.
- Bounded buffering and exporter backpressure can conflict with losslessness → distinguish filtering-induced loss from normal delivery failure and record unsupported cases.
- Collector or agent extension complexity may dominate benefit → compare deployment/maintenance cost before selecting any production hook.
- Benchmark and backend differences can mislead → publish stage-specific measurements and version/environment details without universal performance claims.

## Migration Plan

No production migration is required and removing the isolated harness/prototype restores the investigation-only changes.
Current retention, explicit discard and SDK/export ownership remain regression gates.
A viable production implementation requires a separate approved change that updates any affected observation-policy contracts and operator guidance.

## Open Questions

The processing location, deployable completeness mechanism, acceptable production resource budgets and choice among go/conditional-go/no-go are investigation outputs, not unspecified implementation prerequisites.
No production default threshold or compatibility guarantee is selected by this proposal.
