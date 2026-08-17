## 1. Router and Viewer Module

- [ ] 1.1 Verify matrix entries `REF-VIEWER-01`, `REF-COMPONENT-01`, `REF-COMPONENT-02`, `REF-HOME-01`, and `REF-MENU-01` and all accepted P0 or P1 prerequisites are complete.
- [ ] 1.2 Establish the optional viewer module, browser asset packaging, HTMX dependency, default theme, demonstration application, and explicit enablement path.
- [ ] 1.3 Define canonical bookmark route encoding, parsing, base-path configuration, and semantic navigation-event handling.
- [ ] 1.4 Add route tests for logical types, identifiers, direct links, refresh, malformed routes, absent objects, and access-denied outcomes.

## 2. Custom and Generic Page Resolution

- [ ] 2.1 Implement the exact-logical-type custom fragment or factory registry at the route boundary.
- [ ] 2.2 Implement generic fallback using `<causeway-object>` beneath one route-level object context.
- [ ] 2.3 Verify `<causeway-object>` never performs custom-page discovery or routing.
- [ ] 2.4 Test custom selection, generic fallback, requirement release, shared context, and explicit low-level composition.

## 3. HTMX Shell and History

- [ ] 3.1 Implement the stable application shell, `<causeway-menubars>` placement, route-content region, loading indicators, and fragment transitions.
- [ ] 3.2 Translate object-link, object-action, service-action, and home navigation outcomes into canonical route requests under replaceable policy.
- [ ] 3.3 Implement back, forward, direct-load, refresh, and superseded-page behavior.
- [ ] 3.4 Verify HTMX owns only shell and fragment lifecycle while semantic components own layouts and domain interactions.

## 4. Home, Results, and Lifecycle

- [ ] 4.1 Define initial-shell policy for present, absent, hidden, invalid, object, and action home entries.
- [ ] 4.2 Implement replaceable scalar, object, collection, and void result handlers.
- [ ] 4.3 Implement accessible loading, ready, not-found, access-denied, partial-error, and terminal-error route states.
- [ ] 4.4 Test menu-originated results, object navigation, void refresh, non-object regions, partial errors, and terminal route failures.

## 5. Theme, Documentation, and Verification

- [ ] 5.1 Implement a responsive accessible default shell using documented component light-DOM hooks.
- [ ] 5.2 Verify landmarks, headings, focus after navigation, loading announcements, route errors, menus, keyboard operation, and visible focus.
- [ ] 5.3 Document enablement, canonical routes, router customization, generic fallback, custom fragments, home and result policy, theming, and non-goals.
- [ ] 5.4 Maintain cross-viewer canonical-route and fallback fixtures shared with generic Vue and Svelte viewers.
- [ ] 5.5 Run browser, Maven, GraphQL, accessibility, formatting, and strict OpenSpec validation checks.
