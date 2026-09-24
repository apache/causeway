# entity-change-evaluation-observation Specification

## Purpose
TBD - created by archiving change add-entity-change-evaluation-span. Update Purpose after archive.
## Requirements
### Requirement: Aggregate entity property-change evaluation observation

When observation is active and entity property-change records are enlisted, the framework SHALL create one `causeway.entitychange.evaluate` observation around the complete evaluation of those records.
The observation SHALL use contextual display name `evaluate property changes`.

#### Scenario: Candidate property changes are evaluated

- **WHEN** the tracker evaluates one or more enlisted property-change records
- **THEN** one `causeway.entitychange.evaluate` observation encloses obtaining current post-values and comparing pre-values with post-values
- **AND** the observation closes after evaluation completes

#### Scenario: Evaluation finds no publishable difference

- **WHEN** one or more candidate records are evaluated but no pre-value and post-value comparison requires publication
- **THEN** the completed evaluation is still represented by one `causeway.entitychange.evaluate` observation

#### Scenario: No property-change records are enlisted

- **WHEN** transaction completion has no enlisted property-change records to evaluate
- **THEN** no `causeway.entitychange.evaluate` observation is created

### Requirement: Bounded retry and failure lifecycle

The evaluation observation SHALL preserve the tracker’s existing retry, handled-accessor-failure, and escaping-failure behavior without retaining observation scope beyond the synchronous evaluation.

#### Scenario: Concurrent modification triggers defensive evaluation

- **WHEN** evaluation of the original record collection triggers the existing concurrent-modification recovery path
- **THEN** the original attempt and defensive-copy retry execute within the same `causeway.entitychange.evaluate` observation
- **AND** no additional evaluation observation is created for the retry

#### Scenario: Property access failure becomes an unknown value

- **WHEN** existing property-change behavior converts a non-deadlock accessor failure to an unknown post-value
- **THEN** evaluation continues with unchanged publication semantics
- **AND** telemetry does not turn the handled accessor failure into a new escaping failure

#### Scenario: Evaluation fails

- **WHEN** entity property-change evaluation throws an escaping exception
- **THEN** the observation records the failure
- **AND** the observation scope closes
- **AND** the same exception is rethrown

### Requirement: Natural parentage and automatic JDBC descendants

The evaluation observation SHALL inherit the current Micrometer/OpenTelemetry context and SHALL make synchronous Java-agent JDBC spans produced by property evaluation its descendants.
It SHALL complete before entity property-change subscribers are invoked.

#### Scenario: Evaluation occurs during an action transaction

- **WHEN** entity property changes are evaluated while an action context is current
- **THEN** `evaluate property changes` is a descendant of that action span

#### Scenario: Derived property evaluation executes SQL

- **WHEN** a derived property accessor executes SQL while current post-values are obtained
- **THEN** the Java-agent JDBC spans are descendants of `causeway.entitychange.evaluate`
- **AND** the framework does not create duplicate JDBC spans

#### Scenario: Audit persistence follows evaluation

- **WHEN** evaluated property changes are subsequently delivered to the audit-trail subscriber
- **THEN** `evaluate property changes` closes before `write audit trail` begins
- **AND** the evaluation and audit observations are siblings under their natural current parent rather than one containing the other

#### Scenario: Evaluation has no current action

- **WHEN** entity property changes are evaluated with an interaction, request, or background context current but no action context current
- **THEN** the evaluation observation inherits that natural current context
- **AND** it does not fabricate action parentage

### Requirement: Static metadata and private evaluation telemetry

The evaluation observation SHALL use only static framework metadata needed to identify its bean and module.
It MUST NOT attach entity types, property identifiers, targets, bookmarks, pre-values, post-values, users, tenants, transaction identifiers, interaction identifiers, sequence numbers, candidate counts, changed counts, retry counts, or other change payload data.

#### Scenario: Evaluation telemetry is inspected

- **WHEN** a `causeway.entitychange.evaluate` observation is exported
- **THEN** its low-cardinality metadata identifies only the static framework bean and module
- **AND** its name, contextual name, and attributes contain no entity, property, value, identity, transaction, interaction, sequence, or count data

### Requirement: Inactive observation preserves entity-change behavior

When observation is inactive, entity property-change evaluation and publication SHALL remain unchanged and no Causeway evaluation span SHALL be exported.

#### Scenario: Observation profile is inactive

- **WHEN** enlisted property changes are evaluated without the `observation` profile
- **THEN** the same post-values are obtained and the same property changes are published
- **AND** no `causeway.entitychange.evaluate` span is exported

#### Scenario: No-op registry evaluates changes

- **WHEN** the tracker uses a no-op observation registry
- **THEN** the evaluation closure runs exactly once according to existing retry behavior
- **AND** no tracing SDK, exporter, or Java-agent replacement is installed by the framework
