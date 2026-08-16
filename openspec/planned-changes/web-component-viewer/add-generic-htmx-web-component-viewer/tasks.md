## 1. Viewer Module and Route Contracts

- [ ] 1.1 Establish the optional viewer module, browser asset packaging, HTMX dependency, default theme, demonstration application, and explicit enablement path.
- [ ] 1.2 Define canonical bookmark route encoding, route parsing, object-page fragment contracts, and semantic navigation-event handling.
- [ ] 1.3 Add route tests for encoded logical types and identifiers, direct links, refresh, malformed routes, not-found objects, and access-denied outcomes.

## 2. HTMX Shell and Application Entry

- [ ] 2.1 Implement the stable HTMX application shell, `<causeway-menubars>` placement, object-page region, loading indicators, and fragment transitions.
- [ ] 2.2 Connect menu service-action results and discovered home-page action to replaceable viewer navigation and result policy.
- [ ] 2.3 Verify application-entry state survives object-fragment replacement and invalidates only under its documented context.
- [ ] 2.4 Verify HTMX handles only shell and fragment lifecycle while menu components own menu layout and service-action semantics.

## 3. Navigation and History

- [ ] 3.1 Translate semantic object navigation events from object links, object actions, and service actions into canonical HTMX route requests.
- [ ] 3.2 Implement back, forward, direct-load, refresh, and superseded-page behavior without leaking obsolete context responses.
- [ ] 3.3 Define initial shell and home-page policy for applications with present, absent, hidden, invalid, or object-returning home actions.

## 4. Page Definition Resolution

- [ ] 4.1 Implement the page-definition registry and deterministic exact-logical-type resolution contract.
- [ ] 4.2 Implement application registration for custom templates or factories beneath the route-level object context.
- [ ] 4.3 Implement the default definition using `<causeway-object>` rather than viewer-owned member enumeration or grid parsing.
- [ ] 4.4 Test custom selection, default fallback, requirement release, shared route context, and explicit low-level custom composition.

## 5. Interaction Results and Page Lifecycle

- [ ] 5.1 Implement replaceable default policies for semantic object, collection, scalar, and void results from page and shell actions.
- [ ] 5.2 Implement page-level schema-loading, object-loading, ready, not-found, access-denied, terminal-error, and partial-error presentations.
- [ ] 5.3 Ensure object results navigate only according to configured viewer policy and custom handlers can claim outcomes.
- [ ] 5.4 Test result routing, non-object result regions, void refresh, partial member errors, terminal route errors, and menu-originated results.

## 6. Theme and Accessibility

- [ ] 6.1 Implement the responsive default shell and object-page theme using documented menu-bar, object, and member light-DOM hooks.
- [ ] 6.2 Verify landmarks, headings, focus movement after HTMX navigation, loading announcements, route errors, menus, keyboard operation, and visible focus.
- [ ] 6.3 Add an application theme and custom logical-type page example that reuse high-level and low-level semantic components.

## 7. Documentation and Verification

- [ ] 7.1 Document viewer enablement, canonical routes, HTMX responsibilities, menu shell, home policy, default `<causeway-object>`, custom-page registration, result policies, and theming.
- [ ] 7.2 Document explicit non-parity with authentication pages, standalone values, and existing viewer extensions.
- [ ] 7.3 Add end-to-end tests covering shell menus, direct object pages, grid-driven object composition, custom pages, interactions, collection navigation, history, and refresh.
- [ ] 7.4 Run browser tests, relevant Maven and GraphQL tests, accessibility checks, formatting checks, and strict OpenSpec validation.
