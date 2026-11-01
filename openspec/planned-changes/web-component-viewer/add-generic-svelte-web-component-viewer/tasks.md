## 1. Package and SvelteKit Route Contract

- [ ] 1.1 Verify all accepted P0 and P1 GraphQL, application-entry, composite-object, and menu-bar prerequisites are complete.
- [ ] 1.2 Establish the optional Svelte package, SvelteKit integration contract, custom-element registration, default theme, and acceptance application.
- [ ] 1.3 Implement canonical route and layout helpers, configurable base path, bookmark parsing, and route identity.
- [ ] 1.4 Maintain cross-viewer route fixtures shared with HTMX and Vue implementations.

## 2. Custom and Generic Route Pages

- [ ] 2.1 Implement exact-logical-type registration for Svelte components and lazy component loaders.
- [ ] 2.2 Implement application-authored custom and generic route templates that declare one `<cw-object-context>`, with `<cw-object>` in the generic fallback.
- [ ] 2.3 Bind canonical route identity into declared contexts and fail closed diagnostically when a page omits or duplicates its route context.
- [ ] 2.4 Dispose disconnected requirements and superseded component loaders deterministically across SvelteKit navigation.
- [ ] 2.5 Verify `<cw-object>` never reads SvelteKit route or page-registry state.

## 3. Shell, Upgrade, Events, and Policy

- [ ] 3.1 Implement the application-authored stable SvelteKit layout declaring one `<cw-graphql-client>`, `<cw-menubars>` placement, page slot, endpoint binding, and route lifecycle states.
- [ ] 3.2 Define client custom-element registration, inert server output if supported, hydration behavior, and readiness.
- [ ] 3.3 Bridge semantic custom-element navigation and result events into replaceable SvelteKit navigation and result policy.
- [ ] 3.4 Implement configurable home-page object and service-action policy without duplicating domain state in Svelte stores.

## 4. Verification and Documentation

- [ ] 4.1 Test deep links, refresh, history, custom pages, lazy-loader races, generic fallback, menus, interactions, results, partial errors, absent objects, and access denial.
- [ ] 4.2 Test declarative client/context ownership, missing and duplicate context diagnostics, custom-element attributes, properties, slots, native events, upgrade ordering, route reuse, disconnect, and selected SSR behavior.
- [ ] 4.3 Verify responsive layout, keyboard navigation, focus after routing, announcements, light/dark themes, and accessibility.
- [ ] 4.4 Document installation, SvelteKit mounting, route grammar, custom pages, fallback, policy, client upgrade, SSR scope, theming, and framework-neutral boundaries.
- [ ] 4.5 Run Svelte, browser, Maven, GraphQL, accessibility, formatting, and strict OpenSpec validation checks.
