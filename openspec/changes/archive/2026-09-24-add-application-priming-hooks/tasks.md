## 1. Public priming contracts

- [x] 1.1 Add applib contracts for `PrimingRegistrar`, startup-only `PrimingRegistry`, typed `ActionPrimer`, and typed `ViewPrimer`.
- [x] 1.2 Implement the immutable `ActionArguments` API with arity, indexed access, type-checked access, and an unmodifiable list view.
- [x] 1.3 Add API tests and Javadocs covering array-valued parameters, null values, incompatible typed access, and mutation rejection.

## 2. Metamodel-backed registry

- [x] 2.1 Implement the registry service and initialize it from all application registrars after metamodel loading.
- [x] 2.2 Resolve action keys from an exact domain class plus local action name, including domain-facing contributed-action identities.
- [x] 2.3 Resolve view keys from the exact logical type of the registered domain class without hierarchy traversal.
- [x] 2.4 Validate unknown classes, missing logical types, malformed or unknown action names, and post-freeze mutation with actionable startup failures.
- [x] 2.5 Freeze registrations into immutable exact-match multimaps that retain every callback registered for a key.
- [x] 2.6 Add registry tests for valid singular and plural registration, contributed actions, multiple callbacks, deterministic dispatch, exact type matching, invalid registration, and freezing.

## 3. Action execution integration

- [x] 3.1 Invoke matching action primers after validation and executing-phase argument adjustment but immediately before ordinary domain method invocation.
- [x] 3.2 Supply the domain-facing owner or mixee, effective immutable arguments, and the current interaction's persistence context to each callback.
- [x] 3.3 Exclude pass-through invocation and synthetic mixed-in property or collection access while retaining ordinary nested and wrapper-mediated action priming.
- [x] 3.4 Add action-execution tests for timing, effective arguments, contributed-action targets, nested execution, multiple callbacks, no-registration behavior, and propagated failures.

## 4. Wicket entity-page integration

- [x] 4.1 Invoke matching view primers for authorized entity pages before expensive title, layout, property, and collection preparation.
- [x] 4.2 Add request-local guarding so each matching primer runs at most once per entity page in one rendering request and can run again in a later full or Ajax request.
- [x] 4.3 Add Wicket lifecycle tests for initial rendering, subsequent rendering requests, exact root logical-type selection, no-registration behavior, callback ordering relative to member access, and propagated failures.

## 5. Documentation and verification

- [x] 5.1 Document registrar setup, action and view examples, exact metamodel matching, callback lifecycle, and the read-only, synchronous, idempotent, and thread-safe callback contract.
- [x] 5.2 Run focused applib, metamodel, runtime-services, and Wicket test suites and resolve regressions.
- [x] 5.3 Run strict OpenSpec validation for `add-application-priming-hooks` and confirm the change is apply-ready.
