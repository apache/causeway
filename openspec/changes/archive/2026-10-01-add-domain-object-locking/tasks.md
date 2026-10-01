## 1. Annotation and configuration

- [x] 1.1 Add applib `Locking` enum and `DomainObject.locking()` defaulting to `DEFAULT`, documenting unspecified/inherited versus explicit configured semantics.
- [x] 1.2 Add the resolved configuration enum, optimistic default, and `causeway.applib.annotation.domain-object.locking` property using `DomainObjectConfigOptions` conventions; update configuration metadata as required.
- [x] 1.3 Add focused configuration tests for the optimistic default, pessimistic binding, and rejection of annotation sentinel values.

## 2. Metamodel policy

- [x] 2.1 Implement the persistence-neutral resolved locking facet and annotation/configuration facet implementations, and install them from `DomainObjectAnnotationFacetFactory` using existing precedence/provenance patterns.
- [x] 2.2 Test explicit policies overriding configuration, `DEFAULT` inheritance and meta-annotation resolution, `AS_CONFIGURED` overriding inherited policies, and configuration fallback when no annotation applies.
- [x] 2.3 Update module exports if required for the public annotation and facet packages; verify shared core remains independent of JPA types.

## 3. JPA bookmark loading

- [x] 3.1 Consult the concrete entity's locking facet in `JpaEntityFacet.fetchByBookmark()`; use `PESSIMISTIC_WRITE` for pessimistic policy and retain ordinary two-argument `find()` for optimistic policy.
- [x] 3.2 Add adapter tests covering the selected overload, primary-key conversion, missing entity, and preservation of provider failures without extra adapter refresh calls or separate transaction creation.

## 4. EclipseLink and REST regression coverage

- [x] 4.1 Add a minimal versioned orchestration callback test fixture with line-completion state and an observable final-completion event, reusing existing test infrastructure and dependencies.
- [x] 4.2 Test overlapping transactions on the same orchestration with deterministic coordination and bounded waits; verify commit releases the lock, the waiter sees committed progress, both updates persist, and the fixture publishes completion once.
- [x] 4.3 Test rollback release and concurrent loads of different independent orchestrations.
- [x] 4.4 Verify fresh persistence contexts with EclipseLink shared cache enabled and previously managed entities; cover provider version failures and first-acquisition/repeated-load refresh behavior, and confirm the fresh callback resolution path meets serialization requirements.
- [x] 4.5 Add a REST argument-resolution regression using `value.href` referencing a concrete pessimistic orchestration through a base-type parameter; verify locking precedes validation/invocation and lock-load failures prevent callback execution through the existing argument-error path.
- [x] 4.6 Verify a pessimistic load without an active transaction fails and default optimistic loading retains existing behavior.

## 5. Documentation and verification

- [x] 5.1 Document the annotation values, configuration property/default, callback parameter example, JPA bookmark scope, transaction lifetime, cached-state limits, lock-failure handling, and application responsibility for callback idempotency.
- [x] 5.2 Run targeted tests for affected applib/config/metamodel/JPA modules and the selected regression suites; record commands and results, including the tested database.

## Validation Results

Verified on 2026-10-01 using Maven 3.9.13, Temurin Java 21.0.10 (compile release 11), EclipseLink 2.7.16 and the existing H2 regression database dependency.

The proposal was committed before implementation as `1086e1de085` (`CAUSEWAY-4070: proposes configurable domain object locking`).

Final command (from repository root):

```sh
JAVA_HOME=/Users/danhaywood/.sdkman/candidates/java/21.0.10-tem mvn -o \
  -Drevision=2.2.0-SNAPSHOT -Dmodule-regressiontests \
  -pl regressiontests/persistence-jpa -am \
  -Dtest=JpaPessimisticLockingTest,JpaQueryTest,JpaBootstrappingTest,ObjectActionArgHelperReferenceTest,CausewayConfiguration_locking_Test,DomainObjectLockingFacetFactoryTest,DomainObjectAnnotationFacetFactoryTest,JpaEntityFacetLockingTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.source.skip=true test
```

Result: **BUILD SUCCESS**, with 66 passing test cases and two existing disabled query scenarios skipped (nested factory cases counted from XML test-case entries). The selected reactor compiles the affected applib/config/metamodel/JPA/REST modules and regression fixtures. The tests cover policy resolution and configuration metadata generation, JPA adapter overload selection and unchanged provider failures, REST reference parsing/veto handling, and EclipseLink transaction contention with shared cache enabled. Existing JPA query/bootstrap tests also pass; existing disabled query scenarios remain skipped.

EclipseLink results corrected an initial design assumption: the first locking `find()` refreshes previously unlocked managed state, including unflushed local changes and stale versions. Repeated already-locked loads retain callback changes. Design, specs and documentation now describe the observed provider behavior, and no extra adapter refresh calls are introduced.

REST regression coverage exercises the real argument parser and reference-to-bookmark conversion with a mocked object manager; database concurrency coverage exercises the real Causeway object manager/JPA adapter in independent interactions. This does not launch a full HTTP server or external worker/outbox service.

Configuration metadata includes `causeway.applib.annotation.domain-object.locking` with the resolved policy type and optimistic default. No dependency changes were required. The production database was not tested.

The existing nested domain-object factory cases were also checked with `-pl core/metamodel -am '-Dtest=DomainObjectAnnotationFacetFactoryTest*' -Dsurefire.failIfNoSpecifiedTests=false test` under the same Java/Maven toolchain: **BUILD SUCCESS**, 33 passing cases. Surefire's parent-suite summary reports zero for this nested suite, but the XML contains its 33 successful test-case entries.
