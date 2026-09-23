## Why

An entity collection can spend most of its request time loading and preparing visible table rows and then rendering their property and action cells, but the current trace shows only a page-wide preparation span and an opaque `render collection <collectionId>` span.
A representative trace showed a 2.071-second collection render with no SQL overlap, while a one-plus-sixteen role query pattern occurred earlier beneath the 2.501-second page preparation span.
The earlier instrumentation deliberately excluded collection preparation, table rows, and cells to limit span volume, but this missing detail prevents operators from distinguishing data loading and row population from slow markup rendering or locating the expensive row or logical column.

## What Changes

- Add one `causeway.wicket.collection.prepare` observation around each parented entity collection's Wicket preparation lifecycle, including data-provider iteration and visible-row population, using contextual name `prepare collection <collectionId>`.
- Add one bounded `causeway.wicket.collection.row.prepare` observation around each visible row population callback using contextual name `prepare row <logical-type-name>`.
- Make collection preparation a child of page preparation, make row preparation a child of collection preparation, and keep both preparation categories separate from actual markup rendering.
- Add one bounded render observation for each collection table row actually rendered on the current page.
- Name each row render observation `render row <logical-type-name>` using the collection element's static logical type, without including the row index, bookmark, title, primary key, or any other instance identifier.
- Preserve `causeway.wicket.collection.row.render` as the stable row render observation name and attach the complete element logical type and parent collection identifier as canonical attributes to both row phases.
- Make each row render observation a child of its enclosing `causeway.wicket.collection.render` observation.
- Include regular logical property cells beneath their row using the existing `causeway.wicket.property.render` observation and `render property <propertyId>` contextual naming.
- Include applicable row-action rendering beneath its row using the existing `causeway.wicket.action.render` observation and `render action <actionId>` contextual naming.
- Instrument only preparation and rendering for rows and logical member cells participating in an instrumented parented entity collection in the current full-page or Ajax response; do not create spans for standalone action-result tables, off-page data, headers, navigation controls, selection checkboxes, or presentation-only cells.
- Retain complete canonical `causeway.collection.id`, `causeway.object.type`, `causeway.property.id`, and `causeway.action.id` attributes while applying the existing case-preserving 50-character contextual-name policy.
- Continue to exclude values, titles, bookmarks, object identifiers, row positions, arguments, users, tenancy data, and generated Wicket component paths.
- Extend focused Wicket lifecycle and Java-agent compatibility validation for row nesting, repeated rows, Ajax rendering, failures, serialization, and no-op behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `wicket-region-render-observation`: Add collection and visible-row preparation detail, and replace the existing collection-table render exclusion with bounded row, property-cell, and row-action render observations.

## Impact

The change affects the parented entity-collection preparation lifecycle, Wicket Ajax data-table row population and render boundaries, scalar-property and action observation policies for table contexts, observation descriptors, lifecycle tests, Java-agent trace validation, and tracing operations documentation.
It intentionally increases span count in proportion to the collections, rows, and instrumented logical cells prepared or rendered, while keeping names and attributes bounded and instance-data-free.
It does not change domain behavior, table output, pagination, sampling, SDK ownership, exporters, dependencies, or Java-agent-owned HTTP and JDBC instrumentation.
