## Context

Secman represents user/role membership as a bidirectional JPA many-to-many association whose owning side is `ApplicationUser.roles` and whose inverse side is `ApplicationRole.users`.
It represents the tenancy hierarchy as a bidirectional one-to-many association whose owning side is `ApplicationTenancy.parent` and whose inverse side is `ApplicationTenancy.children`.
Most repository operations already update both sides, but some JPA-specific helpers and role deletion mutate only an inverse collection, and tenancy reassignment can leave the former parent's collection stale.
The maintenance branch is being decommissioned, so the solution must be narrow, avoid dependencies and schema changes, and preserve existing public API contracts where practical.

## Goals / Non-Goals

**Goals:**

- Keep both sides of Secman's JPA role/user and tenancy parent/child associations consistent during supported mutations.
- Ensure the JPA owning side receives every relationship change that must be persisted.
- Make role deletion portable by removing user assignments before deleting the role.
- Verify persistence with JPA integration tests that cross a flush-and-reload boundary.
- Keep the implementation compatible with the shared Secman applib orchestration and the JDO persistence implementation.

**Non-Goals:**

- Replace mutable collection getters with immutable or unmodifiable views.
- Redesign the Secman aggregate or public domain APIs.
- Change association mappings, cascade configuration, database tables, or foreign keys.
- Generalize the fix to unrelated bidirectional associations.
- Change JDO-specific entity behavior unless a shared applib correction necessarily applies to both persistence implementations.

## Decisions

### Keep existing collection contracts

The existing repository implementation mutates `getRoles()`, `getUsers()`, and `getChildren()` directly, and both JPA and JDO implementations inherit those contracts from the applib model.
This change SHALL retain those mutable getters rather than introducing a compatibility-sensitive API redesign.

An unmodifiable collection API was considered because it would prevent accidental one-sided mutation.
It was rejected for this change because it would require new mutation hooks across the applib, JPA, and JDO module boundaries and would exceed the intended maintenance scope.

### Treat repository operations as the canonical relationship boundary

`ApplicationRoleRepository` and `ApplicationTenancyRepository` operations SHALL remain the normal application-level entry points for relationship changes.
They SHALL update both the owning and inverse sides before a flush.
JPA-specific helper methods that remain available SHALL not perform an inverse-only mutation that can silently fail to persist.

Moving all relationship behavior into a newly designed entity aggregate API was considered.
It was rejected because the shared abstract domain classes and persistence-specific backing collections make that a broader public API change.

### Remove role assignments through the existing owning-side operation

Role deletion SHALL take a stable snapshot of assigned users and remove each assignment through the existing role-removal operation before deleting the role and its permissions.
This ensures `ApplicationUser.roles`, the owning side of the join table, is updated for every assignment.

Clearing only `ApplicationRole.users` was rejected because it changes only the inverse side and relies on provider-specific cleanup behavior.

### Make tenancy assignment safe for reassignment

Assigning a parent SHALL remove the tenancy from a different former parent's children collection, update the child's owning parent reference, and add it to the new parent's children collection.
Clearing a parent SHALL remove the child from the former parent's collection and null the owning reference.
Repeated assignment to the same parent and repeated clearing SHALL remain idempotent.

Rejecting all reassignment was considered because the current UI offers only root tenancies as children.
Consistent reassignment was selected because the repository API is callable outside that UI and its `setParentOnTenancy` name implies replacement rather than add-only behavior.
Cycle prevention remains governed by existing calling logic and is outside this association-consistency change.

### Test persisted state rather than only managed-object state

JPA integration tests SHALL assert both sides immediately after each operation and SHALL then flush, detach or clear the persistence context, reload the affected entities, and assert the relationship again.
This distinguishes Java object-graph consistency from actual join-table or foreign-key persistence.
Tests SHALL cover role assignment, role removal, deletion of an assigned role, tenancy assignment, tenancy clearing, and tenancy reassignment.

## Risks / Trade-offs

- [Risk] Synchronizing JPA-specific helpers as well as repositories could cause duplicate `Set.add` or `Set.remove` calls when one delegates to the other. → Rely on set idempotency and structure the implementation to avoid recursive coordination.
- [Risk] Role deletion while iterating `role.users` can cause concurrent modification. → Iterate over a defensive snapshot before invoking assignment removal.
- [Risk] Reassignment could expose pre-existing tenancy cycles through direct repository use. → Retain existing UI cycle filtering and keep general hierarchy validation out of scope.
- [Risk] Persistence-context clearing may make integration tests sensitive to transaction-test infrastructure. → Use the repository and JPA test facilities already established by the Secman persistence module and assert using stable identifiers such as username, role name, and tenancy path.
- [Trade-off] Mutable collections remain externally modifiable, so the design reduces known inconsistencies without making invalid states impossible. → Document repository operations as canonical and keep the fix proportionate to the maintenance branch.
