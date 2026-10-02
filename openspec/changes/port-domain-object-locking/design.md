## Context

The maintenance implementation serializes REST callbacks updating a shared payment orchestration by acquiring a pessimistic write lock while resolving its bookmark. The lock is acquired before parameter validation and callback execution and remains held through the request transaction. Its implementation, approval corrections, and archived specification are available in the sibling maintenance worktree and shared Git history (CAUSEWAY-4070).

Main is 4.0.0-SNAPSHOT and currently uses Jakarta Persistence and EclipseLink 5.0.2. Its configuration consists of immutable records, facets register during construction, metamodel tests live in `core/mmtest`, JPA fixtures live in `regressiontests/base-jpa`, and JPA bookmark loading already has an observation wrapper. These differences require an adapted forward port rather than an unchanged cherry-pick.

The existing REST reference path still resolves `value.href` through `JsonParserHelper`, `IResourceContext`, `ObjectManager`, and the concrete entity specification to `JpaEntityFacet.fetchByBookmark()`. This is the interception point for both action targets and entity reference parameters.

## Goals / Non-Goals

**Goals:**

- Preserve maintenance annotation/configuration semantics and optimistic compatibility.
- Serialize cooperating callbacks loading the same orchestration through JPA bookmarks, retaining the enclosing transaction and observation behavior.
- Verify concurrency and cached-state behavior on main's provider and regenerate main-specific approval snapshots.
- Author the proposal and implementation in the main worktree, pulling selected behavior from maintenance with traceable source commits.

**Non-Goals:**

- Automatic locking of queries, repository loads, QueryDSL results, or lazy relationships; JDO locking changes.
- New lock modes, retry or timeout policies, application callback deduplication, or application-specific orchestration implementations.
- New dependencies, provider upgrades, or importing maintenance build configuration and historical OpenSpec archives into main.

## Decisions

### Preserve the public policy and resolution rules

Add applib `Locking` values `OPTIMISTIC`, `PESSIMISTIC`, `AS_CONFIGURED`, and `DEFAULT`, with `DomainObject.locking()` defaulting to `DEFAULT`. Treat `DEFAULT` as unspecified during merged annotation resolution so superclass or meta-annotation policies can contribute; explicit `AS_CONFIGURED` selects configuration instead. Explicit optimistic/pessimistic values override configuration.

Add `causeway.applib.annotation.domain-object.locking` through a main configuration record component with `@DefaultValue("OPTIMISTIC")` and a restricted `DomainObjectConfigOptions.LockingPolicy` enum. Reject annotation sentinel values at configuration binding. Entities without an annotation use configuration. Use main's property-binding test support rather than maintenance's mutable configuration setters. Preserving this contract gives applications the same adoption path on both branches.

### Use main's metamodel conventions

Port a persistence-neutral resolved locking facet and its annotation/configuration provenance implementations. Follow main's constructor registration and precedence/attribute conventions; avoid registering the same facet twice. Read the facet during bookmark loading to avoid dependence on facet factory ordering. Keep all Jakarta/JPA types in the persistence adapter. Port facet tests to `core/mmtest` using its configuration-aware testing context.

### Keep the lock inside the observed bookmark load

Within the existing `observationProvider` operation, decode `bookmark.identifier()`, consult the concrete entity's facet, and call the three-argument `EntityManager.find(..., LockModeType.PESSIMISTIC_WRITE)` for pessimistic policy. Retain the ordinary two-argument find for optimistic policy and existing empty-result handling. Adapt adapter tests to main's injected services and observation provider.

Use the caller's transaction; do not introduce a nested or separate transaction, explicit refresh calls, or lock downgrades. No transaction and provider lock/version failures remain failures. Preserve REST's existing conversion of reference-load failures into parameter-validation errors, preventing callback invocation. An action-local lock would occur after argument validation; a JVM mutex would not coordinate separate application instances. The maintenance interception point remains the appropriate boundary.

### Revalidate provider behavior instead of assuming version equivalence

Maintenance tests showed EclipseLink 2.7.16 refreshes previously unlocked managed state on first pessimistic find, including unflushed changes and stale versions, while repeated finds of an already locked entity retain callback changes. Re-run these scenarios on EclipseLink 5.0.2 with shared cache enabled and independent persistence contexts. The adapter retains standard provider behavior without additional refresh logic; documentation must reflect the behavior established on main.

Port the concurrency fixture into `regressiontests/base-jpa` and tests into `regressiontests/persistence-jpa`, adapting Jakarta imports, interaction services, real bookmark serialization, and current bootstrap configuration. Use synchronization and bounded waits to establish blocking, state visibility after commit, release on rollback, independent rows, and a single completion event produced by the fixture's idempotent logic. Retain focused REST reference-resolution coverage alongside provider integration tests. If provider behavior differs from the specified cached-state scenarios, reconcile the specification and design explicitly before treating validation as complete.

### Pull selected changes and regenerate approvals on main

Use source commit `746fd6393ce` for implementation and `04029032473` for the approval footprint; use `47f055e3cbd` and the archived maintenance change for the completed contract. Do not merge the maintenance branch or copy its generated approvals wholesale. Main's metamodel and module layout differ, so regenerate and review only affected snapshots, including extension snapshots where still applicable.

Document the feature in Antora's DomainObject annotation reference and associated configuration/adoption guidance, including the callback reference example, scope, transaction lifetime, and first-lock acquisition caveat. Use main's version for new API documentation. Archive this main proposal independently after implementation and sync its capability into main's specs.

## Risks / Trade-offs

- [Provider refresh semantics may differ] → Require main-provider regression evidence and reconcile any discrepancy before completion.
- [Global pessimistic configuration affects bookmark reads and reference validation] → Keep optimistic as the default and document transaction requirements and contention.
- [First lock acquisition can overwrite earlier local changes] → Resolve the orchestration lock before reading/modifying state and verify repeated locked loads preserve pending updates.
- [Long transactions, lock waits, or deadlocks] → Preserve provider failure behavior and existing database configuration; keep callback transactions short and bound test waits.
- [Approval regeneration can obscure unrelated differences] → Compare main's baseline and review only snapshots affected by the new facet.
- [Other loading paths or duplicate callbacks remain application concerns] → Document the bookmark boundary and retain application idempotence responsibility.

## Migration Plan

Implement on a dedicated branch based on main in the main worktree, committing proposal artifacts before applying. Existing applications need no changes. Applications requiring serialized callback access opt in with `@DomainObject(locking = Locking.PESSIMISTIC)` and retain version mappings. Reverting to optimistic policy restores ordinary bookmark loading. No data migration is required.

## Open Questions

- Confirm first-acquisition refresh, stale-version handling, and repeated locked loads on main's EclipseLink 5.0.2 during implementation.
- Confirm the exact approval snapshot set affected by main's installed facets; the maintenance correction commit identifies candidate modules rather than replacement files.
