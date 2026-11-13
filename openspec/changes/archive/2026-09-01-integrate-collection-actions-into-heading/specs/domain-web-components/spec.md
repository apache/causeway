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

### Requirement: Distinct customizable tooltip presentation
Component-owned action, member, collection, parameter, and disabled-state tooltips SHALL use a shared high-contrast explanatory surface that is visually distinct from adjacent action controls.
The default treatment MUST use a light neutral background, dark text, a visible boundary, and stable `--causeway-*` customization variables without changing tooltip semantics or activation.
Every shared tooltip family MUST open below its semantic trigger.

#### Scenario: Tooltip appears beside a filled action
- **WHEN** pointer or keyboard interaction reveals a tooltip adjacent to a filled action control
- **THEN** the tooltip appears below the control using the light neutral explanatory surface and dark text
- **AND** its border or elevation distinguishes it from both the page and the action control

#### Scenario: Different component tooltip families render
- **WHEN** an action description, member description, collection description, action-parameter description, or disabled reason is presented as a tooltip
- **THEN** each tooltip opens below its trigger and consumes the same background, text, border, and shadow presentation tokens
- **AND** existing content, sections, responsive bounds, pointer access, keyboard access, and accessible associations remain unchanged

#### Scenario: Tooltip appears in a bounded collection header
- **WHEN** an associated action tooltip is revealed in an integrated collection header
- **THEN** it extends below the action into the collection body rather than above the panel boundary
- **AND** the complete tooltip remains visible over following content without changing panel clipping or stacking semantics

#### Scenario: Application customizes tooltip presentation
- **WHEN** an application overrides the documented tooltip variables
- **THEN** component-owned tooltip background, text, border, and shadow presentation use those values
- **AND** no component markup or semantic event customization is required

#### Scenario: Optional theme is absent
- **WHEN** an application installs only the structural component styles
- **THEN** equivalent high-contrast light tooltip fallback values remain effective
- **AND** tooltip content remains readable, visually bounded, and positioned below its trigger
