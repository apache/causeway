## MODIFIED Requirements

### Requirement: Entity-page render observation
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.page.render` observation for each server-side render of an entity page.
The observation SHALL join the current Java-agent-owned trace, SHALL use the Wicket viewer module metadata convention, and SHALL have a contextual display name derived from the rendered object's stable logical type.
The stable observation name SHALL remain `causeway.wicket.page.render`, and `causeway.object.type` SHALL retain the full canonical logical type.

#### Scenario: Full entity-page render
- **WHEN** Wicket renders an entity page during a full HTTP response with observation active
- **THEN** one `causeway.wicket.page.render` observation covers the page's actual server-side rendering
- **AND** the observation is a descendant of the current request or Causeway interaction span
- **AND** its contextual display name identifies the render operation and a compact form of the logical object type
- **AND** its `causeway.object.type` attribute contains the full canonical logical type

#### Scenario: Observation is inactive
- **WHEN** Wicket renders an entity page without the `observation` profile active
- **THEN** rendering behavior and output remain unchanged
- **AND** no exported Causeway Wicket render span is produced

## ADDED Requirements

### Requirement: Action-prompt render observation
When observation is active, the Wicket viewer SHALL create one `causeway.wicket.action.prompt.render` observation around each actual render callback of an enclosing action-parameter prompt panel.
The observation SHALL use a contextual display name derived from the prompted action's stable logical type and logical member name and SHALL use the existing Wicket render lifecycle infrastructure.

#### Scenario: Parameterized action prompt is rendered
- **WHEN** Wicket renders the enclosing prompt panel for a parameterized action
- **THEN** one `causeway.wicket.action.prompt.render` observation covers the panel's actual render callback and descendant rendering
- **AND** its contextual display name identifies the prompt operation and a compact form of the action's logical member and declaring type
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

### Requirement: Stable fine-grained render display names
The Wicket viewer SHALL retain the existing stable contextual names for fieldset, property, collection, and action-button render observations in this change.
Their canonical layout and metamodel attributes SHALL continue to identify the rendered region.

#### Scenario: Entity member region is rendered
- **WHEN** Wicket renders an observed fieldset, property, collection, or action-button region
- **THEN** its contextual name remains the applicable stable `causeway.wicket.*.render` name
- **AND** its existing canonical region attributes remain available for identification
