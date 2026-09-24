## ADDED Requirements

### Requirement: Audit-trail write observation
When observation is active and audit persistence is enabled, the audit-trail extension SHALL create a `causeway.audittrail.write` observation around each `EntityPropertyChangeSubscriberForAuditTrail` persistence callback.
The observation SHALL use contextual name `write audit trail` and SHALL inherit the trace context current when the subscriber callback begins.

#### Scenario: Bulk property changes are audited
- **WHEN** the audit subscriber receives a bulk collection of entity property changes
- **THEN** one `causeway.audittrail.write` observation encloses the complete bulk repository operation
- **AND** the extension creates no additional framework observation for each audit entry in that bulk callback

#### Scenario: An individual property change is audited
- **WHEN** the audit subscriber receives one entity property change through its individual callback
- **THEN** one `causeway.audittrail.write` observation encloses that repository operation
- **AND** the observation uses the same stable and contextual names as a bulk callback

#### Scenario: Audit writing occurs beneath an action
- **WHEN** audit persistence executes while an action observation is current
- **THEN** `write audit trail` is a child of that action observation
- **AND** the audit extension does not store or reconstruct an action context explicitly

#### Scenario: Audit writing occurs at another interaction boundary
- **WHEN** audit persistence executes without a current action but with another request, interaction, or background trace context
- **THEN** the audit observation inherits that current context
- **AND** it does not fabricate an action parent

### Requirement: Audit persistence child operations and lifecycle
The audit observation SHALL cover detached audit-entry creation, entry initialization, repository persistence, flushing, and other synchronous work initiated by the subscriber's repository callback.
Automatic JDBC spans initiated by that work SHALL inherit the audit observation as an ancestor while remaining owned by the Java agent.
The observation SHALL close after success and SHALL record and rethrow a persistence failure without changing transaction behavior.

#### Scenario: Audit entries produce JDBC operations
- **WHEN** audit persistence executes database reads or writes with Java-agent JDBC instrumentation active
- **THEN** the resulting JDBC spans are descendants of `causeway.audittrail.write`
- **AND** Causeway does not replace or duplicate those JDBC spans

#### Scenario: Audit persistence succeeds
- **WHEN** the audit repository callback completes normally
- **THEN** the audit observation closes after all synchronous audit persistence work
- **AND** no audit observation remains current afterward

#### Scenario: Audit persistence fails
- **WHEN** detached-entry creation, initialization, persistence, flushing, or another repository operation throws
- **THEN** the audit observation records the failure and closes
- **AND** the original failure is rethrown for the existing transaction handling
- **AND** no audit observation scope leaks into subsequent work

### Requirement: Bounded and private audit telemetry
The audit observation SHALL attach only static framework metadata needed to identify the operation and module.
It MUST NOT attach changed property identifiers, targets, bookmarks, pre-values, post-values, interaction identifiers, transaction identifiers, sequence numbers, usernames, tenancy identifiers, entry counts, or other audit payload data.

#### Scenario: A bulk callback contains many changes
- **WHEN** a bulk audit callback contains changes for multiple objects, properties, or users
- **THEN** its observation name and attributes remain identical in shape to those for an individual callback
- **AND** neither the callback size nor any entry identity or value appears in telemetry

#### Scenario: Canonical framework metadata is emitted
- **WHEN** an audit observation is exported
- **THEN** its stable name, contextual name, bean metadata, and module metadata identify the bounded framework operation
- **AND** no application-instance data is required to filter or aggregate it

### Requirement: Audit behavior without active observation
Observation SHALL NOT change audit-trail enablement, callback selection, persistence ordering, batching, flush behavior, or results.
When the audit extension is disabled, the subscriber SHALL create neither audit entries nor audit observations.
When observation is inactive, enabled audit persistence SHALL remain unchanged and no Causeway audit span SHALL be exported.

#### Scenario: Audit persistence is disabled
- **WHEN** the audit-trail persistence setting disables the subscriber
- **THEN** the subscriber returns according to its existing behavior without invoking the repository
- **AND** it creates no `causeway.audittrail.write` observation

#### Scenario: Observation profile is inactive
- **WHEN** audit persistence is enabled without the `observation` profile
- **THEN** the same audit entries are created with the same transaction behavior
- **AND** no `causeway.audittrail.write` span is exported
