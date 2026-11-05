## Why

Audit persistence currently contributes database work without a semantic boundary identifying its aggregate cost in v4 traces.
Forward-port maintenance CAUSEWAY-4059 commit `7961910a089` as roadmap step 8a, using v4's shared Boot/agent observation integration.

## What Changes

- Observe each enabled audit subscriber repository callback as `causeway.audittrail.write`, with contextual name `write audit trail`.
- Aggregate bulk callbacks without per-entry framework observations and preserve current parent context, persistence behavior and original failures.
- Keep audit metadata static and private, and retain disabled/no-op behavior.
- Add focused subscriber tests, real Boot/agent exported-trace evidence and user documentation.

## Capabilities

### New Capabilities

- `audit-trail-write-observation`: Semantic audit persistence callback observations, natural ancestry, bounded metadata and unchanged lifecycle behavior.

### Modified Capabilities

None.

## Impact

The audit-trail applib subscriber gains an injected `CausewayObservationIntegration`; manually constructed subscribers must supply it.
Existing runtime dependencies expose that integration, so no new production library, SDK, exporter, configuration or schema is planned.
The tracing regression fixture will exercise the actual subscriber with controlled repository JDBC work.
Entity-change evaluation (roadmap 8b), application-defined spans and priming remain outside this change.
This branch starts from the completed Wicket/semantic-name tranche and will be rebased after that PR merges.
