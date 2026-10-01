## 1. Annotation and configuration

- [ ] 1.1 Add applib `Locking` enum and `DomainObject.locking()` defaulting to `DEFAULT`, documenting unspecified/inherited versus explicit configured semantics.
- [ ] 1.2 Add the resolved configuration enum, optimistic default, and `causeway.applib.annotation.domain-object.locking` property using `DomainObjectConfigOptions` conventions; update configuration metadata as required.
- [ ] 1.3 Add focused configuration tests for the optimistic default, pessimistic binding, and rejection of annotation sentinel values.

## 2. Metamodel policy

- [ ] 2.1 Implement the persistence-neutral resolved locking facet and annotation/configuration facet implementations, and install them from `DomainObjectAnnotationFacetFactory` using existing precedence/provenance patterns.
- [ ] 2.2 Test explicit policies overriding configuration, `DEFAULT` inheritance and meta-annotation resolution, `AS_CONFIGURED` overriding inherited policies, and configuration fallback when no annotation applies.
- [ ] 2.3 Update module exports if required for the public annotation and facet packages; verify shared core remains independent of JPA types.

## 3. JPA bookmark loading

- [ ] 3.1 Consult the concrete entity's locking facet in `JpaEntityFacet.fetchByBookmark()`; use `PESSIMISTIC_WRITE` for pessimistic policy and retain ordinary two-argument `find()` for optimistic policy.
- [ ] 3.2 Add adapter tests covering the selected overload, primary-key conversion, missing entity, and preservation of provider failures without automatic refresh or separate transaction creation.

## 4. EclipseLink and REST regression coverage

- [ ] 4.1 Add a minimal versioned orchestration callback test fixture with line-completion state and an observable final-completion event, reusing existing test infrastructure and dependencies.
- [ ] 4.2 Test overlapping transactions on the same orchestration with deterministic coordination and bounded waits; verify commit releases the lock, the waiter sees committed progress, both updates persist, and the fixture publishes completion once.
- [ ] 4.3 Test rollback release and concurrent loads of different independent orchestrations.
- [ ] 4.4 Verify fresh persistence contexts with EclipseLink shared cache enabled and previously managed entities; cover provider version failures and absence of automatic refresh, and confirm the fresh callback resolution path meets serialization requirements.
- [ ] 4.5 Add a REST argument-resolution regression using `value.href` referencing a concrete pessimistic orchestration through a base-type parameter; verify locking precedes validation/invocation and lock-load failures prevent callback execution through the existing argument-error path.
- [ ] 4.6 Verify a pessimistic load without an active transaction fails and default optimistic loading retains existing behavior.

## 5. Documentation and verification

- [ ] 5.1 Document the annotation values, configuration property/default, callback parameter example, JPA bookmark scope, transaction lifetime, cached-state limits, lock-failure handling, and application responsibility for callback idempotency.
- [ ] 5.2 Run targeted tests for affected applib/config/metamodel/JPA modules and the selected regression suites; record commands and results, including the tested database.
