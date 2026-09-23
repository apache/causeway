## Why

An entity collection can spend most of its request time loading and preparing visible table rows and then rendering their property and action cells, but the current trace shows only a page-wide preparation span and an opaque `render collection <collectionId>` span.
A representative trace showed a 2.071-second collection render with no SQL overlap, while a one-plus-sixteen role query pattern occurred earlier beneath the 2.501-second page preparation span.
Initial row instrumentation then showed that five row spans accounted for only 20.370 ms of a 2.073-second collection render, leaving 1.858 seconds before the first row and 193.513 ms after the final row unexplained.
The same trace showed that `prepare collection roles` began after the role SQL had completed, proving that collection construction and loading happen before the existing `onBeforeRender()` preparation boundary.
The instrumentation therefore needs separate collection initialization and bounded table-phase rendering detail to distinguish construction-time loading, table chrome, row rendering, and footer work.

## What Changes

- Add one `causeway.wicket.collection.initialize` observation around each parented entity collection's synchronous UI construction and collection-model setup using contextual name `initialize collection <collectionId>`, so construction-time loading and JDBC work have a domain-facing owner.
- Add one `causeway.wicket.collection.prepare` observation around each parented entity collection's later Wicket preparation lifecycle, including data-provider sizing, iteration, and visible-row population, using contextual name `prepare collection <collectionId>`.
- Add one bounded `causeway.wicket.collection.row.prepare` observation around each visible row population callback using contextual name `prepare row <logical-type-name>`.
- Make collection preparation a child of page preparation, make row preparation a child of collection preparation, and keep both preparation categories separate from actual markup rendering.
- Add one bounded render observation for each collection table row actually rendered on the current page.
- Name each row render observation `render row <logical-type-name>` using the collection element's static logical type, without including the row index, bookmark, title, primary key, or any other instance identifier.
- Preserve `causeway.wicket.collection.row.render` as the stable row render observation name and attach the complete element logical type and parent collection identifier as canonical attributes to both row phases.
- Add a `causeway.wicket.collection.table.render` observation around each parented Ajax table using contextual name `render table <collectionId>`.
- Add bounded `causeway.wicket.collection.table.header.render`, `causeway.wicket.collection.table.body.render`, and `causeway.wicket.collection.table.footer.render` children using contextual names `render table header <collectionId>`, `render table body <collectionId>`, and `render table footer <collectionId>`.
- Make each row render observation a child of the table-body observation, beneath its enclosing table and collection render observations.
- Include regular logical property cells beneath their row using the existing `causeway.wicket.property.render` observation and `render property <propertyId>` contextual naming.
- Include applicable row-action rendering beneath its row using the existing `causeway.wicket.action.render` observation and `render action <actionId>` contextual naming.
- Instrument table header and footer only as aggregate phases, and continue to exclude individual headers, navigation controls, selection checkboxes, and presentation-only cells.
- Instrument only initialization, preparation, and rendering for parented entity collections and rows participating in the current full-page or Ajax response; do not create these spans for standalone action-result tables or off-page data.
- Retain complete canonical `causeway.collection.id`, `causeway.object.type`, `causeway.property.id`, and `causeway.action.id` attributes while applying the existing case-preserving 50-character contextual-name policy.
- Continue to exclude values, titles, bookmarks, object identifiers, row positions, arguments, users, tenancy data, and generated Wicket component paths.
- Extend focused Wicket lifecycle and Java-agent compatibility validation for row nesting, repeated rows, Ajax rendering, failures, serialization, and no-op behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `wicket-region-render-observation`: Add collection initialization and visible-row preparation detail, and decompose collection-table rendering into bounded table phases, rows, property cells, and row actions.

## Impact

The change affects parented entity-collection construction and preparation, Wicket Ajax data-table phase and row boundaries, scalar-property and action observation policies for table contexts, observation descriptors, lifecycle tests, Java-agent trace validation, and tracing operations documentation.
It intentionally adds one initialization span and up to four aggregate table-render spans per participating parented collection, plus observations proportional to visible rows and instrumented logical cells, while keeping names and attributes bounded and instance-data-free.
It does not change domain behavior, table output, pagination, sampling, SDK ownership, exporters, dependencies, or Java-agent-owned HTTP and JDBC instrumentation.
