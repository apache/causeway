## 1. Diagnostic Contracts and Fixtures

- [ ] 1.1 Define immutable diagnostic lifecycle records, correlation identifiers, operation kinds, outcomes, observer subscriptions, and application configuration contracts.
- [ ] 1.2 Define redaction, truncation, bounded retention, export, and clear contracts with secure defaults.
- [ ] 1.3 Extend deterministic fixtures for introspection, reads, validation, autocomplete, safe queries, mutations, partial GraphQL errors, transport errors, cancellation, obsolete responses, hidden wrappers, and sensitive-looking values.

## 2. Executor Instrumentation

- [ ] 2.1 Instrument the shared GraphQL executor boundary without changing request construction, response parsing, cancellation, or error semantics.
- [ ] 2.2 Publish correlated start and completion records containing operation metadata, exact documents, redacted variables and results, timing, HTTP outcome, GraphQL errors, transport failures, and cancellation.
- [ ] 2.3 Preserve ordering and correlation across concurrent operations and distinguish aborted, obsolete, failed, partially successful, and successful outcomes.
- [ ] 2.4 Test all operation families and confirm diagnostics remain absent and allocation remains minimal when disabled.

## 3. Redaction and Retention

- [ ] 3.1 Implement recursive immutable diagnostic snapshots that cannot mutate live request variables or parsed GraphQL results.
- [ ] 3.2 Implement default masking for authorization material, common sensitive key names, and values beneath rich member wrappers reporting `hidden: true`.
- [ ] 3.3 Implement configurable redaction and truncation policies with explicit application opt-in for less restrictive local diagnostics.
- [ ] 3.4 Implement the bounded in-memory ring store, subscription, filtering, clear, and JSON export behavior.
- [ ] 3.5 Test nested objects, lists, large scalar payloads, errors, hidden values, custom policies, eviction, and listener release.

## 4. Diagnostic Presentation

- [ ] 4.1 Implement `<cw-graphql-diagnostics>` as an optional light-DOM consumer of an injected or nearest diagnostic source.
- [ ] 4.2 Implement accessible operation summaries, status and duration indicators, kind and outcome filters, expandable request and response details, copy, export, and clear controls.
- [ ] 4.3 Keep query documents and JSON payloads readable at narrow and wide viewports with keyboard operation, focus management, and live lifecycle announcements.
- [ ] 4.4 Add application styling hooks and examples for replacing the standard presentation while retaining the observer and store contracts.

## 5. Executable Sample

- [ ] 5.1 Add explicit development diagnostics enablement to `sample-html` without changing the default behavior of other component consumers.
- [ ] 5.2 Add a labelled diagnostic region and stable hooks covering initial targeted introspection, the coordinated object read, secondary collections, property validation and update, safe action queries, and mutating action results.
- [ ] 5.3 Ensure diagnostic rendering never exposes the hidden sample value or configured sensitive values under the sample policy.
- [ ] 5.4 Document how to enable, inspect, filter, copy, clear, export, and troubleshoot diagnostics and how to keep them disabled in production.

## 6. Verification

- [ ] 6.1 Add dependency-free Node coverage for observer lifecycle, record immutability, correlation, concurrency, cancellation, redaction, truncation, retention, filtering, and diagnostic-element rendering.
- [ ] 6.2 Extend random-port integration tests to compare emitted diagnostic documents and outcomes with real GraphQL requests and responses for safe and mutating operations.
- [ ] 6.3 Add browser verification for initial reads and interactions, stable hooks, keyboard use, responsive light and dark presentation, copy and clear behavior, and absence of hidden values.
- [ ] 6.4 Run foundation and sample Maven tests, Node tests, syntax and formatting checks, strict OpenSpec validation after promotion, browser console checks, and configured accessibility audits.
