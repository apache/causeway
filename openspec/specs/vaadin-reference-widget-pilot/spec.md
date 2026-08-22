# Vaadin Reference Widget Pilot Specification

## Purpose
Define the strict-CSP, selectively packaged, GraphQL-backed, opt-in Vaadin reference-widget pilot and its compatibility, budget, and rollback boundaries.

## Requirements

### Requirement: Strict-CSP feasibility stop gate
The pilot SHALL identify every candidate-originated style policy violation under the viewer's production Content Security Policy before adding or enabling Vaadin in a production module.
The accepted integration MUST produce zero unexpected CSP violations without a blanket `style-src 'unsafe-inline'` policy, and any policy adjustment MUST have documented security approval and browser evidence.

#### Scenario: Candidate operation is isolated
- **WHEN** the CSP fixture connects, opens, filters, selects, clears, validates, disables, disconnects, or reconnects one candidate reference control
- **THEN** it records the violated directive, blocked operation, source information where available, component state, and browser result
- **AND** distinguishes style elements, style attributes, CSSOM mutations, Constructable StyleSheets, and fixture-owned styles where the platform exposes that distinction

#### Scenario: Acceptable CSP strategy is demonstrated
- **WHEN** the implementation selects nonce propagation, external styles, Constructable StyleSheets, a narrowly scoped directive, or another remedy
- **THEN** the exact production-like browser matrix reports zero unexpected violations across both reference controls and their overlays
- **AND** the security rationale, residual capability, browser support, and automated regression test are retained

#### Scenario: Only blanket inline styles make the controls work
- **WHEN** required candidate behavior cannot operate without blanket `style-src 'unsafe-inline'` or another unapproved relaxation
- **THEN** the pilot stops before adding or enabling the production dependency
- **AND** the existing Causeway reference editor remains the supported implementation

### Requirement: Selective free-core Maven packaging
The pilot SHALL pin and selectively package only the approved free-core Vaadin reference controls and their required transitive browser modules through the Maven build.
The packaged runtime MUST use no CDN, external request, Flow client, commercial component, unknown license, or unverified dependency input.

#### Scenario: Maintainer builds from pinned inputs
- **WHEN** a maintainer runs the documented clean build
- **THEN** Maven acquires or verifies the exact lock, generates the selective route asset, checks deterministic hashes, and packages accepted licenses and notices beneath the documented JAR locations
- **AND** browser runtime requires only same-origin packaged resources

#### Scenario: Dependency closure drifts
- **WHEN** a direct or transitive version, integrity value, license, vulnerability status, imported entry point, telemetry behavior, or generated hash differs from the approved closure
- **THEN** verification fails with the changed input identified
- **AND** the release cannot silently substitute a broader or commercial package

#### Scenario: Route does not use a pilot editor
- **WHEN** a generic or custom route contains no explicitly enabled candidate-backed reference editor
- **THEN** the browser makes zero Vaadin asset requests
- **AND** current viewer readiness and rendering do not wait for the candidate closure

### Requirement: Internal GraphQL-backed reference adapters
The pilot SHALL implement single-reference and supported multi-reference selection as internal Causeway editor adapters using existing public GraphQL choice, validation, mutation, and interaction behavior.
Causeway MUST remain authoritative for pending values, stable domain identity, required state, disabled reasons, validation, cancellation, route generation, and semantic events.

#### Scenario: User searches and selects one reference
- **WHEN** a user enters filter text and selects a matching single reference
- **THEN** the adapter obtains choices through the current public GraphQL path, reconciles the canonical identity, and updates the Causeway pending value
- **AND** application code receives the existing Causeway semantic change contract rather than a Vaadin event

#### Scenario: User changes a multi-reference selection
- **WHEN** a supported multi-reference editor adds, removes, clears, or reorders selected references
- **THEN** the adapter preserves stable identities and deterministic Causeway ordering semantics
- **AND** existing validation and interaction state determines whether the pending value is accepted

#### Scenario: Search is superseded
- **WHEN** newer filter text, route navigation, fragment replacement, or disconnection supersedes an in-flight choice request
- **THEN** the adapter cancels or ignores the stale work
- **AND** stale results, validation, focus actions, and selection state cannot update the current editor

#### Scenario: Candidate-specific state conflicts with Causeway
- **WHEN** Vaadin selection, validation, disabled, overlay, or pending-value state conflicts with the current Causeway context
- **THEN** the adapter reconciles the control to the Causeway state
- **AND** does not establish a second authoritative widget state machine

### Requirement: Honest bounded autocomplete behavior
The pilot SHALL define and enforce an explicit maximum accepted result count and search policy for the current non-paged GraphQL autocomplete operation.
Local slicing for candidate callbacks MUST NOT be represented as server paging, and responses that exceed the accepted bound MUST fail visibly or use the existing fallback rather than appear silently complete.

#### Scenario: Bounded autocomplete response is returned
- **WHEN** GraphQL returns a stable choice set within the configured bound
- **THEN** the adapter may serve candidate callback windows from that result while retaining cancellation and generation checks
- **AND** records that paging is local presentation over one server response

#### Scenario: Autocomplete bound is exceeded
- **WHEN** the server response exceeds the configured maximum or cannot provide stable identities
- **THEN** the editor presents a Causeway-owned limitation state or activates the documented fallback
- **AND** does not truncate the choices while claiming a complete match set

### Requirement: Opt-in budgets and rollback
The pilot SHALL remain explicitly opt-in and retain the current Causeway reference editor as the default or immediate fallback.
The cold candidate reference closure MUST NOT exceed 65 KB gzip, unaffected routes MUST load no candidate asset, and rollback MUST require no GraphQL, route, persisted-data, or custom-page migration.

#### Scenario: Pilot is enabled for a supported member
- **WHEN** explicit viewer configuration selects the candidate for a supported reference descriptor
- **THEN** the semantic editor registry loads the route-lazy internal adapter and preserves the public Causeway contract
- **AND** records bundle, readiness, accessibility, CSP, and external-request evidence for that route

#### Scenario: Pilot is disabled or fails qualification
- **WHEN** configuration disables the pilot, the descriptor is unsupported, a hard gate fails, or rollback is requested
- **THEN** the registry uses the existing reference editor without loading Vaadin
- **AND** domain state, canonical navigation, semantic events, and application markup remain compatible
