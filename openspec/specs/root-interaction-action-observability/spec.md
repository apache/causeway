# root-interaction-action-observability Specification

## Purpose
TBD - created by archiving change instrument-root-interactions-and-actions. Update Purpose after archive.
## Requirements

### Requirement: Observe each root interaction exactly once
When observation is active, the runtime SHALL create one `causeway.root.interaction` observation when the first interaction layer opens and SHALL keep it current until the root interaction closes.
Nested or reused interaction layers MUST participate in the current root observation without creating additional root or nested semantic observations.

#### Scenario: Complete a root interaction
- **WHEN** a top-level Causeway interaction opens and closes successfully while observation is active
- **THEN** exactly one `causeway.root.interaction` span covers the interaction lifecycle and no Causeway interaction observation remains current after close

#### Scenario: Open nested interaction layers
- **WHEN** additional interaction layers open while a root interaction is active
- **THEN** they remain within the existing root observation and do not create additional Causeway interaction spans

#### Scenario: Run without observation enabled
- **WHEN** the same interaction lifecycle runs without the `observation` profile
- **THEN** application behavior is unchanged and no framework span or retained observation state is produced

### Requirement: Observe action invocation as a child operation
When observation is active, `MemberExecutorServiceDefault.invokeAction(...)` SHALL create one `causeway.action.invocation` observation around the complete existing action-invocation boundary.
The action observation SHALL inherit the current root interaction as its parent when invoked within a root interaction.

#### Scenario: Invoke an action successfully
- **WHEN** an action is invoked during an observed root interaction
- **THEN** one `causeway.action.invocation` span is emitted as a child of the root-interaction span and closes before the root interaction closes

#### Scenario: Invoke an action without a root interaction
- **WHEN** an action invocation executes with observation active but no Causeway root observation current
- **THEN** the action observation completes safely using the ambient agent context, if any

### Requirement: Use stable and privacy-conscious action metadata
Semantic observations SHALL use stable operation names rather than embedding action identifiers in span names.
Each action observation SHALL include low-cardinality `causeway.action.id` and `causeway.execution.initiatedBy` attributes.
The instrumentation MUST NOT capture action arguments, return values, target instance identifiers, user names, tenancy tokens, or other instance-specific application data.

#### Scenario: Inspect action span metadata
- **WHEN** an action observation is exported
- **THEN** its name is `causeway.action.invocation`, its action identifier and initiation mode are available through the specified attributes, and prohibited instance data is absent

#### Scenario: Aggregate different actions
- **WHEN** different actions are invoked
- **THEN** their spans share the same `causeway.action.invocation` operation name and differ through bounded action metadata

### Requirement: Preserve failure semantics and cleanup
Root-interaction and action observations SHALL record failures that escape their respective framework boundaries.
Instrumentation MUST close observation scopes before stopping observations, clear retained root lifecycle state, and preserve the original rollback and exception behavior.

#### Scenario: Action invocation fails
- **WHEN** action invocation propagates a failure
- **THEN** the action observation records the failure, closes without remaining current, and the existing failure continues to the caller

#### Scenario: Root interaction work fails
- **WHEN** work executed through the root interaction propagates a failure
- **THEN** the root observation records the failure, existing rollback behavior is requested, and cleanup leaves no current Causeway observation

#### Scenario: Root interaction close fails
- **WHEN** transaction flush or another root-close callback throws
- **THEN** the root observation records the close failure and its lifecycle state is cleared while the existing close failure continues to the caller

### Requirement: Preserve agent-owned trace parentage
The semantic instrumentation SHALL use the Causeway observation substrate and the Java agent's global trace context without constructing an SDK, exporter, or explicit trace identifiers.

#### Scenario: Trace an HTTP-triggered action with JDBC work
- **WHEN** an agent-instrumented HTTP request opens a Causeway root interaction, invokes an action, and performs agent-instrumented JDBC work
- **THEN** the exported trace has the ordered ancestry `HTTP → causeway.root.interaction → causeway.action.invocation → JDBC`, allowing additional agent-created spans between those semantic boundaries

#### Scenario: Run active observation without the agent
- **WHEN** semantic instrumentation runs with the `observation` profile but without the Java agent
- **THEN** interaction and action execution complete safely without requiring an application-owned telemetry runtime
