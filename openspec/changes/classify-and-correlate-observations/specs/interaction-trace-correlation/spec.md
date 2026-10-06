## ADDED Requirements

### Requirement: Root interaction UUID correlation

When observation is active, the existing root interaction span SHALL contain high-cardinality string attribute `causeway.interaction.id`, equal to the canonical UUID returned by its own interaction's `getInteractionId()` at completion. The attribute SHALL be independent of username and tenancy opt-ins and SHALL NOT become a metric label.

#### Scenario: Ordinary interaction
- **WHEN** an observed root interaction completes
- **THEN** its exported ID equals the interaction UUID even with username and tenancy export disabled

#### Scenario: Background replay replaces identity
- **WHEN** command replay replaces the initial command identifier using a command DTO
- **THEN** the root span exports the effective replay interaction UUID rather than the temporary opening-time UUID

#### Scenario: Nested or reused interaction
- **WHEN** a nested layer opens or the current layer is reused
- **THEN** the outer root keeps its own interaction identity, without adding another root solely for correlation or changing existing layer identity semantics

### Requirement: Correlation preserves lifecycle and trace structure

Correlation SHALL NOT allocate new interaction IDs, derive trace/span IDs or display names from the UUID, copy it onto HTTP entry spans or all descendants, or introduce duplicate observations. It SHALL preserve success/failure behavior and deterministic scope cleanup with Boot-managed and agent-managed tracing.

#### Scenario: Failure followed by another interaction
- **WHEN** observed work fails and another interaction subsequently runs on the same thread
- **THEN** the failed root retains its effective ID, cleanup restores the prior context, and the later root exports its own ID without stale correlation

#### Scenario: Inactive observation
- **WHEN** an interaction executes with the observation profile inactive
- **THEN** its existing identity and behavior remain unchanged and no correlation span or exporter is required

### Requirement: Operator verification explains attribute placement

The observability guide SHALL show execution-mode attributes on the applicable entry span and interaction UUID on the root interaction span. It SHALL provide separate Jaeger searches using `causeway.execution.mode` or an exact UUID with All Span Names, explain retention/sampling limitations, and distinguish runnable foreground examples from background instrumentation prerequisites.

#### Scenario: Find a retained trace by interaction ID
- **WHEN** an operator copies a root interaction UUID and searches retained traces using `causeway.interaction.id=<UUID>` without a conflicting operation filter
- **THEN** the documented procedure locates the trace containing that root span when it was exported and is still retained

#### Scenario: Verify default and custom classification
- **WHEN** an operator follows the foreground and background examples with the default or an overridden key
- **THEN** the guide identifies the span to inspect, the exact expected attribute and the requirement for an existing instrumented job span, without implying that every child carries the attribute
