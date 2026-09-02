## 1. Package and Router Contract

- [ ] 1.1 Verify all accepted P0 and P1 GraphQL, application-entry, composite-object, and menu-bar prerequisites are complete.
- [ ] 1.2 Establish the optional Vue package, peer dependencies, plugin contract, custom-element registration, default theme, and acceptance application.
- [ ] 1.3 Implement canonical route records, route parsing, configurable base path, and bookmark resolution using Vue Router.
- [ ] 1.4 Maintain cross-viewer route fixtures shared with HTMX and Svelte implementations.

## 2. Custom and Generic Route Pages

- [ ] 2.1 Implement exact-logical-type registration for Vue components, async components, and accepted page factories.
- [ ] 2.2 Implement application-authored custom and generic route templates that declare one `<cw-object-context>`, with `<cw-object>` in the generic fallback.
- [ ] 2.3 Bind canonical route identity into declared contexts and fail closed diagnostically when a page omits or duplicates its route context.
- [ ] 2.4 Key page lifecycle by canonical bookmark identity and dispose disconnected requirements deterministically.
- [ ] 2.5 Verify `<cw-object>` never reads Vue router or page-registry state.

## 3. Shell, Events, and Policy

- [ ] 3.1 Implement the application-authored stable Vue shell declaring one `<cw-graphql-client>`, `<cw-menubars>` placement, `RouterView` page region, endpoint binding, and route lifecycle states.
- [ ] 3.2 Bridge semantic custom-element navigation and result events into replaceable Vue router and result policy.
- [ ] 3.3 Implement configurable home-page object and service-action policy.
- [ ] 3.4 Keep domain state in GraphQL object contexts rather than a duplicate Vue store.

## 4. Verification and Documentation

- [ ] 4.1 Test deep links, refresh, history, custom pages, async page races, generic fallback, menus, interactions, results, partial errors, absent objects, and access denial.
- [ ] 4.2 Test declarative client/context ownership, missing and duplicate context diagnostics, custom-element attributes, properties, slots, native events, upgrade ordering, disconnect, and route reuse.
- [ ] 4.3 Verify responsive layout, keyboard navigation, focus after routing, announcements, light/dark themes, and accessibility.
- [ ] 4.4 Document installation, router integration, route grammar, custom pages, fallback, policy, theming, lifecycle, SSR exclusion, and framework-neutral boundaries.
- [ ] 4.5 Run Vue, browser, Maven, GraphQL, accessibility, formatting, and strict OpenSpec validation checks.
