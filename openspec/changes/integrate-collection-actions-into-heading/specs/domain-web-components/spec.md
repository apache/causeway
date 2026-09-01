## MODIFIED Requirements

### Requirement: Grouped object action presentation
Object composition SHALL present actions in responsive semantic groups with consistent spacing and SHALL distinguish top-level actions from actions structurally associated with a property or collection.

#### Scenario: Multiple top-level actions are rendered
- **WHEN** an object layout allocates consecutive unassociated actions to a top-level region
- **THEN** the generated action group wraps controls responsively with a visible consistent gap

#### Scenario: Property action is rendered
- **WHEN** an action is nested beneath a property reference in the effective grid
- **THEN** the action is rendered in an associated-action group immediately following that property

#### Scenario: Collection actions are rendered
- **WHEN** one or more actions are nested beneath a collection reference in the effective grid
- **THEN** the actions are generated as direct children of that collection in declaration order
- **AND** they share the collection's heading row rather than joining the top-level action group or occupying a separate toolbar row

### Requirement: Accessible responsive association presentation
Directly authored and grid-generated member associations SHALL expose equivalent ordered, keyboard-operable, responsive presentation through documented Causeway hooks and design variables.

#### Scenario: Property-associated actions render at a wide viewport
- **WHEN** one property has one or more visible associated actions
- **THEN** the property presentation appears before its ordered action region
- **AND** controls retain visible labels, focus indicators, and semantic button behavior

#### Scenario: Collection-associated actions render at a wide viewport
- **WHEN** one collection has one or more visible associated actions and enough inline space
- **THEN** the effective collection heading and one compact right-aligned action toolbar share a single bordered header row
- **AND** visual, DOM, and sequential keyboard order remain heading, actions in declaration order, then collection body controls

#### Scenario: Collection-associated actions render at a narrow viewport
- **WHEN** the collection header cannot safely contain its heading and actions on one line
- **THEN** the heading occupies the first line and the ordered action toolbar wraps beneath it within the same header area
- **AND** the body follows without horizontal page overflow, clipping, overlap, or reordered keyboard focus

#### Scenario: Collection owner is hidden independently of an action
- **WHEN** the collection primary presentation is hidden but an associated action remains independently visible
- **THEN** no promoted collection heading or hidden owner metadata is exposed
- **AND** the visible action retains its existing independent authority and lifecycle

#### Scenario: Application styles associated actions
- **WHEN** an application uses documented host classes, `data-causeway-associated-member`, `data-causeway-action-group`, or `--causeway-*` variables
- **THEN** direct and generated compositions expose stable semantic styling hooks
- **AND** application markup requires no inline styles, raw Vaadin elements, or framework-specific adapter API

#### Scenario: Effective grid contains nested actions
- **WHEN** `<cw-object>` renders property- or collection-associated actions from an effective grid
- **THEN** generated composition remains semantically equivalent to the supported direct-child syntax
- **AND** effective-grid parsing and action authority remain unchanged
