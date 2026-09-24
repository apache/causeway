## Why

Entity property-change publication resolves current post-values at transaction completion, and JDO can invoke derived property accessors that issue substantial SQL during this phase.
Those automatic JDBC spans currently appear as unexplained siblings between command persistence and audit-trail persistence, making their framework purpose and aggregate cost difficult to identify.

## What Changes

- Add one bounded semantic observation around evaluation of enlisted entity property changes and their current post-values.
- Export stable name `causeway.entitychange.evaluate` with contextual display name `evaluate property changes`.
- Keep automatic JDBC spans produced while evaluating derived properties as descendants of the aggregate evaluation span.
- Create no evaluation span when no property-change records are enlisted.
- Keep entity-change evaluation and audit-trail persistence as sibling phases under their natural current action, interaction, request, or background context.
- Preserve existing evaluation, retry, exception, transaction, persistence-adapter, Java-agent, no-op registry, sampling, and export behavior.
- Attach only static framework metadata and exclude entity types, property identifiers, targets, bookmarks, pre-values, post-values, users, tenants, transaction identifiers, interaction identifiers, sequence numbers, and counts.

## Capabilities

### New Capabilities

- `entity-change-evaluation-observation`: Defines the aggregate entity property-change evaluation span, lifecycle, hierarchy, activation, and privacy requirements.

### Modified Capabilities

None.

## Impact

- Affects transaction-scoped entity change tracking in `persistence/commons`, principally `EntityChangeTrackerDefault`.
- Adds focused observation lifecycle tests and Java-agent compatibility evidence for automatic JDBC parentage.
- Exercises existing JDO and JPA property-change publication tests to guard persistence semantics.
- Updates the tracing operations guide with the transaction-completion hierarchy and privacy boundaries.
- Introduces no new external dependencies and does not replace Java-agent-owned JDBC instrumentation.
