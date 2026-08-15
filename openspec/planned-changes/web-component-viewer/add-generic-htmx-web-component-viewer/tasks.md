## 1. Viewer Module and Route Contracts

- [ ] 1.1 Establish the optional viewer module, browser asset packaging, HTMX dependency, default theme, demonstration application, and explicit enablement path.
- [ ] 1.2 Define canonical bookmark route encoding, route parsing, object-page fragment contracts, and semantic navigation-event handling.
- [ ] 1.3 Add route tests for encoded logical types and identifiers, direct links, refresh, malformed routes, not-found objects, and access-denied outcomes.

## 2. HTMX Shell and Navigation

- [ ] 2.1 Implement the HTMX application shell, object-page region, loading indicators, fragment transitions, and browser-history integration.
- [ ] 2.2 Implement translation of semantic object navigation events into canonical HTMX route requests.
- [ ] 2.3 Implement back, forward, direct-load, refresh, and superseded-page behavior without leaking responses from obsolete object contexts.
- [ ] 2.4 Verify that HTMX handles only shell and fragment lifecycle while semantic components continue to own GraphQL operations.

## 3. Page Definition Resolution

- [ ] 3.1 Implement the page-definition registry and deterministic exact-logical-type resolution contract.
- [ ] 3.2 Implement application registration for custom templates or page factories beneath the route-level object context.
- [ ] 3.3 Implement the generic page fallback when no custom registration exists.
- [ ] 3.4 Test custom selection, generic fallback, requirement release when definitions change, and shared route-context behavior.

## 4. Generic Schema-Driven Composition

- [ ] 4.1 Implement enumeration and classification of properties, actions, and collections from the semantic type description produced by GraphQL introspection.
- [ ] 4.2 Compose the standard object header, action, property, and collection components using semantic member IDs.
- [ ] 4.3 Implement deterministic conventional member ordering and page regions for objects without a usable layout resource.
- [ ] 4.4 Verify that dynamic hidden, disabled, loading, and member-error behavior remains delegated to standard components.
- [ ] 4.5 Add integration tests proving that generic composition does not call a member-list endpoint or Causeway metamodel service.

## 5. Causeway Layout Support

- [ ] 5.1 Define the initial supported Causeway grid subset and map its rows, columns, groups, properties, actions, and collections to viewer regions.
- [ ] 5.2 Implement secure grid-resource retrieval through the URL supplied by rich object metadata.
- [ ] 5.3 Implement layout parsing, recognized-member placement, unsupported-instruction diagnostics, and region-level fallback.
- [ ] 5.4 Test complete layouts, partial layouts, missing members, unsupported instructions, forbidden resources, unreachable resources, and malformed resources.

## 6. Interaction Results and Page Lifecycle

- [ ] 6.1 Implement replaceable default policies for semantic object, collection, scalar, and void action results.
- [ ] 6.2 Implement page-level schema-loading, object-loading, ready, not-found, access-denied, terminal-error, and partial-error presentations.
- [ ] 6.3 Ensure object results navigate only according to the configured viewer policy and custom result handlers can claim outcomes.
- [ ] 6.4 Test action-result routing, non-object result regions, void refresh, partial member errors, and terminal route errors.

## 7. Theme and Accessibility

- [ ] 7.1 Implement the responsive default viewer theme using the documented light-DOM semantic component and page-region contracts.
- [ ] 7.2 Verify landmarks, headings, focus movement after HTMX navigation, loading announcements, route errors, keyboard operation, and visible focus.
- [ ] 7.3 Add an application theme and custom logical-type page example that reuse the standard semantic components.

## 8. Documentation and Verification

- [ ] 8.1 Document viewer enablement, canonical routes, HTMX responsibilities, generic fallback, grid support, custom-page registration, result policies, and theming.
- [ ] 8.2 Document the initial object-page scope and explicit non-parity with application menus, home pages, authentication pages, standalone values, and existing viewer extensions.
- [ ] 8.3 Add end-to-end tests covering direct generic pages, grid-driven pages, custom pages, property edits, action invocation, collection navigation, history, and refresh.
- [ ] 8.4 Run browser tests, relevant Maven and GraphQL viewer tests, accessibility checks, formatting checks, and strict OpenSpec validation, and resolve all failures.
