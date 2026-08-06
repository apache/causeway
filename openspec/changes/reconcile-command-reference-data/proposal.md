## Why

The unified command manager now provides the sequence context required by later export validation, but Causeway 4 has no portable way for applications to identify stable reference-data participants. R1 establishes that classification boundary before R2 uses it for reachability decisions.

## What Changes

- Add a public `RefData` marker in applib for bookmarked domain types whose logical type and identifiers are stable across replay environments.
- Add a commandlog SPI that classifies a bookmark as replay reference data, supports zero or more application implementations, and combines implementations with OR semantics.
- Register a default classifier that resolves bookmark types through the Causeway 4 metamodel and recognizes `RefData` implementations without loading bookmarked objects.
- Mark the maintenance branch's built-in SecMan user, role, tenancy, and permission domain types as reference data.
- Keep known-participant tracking, reachability validation, export/import behavior, manager workflows, and background gates in their later reconciliation slices.

## Capabilities

### New Capabilities

- `command-export-reference-data-participants`: Defines the public bookmark-classification SPI, multi-service composition, and conservative fallback behavior.
- `command-export-refdata-marker`: Defines the public marker contract, metamodel-only default classifier, and built-in SecMan reference-data declarations.

### Modified Capabilities

None.

## Impact

- `api/applib`: adds the dependency-neutral public marker contract.
- `extensions/core/commandlog/applib`: adds and registers the classification SPI and default metamodel classifier with focused tests and reference documentation.
- `extensions/security/secman/applib`: opts the four maintenance-designated domain abstractions into the marker contract without changing persistence schemas.
- No command YAML, replay mapping, command-manager memento, datastore, Jakarta Persistence, or commandlog JDO changes are introduced.
