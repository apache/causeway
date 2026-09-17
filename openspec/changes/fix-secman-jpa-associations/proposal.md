## Why

Secman's bidirectional JPA role/user and tenancy parent/child associations are not kept consistent by every supported mutation path, allowing managed state to diverge from the owning-side state persisted by JPA.
The v2 maintenance fix needs a focused forward port to v4, with persistence-level regression coverage adapted to the current Secman model.

## What Changes

- Keep both sides of the JPA role/user association synchronized when assigning or removing a role.
- Remove owning-side user assignments before deleting an assigned role.
- Keep both sides of the JPA tenancy hierarchy synchronized when assigning, clearing, or replacing a parent.
- Make the existing JPA entity association helpers consistent and idempotent without changing the mappings or public collection contracts.
- Add JPA integration tests that verify managed state and persisted state after flush and reload.

## Capabilities

### New Capabilities

- `secman-jpa-association-consistency`: Defines consistent role/user and tenancy parent/child mutations for Secman's JPA persistence implementation.

### Modified Capabilities

None.

## Impact

- Affects the shared Secman role and tenancy repository orchestration used by the JPA implementation.
- Affects the JPA `ApplicationRole` and `ApplicationTenancy` association helpers.
- Adds integration tests under `extensions/security/secman/persistence-jpa`.
- Does not require a database schema migration, new dependency, or intentional public API break.
