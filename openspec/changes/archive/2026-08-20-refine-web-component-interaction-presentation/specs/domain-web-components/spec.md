## MODIFIED Requirements

### Requirement: Read-only property semantics
The `<causeway-property>` component SHALL render property visibility, usability, description, null state, value, loading state, and member-scoped errors from its semantic context state.

#### Scenario: Visible property value
- **WHEN** a visible property read completes successfully
- **THEN** the component renders its semantic label and value using the selected value renderer

#### Scenario: Property description is available
- **WHEN** a property descriptor supplies a non-redundant description
- **THEN** the component exposes that description as the property's default explanatory tooltip and accessible description

#### Scenario: Hidden property
- **WHEN** the rich object state reports that a property is hidden
- **THEN** the component omits the property's label, value, and interactive content

#### Scenario: Disabled property
- **WHEN** the rich object state reports that a property is disabled
- **THEN** the component exposes its disabled state without presenting an edit affordance
- **AND** exposes the disabled reason through a separate focusable tooltip indicator and accessible description without replacing the property-description tooltip or requiring a key modifier

#### Scenario: Null property
- **WHEN** the property value is null
- **THEN** the component renders the library's explicit null presentation rather than an unsupported-value error

### Requirement: Extensible semantic editor selection
The component library SHALL select property and action-parameter editors through a deterministic registry based on the introspected input type, semantic member descriptor, and supported structural presentation hints.

#### Scenario: Standard supported input
- **WHEN** a property or parameter uses a supported scalar, enum, object-reference, or choice-based input shape
- **THEN** the registry supplies the corresponding standard editor

#### Scenario: Multiline string property
- **WHEN** a string property's effective-grid reference supplies a valid `multiLine` value greater than one
- **THEN** the registry supplies a textarea editor using the bounded requested row count
- **AND** pending-value parsing and server validation remain identical to an ordinary string editor

#### Scenario: Application editor override
- **WHEN** an application registers a more-specific semantic editor
- **THEN** the application editor receives pending value and interaction state while GraphQL execution remains owned by the context

### Requirement: Causeway grid interpretation
`<causeway-object>` SHALL interpret a documented safe subset of the effective Causeway grid resource for semantic object composition.

#### Scenario: Supported grid is available
- **WHEN** object metadata references an accessible grid containing supported rows, columns, tabs, field sets, domain-object placement, member references, nested associated actions, and bounded property presentation hints
- **THEN** the context retrieves the opaque origin-relative resource using same-origin no-store semantics
- **AND** the component maps it into responsive semantic layout regions

#### Scenario: Associated action is nested beneath a member
- **WHEN** a supported property or collection reference contains action references
- **THEN** the layout plan preserves those actions beneath the owning member
- **AND** deterministic explicit-member allocation prevents the same action from also appearing in an unreferenced action region

#### Scenario: Multiline hint is valid
- **WHEN** a property reference declares a positive integer `multiLine` value within the supported bound
- **THEN** the generated property component receives that normalized row count

#### Scenario: Multiline hint is invalid
- **WHEN** a property reference declares a malformed, non-positive, or excessive `multiLine` value
- **THEN** the parser emits a bounded diagnostic
- **AND** either caps an excessive value or falls back to the single-line editor without rejecting unrelated layout content

#### Scenario: Application forces fallback composition
- **WHEN** `layout-mode="fallback"` is configured
- **THEN** the component uses the canonical fallback plan without dereferencing the effective-grid resource

#### Scenario: Grid contains unsupported content
- **WHEN** one layout region contains an unknown node or unsupported instruction
- **THEN** the component publishes a bounded diagnostic
- **AND** preserves unrelated recognized regions while applying local fallback where possible

#### Scenario: Grid resource is unsafe
- **WHEN** layout XML is oversized, malformed, uses unknown entities, declares a document type or entity, or contains executable content
- **THEN** the parser rejects that content
- **AND** no external entity, response markup, or executable content is processed

### Requirement: Accessible menu disclosure behavior
Menu bars SHALL use labelled navigation landmarks and keyboard-operable native disclosure and action controls with documented traversal, transient closing, sibling coordination, and focus restoration.

#### Scenario: User opens and closes a menu by keyboard
- **WHEN** the user operates a menu disclosure with Enter or Space and later presses Escape
- **THEN** the menu opens and closes without hover dependency
- **AND** focus returns to the originating disclosure button

#### Scenario: User activates a service action
- **WHEN** the user activates an enabled action inside an expanded menu
- **THEN** the semantic action request is published and the containing menu closes
- **AND** prompt cancellation or non-navigation completion restores focus to the visible originating disclosure rather than a hidden action control

#### Scenario: User dismisses a menu externally
- **WHEN** a pointer activation occurs outside an expanded menu bar
- **THEN** that bar closes its expanded menu without moving focus unexpectedly

#### Scenario: User traverses controls
- **WHEN** the user uses Tab, Shift+Tab, Home, End, or documented arrow keys
- **THEN** focus follows native document order or documented peer-disclosure order without becoming trapped
- **AND** opening one menu closes sibling menus in the same bar

#### Scenario: Assistive technology encounters a bar
- **WHEN** a non-empty bar is rendered
- **THEN** it exposes a labelled navigation landmark, native buttons, `aria-expanded`, and `aria-controls`
- **AND** it does not misuse ARIA application-menu roles for ordinary page navigation

## ADDED Requirements

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
- **THEN** the actions are rendered together immediately following that collection rather than in the top-level action group
