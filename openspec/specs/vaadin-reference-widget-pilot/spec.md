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
The pilot SHALL prefer advertised public GraphQL autocomplete windows and SHALL connect its internal lazy data-provider callback to bounded server response pages.
It MUST retain an explicit maximum search policy for legacy non-paged autocomplete, and local slicing MUST NOT be represented as server paging.

#### Scenario: Window capability is advertised
- **WHEN** an eligible reference editor advertises `autoCompleteWindow`
- **THEN** the internal Vaadin data provider requests bounded offset pages using current filter text and Causeway argument context
- **AND** supplies returned items and authoritative total count to the control without exposing a Vaadin API to the application

#### Scenario: User scrolls beyond the first page
- **WHEN** Vaadin requests a later page for the current filter generation
- **THEN** the adapter obtains that page from the semantic Causeway context
- **AND** does not locally slice a previously downloaded complete result

#### Scenario: Filter changes while pages are in flight
- **WHEN** newer filter text, dependent arguments, route navigation, fragment replacement, prompt closure, or disconnection supersedes page requests
- **THEN** the adapter cancels or ignores stale callbacks and items
- **AND** retains Causeway-owned selection, focus, validation, and pending-value state

#### Scenario: Only bounded legacy autocomplete is available
- **WHEN** the server does not advertise a window field and GraphQL returns a stable choice set within the configured bound
- **THEN** the adapter may use the complete finite result while retaining cancellation and generation checks
- **AND** records that the response is legacy and not server paging

#### Scenario: Legacy autocomplete bound is exceeded
- **WHEN** a legacy server response exceeds the configured maximum or cannot provide stable identities
- **THEN** the editor presents a Causeway-owned limitation state or activates the documented fallback
- **AND** does not truncate the choices while claiming a complete match set

### Requirement: Supported default reference adapter policy
Qualified single-reference and multi-reference editors SHALL use the reviewed internal Vaadin free-core adapter by default while preserving Causeway-owned identity, choices, validation, pending values, cancellation, routes, and semantic events.
The native reference editor MUST remain the explicit rollback, unsupported-descriptor fallback, load-failure fallback, and diagnostic comparison implementation.

#### Scenario: Qualified reference editor uses default policy
- **WHEN** no toolkit override or deprecated compatibility property is configured and an eligible reference editor connects
- **THEN** the registry selects the route-lazy internal Vaadin adapter
- **AND** application markup and listeners continue to depend only on Causeway elements and semantic events

#### Scenario: Native rollback is selected
- **WHEN** the common toolkit policy is explicitly `native`
- **THEN** every reference editor selects the established native implementation and requests no Vaadin reference asset
- **AND** GraphQL operations, identities, routes, validation, and persisted data require no migration

#### Scenario: Default adapter fails to load
- **WHEN** the packaged reference closure cannot load or define its controls
- **THEN** the current document fails closed to the native reference editor
- **AND** no raw toolkit element, protected value, stale result, or false successful state remains visible

#### Scenario: Reference closure exceeds release policy
- **WHEN** checksum, compressed size, entry point, dependency integrity, license, vulnerability result, telemetry behavior, style hash, accessibility, or browser evidence differs from reviewed policy
- **THEN** verification fails before release
- **AND** the default policy is not broadened to accept the drift
