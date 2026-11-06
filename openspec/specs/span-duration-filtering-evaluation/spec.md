# Span Duration Filtering Evaluation

## Purpose

Define reproducible safety evaluation and historical evidence for general span-duration filtering without authorizing production duration-based pruning.

## Requirements

### Requirement: Investigation isolation and unchanged production behavior

The investigation SHALL keep prototypes and experimental configuration isolated from the active production observation/export pipeline.
It MUST preserve existing duration-independent observation retention, explicit discard semantics and single SDK/export ownership.
It SHALL NOT introduce a supported production duration threshold or enable filtering through existing application profiles.
If completed prototype tooling is retired, the evaluation report SHALL preserve its source/reproduction checkpoint and distinguish historical results from currently maintained tests.

#### Scenario: Existing application and tracing regressions

- **WHEN** existing Boot-managed or agent-managed fixtures run without the investigation prototype
- **THEN** their existing retention, identity, ancestry and ownership assertions remain unchanged
- **AND** no duration-based discard or additional SDK/export owner is installed

#### Scenario: Retire completed investigation tooling

- **WHEN** disposable prototype sources and fixtures are removed after results are recorded
- **THEN** the report retains a Git checkpoint containing the complete tools and reproduction instructions
- **AND** it does not present their historical test results as a current regression gate

### Requirement: Explicit scope and placement comparison

The investigation SHALL compare JVM processor/exporter, Collector and backend/UI approaches using version-identified evidence.
It MUST distinguish individual-span pruning from whole-trace sampling and UI-only collapsing, including the pipeline stages where each could reduce traffic, storage or display noise.

#### Scenario: Compare candidate architectures

- **WHEN** an approach is evaluated
- **THEN** the evidence identifies its span visibility, completion assumptions, ownership, resource/deployment costs and savings boundary
- **AND** a trace-sampling or UI-only result is not presented as individual-span export filtering

### Requirement: Retention-first graph reference policy

The evaluation SHALL provide a deterministic reference oracle over explicitly closed test graphs.
It SHALL retain roots, spans at or above the test threshold, spans with recorded error/exception signals and their in-scope ancestor closure.
It MUST preserve retained identities and metadata without silently reparenting, fabricating roots or introducing new missing-parent edges relative to the input graph.
Unknown or invalid decision data SHALL be retained conservatively.

#### Scenario: Short parent with useful descendants

- **WHEN** a short successful parent contains a slow or failed descendant
- **THEN** the reference policy retains the descendant and its original ancestor path

#### Scenario: Trivial successful subtree and threshold boundary

- **WHEN** a closed graph contains a shorter successful subtree outside the retained ancestor closure
- **THEN** that subtree is eligible for pruning without changing surviving parent IDs
- **AND** spans exactly at the threshold remain retained

#### Scenario: Exception without error status or invalid duration

- **WHEN** a span records an exception without ERROR status or has unknown/invalid timing
- **THEN** the reference policy retains that span conservatively

#### Scenario: External parent and retained metadata

- **WHEN** the input already has an external parent outside the captured graph
- **THEN** evaluation distinguishes that pre-existing edge from filtering-induced orphaning
- **AND** surviving IDs, names, attributes, events and links remain unchanged

### Requirement: Reproducible Boot and agent evidence

The evaluation SHALL exercise synthetic graphs and real exported traces in both supported ownership modes, covering Causeway framework and automatic HTTP/JDBC spans.
The evidence SHALL identify fixture versions, commands and graph boundaries rather than assuming the fixture represents a complete production distributed trace.

#### Scenario: Compare ownership modes

- **WHEN** equivalent Boot-managed and agent-managed fixture traces are captured and evaluated
- **THEN** the same reference policy and ancestry/error checks are applied
- **AND** differences in captured spans or supported cases are recorded explicitly

#### Scenario: Closed fixture versus distributed completion

- **WHEN** expected-span manifests or completed fixture processes establish a test graph boundary
- **THEN** that boundary is labelled as test-only evidence
- **AND** it is not claimed to prove a deployable distributed completeness mechanism

### Requirement: Late-trace safety and bounded-state assessment

Each buffering candidate SHALL declare and test bounds for pending traces/spans, decision age and decision-cache state.
The evaluation MUST cover out-of-order/async/remote arrivals, partial graphs, overflow/eviction, timeout, shutdown and exporter/backpressure failures.
Uncertain filtering decisions SHALL retain/pass through affected input; if an approach cannot provide that behavior within its stated scope and bounds, the report SHALL classify the limitation or reject the approach rather than claim safe pruning.
Normal upstream sampling and delivery failures MUST be distinguished from filtering-induced loss.

#### Scenario: Descendant arrives after a parent ended or a decision was made

- **WHEN** a useful descendant arrives late, including beyond a configured waiting window
- **THEN** the evaluation checks whether the candidate introduces an orphan or loses useful context
- **AND** a finite waiting window alone is not accepted as proof of trace completeness

#### Scenario: Resource limit or incomplete trace

- **WHEN** a candidate reaches its declared bounds or cannot establish enough information for safe pruning
- **THEN** its retention/fallback behavior and bounded state are observed and recorded
- **AND** silent pruning or unbounded tombstone retention fails that candidate's safety claim

#### Scenario: Shutdown or exporter failure

- **WHEN** buffered data encounters shutdown, backpressure or export failure
- **THEN** tests use bounded waits and record flush/retry/loss behavior separately from filtering decisions

### Requirement: Evidence-backed decision and honest measurement

The investigation SHALL produce a reproducible evaluation report with a go, conditional-go or no-go recommendation, correctness results, resource limits, operational trade-offs and stage-specific measurements against a retain-all baseline.
It MUST distinguish measured span/byte savings from estimates or UI-only effects and avoid machine-dependent performance gates or production claims based solely on fixture timing.
A positive recommendation SHALL state its supported scope, late/incomplete-trace justification and separate production follow-up requirements; it SHALL NOT enable production filtering in this change.

#### Scenario: Candidate satisfies a stated scope

- **WHEN** evidence supports useful pruning with preserved ancestry/errors and bounded resources for that scope
- **THEN** the report documents the assumptions, measured benefit, residual limitations and required production change

#### Scenario: Only unsafe or insufficient alternatives remain

- **WHEN** individual-span candidates cannot justify their safety/resource claims or only trace-sampling/display alternatives are viable
- **THEN** the report records no-go or explicitly conditional feasibility
- **AND** current production retention remains unchanged

#### Scenario: Report savings

- **WHEN** a prototype reports fewer spans or bytes
- **THEN** it identifies the measured pipeline boundary and workload/environment
- **AND** an offline transformation is not claimed to demonstrate application export or backend storage savings by itself
