## 1. Observation Infrastructure

- [ ] 1.1 Define immutable serializable descriptors for page, fieldset, property, collection, and action render regions with stable observation names and safe metadata.
- [ ] 1.2 Implement a reusable Wicket render behavior that resolves `CausewayObservationIntegration` per request, starts and scopes an observation immediately before actual rendering, and closes it after successful rendering.
- [ ] 1.3 Implement request-local tracking that unregisters normally completed observations and closes remaining observations in reverse start order after an exception or request detach.
- [ ] 1.4 Wire defensive cleanup into the existing Wicket request-cycle integration and record the originating render failure when available.

## 2. Semantic Region Instrumentation

- [ ] 2.1 Attach the page render observation to `EntityPage` with stable logical object-type metadata.
- [ ] 2.2 Attach fieldset render observations to `PropertyGroup` using safe layout identifiers.
- [ ] 2.3 Attach property render observations to regular entity-property scalar panels while excluding action parameters and compact/table-cell renderings.
- [ ] 2.4 Attach collection render observations to `EntityCollectionPanel` using stable collection identifiers.
- [ ] 2.5 Attach action render observations to entity object-form action links while excluding service-menu and collection-row actions.
- [ ] 2.6 Apply the Wicket viewer module-name convention and verify that no domain values, instance identifiers, generated Wicket paths, or other prohibited metadata are emitted.

## 3. Automated Verification

- [ ] 3.1 Add focused lifecycle tests proving observation start, scope nesting, normal reverse-order closure, and idempotent request cleanup.
- [ ] 3.2 Add failure tests proving that an interrupted render records the available failure, drains active observations, and leaves no current observation behind.
- [ ] 3.3 Add Wicket component tests proving the page, fieldset, property, collection, and action observation names, parentage, and safe identifiers.
- [ ] 3.4 Add exclusion tests for table cells, row actions, action parameters, service menus, and inactive observation mode.
- [ ] 3.5 Add a serialization test proving that active observations, scopes, registries, and integration services are not retained with an instrumented page or behavior.
- [ ] 3.6 Add an Ajax partial-render test proving that only rendered instrumented regions create observations under the current Ajax request trace.

## 4. Documentation and Validation

- [ ] 4.1 Update `adoc/micrometer-tracing-operations.adoc` with the Wicket span taxonomy, expected trace tree, activation behavior, metadata privacy rules, exclusions, and render-only limitation.
- [ ] 4.2 Run the focused Wicket UI and viewer tests together with observation integration tests.
- [ ] 4.3 Validate a representative entity page against local Jaeger and record that full-page and Ajax traces have the expected nesting without per-cell span amplification.
