## Why

Application-authored object pages currently have to enumerate every authorized property, collection, and action or silently omit members that were added after the page was authored.
Causeway `layout.xml` already solves this through deterministic unreferenced-member allocation, so framework-neutral web components should expose equivalent catch-all composition without transferring member authority to HTMX or Vue.

## What Changes

- Register `<cw-unreferenced-properties>`, `<cw-unreferenced-collections>`, and `<cw-unreferenced-actions>` as framework-neutral context consumers.
- Allocate authorized members by semantic kind and ID against all ordinary member components bound to the same nearest object context, including members in hidden panels and composite components such as `<cw-metadata>`.
- Isolate nested object contexts, inert preview declarations, and generated catch-all descendants from the enclosing context's explicit claims.
- Coordinate allocation once per object-context boundary so each remaining member is rendered exactly once in authoritative member order and asynchronous claim producers cannot cause stable duplication.
- Permit exactly one effective catch-all destination per member kind; the first valid destination in document order wins and later duplicates fail closed with bounded diagnostics.
- Render remaining properties as ordinary `<cw-property>` children of an **Other** fieldset, remaining collections as ordinary `<cw-collection>` children in one tab per collection, and remaining actions as ordinary `<cw-action>` controls in a wrapping horizontal group.
- Add explicit property editing opt-in, accessible group names, inspectable loading/ready/empty/error state, parser-late synchronization, supersession, and disconnection cleanup.
- Extend strict column composition to admit the three new catch-all elements while continuing to reject arbitrary children.
- Verify equivalent direct use from HTMX and Vue without host-owned allocation, data fetching, ordering, or interaction state.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Add authoritative context-scoped unreferenced property, collection, and action allocation and presentation components.
- `generic-htmx-web-component-viewer`: Permit application templates to use the catch-all components directly while keeping allocation and generated member behavior in foundation.
- `generic-vue-web-component-viewer`: Provide equivalent custom-element use without Vue wrappers or reactive member reconstruction.

## Impact

The change affects foundation component contracts, registration, context-consumer ownership metadata, object-context allocation coordination, declarative layout grammar, styles, documentation, and lifecycle tests.
HTMX and Vue require only documentation and browser integration coverage; their routing, shells, GraphQL clients, interaction controllers, and result ownership remain unchanged.
No GraphQL schema, domain metadata, authorization rule, `layout.xml` format, third-party dependency, or compatibility alias changes.
