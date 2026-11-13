## Context

Secman represents user/role membership as a bidirectional JPA many-to-many association whose owning side is `ApplicationUser.roles` and whose inverse side is `ApplicationRole.users`.
It represents the tenancy hierarchy as a bidirectional one-to-many association whose owning side is `ApplicationTenancy.parent` and whose inverse side is `ApplicationTenancy.children`.
The v4 repository operations normally mutate both sides, but role deletion and some JPA-specific helpers mutate only an inverse collection, while tenancy reassignment can leave the former parent's collection stale.
The forward port must fit the current interface-based Secman applib model and concrete JPA entities without carrying obsolete v2 JDO compatibility concerns.

## Goals / Non-Goals

**Goals:**

- Keep both managed sides consistent during supported role/user and tenancy hierarchy mutations.
- Ensure every relationship change reaches the JPA owning side that controls persisted state.
- Make assigned-role deletion portable by removing owning-side assignments before deleting the role.
- Verify the behavior across a flush-and-reload boundary.
- Preserve current Secman APIs, mappings, tables, and dependencies.

**Non-Goals:**

- Replace mutable collection getters with immutable or unmodifiable views.
- Redesign the Secman aggregate or interface hierarchy.
- Change association mappings, cascade configuration, database tables, or foreign keys.
- Add cycle detection to tenancy hierarchy operations.
- Generalize the fix to unrelated bidirectional associations.

## Decisions

### Keep repository operations as the canonical application boundary

`ApplicationRoleRepository` and `ApplicationTenancyRepository` operations will remain the normal entry points for relationship changes.
Those operations will update both owning and inverse sides before persistence is flushed.
Moving all relationship behavior into a redesigned entity API was considered, but rejected because it would broaden the public model and exceed the forward-port scope.

### Make remaining JPA helpers association-safe

JPA-specific helper methods that remain callable will coordinate both sides directly without recursively calling one another.
Set idempotency will make repeated addition or removal safe.
Leaving the helpers inverse-only was rejected because direct helper use can appear successful in memory while failing to persist the owning-side change.

### Remove role assignments through the existing repository operation

Role deletion will iterate over a defensive snapshot of assigned users and remove each assignment through the existing role-removal operation before deleting permissions and the role.
Clearing only `ApplicationRole.users` was rejected because that collection is the inverse side of the join table.

### Treat child parent assignment as the tenancy synchronization primitive

Changing a tenancy parent will update the owning parent reference, remove the child from a different former parent, and add it to the new parent's children collection.
Clearing and repeated assignment will remain idempotent.
Rejecting reassignment was considered, but the repository API is callable outside the UI and its contract implies replacement rather than add-only behavior.

### Test persisted state

Integration tests will assert both managed sides and then flush, clear, reload, and assert by stable identifiers.
Managed-object-only tests were rejected because they cannot prove that the owning-side database state was corrected.

## Risks / Trade-offs

- [Risk] Coordinating both repository methods and JPA helpers could perform duplicate set operations. → Keep coordination non-recursive and rely on set idempotency.
- [Risk] Removing users while traversing `role.users` could cause concurrent modification. → Iterate over a defensive snapshot.
- [Risk] Direct tenancy reassignment can expose pre-existing cycles. → Keep cycle validation out of scope and preserve existing calling-layer controls.
- [Risk] Flush-and-clear tests can be sensitive to integration-test transaction behavior. → Reuse the existing Secman JPA test bootstrap and reload through repositories using stable identifiers.
- [Trade-off] Mutable collections remain externally modifiable. → Correct supported mutation paths without introducing a compatibility-sensitive API redesign.
