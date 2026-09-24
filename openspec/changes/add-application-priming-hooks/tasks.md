## 1. Public priming contracts

- [ ] 1.1 Add applib contracts for `PrimingRegistrar`, startup-only `PrimingRegistry`, typed `ActionPrimer`, and typed `ViewPrimer`.
- [ ] 1.2 Implement the immutable `ActionArguments` API with arity, indexed access, type-checked access, and an unmodifiable list view.
- [ ] 1.3 Add API tests and Javadocs covering array-valued parameters, null values, incompatible typed access, and mutation rejection.

## 2. Metamodel-backed registry

- [ ] 2.1 Implement the registry service and initialize it from all application registrars after metamodel loading.
- [ ] 2.2 Resolve action keys from an exact domain class plus local action name, including domain-facing contributed-action identities.
- [ ] 2.3 Resolve view keys from the exact logical type of the registered domain class without hierarchy traversal.
- [ ] 2.4 Validate unknown classes, missing logical types, malformed or unknown action names, and post-freeze mutation with actionable startup failures.
- [ ] 2.5 Freeze registrations into immutable exact-match multimaps that retain every callback registered for a key.
- [ ] 2.6 Add registry tests for valid singular and plural registration, contributed actions, multiple callbacks, deterministic dispatch, exact type matching, invalid registration, and freezing.

## 3. Action execution integration

- [ ] 3.1 Invoke matching action primers after validation and executing-phase argument adjustment but immediately before ordinary domain method invocation.
- [ ] 3.2 Supply the domain-facing owner or mixee, effective immutable arguments, and the current interaction's persistence context to each callback.
- [ ] 3.3 Exclude pass-through invocation and synthetic mixed-in property or collection access while retaining ordinary nested and wrapper-mediated action priming.
- [ ] 3.4 Add action-execution tests for timing, effective arguments, contributed-action targets, nested execution, multiple callbacks, no-registration behavior, and propagated failures.

## 4. Wicket entity-page integration

- [ ] 4.1 Invoke matching view primers for authorized entity pages before expensive title, layout, property, and collection preparation.
- [ ] 4.2 Add request-local guarding so each matching primer runs at most once per entity page in one rendering request and can run again in a later full or Ajax request.
- [ ] 4.3 Add Wicket lifecycle tests for initial rendering, subsequent rendering requests, exact root logical-type selection, no-registration behavior, callback ordering relative to member access, and propagated failures.

## 5. Documentation and verification

- [ ] 5.1 Document registrar setup, action and view examples, exact metamodel matching, callback lifecycle, and the read-only, synchronous, idempotent, and thread-safe callback contract.
- [ ] 5.2 Run focused applib, metamodel, runtime-services, and Wicket test suites and resolve regressions.
- [ ] 5.3 Run strict OpenSpec validation for `add-application-priming-hooks` and confirm the change is apply-ready.
