## Why

Audit-trail persistence currently appears as a large sequence of Java-agent JDBC spans directly beneath an action or interaction, so operators cannot identify their aggregate cost as audit work.
A bounded semantic parent span is needed to distinguish audit-trail writing from domain SQL while preserving the existing automatic JDBC detail.

## What Changes

- Add a `causeway.audittrail.write` observation around enabled audit-trail persistence with contextual name `write audit trail`.
- Make the observation inherit the current action, interaction, or request context so audit queries and inserts become its automatic descendants.
- Use one aggregate observation for a bulk property-change callback and one observation for an individual callback, without adding per-entry child observations.
- Record audit persistence failures on the observation while preserving the existing exception and transaction behavior.
- Keep audit writing unchanged and telemetry inactive when the `observation` profile is absent or the audit-trail extension is disabled.
- Attach only static framework metadata and exclude property values, pre/post values, targets, bookmarks, interaction identifiers, sequence numbers, usernames, tenants, and entry counts.
- Add focused lifecycle, parentage, failure, no-op, and persistence compatibility tests and document the resulting trace hierarchy.

## Capabilities

### New Capabilities

- `audit-trail-write-observation`: Define a bounded semantic observation around audit-trail persistence and its automatic JDBC descendants.

### Modified Capabilities

None.

## Impact

The change affects the audit-trail extension's entity-property-change subscriber, its observation dependency wiring, focused extension tests, Java-agent compatibility evidence, and tracing operations documentation.
It adds no exporter, sampler, SDK ownership, database schema, persistence behavior, configuration property, or third-party dependency.
