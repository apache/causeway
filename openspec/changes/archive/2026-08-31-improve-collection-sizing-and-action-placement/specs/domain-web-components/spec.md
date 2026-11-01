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
- **THEN** the actions are rendered together in declaration order immediately before that collection's primary surface
- **AND** they remain distinct from the top-level action group

### Requirement: Stable associated-action lifecycle
Property and collection components SHALL preserve each declared associated action as one independently connected semantic element across owner rendering and interaction state changes.

#### Scenario: Property presentation changes
- **WHEN** a property moves through loading, ready, editing, validating, failed, cancelled, and reconciled states
- **THEN** its associated action elements retain node identity, declaration order, context generation, and pending action state
- **AND** owner rendering does not clone, serialize, recreate, or issue requests for those actions

#### Scenario: Collection presentation changes
- **WHEN** a collection moves through inactive, loading, ready, empty, error, paging, and refreshed states
- **THEN** its associated action elements remain connected before the collection's stable primary presentation
- **AND** collection-column capture and row rendering neither consume nor duplicate them

#### Scenario: Owner route is replaced
- **WHEN** HTMX or another host disconnects the containing route context
- **THEN** the owner and every associated action disconnect through the existing context lifecycle
- **AND** cancellation and stale-result protection prevent obsolete action state from rendering after replacement

#### Scenario: Owner reconnects
- **WHEN** the same authored composition disconnects and later reconnects
- **THEN** each action reconnects once through its ordinary semantic lifecycle
- **AND** owner declaration capture does not create an additional live action or action request

### Requirement: Accessible responsive association presentation
Directly authored and grid-generated member associations SHALL expose equivalent ordered, keyboard-operable, responsive presentation through documented Causeway hooks and design variables.

#### Scenario: Property-associated actions render at a wide viewport
- **WHEN** one property has one or more visible associated actions
- **THEN** the property presentation appears before its ordered action region
- **AND** controls retain visible labels, focus indicators, and semantic button behavior

#### Scenario: Collection-associated actions render at a wide viewport
- **WHEN** one collection has one or more visible associated actions
- **THEN** one right-aligned ordered action toolbar appears before the collection's primary surface in both visual and sequential keyboard order
- **AND** the toolbar does not overlap the collection heading, description, search, rows, Grid, or paging controls

#### Scenario: Associated actions render at a narrow viewport
- **WHEN** the available inline size cannot contain every associated action on one row
- **THEN** the action region wraps without horizontal page overflow, clipping, overlap, or reordered keyboard focus

#### Scenario: Application styles associated actions
- **WHEN** an application uses documented host classes, `data-causeway-associated-member`, `data-causeway-action-group`, or `--causeway-*` variables
- **THEN** direct and generated compositions expose stable semantic styling hooks
- **AND** application markup requires no inline styles, raw Vaadin elements, or framework-specific adapter API

#### Scenario: Effective grid contains nested actions
- **WHEN** `<cw-object>` renders property- or collection-associated actions from an effective grid
- **THEN** generated composition remains semantically equivalent to the supported direct-child syntax
- **AND** effective-grid parsing and action authority remain unchanged
