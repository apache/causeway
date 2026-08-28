## Why

`<cw-collection>` currently owns a custom table and responsive-list presentation even when Vaadin is the selected toolkit.
Vaadin Grid could provide a more consistent data-oriented surface with established column layout, keyboard navigation, focus, sorting affordances, and lazy data-provider integration, but its data and lifecycle model must be reconciled explicitly with Causeway collection windows and semantic renderers.

## What Changes

- Add a qualified internal `vaadin-grid` adapter behind `<cw-collection>` without exposing Grid as the application-facing component contract.
- Map declarative `<cw-collection-column>` definitions, effective-grid collection metadata, labels, order, visibility, and semantic value descriptors into reviewed internal Grid columns.
- Render cells through Causeway's established semantic value and object-link renderers so nulls, references, resources, hidden values, disabled reasons, row-relative errors, navigation, and custom renderer precedence remain correct.
- Map Grid range requests only onto the existing bounded GraphQL collection-window contract, preserving generation, cancellation, ordering, authorization, request deduplication, nullable totals, concurrent-change behavior, and stale-result protection.
- Do not claim persistence-level pagination or query pushdown; the separate collection-query-pushdown analysis remains responsible for server materialization concerns.
- Use a Vaadin lazy data provider only where stable ordering and total semantics satisfy a documented qualification gate.
- Where that gate is not satisfied, render the established bounded window in Grid with explicit paging, or fall back to the native collection presentation rather than inventing a total or silently changing traversal semantics.
- Preserve collection activation, empty, loading, partial-error, terminal-error, paging, refresh, associated-action, and route-disconnection behavior.
- Preserve the responsive no-page-overflow contract; qualify a narrow Grid presentation or retain the native card/list fallback below the documented threshold.
- Retain the native collection renderer as explicit toolkit rollback and as automatic fallback for unsupported columns, adapter-load failure, policy failure, accessibility failure, or unqualified window semantics.
- Package Grid and only its reviewed free-core transitive closure independently with pinned inputs, deterministic bytes, checksums, licenses, vulnerability review, exact CSP hashes, compressed budgets, accessibility, keyboard, responsive, theme, and lifecycle evidence.

## Capabilities

### New Capabilities

- `vaadin-collection-grid-adapter`: Defines Grid eligibility, column and cell projection, bounded-window integration, data-provider semantics, responsiveness, accessibility, delivery, and native fallback.

### Modified Capabilities

- `domain-web-components`: Allow qualified collections to use an internal Vaadin Grid while preserving `<cw-collection>`, collection-column declarations, semantic events, renderers, and lifecycle contracts.
- `generic-htmx-web-component-viewer`: Add independently route-lazy Grid delivery, CSP, toolkit rollback, configuration, and documentation.
- `reference-application-viewer-regression-suite`: Add representative scalar, reference, polymorphic, nullable, hidden, disabled, erroneous, paged, refreshed, and narrow collection journeys in Vaadin and native modes.

## Impact

The change affects collection rendering, row-context attachment, column projection, window scheduling, packaged browser assets, CSP, themes, samples, and browser evidence.
Grid can improve visual consistency and keyboard behavior, but it adds a larger closure and a virtualized lifecycle that must not outlive Causeway route or context generations.
Public GraphQL operations, collection-window semantics, `<cw-collection>` markup, canonical routes, semantic navigation, and application-authored column declarations remain stable.
It introduces no Vaadin Flow, Binder, server-side Vaadin state, Pro Grid feature, commercial component, CDN asset, telemetry, persistence query pushdown, or raw application-facing Grid API.
