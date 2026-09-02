## 1. Foundation peek declaration and context

- [ ] 1.1 Add the public `<cw-peek>` contract, registration, exports, semantic events, host classes, documentation, and inert direct-declaration styling.
- [ ] 1.2 Capture zero or one direct peek declaration as a reusable template, implement complete inline precedence, and fail closed diagnostically for duplicates.
- [ ] 1.3 Resolve empty declarations by current runtime row type through an optional host resolver, cache bounded availability per projection generation, and hide disclosure when no safe template exists.
- [ ] 1.4 Create one dedicated hydrated row context before connecting a live peek clone and retire its requirements, nested components, and asynchronous work deterministically on collapse.
- [ ] 1.5 Implement single-row disclosure state, ordinary Tab entry, Escape collapse, disclosure focus restoration, stable collection fallback focus, and accessible labels and relationships.
- [ ] 1.6 Collapse and perform one criteria-preserving forced parent reload after successful action-result or property-updated events originating inside the live peek.

## 2. Native collection row presentation

- [ ] 2.1 Extend native object-link lists with eligible row disclosures and one inline live details subtree without changing canonical links.
- [ ] 2.2 Extend native collection tables with a compact leading disclosure column and one correctly spanned details row immediately after the selected data row.
- [ ] 2.3 Collapse and retire native details across sorting, filtering, paging, reload, parent generation change, responsive renderer replacement, supersession, and disconnection.

## 3. Vaadin Grid row details

- [ ] 3.1 Extend the toolkit-neutral Grid projection and frozen presentation with bounded preview availability, stable row identity, disclosure rendering, and collection-owned details callbacks.
- [ ] 3.2 Map the neutral contract to a leading Vaadin disclosure column, `rowDetailsRenderer`, and exactly one opened item without moving domain or context ownership into the adapter.
- [ ] 3.3 Preserve preview availability, generations, Escape focus, virtual ranges, responsive replacement, sorting, paging, and Grid-scoped native rollback under row-details rendering.

## 4. HTMX default preview resources

- [ ] 4.1 Add bounded `previews/<logical-type-name>.html` classpath discovery, immutable definitions, duplicate detection, registry wiring, and module configuration.
- [ ] 4.2 Add the private logical-type preview endpoint with safe routing, UTF-8 content, private no-store behavior, distinguishing headers, and missing-resource responses.
- [ ] 4.3 Add the client preview resolver, cached and reload behavior, one-root inert parsing, safe cloning, markup and identity rejection, and bounded diagnostics.
- [ ] 4.4 Add Java and JavaScript coverage for resource limits, invalid names and UTF-8, duplicates, safe semantic component layout, missing defaults, executable markup, caching, reload, and redaction.

## 5. Petclinic demonstration and browser journeys

- [ ] 5.1 Add deterministic Petclinic inline and empty peek declarations plus plural runtime-type preview resources that exercise properties, actions, nested collections, and ordinary layout.
- [ ] 5.2 Add or adapt a deterministic row action and editable property so successful peek mutations visibly change and refresh the containing collection.
- [ ] 5.3 Add integration coverage for preview resource discovery, endpoint headers and bodies, missing-type behavior, page declarations, and reload policy.
- [ ] 5.4 Add Vaadin and native Playwright journeys for single expansion, inline precedence, default and missing previews, hydration, lazy requirements, Escape, focus, actions, property edits, parent refresh, sorting, filtering, paging, responsive replacement, navigation, overflow, console, and network policy.

## 6. Regression and specification validation

- [ ] 6.1 Add Foundation component, native renderer, row-context, mutation-refresh, Grid projection, adapter, accessibility, cleanup, and style tests.
- [ ] 6.2 Run Foundation Node and Maven suites, HTMX and Petclinic integration suites, full Vaadin and native browser qualification, JavaScript syntax checks, and the IDE build with Java 21 where required.
- [ ] 6.3 Run strict OpenSpec validation, inspect diagnostics for bounded redaction, and pass `git diff --check`.
