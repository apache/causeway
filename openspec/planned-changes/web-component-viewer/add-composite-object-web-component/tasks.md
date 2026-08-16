## 1. Public Contract and Layout Plan

- [ ] 1.1 Translate matrix entries `REF-LAYOUT-01`, `REF-COMPONENT-01`, `REF-METADATA-02`, and `REF-COLLECTION-02` into effective-grid, fallback, and collection fixtures.
- [ ] 1.2 Verify corrected object interaction and safe structural resource-link prerequisites are complete.
- [ ] 1.3 Define `<causeway-object>` attributes, context requirements, lifecycle states, events, diagnostics, and customization hooks.
- [ ] 1.4 Define the immutable layout-plan model and member-allocation rules.
- [ ] 1.5 Define the supported Causeway grid subset and canonical fallback plan derived from `GridFallbackLayout.xml`.

## 2. Secure Grid Retrieval and Parsing

- [ ] 2.1 Request effective grid metadata through the shared object context and fetch the authorized resource.
- [ ] 2.2 Parse XML with external entities and executable content disabled.
- [ ] 2.3 Map rows, columns, spans, tabs, field sets, domain-object placement, member references, and unreferenced markers into the layout plan.
- [ ] 2.4 Diagnose missing, forbidden, unreachable, malformed, stale, duplicate, wrong-kind, and unsupported layout content with local fallback.

## 3. Semantic Decomposition

- [ ] 3.1 Discover member IDs through the context's targeted schema description without a member-list endpoint.
- [ ] 3.2 Generate `<causeway-object-header>`, `<causeway-property>`, `<causeway-action>`, and `<causeway-collection>` children in light DOM.
- [ ] 3.3 Allocate explicit and unreferenced members deterministically and at most once.
- [ ] 3.4 Preserve shared context requirements, interaction state, child-local errors, and authoritative refresh behavior.
- [ ] 3.5 Collapse empty regions without disturbing active interactions or unrelated successful regions.

## 4. Responsive Layout and Accessibility

- [ ] 4.1 Implement twelve-column responsive CSS Grid behavior without Bootstrap dependency.
- [ ] 4.2 Implement labelled sections, field sets, tab lists, tabs, panels, arrow-key movement, focus behavior, and status announcements.
- [ ] 4.3 Add stable light-DOM styling hooks, custom properties, region attributes, and supported layout lifecycle events.

## 5. Acceptance and Documentation

- [ ] 5.1 Add deterministic fixtures for canonical fallback, complete custom grid, partial grid, explicit references, unreferenced placement, unknown nodes, and malformed resources.
- [ ] 5.2 Add vanilla-HTML sample coverage that renders a complete object using only context plus `<causeway-object>`.
- [ ] 5.3 Add real-browser tests for pointer, keyboard, responsive, light/dark, editing, action prompts, collections, partial errors, and layout fallback.
- [ ] 5.4 Document the public contract, supported grid subset, fallback mapping, customization, diagnostics, security, and explicit-composition alternative.
- [ ] 5.5 Run Node, Maven, browser, accessibility, formatting, and strict OpenSpec validation checks.
