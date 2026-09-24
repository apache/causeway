## ADDED Requirements

### Requirement: Configured Wicket observation eligibility

The observation-creation requirements in this capability SHALL apply only when the candidate category is permitted by the configured Wicket observation detail and admitted by the remaining per-request Wicket observation budget.
Suppression SHALL preserve the guarded Wicket behavior, output, failure semantics, static metadata privacy, and Java-agent ownership defined by this capability.

#### Scenario: Existing observation is permitted

- **WHEN** an existing Wicket observation category is enabled by detail and admitted by budget
- **THEN** the viewer creates it with the stable name, contextual name, metadata, hierarchy, and lifecycle required by this capability

#### Scenario: Existing observation is suppressed

- **WHEN** an otherwise eligible Wicket observation is excluded by detail or denied by budget
- **THEN** its guarded framework operation executes unchanged without creating that observation
- **AND** automatic Java-agent instrumentation remains attached to the nearest naturally current context

## MODIFIED Requirements

### Requirement: Bounded render metadata

Each Wicket preparation or render observation SHALL use stable observation names and SHALL attach only bounded layout or metamodel identifiers needed to identify the logical region.
Collection and collection-row preparation and table-phase observations SHALL use the same complete canonical collection and applicable static object-type identifiers as their render counterparts.
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

The viewer SHALL exclude non-semantic and non-server-rendered table contexts from preparation, table-phase, and member-level render observations.
It SHALL create aggregate table-phase observations only for parented entity collections, preparation observations only for parented collections and visible rows participating in the current server response, and member render observations only for rows, logical property cells, and eligible row actions actually rendered in that response.
It SHALL NOT create individual observations for standalone action-result tables, off-page rows, action parameters, service-menu actions, table headers, navigation controls, selection checkboxes, empty-table placeholders, or client-side rendering.

#### Scenario: Paginated collection table rendering

- **WHEN** an entity collection contains more rows than the current page displays
- **THEN** the viewer creates preparation and render observations only for rows participating in the current page response and permitted by the configured observation controls
- **AND** it creates no row observations for rows outside that page

#### Scenario: Standalone action-result table

- **WHEN** a table renders the standalone result of an action without a canonical parent collection association
- **THEN** it does not produce collection or row preparation, table-phase, or `causeway.wicket.collection.row.render` observations
- **AND** no action identifier is mislabeled as `causeway.collection.id`

#### Scenario: Logical table cells

- **WHEN** a rendered row contains property cells and eligible row actions
- **THEN** each rendered logical property cell permitted by observation controls can produce one `causeway.wicket.property.render` observation
- **AND** each rendered eligible action permitted by observation controls can produce one `causeway.wicket.action.render` observation
- **AND** presentation-only cells produce no additional semantic observation

### Requirement: Collection and row preparation observations

When observation is active and permitted by the configured controls, the Wicket viewer SHALL create one `causeway.wicket.collection.prepare` observation around each parented entity collection's later preparation lifecycle before actual markup rendering begins.
The collection preparation observation SHALL use contextual name `prepare collection <collectionId>`, SHALL be a child of the enclosing page preparation observation when emitted, and SHALL own later data-provider sizing and iteration, visible-row selection and population, and automatic JDBC work initiated by that lifecycle.
For each visible row population callback permitted by the configured controls, the viewer SHALL create one `causeway.wicket.collection.row.prepare` observation using contextual name `prepare row <logical-type-name>` as a child of the collection preparation observation or nearest active permitted ancestor.
Preparation contextual names SHALL follow the existing complete-identifier, namespace-fallback, and 50-character truncation policy.

#### Scenario: Collection data is loaded during later preparation

- **WHEN** a parented collection's later Wicket preparation invokes its data provider or loads persistent collection state
- **THEN** that work inherits `causeway.wicket.collection.prepare` as an ancestor when that observation is emitted
- **AND** automatic JDBC spans are not incorrectly attributed to the later `causeway.wicket.collection.render` phase

#### Scenario: Visible rows are populated

- **WHEN** the table synchronously constructs or populates each visible row for the current response
- **THEN** each row population callback permitted by observation controls receives one `causeway.wicket.collection.row.prepare` observation
- **AND** work performed synchronously for that row inherits the row preparation observation when emitted
- **AND** collection-fetch work performed before an individual row callback remains a direct or indirect child of collection preparation when emitted

#### Scenario: Preparation and rendering are separate phases

- **WHEN** Wicket completes collection and row preparation and starts actual markup rendering
- **THEN** all emitted collection and row preparation scopes are closed
- **AND** `causeway.wicket.collection.render` and `causeway.wicket.collection.row.render` observations represent the later rendering phase rather than enclosing preparation work

#### Scenario: Collection or row preparation fails

- **WHEN** collection preparation or a row population callback throws before normal completion
- **THEN** the originating emitted preparation observation records the failure
- **AND** request cleanup leaves no collection or row preparation scope active

#### Scenario: Preparation observation is inactive

- **WHEN** the parented collection is prepared without the `observation` profile active
- **THEN** data-provider and row-population behavior remain unchanged
- **AND** no collection or row preparation spans are exported

## REMOVED Requirements

### Requirement: Collection initialization observation

**Reason:** Representative entity pages create many `causeway.wicket.collection.initialize` siblings that generally take only hundreds of microseconds to a few milliseconds and add substantial trace volume without enough diagnostic separation from page preparation.

**Migration:** Use `causeway.wicket.page.prepare` to identify construction-time page work and `causeway.wicket.collection.prepare` for later collection data-provider and visible-row work.
Automatic JDBC spans formerly parented by collection initialization inherit page preparation when active or the nearest natural request, interaction, or action context.

#### Scenario: Collection loading occurs during component construction

- **WHEN** parented collection UI construction evaluates its collection model, visibility, presentation, or child components and triggers persistent loading
- **THEN** no `causeway.wicket.collection.initialize` observation is created
- **AND** the work and automatic JDBC spans retain their nearest naturally active parent

#### Scenario: Sibling collections initialize

- **WHEN** multiple parented collection regions are constructed sequentially
- **THEN** no observation is created for each sibling's construction
- **AND** no collection-initialization scope is retained across siblings

#### Scenario: Collection initialization fails or observation is inactive

- **WHEN** collection UI construction fails with observation active
- **THEN** existing failure propagation remains unchanged without a collection-initialization observation
- **AND WHEN** observation is inactive
- **THEN** collection construction and loading behavior remain unchanged and no initialization span is exported
