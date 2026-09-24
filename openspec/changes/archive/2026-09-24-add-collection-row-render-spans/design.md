## Context

The current Wicket instrumentation observes page-wide preparation and an entity collection's actual rendering, but it does not identify which collection or row owns preparation work and intentionally suppresses compact table-property cells and row actions during rendering.
A representative trace showed a 2.071-second `render collection roles` span with no overlapping SQL and only microseconds of observed children.
The same trace showed the role collection query followed by sixteen per-role queries earlier beneath the 2.501-second page preparation span, confirming that Wicket data loading and row setup happen before the collection render observation starts.
After row instrumentation was added, a second trace showed five row spans accounting for only 20.370 ms of a 2.073-second collection render, with 1.858 seconds before the first row and 193.513 ms after the final row remaining in collection self-time.
It also showed that the role SQL completed before `prepare collection roles` began, because `EntityCollectionPanel.buildGui()` performs collection-model and presentation setup during component construction rather than during `onBeforeRender()`.

The standard Ajax table is built by `CollectionContentsAsAjaxTablePanel` and `CausewayAjaxDataTable`.
Wicket invokes data-provider iteration and row population during the table's `onBeforeRender()` lifecycle, before behavior-based actual-render observations begin.
`CausewayAjaxDataTable.newRowItem(...)` is the common server-side render boundary for each visible `DataRow`, the row item-factory callback encloses synchronous row population, and `GenericColumnAbstract.populateItem(...)` creates each cell beneath that row.
Property cells become `ScalarPanelAbstract` components with `RenderingHint.PARENTED_PROPERTY_COLUMN`, and action-column cells create `ActionLink` components using the concrete parented or standalone table context.

The existing `WicketRenderObservationBehavior`, request-local tracker, serializable descriptors, case-preserving naming helper, and no-op observation integration already provide the required lifecycle and trace-parenting infrastructure.

## Goals / Non-Goals

**Goals:**

- Distinguish construction-time collection initialization and loading from later visible-row preparation and actual markup rendering.
- Attribute initialization and preparation work to the parent collection and synchronous row population where possible.
- Reveal how collection render time is distributed across table header, body, rows, and footer.
- Reveal logical property-cell and row-action rendering beneath each row.
- Preserve natural Wicket component-tree parentage beneath the enclosing collection observation.
- Keep every name and attribute bounded, static, and independent of domain-object instances.
- Retain lifecycle failure cleanup, Ajax behavior, page serialization safety, and no-op behavior.
- Make the deliberate increase in span volume explicit and testable.

**Non-Goals:**

- Identify a row by index, title, bookmark, primary key, or value.
- Observe rows outside the current paginated response.
- Observe preparation or rendering of standalone action-result tables that have no canonical parent collection association.
- Observe individual header cells, pagination controls, selection checkboxes, empty-table placeholders, or client-side DataTables rendering.
- Attach property values, action arguments, users, tenancy identifiers, or generated Wicket component paths.
- Change collection loading, sorting, pagination, table markup, or domain behavior.
- Add dependencies or alter Java-agent SDK ownership, sampling, or export.

## Decisions

### Separate collection initialization from later preparation

A parented entity collection will create one synchronous `causeway.wicket.collection.initialize` observation with contextual name `initialize collection <collectionId>` around `EntityCollectionPanel.buildGui()`.
This boundary owns collection-model access, visibility and presentation setup, child component construction, and automatic JDBC work initiated during that construction.
It remains a child of page preparation when construction occurs there, closes before sibling collection construction proceeds, and does not remain active until the later Wicket lifecycle.

Stretching one collection scope from construction until `onBeforeRender()` was rejected because sibling collection components initialize sequentially and would create overlapping or incorrectly nested scopes.
Using only the later preparation boundary was rejected by trace evidence showing all role SQL completed before that boundary started.

### Separate collection preparation from collection rendering

A parented entity collection will create a `causeway.wicket.collection.prepare` observation with contextual name `prepare collection <collectionId>`.
The observation will start when the visible collection enters `onBeforeRender()`, before Wicket configures and prepares its descendant table subtree, and stop after descendant preparation completes while the enclosing page preparation observation remains current.
It therefore owns data-provider sizing and iteration, visible-row selection and population, and automatic JDBC work initiated by those later operations.
The existing `causeway.wicket.collection.render` observation remains a separate sibling phase that owns only actual markup rendering.

The preparation implementation will reuse or generalize the page-preparation lifecycle coordinator so failures are recorded, scopes close in order, detach provides cleanup, active state remains transient, and inactive observation remains a no-op.
Starting collection preparation during the collection component's `onConfigure()` was rejected because an invisible component might never receive `onBeforeRender()` and could leave its scope active until detach.
Starting it from `WicketRenderObservationBehavior.beforeRender(...)` was also rejected because Wicket invokes that callback only after data-provider iteration and descendant `onBeforeRender()` work have completed.

### Observe synchronous row preparation

The Ajax table's per-row item-factory or equivalent population callback will create one `causeway.wicket.collection.row.prepare` observation for each visible row it prepares.
The observation will use contextual name `prepare row <logical-type-name>` and will be a child of `prepare collection <collectionId>`.
It will enclose synchronous row-item creation and cell population without remaining open across sibling rows or across the later render phase.
Collection-fetch work that occurs before iteration reaches an individual row remains directly beneath collection preparation, while work performed while constructing that row inherits row preparation.

Keeping one row observation open from population through actual rendering was rejected because Wicket prepares all rows before rendering them and such scopes would overlap or close out of stack order.
Instrumenting `DataRowWkt` was rejected because it is a model rather than a lifecycle boundary.

### Observe the Wicket row item during actual rendering

`CausewayAjaxDataTable.newRowItem(...)` will create or decorate each `Item<DataRow>` with a `WicketRenderObservationBehavior` using a collection-row render descriptor.
This boundary encloses every cell component actually rendered for that row and naturally makes row render observations children of the enclosing collection render observation.

The parented entity-collection construction path will provide both preparation and render descriptors with the collection element logical type and the parent collection's canonical identifier.
Standalone action-result tables will not receive collection or row instrumentation because they have no canonical `causeway.collection.id` or enclosing parented collection observation.
The item or a small dedicated subclass will implement the metamodel-context contract required by the reusable render observation behavior.

### Decompose Ajax table rendering into bounded phases

Each instrumented parented `CausewayAjaxDataTable` will create one `causeway.wicket.collection.table.render` observation with contextual name `render table <collectionId>`.
Its caption, column group, top toolbar, body, and bottom toolbar render within that table aggregate.
The table's top-toolbar container, body container, and bottom-toolbar container will create aggregate `causeway.wicket.collection.table.header.render`, `causeway.wicket.collection.table.body.render`, and `causeway.wicket.collection.table.footer.render` observations.
Their contextual names will be `render table header <collectionId>`, `render table body <collectionId>`, and `render table footer <collectionId>`.
Row observations naturally become descendants of the table-body observation.

The aggregate header and footer phases deliberately include presentation mechanics without creating observations for each header, sort link, navigation control, count label, or empty placeholder.
A table-level parent is retained so work outside the three child containers, including caption, column-group, component-tag, and table markup work, remains measurable as table self-time rather than collection self-time.
Standalone action-result tables remain excluded because they have no canonical parent collection identifier.

### Use static row identity only

A row preparation observation will use stable name `causeway.wicket.collection.row.prepare` and contextual name `prepare row <logical-type-name>`.
A row render observation will use stable name `causeway.wicket.collection.row.render` and contextual name `render row <logical-type-name>`.
Both row phases will carry canonical `causeway.object.type` containing the complete collection element logical type and canonical `causeway.collection.id` containing the complete parent collection identifier.

Every row of the same logical element type in the same collection can therefore have the same names and attributes within each phase.
The trace structure and timing distinguish occurrences without introducing unbounded row identity.

A row index attribute was rejected because pagination, sorting, and filtering make it unstable and because it creates unnecessary per-instance telemetry.
Bookmarks and titles were rejected as instance-specific and potentially sensitive.

### Reuse property and action render observations beneath rows

`WicketRenderObservationPolicy.propertyDescriptor(...)` will include scalar property models using `RenderingHint.PARENTED_PROPERTY_COLUMN` in addition to regular object-form properties.
The existing stable name `causeway.wicket.property.render`, contextual name `render property <propertyId>`, and canonical `causeway.property.id` will remain unchanged.

`ActionColumn` will pass its concrete collection variant context, and `WicketRenderObservationPolicy.actionDescriptor(...)` will include applicable action links rendered in the `Where.PARENTED_TABLES` context while excluding `Where.STANDALONE_TABLES`.
The existing stable name `causeway.wicket.action.render`, contextual name `render action <actionId>`, and canonical `causeway.action.id` will remain unchanged.

Because these components are descendants of the row item, the existing behavior and tracker will establish row parentage without explicit trace-parent wiring.
Creating new cell-specific stable observation categories was rejected because property and action cells render the same logical members already represented by the existing taxonomy.

### Keep presentation-only cells inside the row aggregate

Title, toggle, and other presentation-only cells will not receive separate observations in this change.
Their work remains visible in the duration of the enclosing row span, while logical property and action cells provide the diagnostically useful member detail.

This avoids inventing canonical member identifiers for presentation mechanics and limits span amplification.

### Bound span amplification to rendered content

Each participating parented collection adds one initialization observation and up to four table-render observations in addition to its existing preparation and render observations.
The remaining number of new observations is proportional to visible rows prepared, plus the rows, logical property cells, and row-action components actually rendered in the current response.
Pagination and Ajax partial rendering naturally bound the work because Wicket creates and renders only the applicable row items.

No configurable threshold or sampling control will be added in this maintenance branch.
The existing Java-agent or collector sampling policy remains the operational control for total trace volume.

## Risks / Trade-offs

- [Large page sizes create many spans] → Add only a fixed initialization and table-phase cost per collection, instrument only rendered rows and logical cells, retain pagination, and document the intentional rows-times-members growth.
- [Table-phase spans merely move unexplained time] → Keep an enclosing table span so caption, column-group, tag, and markup work remains visible as bounded table self-time, and use profiling rather than unbounded cell spans if that self-time remains material.
- [Repeated row names cannot identify a domain instance] → Treat this as a privacy and cardinality requirement; use ordering, duration, and child operations for diagnosis rather than instance identity.
- [Collection preparation spans overlap or leak into rendering] → Use explicit preparation lifecycle callbacks, close before actual rendering, and retain detach-time cleanup.
- [Sibling row preparation scopes become incorrectly nested] → Observe only each synchronous item-population callback and close it before advancing to the next row.
- [Row item reuse across Ajax requests retains telemetry state] → Persist only serializable descriptors and keep active observations and scopes transient through the existing lifecycle helpers.
- [Collection preparation, row population, or cell rendering fails before normal completion] → Record the originating failure and close remaining scopes in reverse order.
- [Action-column helpers render presentation wrappers as well as actions] → Attach observations only to actual eligible action links, not the wrapper or empty action column.
- [Property-cell observations duplicate names across rows] → Preserve the row hierarchy so each repeated property observation remains attributable to one row occurrence.

## Migration Plan

Applications receive collection initialization, collection and row preparation, table-phase rendering, row rendering, and cell detail only when the existing `observation` profile is active.
No configuration or data migration is required.
Operators should expect larger traces for collection-heavy pages and should review page-size and sampling settings before production rollout.
Rollback consists of reverting collection initialization and preparation, removing table-phase and row render attachment, and restoring table-context exclusions; the existing page preparation and collection render observations remain compatible.

## Open Questions

- Confirm during implementation that `EntityCollectionPanel.buildGui()` owns the construction-time SQL seen before `prepare collection`, and whether any earlier model access requires an additional boundary.
- Confirm whether the Ajax table item-reuse strategy provides one synchronous preparation callback for both newly created and reused visible rows, or whether reused rows need an equivalent boundary.
- Confirm during implementation whether every supported parented collection table presentation uses `CausewayAjaxDataTable.newRowItem(...)` or whether an additional server-side row render boundary requires equivalent instrumentation.
- Confirm that Wicket renders the top-toolbar, body, and bottom-toolbar containers in header/body/footer order and that attaching aggregate observations does not alter visibility or markup.
- Confirm that row actions created outside `ActionColumn` but rendered in the concrete `Where.PARENTED_TABLES` context follow the same inclusion rule without capturing standalone-table or service-menu actions.
