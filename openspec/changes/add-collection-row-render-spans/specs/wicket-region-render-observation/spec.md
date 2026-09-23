## MODIFIED Requirements

### Requirement: Logical region render observations
The Wicket viewer SHALL create nested render observations for fieldsets, regular entity properties, entity collections, rendered collection rows, logical property cells, and relevant entity-page or row action buttons that are actually rendered.
The stable observation names SHALL be `causeway.wicket.fieldset.render`, `causeway.wicket.property.render`, `causeway.wicket.collection.render`, `causeway.wicket.collection.row.render`, and `causeway.wicket.action.render` respectively.

#### Scenario: Nested entity regions
- **WHEN** an entity page renders a fieldset containing regular properties and associated action buttons
- **THEN** the fieldset render observation is a child of the page render observation
- **AND** each rendered property and relevant action-button observation is a child of the enclosing rendered region

#### Scenario: Entity collection region
- **WHEN** an entity collection region is rendered
- **THEN** one `causeway.wicket.collection.render` observation covers that collection region's actual rendering
- **AND** work initiated during that rendering inherits the collection observation as its current parent

#### Scenario: Collection row hierarchy
- **WHEN** an entity collection table renders rows containing logical property and action cells
- **THEN** each rendered row observation is a child of the collection render observation
- **AND** each observed property cell or row action is a child of its rendered row observation

#### Scenario: Ajax partial render
- **WHEN** an Ajax response renders an instrumented logical region or collection table without rendering the complete entity page
- **THEN** the viewer creates observations only for the instrumented regions, rows, and logical cells actually rendered
- **AND** each observation joins the current Ajax request trace

### Requirement: Bounded render metadata
Each Wicket preparation or render observation SHALL use stable observation names and SHALL attach only bounded layout or metamodel identifiers needed to identify the logical region.
Collection and collection-row preparation observations SHALL use the same complete canonical collection and static element-type identifiers as their render counterparts.
Collection-row observations SHALL identify the static collection element type and parent collection but SHALL NOT identify the row instance.
The viewer MUST NOT attach object values, rendered labels, object titles, bookmarks, primary keys, row indexes, action arguments, user identities, tenancy identifiers, or generated Wicket component paths.

#### Scenario: Property metadata
- **WHEN** a regular entity property or collection-table property cell is rendered with observation active
- **THEN** its observation identifies the logical object type and property member using stable metamodel identifiers
- **AND** it contains no property value or object-instance identifier

#### Scenario: Collection row metadata
- **WHEN** a collection row is rendered with observation active
- **THEN** its observation carries the complete element logical type as `causeway.object.type`
- **AND** it carries the complete parent collection identifier as `causeway.collection.id`
- **AND** its name and attributes contain no row index, title, bookmark, primary key, or value

#### Scenario: Other region metadata
- **WHEN** a fieldset, collection, or action button is rendered with observation active
- **THEN** its observation identifies the applicable layout or metamodel member
- **AND** the identifier describes application structure rather than a particular domain-object instance

### Requirement: Bounded instrumentation scope
The viewer SHALL exclude non-semantic and non-server-rendered table contexts from collection preparation and member-level render observations.
It SHALL create preparation observations only for parented entity collections and visible rows participating in the current server response, and render observations only for collection rows, logical property cells, and eligible row actions actually rendered in that response.
It SHALL NOT create observations for standalone action-result tables, off-page rows, action parameters, service-menu actions, table headers, navigation controls, selection checkboxes, empty-table placeholders, or client-side rendering.

#### Scenario: Paginated collection table rendering
- **WHEN** an entity collection contains more rows than the current page displays
- **THEN** the viewer creates preparation and render observations only for rows participating in the current page response
- **AND** it creates no row observations for rows outside that page

#### Scenario: Standalone action-result table
- **WHEN** a table renders the standalone result of an action without a canonical parent collection association
- **THEN** it does not produce collection or row preparation observations or `causeway.wicket.collection.row.render` observations
- **AND** no action identifier is mislabeled as `causeway.collection.id`

#### Scenario: Logical table cells
- **WHEN** a rendered row contains property cells and eligible row actions
- **THEN** each rendered logical property cell can produce one `causeway.wicket.property.render` observation
- **AND** each rendered eligible action can produce one `causeway.wicket.action.render` observation
- **AND** presentation-only title, toggle, and empty cells do not produce member observations

#### Scenario: Action prompt rendering
- **WHEN** Wicket renders action parameter fields in a prompt
- **THEN** those parameter fields do not produce `causeway.wicket.property.render` observations

## ADDED Requirements

### Requirement: Collection and row preparation observations
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.collection.prepare` observation around each parented entity collection's preparation lifecycle before actual markup rendering begins.
The collection preparation observation SHALL use contextual name `prepare collection <collectionId>`, SHALL be a child of the enclosing page preparation observation, and SHALL own data-provider sizing and iteration, collection loading, visible-row selection and population, and automatic JDBC work initiated by that lifecycle.
For each visible row population callback, the viewer SHALL create one `causeway.wicket.collection.row.prepare` observation using contextual name `prepare row <logical-type-name>` as a child of the collection preparation observation.
Preparation contextual names SHALL follow the existing complete-identifier, namespace-fallback, and 50-character truncation policy.

#### Scenario: Collection data is loaded before rendering
- **WHEN** a parented collection's Wicket preparation invokes its data provider or loads persistent collection state
- **THEN** that work inherits `causeway.wicket.collection.prepare` as an ancestor
- **AND** automatic JDBC spans are not incorrectly attributed to the later `causeway.wicket.collection.render` phase

#### Scenario: Visible rows are populated
- **WHEN** the table synchronously constructs or populates each visible row for the current response
- **THEN** each row population callback receives one `causeway.wicket.collection.row.prepare` observation
- **AND** work performed synchronously for that row inherits the row preparation observation
- **AND** collection-fetch work performed before an individual row callback remains a direct or indirect child of collection preparation

#### Scenario: Preparation and rendering are separate phases
- **WHEN** Wicket completes collection and row preparation and starts actual markup rendering
- **THEN** all collection and row preparation scopes are closed
- **AND** `causeway.wicket.collection.render` and `causeway.wicket.collection.row.render` observations represent the later rendering phase rather than enclosing preparation work

#### Scenario: Collection or row preparation fails
- **WHEN** collection preparation or a row population callback throws before normal completion
- **THEN** the originating preparation observation records the failure
- **AND** request cleanup leaves no collection or row preparation scope active

#### Scenario: Preparation observation is inactive
- **WHEN** the parented collection is prepared without the `observation` profile active
- **THEN** data-provider and row-population behavior remain unchanged
- **AND** no collection or row preparation spans are exported

### Requirement: Collection-row render observation
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.collection.row.render` observation around the actual Wicket render callback of each collection table row rendered by the server.
The observation SHALL use contextual name `render row <logical-type-name>` and SHALL follow the existing full-logical-type, namespace-fallback, and 50-character truncation policy.

#### Scenario: Multiple rows are rendered
- **WHEN** the current collection page renders multiple rows of the same logical element type
- **THEN** one row observation is produced for each rendered row
- **AND** the repeated observations use the same bounded contextual name and canonical attributes
- **AND** no instance-specific identifier is introduced to distinguish them

#### Scenario: Row rendering performs automatic or semantic work
- **WHEN** property evaluation, action visibility, or automatic JDBC work occurs while a row is rendered
- **THEN** that work inherits the row observation as an ancestor according to the Wicket component hierarchy

#### Scenario: Row rendering fails
- **WHEN** rendering a row or descendant cell throws before normal completion
- **THEN** the active row and collection observations record or inherit the failure according to the existing render lifecycle policy
- **AND** request cleanup leaves no row observation scope active

#### Scenario: Observation is inactive
- **WHEN** collection rows render without the `observation` profile active
- **THEN** table behavior and output remain unchanged
- **AND** no collection-row render, property-cell, or row-action spans are exported
