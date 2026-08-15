## ADDED Requirements

### Requirement: Semantic domain component vocabulary
The component library SHALL provide framework-neutral custom elements for domain objects, properties, values, action affordances, collections, collection columns, and object links.

#### Scenario: Declarative custom page
- **WHEN** an application composes domain components beneath a Causeway object context using plain HTML
- **THEN** each component resolves its member by semantic identifier and receives data through that context
- **AND** the page does not construct GraphQL documents or generated GraphQL names

### Requirement: Rich-schema-driven member capabilities
Each domain member component SHALL derive its supported read behavior from the introspected rich GraphQL member wrapper and its current instance state from the object context.

#### Scenario: Supported property operations
- **WHEN** a property wrapper exposes hidden, disabled, and get fields
- **THEN** the property component registers only those supported semantic reads needed by its current presentation

#### Scenario: Capability absent from schema
- **WHEN** a requested presentation depends on a field absent from the introspected member wrapper
- **THEN** the component reports that the presentation is unsupported rather than synthesizing the missing semantic

### Requirement: Read-only property semantics
The `<causeway-property>` component SHALL render property visibility, usability, description, null state, value, loading state, and member-scoped errors from its semantic context state.

#### Scenario: Visible property value
- **WHEN** a visible property read completes successfully
- **THEN** the component renders its semantic label and value using the selected value renderer

#### Scenario: Hidden property
- **WHEN** the rich object state reports that a property is hidden
- **THEN** the component omits the property's label, value, and interactive content

#### Scenario: Disabled property
- **WHEN** the rich object state reports that a property is disabled
- **THEN** the component exposes its disabled state and reason accessibly without presenting an edit affordance

#### Scenario: Null property
- **WHEN** the property value is null
- **THEN** the component renders the library's explicit null presentation rather than an unsupported-value error

### Requirement: Extensible value-renderer selection
The component library SHALL select read-only value renderers through an extensible registry using the introspected GraphQL output shape and semantic member descriptor.

#### Scenario: Standard scalar
- **WHEN** a property returns a supported GraphQL scalar
- **THEN** the registry selects the corresponding standard scalar renderer

#### Scenario: Application override
- **WHEN** an application registers a renderer that is more specific than a standard renderer for the same semantic value
- **THEN** the application renderer is selected according to documented deterministic precedence

#### Scenario: Unsupported value shape
- **WHEN** no registered renderer supports an introspected value shape
- **THEN** the component renders an explicit unsupported-value state containing a diagnostic type identifier

### Requirement: Semantic object links
The `<causeway-object-link>` component SHALL render object identity and title from rich object metadata and publish navigation requests as semantic web events.

#### Scenario: User follows an object link
- **WHEN** a user activates an enabled object link
- **THEN** the component emits a bubbling and composed navigation event containing the target logical type name and identifier
- **AND** it does not impose a URL or router implementation

### Requirement: Read-only action affordances
The `<causeway-action>` component SHALL render action visibility and usability and SHALL publish a semantic action request without invoking the action.

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
The `<causeway-collection>` component SHALL load collection contents as a context-coordinated secondary operation only when the collection is activated.

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

### Requirement: Hydrated row object contexts
Object-valued collection rows SHALL create nested object contexts hydrated with the identity, metadata, and selected member data returned by the collection operation.

#### Scenario: Row component uses already selected data
- **WHEN** a descendant of a hydrated row context requests data present in the collection result
- **THEN** the row context serves that data without repeating the object read

#### Scenario: Row component requests additional data
- **WHEN** a descendant requests an object field absent from the hydrated row snapshot
- **THEN** the row context loads only the missing requirement through the shared GraphQL client

### Requirement: Local loading and error presentation
Each domain component SHALL render loading, empty, disabled, unsupported, and member-path error states locally without suppressing successful sibling components.

#### Scenario: One member has a GraphQL error
- **WHEN** the object context associates a partial GraphQL error with one member
- **THEN** that member renders its error presentation
- **AND** sibling members continue to render their successful state

### Requirement: Accessible and styleable output
Domain components SHALL render keyboard-operable, labelled, and state-exposing light-DOM markup with documented semantic host classes and slots.

#### Scenario: Keyboard navigation
- **WHEN** a user navigates object links and action affordances using a keyboard
- **THEN** the controls expose equivalent focus and activation behavior to pointer interaction

#### Scenario: Application styling
- **WHEN** an application applies its design system to semantic component hosts and documented internal classes
- **THEN** the light-DOM output accepts those styles without requiring framework-specific adapters

### Requirement: Framework-neutral read-only composition
The read-only component library SHALL operate without requiring HTMX or another host framework runtime.

#### Scenario: Plain HTML domain summary
- **WHEN** a plain HTML page composes an object context, header, properties, action affordances, and collection
- **THEN** the components discover and render their semantics through the shared GraphQL client
- **AND** all navigation and action requests are available as standard semantic custom events
