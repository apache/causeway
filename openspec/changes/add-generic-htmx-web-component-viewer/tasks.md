## 1. Evidence, Module, and Route Foundation

- [x] 1.1 Verify matrix entries `REF-VIEWER-01`, `REF-COMPONENT-01`, `REF-COMPONENT-02`, `REF-HOME-01`, and `REF-MENU-01`, accepted P0/P1 prerequisites, current Wicket presentation hooks, and pinned Petclinic source `16a10608129ca9ce8ae04d21df1462f4d69ac018`.
- [x] 1.2 Establish optional `htmx` and `sample-htmx-petclinic` Maven modules, explicit module enablement, HTMX `2.0.6` WebJar packaging, browser assets, run profile, and integration-test wiring.
- [x] 1.3 Implement configurable `/htmx` base path and canonical object-route encoding and strict round-trip parsing with bounded invalid-route errors.
- [x] 1.4 Add codec and route tests for Unicode logical types, opaque identifiers, deployment paths, direct links, refresh, malformed escapes, separators, controls, dot segments, overlong values, absent objects, and access-denied outcomes.

## 2. Server Fragments and Stable Shell

- [x] 2.1 Implement complete-document and `HX-Request` fragment responses with canonical history instructions and no duplicate shell.
- [x] 2.2 Implement the exact-logical-type `HtmxPageFragmentFactory` registry with duplicate-registration failure and no metamodel or persistence exposure.
- [x] 2.3 Implement generic fallback using `<causeway-object>` beneath exactly one route-level object context.
- [x] 2.4 Implement the stable shell with one GraphQL client, `<causeway-menubars>`, branding, main route region, loading indicator, announcement region, result region, and CSP-compatible external assets.
- [x] 2.5 Test custom selection, generic fallback, safe escaping, requirement release, shared context, explicit low-level composition, full-page requests, HTMX requests, and duplicate registrations.

## 3. HTMX Navigation, History, Home, and Results

- [x] 3.1 Implement normal canonical anchors and an HTMX browser bridge using documented target, swap, push, replace, and lifecycle APIs.
- [x] 3.2 Translate object-link, object-action, service-action, and explicit navigation events into canonical route requests under replaceable policy.
- [x] 3.3 Implement direct-load, refresh, back, forward, history-cache-miss, loading, response-error, superseded-page, and focus behavior.
- [x] 3.4 Extend targeted application-entry discovery for the established optional object-home identity and implement bounded landing behavior for absent, hidden, invalid, unsupported, and partial home states.
- [x] 3.5 Implement replaceable scalar, object, collection, and void result handlers with object routing, accessible result presentation, and bounded current-context refresh.
- [x] 3.6 Verify HTMX owns only shell and fragment lifecycle while semantic components own GraphQL, layouts, domain interactions, menus, validation, mutations, and typed results.

## 4. Petclinic Copy and Current-Causeway Port

- [x] 4.1 Record Apache Petclinic provenance, pinned commit, copied source inventory, license, intentional omissions, and current-API porting notes.
- [x] 4.2 Port Pet Owner, Pet, Visit, repositories, services, contributions, home page, fixtures, and deterministic sample data to current Jakarta/JPA and Causeway APIs.
- [x] 4.3 Port effective menu bars, object grids, collection columns, labels, icons, choices, defaults, validation, object actions, and service actions needed for cohesive viewer coverage.
- [x] 4.4 Configure GraphQL, structural resources, H2 persistence, security bypass, the HTMX module, and the current Wicket viewer over the same application state.
- [x] 4.5 Add one exact-logical-type custom Petclinic fragment that demonstrates router precedence and semantic low-level composition while all other types exercise generic fallback.
- [x] 4.6 Add integration tests for deterministic fixture identities, application entry, object home, effective menu/grid resources, Petclinic service and object interactions, canonical HTMX routes, and Wicket comparison availability.

## 5. Cohesive Wicket-Inspired Theme

- [x] 5.1 Define shared design tokens and optional theme assets for navigation, content width, typography, surfaces, borders, links, focus, status colors, spacing, radii, shadows, labels, tables, prompts, and responsive behavior.
- [x] 5.2 Style the HTMX shell and documented menubar, object header, action, grid, card, tab, property, collection, table, editor, prompt, result, loading, disabled, diagnostic, and error hooks without Bootstrap coupling.
- [x] 5.3 Implement accessible narrow navigation, stacked object regions, contained table scrolling, touch targets, visible focus, reduced motion, forced colors, and light/dark palettes without horizontal page overflow.
- [x] 5.4 Compare live Wicket and HTMX Petclinic home and object pages and correct hierarchy, spacing, alignment, action placement, labels, cards, tabs, collections, prompts, and responsive discrepancies.

## 6. Vanilla Sample Styling Repair

- [x] 6.1 Replace the ad hoc `sample-html` page styling with the cohesive tokens, contained shell, consistent hierarchy, cards, spacing, grids, and responsive rules while retaining vanilla HTML and ESM.
- [x] 6.2 Correct component-level spacing, alignment, disclosure, overflow, form, table, prompt, result, focus, light/dark, and narrow-layout defects exposed by the vanilla fixture.
- [x] 6.3 Preserve `/sample-html/index.html`, `/graphql`, bookmark `s_sample-1`, test selectors, readiness semantics, custom-element ownership, and all existing browser interactions.
- [x] 6.4 Extend automated and browser regression coverage for visual structure, control overlap, target sizes, table containment, modal bounds, responsive reflow, focus visibility, contrast, and zero console errors.

## 7. Documentation and Final Verification

- [x] 7.1 Document enablement, HTMX version, canonical routes, base-path deployment, router customization, generic fallback, custom factories, object-home policy, result policy, theming, CSP, history, accessibility, and non-goals.
- [x] 7.2 Document Petclinic provenance, launch workflow, stable fixture bookmarks, HTMX routes, Wicket comparison route, and intentional differences from the pinned source.
- [x] 7.3 Maintain cross-viewer canonical-route and fallback fixtures shared with planned Vue and Svelte viewers.
- [x] 7.4 Run Node, Maven, GraphQL, Petclinic integration, vanilla integration, real-browser pointer and keyboard, direct-link and history, responsive light/dark, Wicket-relative screenshot, console, accessibility, Lighthouse, AsciiDoc, syntax, source-approval, formatting, and strict OpenSpec validation checks.

## 8. Playwright Interaction Hardening and Reactor Integration

- [ ] 8.1 Add `viewers/webcomponents` to the established top-level viewer aggregation and verify ordinary reactor discovery without changing application opt-in behavior.
- [ ] 8.2 Add an opt-in Playwright-for-Java Petclinic profile, configurable Chromium launch, browser installation documentation, stable UI hooks, and Maven wiring that leaves ordinary builds browser-download independent.
- [ ] 8.3 Add Playwright journeys for home, custom and generic routes, menus, direct links, back and forward history, object links, property edits, collection hydration, responsive behavior, console failures, failed resources, and GraphQL response failures.
- [ ] 8.4 Exercise every exposed Petclinic service action and object action through the UI, including defaults, choices, valid invocation, invalid input, cancellation, scalar, object, collection, void, mutation, refresh, and disposable destructive coverage.
- [ ] 8.5 Reproduce and fix prompt, validation, cancellation, refresh, result, and route focus regressions so each transition has deterministic documented focus ownership.
- [ ] 8.6 Run Playwright, Node, Maven module, top-level reactor-discovery, GraphQL, syntax, AsciiDoc, formatting, source-approval, and strict OpenSpec validation checks.
