## Why

Secman's JPA entities expose bidirectional role/user and tenancy parent/child associations through mutation paths that do not always update both sides, so in-memory state can diverge from the owning-side state persisted by JPA.
Targeted corrections and flush-and-reload integration tests are needed to make existing operations reliable without redesigning the shared Secman domain API.

## What Changes

- Correct supported Secman role/user operations so they update the owning `ApplicationUser.roles` side and the inverse `ApplicationRole.users` side consistently.
- Ensure deleting an application role removes its assignments through the owning user collections before deleting the role.
- Correct supported tenancy parent/child operations so the owning child reference and inverse parent collection remain consistent.
- Preserve the existing API shape and mutable collection contracts to minimize compatibility risk for this maintenance branch.
- Add JPA integration tests that flush, clear, and reload entities to verify both in-memory association consistency and persisted join-table or foreign-key state.

## Capabilities

### New Capabilities

- `secman-jpa-association-consistency`: Defines consistent persistence behavior for Secman JPA role/user and tenancy parent/child association operations.

### Modified Capabilities

None.

## Impact

The change affects the Secman applib relationship orchestration and JPA persistence integration tests under `extensions/security/secman`.
It may make targeted adjustments to JPA-specific entity helper methods where those methods currently update only one side of an association.
No new dependencies, database schema changes, or intentional public API breaks are expected.
The JDO implementation is outside the behavioral scope except where shared applib orchestration must remain compatible with it.
