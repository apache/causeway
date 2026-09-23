## 1. Collection and Row Preparation Boundaries

- [ ] 1.1 Confirm the narrowest parented entity-collection lifecycle callback that encloses data-provider sizing, iteration, and descendant preparation while ending before actual markup rendering, and keep standalone action-result tables excluded.
- [ ] 1.2 Add serializable descriptors for `causeway.wicket.collection.prepare` with `prepare collection <collectionId>` naming and `causeway.wicket.collection.row.prepare` with `prepare row <logical-type-name>` naming, complete element-type metadata, and the canonical parent collection identifier.
- [ ] 1.3 Instrument parented entity-collection preparation as a child of page preparation while preserving failure recording, detach-time cleanup, serialization safety, and inactive-profile behavior.
- [ ] 1.4 Instrument each synchronous visible-row population callback as a child of collection preparation without leaving scopes open across sibling rows or into actual rendering.
- [ ] 1.5 Verify item-reuse behavior and add an equivalent preparation boundary if reused visible rows bypass the primary row-population callback.

## 2. Collection-Row Render Boundary

- [ ] 2.1 Confirm the parented entity-collection presentations that use `CausewayAjaxDataTable.newRowItem(...)` and keep standalone action-result tables excluded.
- [ ] 2.2 Add the serializable `causeway.wicket.collection.row.render` descriptor with `render row <logical-type-name>` contextual naming, complete element-type metadata, and the canonical parent collection identifier.
- [ ] 2.3 Thread the static element logical type and collection identifier from the collection model into the Ajax data table without retaining domain-object instances.
- [ ] 2.4 Attach the reusable render observation behavior to each rendered row item while preserving item reuse, CSS decoration, serialization, and no-op behavior.

## 3. Logical Cell and Row-Action Detail

- [ ] 3.1 Extend the property observation policy to include `PARENTED_PROPERTY_COLUMN` scalar models while continuing to exclude action parameters and presentation-only cells.
- [ ] 3.2 Extend the action observation policy to include eligible `Where.ALL_TABLES` row actions while continuing to exclude service-menu and non-table action contexts.
- [ ] 3.3 Verify that property-cell and row-action observations inherit the active row render observation through the Wicket component hierarchy and retain complete canonical member attributes.
- [ ] 3.4 Preserve the existing case-sensitive 50-character naming fallback for collection preparation, row preparation, row rendering, property-cell, and row-action contextual names.

## 4. Lifecycle, Parentage, and Volume Tests

- [ ] 4.1 Add descriptor and policy tests for collection and row preparation metadata, row render metadata, long identifier fallback, table property inclusion, parameter exclusion, row-action inclusion, and service-action exclusion.
- [ ] 4.2 Add Wicket lifecycle tests proving that data-provider and collection-loading work is parented beneath collection preparation, synchronous row-population work is parented beneath row preparation, and all preparation scopes close before collection rendering starts.
- [ ] 4.3 Add Wicket rendering tests for collection-to-row-to-cell/action parentage across multiple rows without row indexes, titles, bookmarks, values, or generated component paths.
- [ ] 4.4 Add pagination and Ajax partial-response tests confirming that only collections, rows, and logical cells participating in the current response produce observations.
- [ ] 4.5 Add preparation and render failure-cleanup, item-reuse serialization, inactive-profile, and no-op tests.

## 5. Documentation and Compatibility Validation

- [ ] 5.1 Update the tracing operations guide with separate collection and row preparation and rendering hierarchies, intentional span-volume increase, privacy constraints, and pagination and sampling guidance.
- [ ] 5.2 Extend the Java-agent compatibility fixture to assert collection-preparation, row-preparation, row-render, property-cell, and row-action nesting while automatic HTTP and JDBC spans remain agent-owned.
- [ ] 5.3 Add representative trace validation showing collection-loading and N-plus-one JDBC spans beneath preparation while actual row markup time is divided among row-render spans.
- [ ] 5.4 Run the focused Wicket observation and Java-agent compatibility suites and record test counts and representative exported trace evidence.
- [ ] 5.5 Validate the OpenSpec change strictly and confirm that all new span names and attributes remain bounded and instance-data-free.
