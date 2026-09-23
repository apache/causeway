# wicket-region-render-observation Specification

## Purpose
TBD - created by archiving change add-wicket-region-render-spans. Update Purpose after archive.
## Requirements
### Requirement: Entity-page render observation
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.page.render` observation for each server-side render of an entity page.
The observation SHALL join the current Java-agent-owned trace and SHALL use the Wicket viewer module metadata convention.

#### Scenario: Full entity-page render
- **WHEN** Wicket renders an entity page during a full HTTP response with observation active
- **THEN** one `causeway.wicket.page.render` observation covers the page's actual server-side rendering
- **AND** the observation is a descendant of the current request or Causeway interaction span

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

