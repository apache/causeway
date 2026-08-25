## ADDED Requirements

### Requirement: Supported default reference adapter policy
Qualified single-reference and multi-reference editors SHALL use the reviewed internal Vaadin free-core adapter by default while preserving Causeway-owned identity, choices, validation, pending values, cancellation, routes, and semantic events.
The native reference editor MUST remain the explicit rollback, unsupported-descriptor fallback, load-failure fallback, and diagnostic comparison implementation.

#### Scenario: Qualified reference editor uses default policy
- **WHEN** no toolkit override or deprecated compatibility property is configured and an eligible reference editor connects
- **THEN** the registry selects the route-lazy internal Vaadin adapter
- **AND** application markup and listeners continue to depend only on Causeway elements and semantic events

#### Scenario: Native rollback is selected
- **WHEN** the common toolkit policy is explicitly `native`
- **THEN** every reference editor selects the established native implementation and requests no Vaadin reference asset
- **AND** GraphQL operations, identities, routes, validation, and persisted data require no migration

#### Scenario: Default adapter fails to load
- **WHEN** the packaged reference closure cannot load or define its controls
- **THEN** the current document fails closed to the native reference editor
- **AND** no raw toolkit element, protected value, stale result, or false successful state remains visible

#### Scenario: Reference closure exceeds release policy
- **WHEN** checksum, compressed size, entry point, dependency integrity, license, vulnerability result, telemetry behavior, style hash, accessibility, or browser evidence differs from reviewed policy
- **THEN** verification fails before release
- **AND** the default policy is not broadened to accept the drift

## REMOVED Requirements

### Requirement: Opt-in budgets and rollback
**Reason**: Completed qualification promotes the reviewed adapter to the supported default and replaces independent pilot opt-in with a common toolkit rollback policy.
**Migration**: Use `causeway.viewer.webcomponents.htmx.editor-toolkit=native` for immediate native rollback; deprecated pilot properties remain readable during the compatibility period.
