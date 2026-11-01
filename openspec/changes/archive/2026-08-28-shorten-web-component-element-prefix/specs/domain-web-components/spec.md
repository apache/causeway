## ADDED Requirements

### Requirement: Compact custom-element namespace
The component library SHALL register every Causeway-owned custom element exclusively under the `cw-` prefix.
It MUST retain existing `causeway-*` semantic event names, CSS classes, data attributes, CSS variables, and asset paths because those contracts are not custom-element names.

#### Scenario: Components are registered
- **WHEN** the foundation registration module initializes against an empty custom-element registry
- **THEN** all documented `cw-*` elements are registered with their established constructors
- **AND** no former `causeway-*` custom-element name is registered

#### Scenario: Application markup is migrated
- **WHEN** application HTML replaces each former Causeway custom-element tag with its documented `cw-*` equivalent
- **THEN** context discovery, rendering, interaction, navigation, menus, collections, and editors retain their existing behavior
- **AND** no compatibility alias or mixed element vocabulary is required

#### Scenario: Non-element contracts are consumed
- **WHEN** an application listens for a Causeway semantic event or uses a documented Causeway class, data attribute, CSS variable, or asset path
- **THEN** that non-element contract retains its existing `causeway-*` spelling

## MODIFIED Requirements

### Requirement: Read-only property semantics
The `<cw-property>` component SHALL render property visibility, usability, description, null state, value, loading state, member-scoped errors, and semantic multiline presentation from its context state and public attributes.
A standard GraphQL `String` property value MUST be explicitly aligned to the logical start so its view and edit presentations remain consistent.

#### Scenario: Visible property value
- **WHEN** a visible property read completes successfully
- **THEN** the component renders its semantic label and value using the selected value renderer

#### Scenario: String property value
- **WHEN** a visible property has the standard GraphQL `String` output type
- **THEN** the component marks its output with the semantic string-value presentation class
- **AND** baseline and cohesive-theme presentation align the value to the logical start

#### Scenario: Property description is available
- **WHEN** a property descriptor supplies a non-redundant description
- **THEN** the component exposes that description as the property's default explanatory tooltip and accessible description

#### Scenario: Described multiline property renders at wide width
- **WHEN** a read-only multiline property has a visible value, description, and built-in edit affordance at a wide layout width
- **THEN** the value remains in the normal value column beside the label
- **AND** the description appears beneath the label in the label column
- **AND** the edit affordance remains a compact content-sized control rather than stretching across the value column

#### Scenario: Described multiline property renders at narrow width
- **WHEN** the available inline size requires a multiline property to collapse
- **THEN** its label, description, value, and edit affordance remain readable in logical order
- **AND** the presentation introduces no horizontal page overflow, clipping, or overlap

#### Scenario: Hidden property
- **WHEN** the rich object state reports that a property is hidden
- **THEN** the component omits the property's label, value, and interactive content

#### Scenario: Disabled property
- **WHEN** the rich object state reports that a property is disabled with a bounded reason
- **THEN** the component exposes its disabled state without presenting an edit affordance
- **AND** attaches the disabled-reason tooltip to the property label rather than rendering a separate information indicator
- **AND** makes the label tooltip available by pointer hover and ordinary keyboard focus
- **AND** retains the disabled reason as an accessible description without replacing a distinct property-description tooltip

#### Scenario: Null property
- **WHEN** the property value is null
- **THEN** the component renders the library's explicit null presentation rather than an unsupported-value error

### Requirement: Semantic object links
The `<cw-object-link>` component SHALL render object identity and title from rich object metadata and publish navigation requests as semantic web events.

#### Scenario: User follows an object link
- **WHEN** a user activates an enabled object link
- **THEN** the component emits a bubbling and composed navigation event containing the target logical type name and identifier
- **AND** it does not impose a URL or router implementation

### Requirement: Read-only action affordances
The `<cw-action>` component SHALL render action visibility and usability and SHALL publish a semantic action request without invoking the action.

#### Scenario: Enabled action selected
- **WHEN** a user activates a visible and enabled action affordance
- **THEN** the component emits a bubbling and composed action-request event containing the semantic action identifier and object context

#### Scenario: Disabled action selected
- **WHEN** an action is disabled
- **THEN** the component prevents activation and exposes the disabled reason accessibly

#### Scenario: Hidden action
- **WHEN** an action is hidden
- **THEN** the component does not render an actionable affordance

### Requirement: Lazy read-only collections
The `<cw-collection>` component SHALL load collection contents as a context-coordinated secondary operation only when the collection is activated.

#### Scenario: Collection not activated
- **WHEN** a collection component is connected in an inactive region
- **THEN** it may register collection visibility and usability
- **AND** it does not request collection contents

#### Scenario: Default object-row presentation
- **WHEN** an activated object-valued collection has no declared columns
- **THEN** it requests each row's identity and title metadata
- **AND** renders each row as a semantic object link

#### Scenario: Declarative columns
- **WHEN** a collection declares semantic column components
- **THEN** their row-member requirements are merged into the collection operation
- **AND** returned values are rendered using the standard property and value semantics

#### Scenario: Empty collection
- **WHEN** an activated collection returns no rows
- **THEN** the component renders its accessible empty state

### Requirement: Property editing semantics
An editable `<cw-property>` SHALL provide view, preparing, editing, validating, saving, success, and failed interaction states driven by its rich-schema capabilities.
Its view-state edit affordance MUST be a compact conventional icon control adjacent to the property value with a property-specific accessible name and pointer description.
Its active editor MUST present Save and Cancel as compact conventional icon controls with property-specific accessible names and pointer descriptions while preserving native button, gating, keyboard, and focus behavior.
During stable ordinary editing, the component MUST communicate edit mode through the focused editor and its controls without rendering a redundant “Editing” status label or empty status row.
Meaningful preparing, validating, saving, correction-required, and unsupported states SHALL retain their bounded status presentation.

#### Scenario: User starts editing
- **WHEN** a visible and enabled property advertises a supported update operation and the user activates edit
- **THEN** the component selects an editor from the semantic editor registry
- **AND** lazily obtains the supported editor semantics needed for the current property
- **AND** focuses and presents the editor with compact Save and Cancel icon controls without a separate “Editing” status label

#### Scenario: Editable property is presented
- **WHEN** a visible and enabled property can offer editing in view state
- **THEN** the component presents a compact pencil icon control adjacent to the property value
- **AND** the control's accessible name and pointer description identify the property that will be edited
- **AND** the decorative icon is not a separate focus or accessibility target

#### Scenario: Property editor actions are presented
- **WHEN** a property is in active edit mode
- **THEN** Save is presented as a compact tick icon control and Cancel as a compact cross icon control
- **AND** each control's accessible name and pointer description identify both its action and property
- **AND** each decorative icon is outside the focus and accessibility trees
- **AND** Save retains validation gating and primary treatment while Cancel retains cancellation and focus behavior

#### Scenario: User cancels editing
- **WHEN** the user cancels before a successful update
- **THEN** the component restores its authoritative context value
- **AND** does not execute an update command

#### Scenario: Property is not editable
- **WHEN** the property is hidden, disabled, or lacks an update capability
- **THEN** the component does not expose an enabled edit affordance

#### Scenario: Property edit changes state
- **WHEN** an active property editor is preparing, validating, saving, failed, or unsupported
- **THEN** the component presents the corresponding bounded meaningful status
- **AND** does not replace that status with an ordinary “Editing” label

### Requirement: High-level semantic object component
The component library SHALL provide `<cw-object>` as a framework-neutral high-level projection that composes a complete object from established semantic child components.

#### Scenario: Object component connects
- **WHEN** `<cw-object>` connects beneath an authoritative object context
- **THEN** it discovers the logical type's members through the context's targeted schema description
- **AND** composes the object's supported layout without constructing GraphQL documents itself
- **AND** only generated properties are affected by the component's optional `editable` attribute

#### Scenario: Object context is absent
- **WHEN** the component has no usable object context
- **THEN** it presents a local actionable configuration error
- **AND** does not create an unrelated second state owner

### Requirement: Causeway grid interpretation
`<cw-object>` SHALL interpret a documented safe subset of the effective Causeway grid resource for semantic object composition.

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

### Requirement: Decomposition into established components
The object component SHALL generate existing semantic header, property, action, and collection elements rather than duplicate their runtime behavior.

#### Scenario: Layout references a property
- **WHEN** a valid property reference is placed
- **THEN** the generated `<cw-property>` uses the same shared object context, editing, validation, mutation, loading, and error contracts as an authored property element

#### Scenario: Layout references an action or collection
- **WHEN** a valid action or collection reference is placed
- **THEN** the generated established component retains its standard hidden, disabled, interaction, result, lazy-read, and error semantics

### Requirement: Causeway menu-bar component vocabulary
The component library SHALL provide `<cw-menubars>`, `<cw-menubar-primary>`, `<cw-menubar-secondary>`, and `<cw-menubar-tertiary>` as framework-neutral semantic application-menu components.

#### Scenario: Composite menu bars connect
- **WHEN** `<cw-menubars>` connects beneath a configured GraphQL client
- **THEN** it discovers the optional application menu capability through targeted introspection
- **AND** coordinates present primary, secondary, and tertiary bar components in semantic order

#### Scenario: One bar is used independently
- **WHEN** an application uses a primary, secondary, or tertiary bar without the composite
- **THEN** that component can obtain the same authorized effective resource and render only its semantic bar
- **AND** does not require a public application-context element

### Requirement: Declarative member-associated action composition
The domain components SHALL treat each direct `<cw-action>` child of `<cw-property>` or `<cw-collection>` as an ordered presentation association with that owner member.

#### Scenario: Property declares an associated action
- **WHEN** authored HTML places `<cw-action member="updateName">` directly beneath `<cw-property member="name">`
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
- **WHEN** `<cw-object>` renders property- or collection-associated actions from an effective grid
- **THEN** generated composition remains semantically equivalent to the supported direct-child syntax
- **AND** effective-grid parsing and action authority remain unchanged
