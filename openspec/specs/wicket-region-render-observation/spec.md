# wicket-region-render-observation Specification

## Purpose
Define bounded, lifecycle-safe observations for Wicket entity-page preparation and semantic render regions.
## Requirements
### Requirement: Entity-page render observation
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.page.render` observation for each server-side render of an entity page.
The observation SHALL join the current Java-agent-owned trace, SHALL use the Wicket viewer module metadata convention, and SHALL have the contextual display name `render <logical-type-name>`.
The stable observation name SHALL remain `causeway.wicket.page.render`, and `causeway.object.type` SHALL retain the full canonical logical type.

#### Scenario: Full entity-page render
- **WHEN** Wicket renders an entity page during a full HTTP response with observation active
- **THEN** one `causeway.wicket.page.render` observation covers the page's actual server-side rendering
- **AND** the observation is a descendant of the current request or Causeway interaction span
- **AND** its contextual display name begins with `render ` and contains the full logical type name when that name fits within 50 characters
- **AND** an overlength name falls back to the namespace-free logical type before truncation
- **AND** its `causeway.object.type` attribute contains the full canonical logical type

#### Scenario: Observation is inactive
- **WHEN** Wicket renders an entity page without the `observation` profile active
- **THEN** rendering behavior and output remain unchanged
- **AND** no exported Causeway Wicket render span is produced

### Requirement: Logical region render observations
The Wicket viewer SHALL create nested render observations for fieldsets, regular entity properties, entity collections, collection tables and their aggregate header, body, and footer phases, rendered collection rows, logical property cells, and relevant entity-page or row action buttons that are actually rendered.
The stable observation names SHALL include `causeway.wicket.fieldset.render`, `causeway.wicket.property.render`, `causeway.wicket.collection.render`, `causeway.wicket.collection.table.render`, `causeway.wicket.collection.table.header.render`, `causeway.wicket.collection.table.body.render`, `causeway.wicket.collection.table.footer.render`, `causeway.wicket.collection.row.render`, and `causeway.wicket.action.render`.

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
- **THEN** the table observation is a child of the collection render observation
- **AND** the table-body observation is a child of the table observation
- **AND** each rendered row observation is a child of the table-body observation
- **AND** each observed property cell or row action is a child of its rendered row observation

#### Scenario: Ajax partial render
- **WHEN** an Ajax response renders an instrumented logical region or collection table without rendering the complete entity page
- **THEN** the viewer creates observations only for the instrumented regions, rows, and logical cells actually rendered
- **AND** each observation joins the current Ajax request trace

### Requirement: Bounded render metadata
Each Wicket initialization, preparation, or render observation SHALL use stable observation names and SHALL attach only bounded layout or metamodel identifiers needed to identify the logical region.
Collection initialization, collection and collection-row preparation, and table-phase observations SHALL use the same complete canonical collection and applicable static object-type identifiers as their render counterparts.
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
The viewer SHALL exclude non-semantic and non-server-rendered table contexts from collection initialization, preparation, table-phase, and member-level render observations.
It SHALL create initialization and aggregate table-phase observations only for parented entity collections, preparation observations only for parented collections and visible rows participating in the current server response, and member render observations only for rows, logical property cells, and eligible row actions actually rendered in that response.
It SHALL NOT create individual observations for standalone action-result tables, off-page rows, action parameters, service-menu actions, table headers, navigation controls, selection checkboxes, empty-table placeholders, or client-side rendering.

#### Scenario: Paginated collection table rendering
- **WHEN** an entity collection contains more rows than the current page displays
- **THEN** the viewer creates preparation and render observations only for rows participating in the current page response
- **AND** it creates no row observations for rows outside that page

#### Scenario: Standalone action-result table
- **WHEN** a table renders the standalone result of an action without a canonical parent collection association
- **THEN** it does not produce collection initialization, collection or row preparation, table-phase, or `causeway.wicket.collection.row.render` observations
- **AND** no action identifier is mislabeled as `causeway.collection.id`

#### Scenario: Logical table cells
- **WHEN** a rendered row contains property cells and eligible row actions
- **THEN** each rendered logical property cell can produce one `causeway.wicket.property.render` observation
- **AND** each rendered eligible action can produce one `causeway.wicket.action.render` observation
- **AND** presentation-only title, toggle, and empty cells do not produce member observations

#### Scenario: Action prompt rendering
- **WHEN** Wicket renders action parameter fields in a prompt
- **THEN** those parameter fields do not produce `causeway.wicket.property.render` observations

### Requirement: Render observation lifecycle safety
The viewer SHALL close render observation scopes after successful rendering and SHALL defensively close any remaining active render observations when request rendering fails or the request detaches.
A rendering failure SHALL be recorded on the active observation when the originating failure is available.

#### Scenario: Successful nested rendering
- **WHEN** nested instrumented regions render successfully
- **THEN** their observation scopes close in reverse nesting order
- **AND** no Wicket render observation remains current after request completion

#### Scenario: Rendering failure
- **WHEN** rendering throws before a normal component completion callback
- **THEN** request-level cleanup records the failure when available
- **AND** closes every remaining render observation scope
- **AND** no render observation state leaks into subsequent work on the request thread

#### Scenario: Defensive cleanup after normal completion
- **WHEN** all observations close normally and request detach subsequently performs defensive cleanup
- **THEN** cleanup is harmless and does not stop an observation more than once

### Requirement: Serializable instrumentation state
Wicket render instrumentation SHALL retain only serializable region descriptors in persisted component state and SHALL NOT serialize active observations, scopes, registries, or integration services.

#### Scenario: Page passivation and later rendering
- **WHEN** an instrumented Wicket page is serialized, restored, and rendered in a later request
- **THEN** no telemetry object from the earlier request is restored
- **AND** the later render creates observations using that request's current trace context

### Requirement: Render-only semantics
The duration of a Wicket region render observation SHALL represent the component's actual Wicket rendering callback and descendant work performed during that callback.
It SHALL NOT be presented as covering component construction, initialization, configuration, visibility, usability, or authorization evaluation performed before rendering.

#### Scenario: Preparation precedes rendering
- **WHEN** component preparation completes before an instrumented region begins actual rendering
- **THEN** the region render observation starts only for the actual render callback
- **AND** operational documentation states that preparation time can remain outside region render spans

### Requirement: Entity-page preparation observation
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.page.prepare` observation for entity-page initialization, component configuration, visibility and usability evaluation, and component-tree preparation performed before actual markup rendering.
The observation SHALL use contextual name `prepare <logical-type-name>` and SHALL carry the complete logical type as `causeway.object.type`.

#### Scenario: Full entity page is prepared
- **WHEN** Wicket configures an entity page before a full server-side render
- **THEN** the preparation observation starts before `EntityPage` delegates to `Page.onConfigure()`
- **AND** it encloses page initialization and descendant component configuration and `onBeforeRender` callbacks
- **AND** it closes before `causeway.wicket.page.render` begins
- **AND** the preparation and render observations are consecutive descendants of the current request or Causeway interaction span

#### Scenario: Mixed-in associations are evaluated during preparation
- **WHEN** a visibility check, component initialization, or other pre-render operation evaluates a mixed-in property or collection
- **THEN** the resulting `causeway.property.access` or `causeway.collection.access` observation is a descendant of `causeway.wicket.page.prepare`

#### Scenario: Page preparation fails
- **WHEN** configuration or pre-render preparation throws
- **THEN** the preparation observation records the error and closes
- **AND** no preparation scope remains active after request cleanup

#### Scenario: Observation is inactive during preparation
- **WHEN** Wicket prepares an entity page without the `observation` profile active
- **THEN** page lifecycle behavior remains unchanged
- **AND** no exported page-preparation span is produced

### Requirement: Action-prompt render observation
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.action.prompt.render` observation around each actual render callback of an enclosing action-parameter prompt panel.
The observation SHALL use the contextual display name `prompt <logical-member-identifier>` and SHALL use the existing Wicket render lifecycle infrastructure.

#### Scenario: Parameterized action prompt is rendered
- **WHEN** Wicket renders the enclosing prompt panel for a parameterized action
- **THEN** one `causeway.wicket.action.prompt.render` observation covers the panel's actual render callback and descendant rendering
- **AND** its contextual display name begins with `prompt ` and uses the full domain-facing logical member identifier when that name fits within 50 characters
- **AND** an overlength name falls back to the namespace-free logical type and member id before truncation
- **AND** it joins the current request trace with the active render region as its parent when one exists

#### Scenario: Prompt presentation styles
- **WHEN** the same logical action prompt is presented inline, in a modal, or in a sidebar
- **THEN** the viewer uses the same semantic observation name and contextual naming convention
- **AND** presentation-shell rendering does not create a duplicate action-prompt observation

### Requirement: Bounded action-prompt metadata
An action-prompt render observation SHALL carry the full canonical `causeway.object.type` and `causeway.action.id` attributes.
It MUST NOT carry action arguments, parameter values, object titles, bookmarks, target-instance identifiers, localized labels, user identities, tenancy identifiers, or generated Wicket component paths.

#### Scenario: Prompt contains populated parameters
- **WHEN** Wicket renders an action prompt whose parameter models contain defaults or user-supplied values
- **THEN** the observation identifies only the logical object type and action
- **AND** neither its contextual name nor its attributes contain parameter values or target-instance data

### Requirement: Single bounded prompt region
The action-prompt observation SHALL cover the enclosing prompt render region and SHALL NOT create observations for individual action parameter fields.
The observation SHALL represent rendering only and SHALL NOT be presented as covering defaults, choices, validation, usability, visibility, authorization, or other preparation performed outside the render callback.

#### Scenario: Prompt has multiple parameters
- **WHEN** an action prompt renders multiple parameter fields
- **THEN** one enclosing action-prompt render observation is produced
- **AND** the parameter fields do not produce `causeway.wicket.property.render` observations

#### Scenario: Prompt preparation precedes rendering
- **WHEN** defaults, choices, validation, or authorization work completes before the prompt panel's render callback
- **THEN** that work is not attributed to `causeway.wicket.action.prompt.render`
- **AND** operational documentation describes the render-only boundary

### Requirement: Meaningful fine-grained render display names
The Wicket viewer SHALL retain stable observation names while assigning member-level contextual names to fieldset, property, collection, and action-button render observations.
The contextual names SHALL preserve the casing of static layout or metamodel identifiers and SHALL be truncated to 50 characters when necessary.
Their canonical layout and metamodel attributes SHALL continue to identify the rendered region.

#### Scenario: Named fieldset is rendered
- **WHEN** Wicket renders an observed fieldset with id `identity`
- **THEN** its contextual name is `render fieldset identity`
- **AND** its observation name remains `causeway.wicket.fieldset.render`

#### Scenario: Unnamed fieldset is rendered
- **WHEN** Wicket renders the default unnamed fieldset
- **THEN** its contextual name is `render fieldset default`
- **AND** the internal marker `<default>` is not exposed in the contextual name

#### Scenario: Property is rendered
- **WHEN** Wicket renders an observed property with member id `emailAddress`
- **THEN** its contextual name is `render property emailAddress`
- **AND** its observation name remains `causeway.wicket.property.render`

#### Scenario: Collection is rendered
- **WHEN** Wicket renders an observed collection with member id `roles`
- **THEN** its contextual name is `render collection roles`
- **AND** its observation name remains `causeway.wicket.collection.render`

#### Scenario: Action button is rendered
- **WHEN** Wicket renders an observed action button with member id `updateEmailAddress`
- **THEN** its contextual name is `render action updateEmailAddress`
- **AND** its observation name remains `causeway.wicket.action.render`

#### Scenario: Fine-grained display name exceeds the limit
- **WHEN** a fieldset or member render contextual name exceeds 50 characters
- **THEN** Causeway truncates the contextual name to 50 characters
- **AND** its full canonical region attribute remains unchanged

### Requirement: Collection initialization observation
When observation is active, the Wicket viewer SHALL create one synchronous `causeway.wicket.collection.initialize` observation around each parented entity collection's UI construction and collection-model setup.
The observation SHALL use contextual name `initialize collection <collectionId>`, SHALL join the current page-preparation scope when one is active, and SHALL own automatic JDBC work initiated while constructing that collection region.

#### Scenario: Collection loading occurs during component construction
- **WHEN** parented collection UI construction evaluates its collection model, visibility, presentation, or child components and triggers persistent loading
- **THEN** that work inherits `causeway.wicket.collection.initialize` as an ancestor
- **AND** automatic JDBC spans identify the canonical parent collection through that initialization ancestor

#### Scenario: Sibling collections initialize
- **WHEN** multiple parented collection regions are constructed sequentially
- **THEN** each initialization observation closes before construction proceeds to the next sibling
- **AND** initialization scopes do not overlap or remain active until later Wicket preparation

#### Scenario: Collection initialization fails or observation is inactive
- **WHEN** collection UI construction fails with observation active
- **THEN** the initialization observation records the failure and closes its scope
- **AND WHEN** observation is inactive
- **THEN** collection construction and loading behavior remain unchanged and no initialization span is exported

### Requirement: Collection and row preparation observations
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.collection.prepare` observation around each parented entity collection's later preparation lifecycle before actual markup rendering begins.
The collection preparation observation SHALL use contextual name `prepare collection <collectionId>`, SHALL be a child of the enclosing page preparation observation, and SHALL own later data-provider sizing and iteration, visible-row selection and population, and automatic JDBC work initiated by that lifecycle.
For each visible row population callback, the viewer SHALL create one `causeway.wicket.collection.row.prepare` observation using contextual name `prepare row <logical-type-name>` as a child of the collection preparation observation.
Preparation contextual names SHALL follow the existing complete-identifier, namespace-fallback, and 50-character truncation policy.

#### Scenario: Collection data is loaded during later preparation
- **WHEN** a parented collection's later Wicket preparation invokes its data provider or loads persistent collection state
- **THEN** that work inherits `causeway.wicket.collection.prepare` as an ancestor
- **AND** automatic JDBC spans are not incorrectly attributed to initialization or the later `causeway.wicket.collection.render` phase

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

### Requirement: Collection-table render phases
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.collection.table.render` observation around each parented Ajax table's actual render callback using contextual name `render table <collectionId>`.
The table SHALL create aggregate `causeway.wicket.collection.table.header.render`, `causeway.wicket.collection.table.body.render`, and `causeway.wicket.collection.table.footer.render` observations for the visible top-toolbar, body, and bottom-toolbar containers using contextual names `render table header <collectionId>`, `render table body <collectionId>`, and `render table footer <collectionId>`.

#### Scenario: Table renders rows and toolbars
- **WHEN** a parented Ajax table renders its header, body rows, and footer
- **THEN** the header, body, and footer observations are children of the table observation
- **AND** row observations are children of the table-body observation
- **AND** caption, column-group, component-tag, and other table work outside those containers remains attributable to table self-time

#### Scenario: Aggregate phases avoid span amplification
- **WHEN** a table contains multiple columns, sortable headers, navigation controls, or record-count labels
- **THEN** the viewer creates at most one header, one body, and one footer observation for that table render
- **AND** it creates no observation for each individual header, sort link, navigation control, or count label

#### Scenario: Table phase fails or observation is inactive
- **WHEN** a table or phase render fails with observation active
- **THEN** the existing render lifecycle records or inherits the failure and leaves no table-phase scope active
- **AND WHEN** observation is inactive
- **THEN** table markup and behavior remain unchanged and no table-phase span is exported

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

