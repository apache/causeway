## ADDED Requirements

### Requirement: Declarative member-associated action composition
The domain components SHALL treat each direct `<causeway-action>` child of `<causeway-property>` or `<causeway-collection>` as an ordered presentation association with that owner member.

#### Scenario: Property declares an associated action
- **WHEN** authored HTML places `<causeway-action member="updateName">` directly beneath `<causeway-property member="name">`
- **THEN** the property renders its primary presentation followed by the `updateName` action in one member composition
- **AND** no adjacent association attribute, wrapper, grid resource, or Java renderer is required

#### Scenario: Collection declares associated actions and columns
- **WHEN** a collection directly contains collection-column and action declarations
- **THEN** column declarations contribute only to row projection and table presentation
- **AND** action declarations contribute only to the associated-action presentation in their source order

#### Scenario: Declaration is not a direct child
- **WHEN** an action is nested inside an arbitrary descendant wrapper rather than directly beneath the property or collection
- **THEN** the owner does not claim it as an associated-action declaration
- **AND** does not infer association from naming, proximity, or descendant scanning

#### Scenario: Parser completes children after owner connection
- **WHEN** the HTML parser or application appends a direct action declaration after the owner custom element has connected
- **THEN** the owner recognizes that direct declaration deterministically
- **AND** does not duplicate or reorder existing declarations

### Requirement: Stable associated-action lifecycle
Property and collection components SHALL preserve each declared associated action as one independently connected semantic element across owner rendering and interaction state changes.

#### Scenario: Property presentation changes
- **WHEN** a property moves through loading, ready, editing, validating, failed, cancelled, and reconciled states
- **THEN** its associated action elements retain node identity, declaration order, context generation, and pending action state
- **AND** owner rendering does not clone, serialize, recreate, or issue requests for those actions

#### Scenario: Collection presentation changes
- **WHEN** a collection moves through inactive, loading, ready, empty, error, paging, and refreshed states
- **THEN** its associated action elements remain connected after the collection's primary presentation
- **AND** collection-column capture and row rendering neither consume nor duplicate them

#### Scenario: Owner route is replaced
- **WHEN** HTMX or another host disconnects the containing route context
- **THEN** the owner and every associated action disconnect through the existing context lifecycle
- **AND** cancellation and stale-result protection prevent obsolete action state from rendering after replacement

#### Scenario: Owner reconnects
- **WHEN** the same authored composition disconnects and later reconnects
- **THEN** each action reconnects once through its ordinary semantic lifecycle
- **AND** owner declaration capture does not create an additional live action or action request

### Requirement: Independent associated-action authority
Association SHALL affect presentation placement only, while every associated action remains governed by its own GraphQL visibility, usability, parameters, validation, invocation, concurrency, and result semantics.

#### Scenario: Owner is hidden but action is visible
- **WHEN** GraphQL reports the property or collection hidden and reports an associated action visible
- **THEN** the owner's label, value, collection content, and owner controls are omitted
- **AND** the independently visible action remains present in the member composition

#### Scenario: Owner is disabled but action is enabled
- **WHEN** GraphQL disables the owner member but enables its associated action
- **THEN** owner edit or activation controls remain disabled according to owner semantics
- **AND** the action remains independently operable

#### Scenario: Action is hidden or disabled
- **WHEN** GraphQL hides or disables an associated action
- **THEN** the action element applies its established hidden or disabled presentation
- **AND** the owner does not override, fabricate, or reinterpret that state

#### Scenario: Associated action is invoked
- **WHEN** a user activates an associated action
- **THEN** its semantic request reaches the existing interaction controller using the nearest object context
- **AND** parameter negotiation, validation, single-shot invocation, results, navigation, announcements, focus restoration, and errors remain the standard action behavior

### Requirement: Accessible responsive association presentation
Directly authored and grid-generated member associations SHALL expose equivalent ordered, keyboard-operable, responsive presentation through documented Causeway hooks and design variables.

#### Scenario: Associated actions render at a wide viewport
- **WHEN** one owner has multiple visible associated actions
- **THEN** the owner presentation appears before one ordered action region
- **AND** controls retain visible labels, focus indicators, and semantic button behavior

#### Scenario: Associated actions render at a narrow viewport
- **WHEN** the available inline size cannot contain every associated action on one row
- **THEN** the action region wraps without horizontal page overflow, clipping, overlap, or reordered keyboard focus

#### Scenario: Application styles associated actions
- **WHEN** an application uses documented host classes, `data-causeway-associated-member`, `data-causeway-action-group`, or `--causeway-*` variables
- **THEN** direct and generated compositions expose stable semantic styling hooks
- **AND** application markup requires no inline styles, raw Vaadin elements, or framework-specific adapter API

#### Scenario: Effective grid contains nested actions
- **WHEN** `<causeway-object>` renders property- or collection-associated actions from an effective grid
- **THEN** generated composition remains semantically equivalent to the supported direct-child syntax
- **AND** effective-grid parsing and action authority remain unchanged

### Requirement: Executable associated-action acceptance
The executable web-component samples SHALL verify natural property- and collection-associated action declarations against real GraphQL interactions and both supported toolkit policies.

#### Scenario: Maintainer inspects Petclinic HTML
- **WHEN** a maintainer opens `petclinic.PetOwner.html`
- **THEN** `updateName` is nested beneath `name`, `addPet` and `removePet` are nested beneath `pets`, and `bookVisit` is nested beneath `visits`
- **AND** no Petclinic-only adjacent association wrapper is needed for those members

#### Scenario: Petclinic associated actions execute
- **WHEN** browser acceptance invokes the nested property and collection actions
- **THEN** prompts, validation, cancellation, scalar results, object results, collection refresh, navigation, history, and focus satisfy the existing semantic contracts
- **AND** each user activation produces at most one action invocation

#### Scenario: Toolkit policy changes
- **WHEN** Petclinic runs once with Vaadin-default policy and once with explicit native policy
- **THEN** the same nested Causeway action declarations remain functional in both modes
- **AND** toolkit selection does not alter association ownership or action authority
