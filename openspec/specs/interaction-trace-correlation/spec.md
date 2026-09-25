# interaction-trace-correlation Specification

## Purpose
TBD - created by archiving change add-interaction-id-to-root-span. Update Purpose after archive.
## Requirements
### Requirement: Root interaction span exposes the interaction ID
When observation is active, the system SHALL add the current Causeway interaction ID to the corresponding `causeway.root.interaction` span as the high-cardinality string attribute `causeway.interaction.id`.
The attribute value MUST be the canonical string representation of the UUID returned by that interaction's `getInteractionId()` method.

#### Scenario: Top-level interaction starts with active observation
- **WHEN** the system opens a top-level Causeway interaction while observation is active
- **THEN** its `causeway.root.interaction` span contains `causeway.interaction.id` with the exact current interaction UUID

#### Scenario: Nested interaction layer opens
- **WHEN** the system opens a nested interaction layer within an existing top-level interaction
- **THEN** the system retains one root interaction span and its attribute identifies the shared top-level interaction

### Requirement: Correlation does not alter trace identity or structure
The system SHALL use the interaction ID only as a span attribute and MUST NOT derive an OpenTelemetry trace ID, span ID, or span display name from it.
The system MUST NOT directly copy the attribute to the agent-owned HTTP entry span or every descendant span.

#### Scenario: Root interaction is beneath an HTTP entry span
- **WHEN** an HTTP request has an agent-owned entry span and opens a Causeway interaction
- **THEN** the Causeway root interaction span is annotated without changing the existing trace and span parentage

#### Scenario: Root interaction has semantic descendants
- **WHEN** action, rendering, application, or JDBC spans occur beneath the root interaction span
- **THEN** the interaction-ID annotation does not change their names or hierarchy

### Requirement: Observation remains optional
The interaction-ID correlation SHALL preserve existing interaction lifecycle, result, and failure behavior when observation is inactive or active.

#### Scenario: Observation is inactive
- **WHEN** a top-level interaction runs without the `observation` profile
- **THEN** the interaction completes with its existing behavior and no telemetry SDK or exporter is required

#### Scenario: Interaction work fails
- **WHEN** work inside an observed top-level interaction throws a failure
- **THEN** the root observation records and closes with the existing failure behavior while retaining its interaction-ID attribute

### Requirement: Jaeger correlation is documented
The tracing operations guidance SHALL describe how to search for a stored trace using the exact `causeway.interaction.id` span attribute.
The guidance MUST state the relevant in-memory retention and operation-filter constraints.

#### Scenario: Operator has an interaction ID
- **WHEN** an operator searches Jaeger for `causeway.interaction.id=<UUID>` without restricting the search to a different span operation
- **THEN** the guidance explains that Jaeger can return the trace containing the matching root interaction span while that trace remains retained

