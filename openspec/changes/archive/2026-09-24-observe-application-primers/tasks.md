## 1. Primer Observation Model

- [x] 1.1 Add stable action-primer and view-primer observation names and bounded contextual-name construction using the existing semantic naming policy.
- [x] 1.2 Define canonical action-primer metadata for complete domain-facing object type and action identifiers.
- [x] 1.3 Define canonical view-primer metadata for the complete domain-facing object type.
- [x] 1.4 Retain the validated domain-facing action identity with each registered action primer without exposing primer implementation identity.

## 2. Per-Callback Observation Dispatch

- [x] 2.1 Resolve the optional Causeway observation integration at the `PrimingRegistryDefault` dispatch boundary without changing the public priming SPI.
- [x] 2.2 Wrap every matching action-primer callback in its own `causeway.priming.action` observation.
- [x] 2.3 Wrap every matching view-primer callback in its own `causeway.priming.view` observation.
- [x] 2.4 Preserve deterministic callback order, fail-fast behavior, original exception propagation, and observation scope cleanup.
- [x] 2.5 Preserve no-match, excluded association-access, repeated Wicket visit, absent-integration, and no-op behavior without creating synthetic or aggregate observations.
- [x] 2.6 Verify that action primers remain children of action invocation and view primers remain natural request-level siblings of later Wicket preparation and rendering.

## 3. Focused Tests

- [x] 3.1 Test zero, one, and multiple matching action primers, including one sibling observation per invoked callback and no aggregate parent.
- [x] 3.2 Test zero, one, and multiple matching view primers together with the existing once-per-request page visit guard.
- [x] 3.3 Test ordinary and contributed action naming, complete canonical attributes, namespace fallback, deterministic truncation, and case preservation.
- [x] 3.4 Test callback success, runtime exception, error, fail-fast ordering, scope cleanup, and unchanged exception identity.
- [x] 3.5 Test inactive and absent observation integration paths invoke every applicable callback exactly once without exporting observations.
- [x] 3.6 Test metadata privacy, including absence of implementation classes, lambda names, registration order, targets, arguments, values, bookmarks, titles, users, tenants, and generated component paths.

## 4. Compatibility and Operations Evidence

- [x] 4.1 Extend Java-agent compatibility coverage to verify action-primer and view-primer parentage and Java-agent-owned JDBC descendants without duplicate HTTP or JDBC spans.
- [x] 4.2 Update the tracing operations guide with stable names, contextual names, hierarchy examples, multiple-primer behavior, privacy, and view-primer placement before page preparation.
- [x] 4.3 Run focused metamodel, Wicket, and Java-agent compatibility tests, run `git diff --check`, validate the OpenSpec change strictly, and record commands and results in `validation.md`.
