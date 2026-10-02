## 1. Prepare the main-side port

- [ ] 1.1 Create a dedicated implementation branch from main in this worktree and commit the proposal artifacts before applying, using the branch/Jira prefix.
- [ ] 1.2 Review maintenance implementation `746fd6393ce`, approval corrections `04029032473`, and archived contract `47f055e3cbd`; map selected files to main's current modules without merging maintenance history or copying its archive.

## 2. Port annotation, configuration, and facet resolution

- [ ] 2.1 Add applib `Locking` and `DomainObject.locking()` with the maintenance values and DEFAULT semantics, using main's API documentation version.
- [ ] 2.2 Add the restricted configuration enum and immutable DomainObject record component defaulting to OPTIMISTIC; test property binding, absent defaults, and rejection of sentinel values, and verify generated configuration metadata.
- [ ] 2.3 Port persistence-neutral locking facets and factory installation using main's automatic registration, precedence, attribute visitor, and module export conventions.
- [ ] 2.4 Port policy tests to `core/mmtest`, covering explicit policies, no annotation, configuration, superclass/meta-annotation resolution, and AS_CONFIGURED overriding inherited policy; run the existing DomainObject factory tests.

## 3. Port observed JPA bookmark loading and REST coverage

- [ ] 3.1 Adapt `JpaEntityFacet.fetchByBookmark()` to request Jakarta PESSIMISTIC_WRITE for pessimistic policy inside the existing observation wrapper, retaining ordinary optimistic find and empty-result behavior.
- [ ] 3.2 Adapt adapter tests to main's services/observation support; verify find overloads, missing entities, observed execution, and preservation of provider lock/version failures.
- [ ] 3.3 Port REST reference-parameter tests to main, verifying serialized href/bookmark resolution to the concrete entity and preventing validation/action execution after argument-load failure.

## 4. Verify concurrency and EclipseLink state behavior

- [ ] 4.1 Port the versioned callback orchestration fixture into `regressiontests/base-jpa` and adapt interaction/bootstrap APIs in the persistence-jpa tests.
- [ ] 4.2 Verify same-row callback blocking with independent transactions, bounded waits, shared cache enabled, visibility of committed progress, and the fixture's single final completion event.
- [ ] 4.3 Verify rollback releases the lock without exposing rolled-back progress and different orchestration rows remain independent.
- [ ] 4.4 Verify EclipseLink 5.0.2 first-lock refresh of previously unlocked managed state, stale-version refresh behavior, and preservation of changes on repeated already-locked loads; reconcile any provider differences in the design/specification before completing this task.
- [ ] 4.5 Verify pessimistic loads fail without a transaction while ordinary optimistic loads retain existing behavior; run existing JPA bootstrap/query regressions alongside the new tests.

## 5. Integrate documentation and approvals

- [ ] 5.1 Update Antora annotation/configuration guidance with resolution rules, a REST orchestration reference example, transaction lifetime, provider cache behavior, scope exclusions, and application idempotence responsibility.
- [ ] 5.2 Regenerate and review main's affected metamodel and extension approval snapshots using the maintenance approval correction as a footprint reference; run the affected approval tests.
- [ ] 5.3 Run the focused configuration, metamodel, adapter, REST, and JPA regression checks using main's current toolchain; record commands, results, and any remaining limitations in this change.
- [ ] 5.4 Validate the completed OpenSpec change and review the final diff for unintended maintenance build settings, dependencies, or unrelated snapshot changes.
