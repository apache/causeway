## 1. Prepare the main-side port

- [x] 1.1 Create a dedicated implementation branch from main in this worktree and commit the proposal artifacts before applying, using the branch/Jira prefix.
- [x] 1.2 Review maintenance implementation `746fd6393ce`, approval corrections `04029032473`, and archived contract `47f055e3cbd`; map selected files to main's current modules without merging maintenance history or copying its archive.

## 2. Port annotation, configuration, and facet resolution

- [x] 2.1 Add applib `Locking` and `DomainObject.locking()` with the maintenance values and DEFAULT semantics, using main's API documentation version.
- [x] 2.2 Add the restricted configuration enum and immutable DomainObject record component defaulting to OPTIMISTIC; test property binding, absent defaults, and rejection of sentinel values, and verify generated configuration metadata.
- [x] 2.3 Port persistence-neutral locking facets and factory installation using main's automatic registration, precedence, attribute visitor, and module export conventions.
- [x] 2.4 Port policy tests to `core/mmtest`, covering explicit policies, no annotation, configuration, superclass/meta-annotation resolution, and AS_CONFIGURED overriding inherited policy; run the existing DomainObject factory tests.

## 3. Port observed JPA bookmark loading and REST coverage

- [x] 3.1 Adapt `JpaEntityFacet.fetchByBookmark()` to request Jakarta PESSIMISTIC_WRITE for pessimistic policy inside the existing observation wrapper, retaining ordinary optimistic find and empty-result behavior.
- [x] 3.2 Adapt adapter tests to main's services/observation support; verify find overloads, missing entities, observed execution, and preservation of provider lock/version failures.
- [x] 3.3 Port REST reference-parameter tests to main, verifying serialized href/bookmark resolution to the concrete entity and preventing validation/action execution after argument-load failure.

## 4. Verify concurrency and EclipseLink state behavior

- [x] 4.1 Port the versioned callback orchestration fixture into `regressiontests/base-jpa` and adapt interaction/bootstrap APIs in the persistence-jpa tests.
- [x] 4.2 Verify same-row callback blocking with independent transactions, bounded waits, shared cache enabled, visibility of committed progress, and the fixture's single final completion event.
- [x] 4.3 Verify rollback releases the lock without exposing rolled-back progress and different orchestration rows remain independent.
- [x] 4.4 Verify EclipseLink 5.0.2 first-lock refresh of previously unlocked managed state, stale-version refresh behavior, and preservation of changes on repeated already-locked loads; reconcile any provider differences in the design/specification before completing this task.
- [x] 4.5 Verify pessimistic loads fail without a transaction while ordinary optimistic loads retain existing behavior; run existing JPA bootstrap/query regressions alongside the new tests.

## 5. Integrate documentation and approvals

- [x] 5.1 Update Antora annotation/configuration guidance with resolution rules, a REST orchestration reference example, transaction lifetime, provider cache behavior, scope exclusions, and application idempotence responsibility.
- [x] 5.2 Regenerate and review main's affected metamodel and extension approval snapshots using the maintenance approval correction as a footprint reference; run the affected approval tests.
- [x] 5.3 Run the focused configuration, metamodel, adapter, REST, and JPA regression checks using main's current toolchain; record commands, results, and any remaining limitations in this change.
- [x] 5.4 Validate the completed OpenSpec change and review the final diff for unintended maintenance build settings, dependencies, or unrelated snapshot changes.

## Verification results

- Proposal committed before implementation as `6d89fce763a` on `CAUSEWAY-4070-v4` in the main worktree.
- Focused reactor build: **BUILD SUCCESS**, 73 tests, 0 failures, 0 errors, 2 existing JpaQueryTest skips (71 passed). Includes 33 existing DomainObject factory tests, made executable via a concrete subclass of main's abstract suite.
- EclipseLink **5.0.2** passes all eight concurrency/cache/transaction regressions: same-row blocking, commit visibility and single fixture completion event, rollback release, independent rows, first-lock refresh of local/stale managed state, repeated-lock preservation, pessimistic transaction requirement, and ordinary optimistic compatibility.
- Adapter tests verify both find overloads execute inside the bookmark observation and provider exceptions remain intact. REST tests resolve the concrete serialized bookmark and exercise the existing parameter-veto failure path; they are parser tests, not a complete HTTP concurrency test.
- Generated configuration metadata exposes `causeway.applib.annotation.domain-object.locking` with restricted LockingPolicy and default OPTIMISTIC.
- Five approval files regenerated on main. Removing the new locking facet blocks yields exactly the previous content: 55 domainmodel facets and six facets across four PDF.js snapshots.
- No dependency or build configuration changes. Provider concurrency tests use the existing H2 regression database; production-database lock timeout/deadlock behavior remains deployment-specific.

Command (from the main worktree, Maven 3.9.13, Java 25.0.4, compile release 17):

```sh
JAVA_HOME=/Users/danhaywood/.sdkman/candidates/java/25.0.4-tem mvn -o \
  -Drevision=4.0.0-SNAPSHOT -Dmodule-regressiontests \
  -pl core/mmtest,viewers/restfulobjects/viewer,regressiontests/persistence-jpa,regressiontests/domainmodel,extensions/vw/pdfjs/metamodel -am \
  '-Dtest=CausewayConfiguration_locking_Test,DomainObjectLockingFacetFactoryTest,DomainObjectAnnotationFacetFactoryRegressionTest,JpaEntityFacetLockingTest,ObjectActionArgHelperReferenceTest,JpaPessimisticLockingTest,JpaQueryTest,JpaBootstrappingTest,MetaModelRegressionTest,PdfjsViewer*IntegTest' \
  -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.source.skip=true test
```

The initial builds fetched missing main dependencies online; the final verification succeeded offline. Log: `/tmp/causeway-4070-v4-verification.log`.

Archive verification (2026-10-06): reran the focused reactor command above successfully against the current checkout. All eight pessimistic-locking regressions passed; the two existing JpaQueryTest skips remain. Log: `/tmp/locking-archive-validation.log`.
