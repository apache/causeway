## Why

Maintenance CAUSEWAY-4061 (`680ac40ef52`) prevents per-target discovery from fully introspecting unrelated mixins.
V4 already uses separate mixin bootstrap and an eager type-hierarchy index, so transplanting the old loader algorithm would be redundant and potentially disruptive.
The user approved a tests-only verification of that architectural equivalence.

## What Changes

- Verify eager candidate selection across exact, superclass and ordinary interface targets, with unrelated and missing-facet candidates excluded.
- Verify action and association discovery do not read rejected candidates' members, while retaining domain-service Object-mixin exclusions.
- Exercise real mixed-in action/property/collection contributions, inheritance, interface applicability, explicit ordering and recording-navigation synthesis.
- Document the bootstrap-versus-discovery distinction and close the remaining roadmap item as covered by v4 rather than claiming a new runtime optimization.
- Do not change production Java, configuration, dependencies, diagnostics or introspection phases.

## Capabilities

### New Capabilities

- `v4-mixin-applicability`: Existing eager-index applicability and contribution behavior, protected by focused and real bootstrap regression coverage.

### Modified Capabilities

None.

## Impact

Tests in core/metamodel and regressiontests/domainmodel, the existing diagnostic guide, roadmap and OpenSpec artifacts.
No public API, runtime code or dependency changes.
The deprecated unused on-the-fly implementation is not modified or revived.
