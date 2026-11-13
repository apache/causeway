## ADDED Requirements

### Requirement: Audit-trail write observation

When observation is active and audit persistence is enabled, the audit-trail extension SHALL create a `causeway.audittrail.write` observation around each subscriber persistence callback, with contextual name `write audit trail`.
It SHALL inherit the current trace context without storing or fabricating an action parent.

#### Scenario: Individual callback

- **WHEN** the subscriber receives an individual entity property change
- **THEN** one audit observation encloses the unchanged individual repository delegation

#### Scenario: Bulk callback

- **WHEN** the subscriber receives a bulk collection of changes
- **THEN** one audit observation encloses the unchanged bulk repository delegation
- **AND** no per-entry framework audit observations are created

#### Scenario: Natural parentage

- **WHEN** an action, interaction, request or background context is current at callback entry
- **THEN** the audit observation inherits that context without explicit reparenting

### Requirement: Audit persistence lifecycle and child work

The observation SHALL cover synchronous repository work including entry creation, initialization, persistence and flushing.
JDBC work SHALL remain under the audit observation without replacing or duplicating existing instrumentation.
The wrapper SHALL preserve callback selection, ordering, batching, results and transaction behavior.

#### Scenario: Successful persistence

- **WHEN** a repository callback succeeds
- **THEN** its synchronous child operations are descendants of the audit observation
- **AND** the audit observation stops and restores the prior current context

#### Scenario: Persistence failure

- **WHEN** a repository callback throws
- **THEN** the observation records the failure and stops
- **AND** the identical original failure propagates to existing transaction handling
- **AND** subsequent callbacks do not inherit a stale audit scope

### Requirement: Bounded private metadata

Audit observations SHALL add only static operation, contextual, bean and module metadata.
They MUST NOT add changed property identifiers, targets, bookmarks, old/new values, user/tenant identities, interaction/transaction IDs, sequence numbers, entry counts or other audit payload data.

#### Scenario: Different payloads and callback sizes

- **WHEN** individual and bulk callbacks contain different entity changes
- **THEN** their audit observation names and metadata remain identical in shape and free of payload values

### Requirement: Disabled and inactive behavior

Disabled audit persistence SHALL invoke neither the repository nor an audit observation.
Enabled audit persistence SHALL execute unchanged without exported audit spans when observation is inactive or no exporter/agent is available.

#### Scenario: Audit persistence disabled

- **WHEN** audit persistence is disabled
- **THEN** both subscriber overloads return without repository calls or audit observations

#### Scenario: Observation inactive

- **WHEN** audit persistence is enabled but observation is inactive
- **THEN** the same repository callbacks execute and failures propagate unchanged
- **AND** no Causeway audit span is exported

#### Scenario: Exporter absent

- **WHEN** observation is enabled without an available exporter or agent
- **THEN** audit callbacks still execute normally without requiring a new tracing pipeline
