## MODIFIED Requirements

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

## ADDED Requirements

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
