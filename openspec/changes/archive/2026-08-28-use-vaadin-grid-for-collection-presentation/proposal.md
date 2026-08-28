## Why

`<cw-collection>` still owns a custom table and responsive-list presentation while the default component toolkit now uses reviewed Vaadin controls for qualified fields and actions.
Vaadin Grid can provide a coherent data-oriented surface and stronger keyboard behavior, but only if virtualization, totals, ordering, semantic cells, and asynchronous lifecycle remain subordinate to the existing bounded Causeway collection contract.

## What Changes

- Add a qualified internal `vaadin-grid` adapter behind `<cw-collection>` without exposing Grid, its data provider, renderer callbacks, events, item model, or Shadow DOM as application-facing APIs.
- Map declarative `<cw-collection-column>` definitions, effective-grid metadata, labels, order, visibility, test identity, and semantic row descriptors into internal Grid columns.
- Keep Causeway's object-link and value-renderer registries authoritative for every cell, including nulls, references, resources, disabled reasons, row-relative errors, unsupported values, and application renderer precedence.
- Use virtualized range loading only when the server exposes the bounded `window` operation, ordering is deterministic across requests, and a stable total count is safely available.
- When deterministic ordering exists but total count is unavailable, render only the current bounded window in Grid and retain Causeway-owned explicit previous/next paging rather than inventing a size for Grid's lazy data provider.
- Fall back to the established native collection presentation when ordering is encounter-only or unstable, window capability is absent, columns or renderers are unsupported, or the adapter cannot preserve the collection contract.
- Retain the established native card/list collection presentation at narrow viewports; qualify Grid only at the existing wide collection breakpoint so responsive behavior never introduces horizontal page overflow.
- Preserve collection activation, empty, loading, disabled, partial-error, terminal-error, paging, refresh, associated-action, navigation, cancellation, request-deduplication, concurrent-change, and route-disconnection behavior.
- Extend the common `component-toolkit=vaadin|native` policy to qualified Grid presentation while preserving complete explicit-native rollback and deprecated compatibility-policy boundaries.
- Package Grid and only its reviewed free-core transitive closure independently with pinned inputs, deterministic bytes, checksums, legal metadata, vulnerability review, exact CSP hashes, telemetry exclusion, compressed budgets, accessibility, keyboard, responsive, theme, and lifecycle evidence.
- Keep persistence query pushdown outside this change; Grid range requests map only to the existing GraphQL collection-window operation, whose current implementation may still materialize the complete domain collection.

## Capabilities

### New Capabilities

- `vaadin-collection-grid-adapter`: Defines Grid eligibility, column and semantic-cell projection, bounded-window integration, virtualized and explicit-paging modes, responsiveness, accessibility, delivery, lifecycle safety, and native fallback.

### Modified Capabilities

- `domain-web-components`: Allow qualified collections to use an internal Vaadin Grid while preserving `<cw-collection>`, `<cw-collection-column>`, associated actions, semantic events, renderers, navigation, and lifecycle contracts.
- `generic-htmx-web-component-viewer`: Add independently route-lazy Grid delivery, exact-hash CSP, common-toolkit rollback, diagnostics, configuration, packaging, and documentation.
- `reference-application-viewer-regression-suite`: Add representative scalar, reference, polymorphic, nullable, hidden, disabled, erroneous, paged, refreshed, disconnected, and narrow collection journeys in default and native modes.

## Impact

The change affects collection rendering, row-context attachment, column projection, range scheduling, paging controls, component policy, packaged browser assets, CSP, themes, vanilla and Petclinic samples, and Reference Application browser evidence.
Public GraphQL operations, collection-window semantics, canonical routes, application-authored `cw-*` markup, semantic navigation, and Causeway renderer contracts remain stable.
The Grid closure is independently optional and may be rejected if its payload, accessibility, semantic, lifecycle, or fallback cost exceeds the accepted qualification budget.
The change introduces no Vaadin Flow, Binder, server-side Vaadin state, Grid Pro or commercial component, CDN asset, telemetry, persistence query pushdown, application-facing data provider, or raw Vaadin API.
