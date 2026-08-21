## 1. Strict-CSP Stop Gate

- [ ] 1.1 Create a headless CSP isolation fixture using the exact current viewer policy, same-origin packaged assets, and no fixture-owned inline style.
- [ ] 1.2 Instrument `SecurityPolicyViolationEvent`, console errors, component state, source location, violated directive, blocked URI, overlay state, focus, overflow, and external requests in retained machine-readable evidence.
- [ ] 1.3 Run one-operation-at-a-time matrices for Combo Box and Multi-Select Combo Box covering connection, overlay open and close, filtering, selection, clearing, validation, disabled state, narrow and dark presentation, disconnection, and reconnection.
- [ ] 1.4 Trace every retained violation to style-element insertion, style attributes, CSSOM mutation, Constructable StyleSheet behavior, fixture code, or another concrete source using current Vaadin and Lit CSP documentation.
- [ ] 1.5 Prototype viable remedies in isolation, including external styles, Constructable StyleSheets, nonce propagation where supported, candidate configuration or patching, and narrowly scoped CSP Level 3 directives.
- [ ] 1.6 Publish the security trade-off and browser matrix, prove zero unexpected violations under the proposed policy, and stop the remaining implementation if blanket inline-style permission or another unapproved relaxation is required.

## 2. Selective Free-Core Build and Maven Packaging

- [ ] 2.1 Freeze the minimum approved Combo Box and Multi-Select Combo Box direct and transitive package closure with exact versions, integrity values, imported entry points, repositories, licenses, notices, and vulnerability status.
- [ ] 2.2 Add a deterministic selective browser entry and build that excludes Flow, Pro components, broad field widgets, external runtime assets, and telemetry submission.
- [ ] 2.3 Integrate pinned acquisition, selective generation or verification, checksums, license and notice checks, and package-policy failures into the appropriate Maven module without requiring package lifecycle scripts.
- [ ] 2.4 Package the route-lazy reference closure beneath `META-INF/resources` and accepted licensing material beneath the repository's release locations with no runtime CDN dependency.
- [ ] 2.5 Measure the recursive cold closure, enforce the 65 KB gzip budget, record shared chunks and initialization timings, and verify that unaffected routes request no Vaadin resource.

## 3. Semantic Reference Adapters

- [ ] 3.1 Add explicit opt-in configuration and deterministic editor-registry qualification while retaining the existing reference editor as default and fallback.
- [ ] 3.2 Implement shared adapter state mapping for labels, current values, stable identities, required state, disabled reasons, loading, empty, error, validation, focus, and Causeway semantic changes.
- [ ] 3.3 Implement single-reference search, initial selection, selection, clearing, conversion, validation, and reconciliation through existing public GraphQL context operations.
- [ ] 3.4 Implement multi-reference selection only for introspected shapes with authoritative list pending values, stable identities, deterministic ordering, and existing validation semantics.
- [ ] 3.5 Implement debouncing, abort propagation, route-generation checks, stale-response suppression, and deterministic handling of overlapping search and selection updates.
- [ ] 3.6 Define and enforce the search-only autocomplete minimum-input and maximum-result policy, expose an honest limitation or fallback above the bound, and label local callback slicing as non-server paging.
- [ ] 3.7 Reconcile any candidate-owned value, disabled, validation, or overlay state back to the authoritative Causeway state without exposing Vaadin events or data-provider APIs to application code.

## 4. HTMX Lifecycle, Delivery, and Theme Integration

- [ ] 4.1 Add route-lazy candidate loading for the first eligible enabled semantic editor with deduplicated initialization and a recoverable existing-editor fallback.
- [ ] 4.2 Dispose requests, callbacks, listeners, overlays, focus ownership, and candidate references on disconnection, HTMX replacement, custom-fragment navigation, and route supersession.
- [ ] 4.3 Map documented `--causeway-*` variables to supported candidate theme properties while preserving light, dark, reduced-motion, forced-colors, narrow-layout, and long-label behavior.
- [ ] 4.4 Demonstrate enabled semantic editors inside generic and router-selected custom HTML pages beneath one disposable route object context without requiring raw Vaadin tags.
- [ ] 4.5 Enable the pilot only through explicit Petclinic and vanilla HTML sample configuration and verify that default application configuration remains unchanged.

## 5. Automated Qualification

- [ ] 5.1 Add foundation tests for registry qualification, fallback, stable identity, single and multi pending values, semantic events, validation reconciliation, cancellation, stale responses, and disconnect cleanup.
- [ ] 5.2 Add headless keyboard journeys for search, single selection, multi-selection, token removal, clearing, required errors, disabled reasons, Escape dismissal, focus restoration, and canonical object navigation.
- [ ] 5.3 Run axe and manual-focus evidence across desktop, narrow, light, dark, reduced-motion, and forced-colors scenarios with zero unresolved critical or serious pilot violation and zero page overflow.
- [ ] 5.4 Add real Petclinic CSP journeys that fail on unclassified policy violations, Flow detection, external requests, telemetry, residual overlays, stale updates, menu regression, route regression, or candidate loading on unaffected routes.
- [ ] 5.5 Run existing foundation, HTMX, Petclinic integration, and Playwright suites and classify every regression before accepting the pilot.
- [ ] 5.6 Reproduce clean npm acquisition, deterministic generation, vulnerability audit, Maven packaging, asset checksums, compressed budgets, browser evidence, and rollback from documented commands.

## 6. Pilot Documentation and Adoption Review

- [ ] 6.1 Document opt-in configuration, supported member shapes, autocomplete bounds, CSP requirements, packaged assets, stable Causeway contracts, internal Vaadin boundaries, and application-owned custom-page options.
- [ ] 6.2 Document one-step rollback to the existing editor and verify that it requires no GraphQL, route, persisted-data, semantic-event, or custom-page migration.
- [ ] 6.3 Publish final gate results and retain the pilot as sample-scoped or optional unless CSP, licensing, accessibility, lifecycle, bundle, external-request, and existing-viewer compatibility all pass.
- [ ] 6.4 Record deferred work for true paged GraphQL autocomplete, Grid collection sorting and filtering, any raw-widget profile, broad field adoption, and an independent server-side Vaadin viewer.
