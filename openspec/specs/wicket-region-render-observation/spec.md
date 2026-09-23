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
The Wicket viewer SHALL create nested render observations for fieldsets, regular entity properties, entity collections, and relevant entity-page action buttons that are actually rendered.
The stable observation names SHALL be `causeway.wicket.fieldset.render`, `causeway.wicket.property.render`, `causeway.wicket.collection.render`, and `causeway.wicket.action.render` respectively.

#### Scenario: Nested entity regions
- **WHEN** an entity page renders a fieldset containing regular properties and associated action buttons
- **THEN** the fieldset render observation is a child of the page render observation
- **AND** each rendered property and relevant action-button observation is a child of the enclosing rendered region

#### Scenario: Entity collection region
- **WHEN** an entity collection region is rendered
- **THEN** one `causeway.wicket.collection.render` observation covers that collection region's actual rendering
- **AND** work initiated during that rendering inherits the collection observation as its current parent

#### Scenario: Ajax partial render
- **WHEN** an Ajax response renders an instrumented logical region without rendering the complete entity page
- **THEN** the viewer creates an observation for each instrumented region actually rendered
- **AND** each observation joins the current Ajax request trace

### Requirement: Bounded render metadata
Each Wicket render observation SHALL use stable observation names and SHALL attach only bounded layout or metamodel identifiers needed to identify the logical region.
The viewer MUST NOT attach object values, rendered labels, object titles, bookmarks, action arguments, user identities, tenancy identifiers, or generated Wicket component paths.

#### Scenario: Property metadata
- **WHEN** a regular entity property is rendered with observation active
- **THEN** its observation identifies the logical object type and property member using stable metamodel identifiers
- **AND** it contains no property value or object-instance identifier

#### Scenario: Other region metadata
- **WHEN** a fieldset, collection, or action button is rendered with observation active
- **THEN** its observation identifies the applicable layout or metamodel member
- **AND** the identifier describes application structure rather than a particular domain-object instance

### Requirement: Bounded instrumentation scope
The viewer SHALL exclude high-volume or non-entity rendering contexts from member-level observations in this capability.
It SHALL NOT create property render observations for compact collection-table cells or action parameters, and SHALL NOT create action render observations for service menus or row-level collection actions.

#### Scenario: Collection table rendering
- **WHEN** an entity collection renders multiple rows and scalar cells
- **THEN** the viewer creates the enclosing collection render observation
- **AND** it does not create a property render observation for each table cell
- **AND** it does not create an action render observation for each row action

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

