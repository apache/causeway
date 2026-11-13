## 1. Public Contract and Layout Plan

- [x] 1.1 Translate matrix entries `REF-LAYOUT-01`, `REF-COMPONENT-01`, `REF-METADATA-02`, and `REF-COLLECTION-02` into effective-grid, fallback, and collection fixtures.
- [x] 1.2 Verify the archived object-interaction, resource-link, value-semantics, and collection-windowing prerequisites are complete.
- [x] 1.3 Define the context-only `<causeway-object>` contract, `editable` and `layout-mode` attributes, lifecycle states, events, bounded diagnostics, `refreshLayout()`, and customization hooks.
- [x] 1.4 Define the immutable layout-plan model and member-allocation rules.
- [x] 1.5 Define the supported Causeway grid subset and canonical fallback plan derived from `GridFallbackLayout.xml`.

## 2. Secure Grid Retrieval and Parsing

- [x] 2.1 Request effective grid metadata and fetch the opaque authorized resource through context-owned same-origin no-store retrieval.
- [x] 2.2 Parse bounded XML while rejecting document types, entities, executable content, unsafe processing instructions, malformed markup, and unknown entity references.
- [x] 2.3 Map rows, columns, spans, tabs, field sets, domain-object placement, member references, and unreferenced markers into the layout plan.
- [x] 2.4 Diagnose missing, forbidden, unreachable, malformed, stale, duplicate, wrong-kind, and unsupported layout content with local fallback.

## 3. Semantic Decomposition

- [x] 3.1 Discover member IDs through the context's targeted schema description without a member-list endpoint.
- [x] 3.2 Generate `<causeway-object-header>`, `<causeway-property>`, `<causeway-action>`, and `<causeway-collection>` children in light DOM.
- [x] 3.3 Allocate explicit and unreferenced members deterministically and at most once.
- [x] 3.4 Preserve shared context requirements, interaction state, child-local errors, and authoritative refresh behavior.
- [x] 3.5 Collapse empty regions without disturbing active interactions or unrelated successful regions.

## 4. Responsive Layout and Accessibility

- [x] 4.1 Implement twelve-column responsive CSS Grid behavior without Bootstrap dependency.
- [x] 4.2 Implement labelled sections, field sets, tab lists, tabs, panels, arrow-key movement, focus behavior, and status announcements.
- [x] 4.3 Add stable light-DOM styling hooks, custom properties, region attributes, layout state events, and redacted bounded diagnostic events.

## 5. Acceptance and Documentation

- [x] 5.1 Add deterministic fixtures for canonical fallback, complete custom grid, partial grid, explicit references, unreferenced placement, unknown nodes, and malformed resources.
- [x] 5.2 Add vanilla-HTML sample coverage that renders a complete object using only context plus `<causeway-object>`.
- [x] 5.3 Execute real-browser acceptance checks for pointer, keyboard, responsive, light/dark, editing, action prompts, collections, partial errors, and layout fallback.
- [x] 5.4 Document the public contract, supported grid subset, fallback mapping, customization, diagnostics, security, and explicit-composition alternative.
- [x] 5.5 Run Node, Maven, browser, accessibility, formatting, and strict OpenSpec validation checks.
