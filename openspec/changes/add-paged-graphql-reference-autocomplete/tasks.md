## 1. Establish current and target contracts

- [x] 1.1 Capture existing property and action-parameter autocomplete field shapes and legacy client limits.
- [x] 1.2 Record deterministic Reference Application autocomplete order, total, and configured qualification page size.
- [x] 1.3 Define property, object-action, service-action, scalar, enum, object, versionless, and polymorphic window shapes.
- [x] 1.4 Define valid, defaulted, boundary, empty, overflow, dependent-argument, and changing-result vectors.

## 2. Add GraphQL configuration and window types

- [x] 2.1 Add GraphQL autocomplete default-window-size and max-window-size configuration with validated defaults.
- [x] 2.2 Add deterministic generated type names for property and action-parameter autocomplete windows.
- [x] 2.3 Add a dedicated bounded autocomplete-window exception that omits submitted values.
- [x] 2.4 Add the `APPLICATION` ordering enum and deduplicated registry construction.
- [x] 2.5 Build member-specific window result types with items and complete paging metadata.
- [x] 2.6 Validate offset and size before invoking domain autocomplete.
- [x] 2.7 Materialize one authoritative result per request, preserve encounter order, and return an immutable slice.

## 3. Expose additive property windows

- [x] 3.1 Add `autoCompleteWindow` beside each advertised rich property `autoComplete` field.
- [x] 3.2 Preserve the existing property autocomplete field and data fetcher unchanged.
- [x] 3.3 Use the same source object, authorization, search text, item output mapping, and Causeway autocomplete semantic.
- [x] 3.4 Register the generated field, result type, item type, and data fetcher deterministically.

## 4. Expose additive action-parameter windows

- [x] 4.1 Add `autoCompleteWindow` beside each advertised rich action-parameter autocomplete field.
- [x] 4.2 Preserve preceding declared parameter arguments and append search, offset, and size.
- [x] 4.3 Reuse parameter negotiation, conversion, authorization, and item output mapping.
- [x] 4.4 Preserve existing object and service action autocomplete documents unchanged.
- [x] 4.5 Register action-parameter window result types and data fetchers deterministically.

## 5. Add GraphQL model and integration verification

- [x] 5.1 Test autocomplete configuration defaults, valid overrides, and default-not-greater-than-maximum validation.
- [x] 5.2 Test deterministic result type naming and registry deduplication.
- [x] 5.3 Test valid first, middle, final, empty, exact-maximum, and defaulted windows.
- [x] 5.4 Test negative offset, zero size, and above-maximum requests fail before domain invocation.
- [x] 5.5 Test action dependent arguments remain declared and authoritative.
- [x] 5.6 Test legacy fields retain their previous schema and result behavior.
- [x] 5.7 Test item output mapping for object identity and supported non-object values.

## 6. Extend targeted client discovery and semantic contexts

- [x] 6.1 Follow advertised autocomplete-window result and item types through targeted introspection.
- [x] 6.2 Describe window defaults, maximum metadata fields, and item output shape without speculative selections.
- [x] 6.3 Add one immutable normalized semantic autocomplete-window result shape.
- [x] 6.4 Add object-context property window execution that emits only advertised arguments and fields.
- [x] 6.5 Add object-action parameter window execution with current preceding values.
- [x] 6.6 Add service-action parameter window execution with current preceding values.
- [x] 6.7 Preserve existing list-returning context methods for compatibility.
- [x] 6.8 Normalize legacy list fallback only within the existing accepted bound and mark it non-windowed.
- [x] 6.9 Add cancellation, request-key, filter-generation, and stale-response tests across overlapping pages.

## 7. Integrate toolkit-neutral property and action controllers

- [x] 7.1 Prefer window capability for property searches and reset paging when filter text changes.
- [x] 7.2 Prefer window capability for object and service action parameter searches.
- [x] 7.3 Reset dependent parameter paging when preceding pending values change.
- [x] 7.4 Preserve pending value, semantic identity, validation, focus, cancellation, and prompt contracts.
- [x] 7.5 Expose an accessible additional-results or refine indication for native first-window presentation.
- [x] 7.6 Retain visible legacy over-bound failure without silent slicing.
- [x] 7.7 Add controller tests for later pages, out-of-order completion, supersession, prompt closure, route replacement, and disconnect.

## 8. Connect the internal Vaadin lazy data provider

- [x] 8.1 Confirm current Vaadin Combo Box data-provider and callback semantics from pinned-version documentation.
- [x] 8.2 Add an internal page-request bridge carrying filter, offset, size, and a one-shot responder.
- [x] 8.3 Configure Combo Box page size and lazy data provider only when window capability is advertised.
- [x] 8.4 Return authoritative items and total count without assigning or slicing a complete local item array.
- [x] 8.5 Cancel or ignore stale provider callbacks after filter change, rerender, route replacement, or disconnect.
- [x] 8.6 Reconcile selected single and multi-reference identities even when their pages are not loaded.
- [x] 8.7 Preserve finite-items behavior and fallback for choices and legacy autocomplete.
- [x] 8.8 Add adapter tests for first and later pages, empty results, changed totals, stale callbacks, selection, and fallback.

## 9. Qualify the pinned Reference Application

- [x] 9.1 Configure a deterministic qualification page smaller than the existing autocomplete result set.
- [x] 9.2 Add target-catalogue and inventory coverage for multi-window autocomplete.
- [x] 9.3 Verify property or action-parameter first, middle, final, and empty windows through real GraphQL.
- [x] 9.4 Verify counts, continuation, encounter order, requested maximum, and invalid-window errors.
- [x] 9.5 Verify legacy autocomplete still returns its established complete result.
- [x] 9.6 Add a browser journey that obtains and selects a reference absent from the first window.
- [x] 9.7 Verify dependent values, validation or submission, semantic identity, cancellation, and stale-filter behavior.
- [x] 9.8 Qualify native fallback and explicitly enabled Vaadin modes without external requests or policy regressions.
- [x] 9.9 Verify clean and incremental capability inventory generation remain byte-identical.

## 10. Preserve compatibility and production isolation

- [x] 10.1 Verify no existing GraphQL field, route, component, semantic event, context result, or operation placement is removed or renamed.
- [x] 10.2 Verify no Flow, Binder, Java DataProvider, Pro component, telemetry, CDN, or application-facing Vaadin API is introduced.
- [x] 10.3 Verify unaffected routes remain route-lazy with zero Vaadin requests.
- [x] 10.4 Verify exact CSP hashes, `style-src-attr 'none'`, bundle checksum, native rollback, and package dependencies remain unchanged.
- [x] 10.5 Verify errors and evidence omit search text, protected pending arguments, identifiers, and returned values.
- [x] 10.6 Verify documentation does not claim database pushdown, cursor stability, or cross-request snapshots.

## 11. Document the capability

- [x] 11.1 Update GraphQL setup documentation with fields, result metadata, configuration, ordering, and consistency limits.
- [x] 11.2 Update foundation and HTMX reference-editor documentation with window preference and legacy fallback.
- [x] 11.3 Add an operation-shape and window-bound matrix with safe Reference Application counts.
- [x] 11.4 Add reproducible focused and full qualification commands.
- [x] 11.5 Add implementation, compatibility, security, rollback, and retained-limit evidence.
- [x] 11.6 Record final gate results and the next recommended Vaadin editor-family change.

## 12. Run final qualification gates

- [x] 12.1 Run GraphQL configuration, model, schema, and focused integration suites.
- [x] 12.2 Run foundation Node and web-component Maven suites.
- [x] 12.3 Run full Petclinic integration and Playwright suites in native and applicable candidate modes.
- [x] 12.4 Run Reference Application clean package, integration, inventory, and Playwright suites.
- [x] 12.5 Run strict CSP, accessibility, keyboard, responsive, theme, external-request, console-error, page-error, and overflow gates.
- [x] 12.6 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation verification.
