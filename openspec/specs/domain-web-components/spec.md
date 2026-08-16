# Domain Web Components Specification

## Purpose

Define framework-neutral semantic web components for composing read-only Causeway domain pages from the rich GraphQL schema, including values, actions, links, collections, accessibility, and executable vanilla-HTML verification.

## Requirements

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

### Requirement: Executable vanilla-HTML acceptance composition
The read-only component slice SHALL extend the existing `sample-html` application as an executable acceptance fixture using the packaged web-component artifact and the real same-origin rich GraphQL endpoint.

#### Scenario: Existing sample contract remains stable
- **WHEN** the extended sample application is started
- **THEN** `/sample-html/index.html`, `/causeway-webcomponents/index.mjs`, and `/graphql` remain available from the same origin
- **AND** the root logical type, bookmark `s_sample-1`, existing `data-testid` selectors, and `data-state` readiness contract remain valid
- **AND** the page requires no npm build, HTMX, Playwright, or frontend-framework runtime

#### Scenario: Complete read-only custom page
- **WHEN** the sample page reaches `data-state="ready"`
- **THEN** it composes the object header, text, numeric, boolean, and enum values, null presentation, object link, action affordances, populated collection with declared columns, and empty collection through the public semantic components
- **AND** the page groups those components into labelled object-summary, property, action, collection, and event-diagnostic sections
- **AND** hidden members render no semantic content while disabled members expose their reasons accessibly

### Requirement: Deterministic representative sample domain
The executable sample SHALL provide stable domain data covering the representative rich-schema semantics required by the read-only components.

#### Scenario: Representative root members
- **WHEN** the root object `causeway.webcomponents.sample.SampleObject:s_sample-1` is read
- **THEN** its existing `name`, `code`, and `secret` semantics remain deterministic
- **AND** `summary` provides representative text, `capacity` provides a numeric value, and `featured` provides a boolean value
- **AND** `status` provides an enum value, `notes` is null, and `relatedObject` identifies a stable related object
- **AND** ordinary properties are enabled while `code`, `relatedObject`, and `archive` retain deterministic disabled semantics and reasons
- **AND** `inspect` is a visible enabled action, `archive` is a visible disabled action with a reason, and `hiddenAction` is hidden

#### Scenario: Representative collections
- **WHEN** the sample's `relatedObjects` collection is activated
- **THEN** it returns stable related objects with deterministic `name` and `code` column values
- **AND** their returned metadata and selected columns can hydrate row object contexts
- **WHEN** the sample's `emptyRelatedObjects` collection is activated
- **THEN** it returns the accessible empty state

### Requirement: Expanded browser-automation contract
The extended sample SHALL expose additive stable browser hooks for observing the new read-only components and their semantic events without depending on incidental generated markup.

#### Scenario: Stable read-only selectors
- **WHEN** automation inspects the extended sample page
- **THEN** it can address `property-summary`, `property-capacity`, `property-featured`, `property-status`, `property-notes`, `property-related-object`, `object-link-related-object`, `action-inspect`, `action-archive`, `action-hidden`, `collection-related-objects`, `collection-empty-related-objects`, `column-related-name`, and `column-related-code` through `data-testid`
- **AND** it can address `section-object-summary`, `section-properties`, `section-actions`, `section-collections`, `section-events`, `sample-coverage`, and `collection-related-count` without depending on incidental generated markup
- **AND** all foundation selectors remain available

#### Scenario: Observable semantic requests
- **WHEN** the sample receives a navigation or action-request event from an enabled component
- **THEN** plain application JavaScript reports its semantic payload through `[data-testid="sample-event"]`
- **AND** the sample does not impose routing, prompt, or invocation behavior

### Requirement: Illustrative vanilla-HTML reference presentation
The executable sample SHALL present the deterministic component composition as an understandable, responsive, and accessible reference page without becoming a generic viewer.

#### Scenario: Structured reference showcase
- **WHEN** a user opens the sample page
- **THEN** the object summary, representative properties, action affordances, collections, and diagnostics appear in clearly labelled visual sections
- **AND** page-specific explanatory copy distinguishes enabled, disabled, null, reference, empty, and hidden coverage
- **AND** every domain value and member state continues to be rendered by the public semantic components

#### Scenario: Hidden-state coverage remains understandable
- **WHEN** the secret property and hidden action omit their semantic content
- **THEN** a static coverage guide explains that hidden members are intentionally absent
- **AND** the guide does not reveal, reproduce, or synthesize the hidden property value

#### Scenario: Responsive accessible application theme
- **WHEN** the sample is viewed with a narrow or wide viewport and a light or dark color scheme
- **THEN** its cards, typography, controls, diagnostics, and collection table remain readable and keyboard operable
- **AND** the page passes the configured accessibility contrast and semantic-markup checks

#### Scenario: Visible collection and event diagnostics
- **WHEN** the populated collection finishes loading
- **THEN** the page reports its deterministic row count through `[data-testid="collection-related-count"]`
- **WHEN** an enabled navigation or action request is published
- **THEN** the diagnostics section makes the latest semantic payload visibly identifiable through the existing `[data-testid="sample-event"]` hook

### Requirement: Automated executable-sample verification
The Maven build SHALL continue to exercise the expanded sample application against its packaged page, modules, deterministic data, and real rich GraphQL endpoint.

#### Scenario: Real read-only GraphQL contract
- **WHEN** random-port integration verification calls the running `/graphql` endpoint
- **THEN** GraphQL Java-compatible targeted introspection resolves the root and reachable read-only wrapper types without a bad-faith introspection rejection
- **AND** rich object and collection operations return the deterministic enum, null, reference, action-state, collection, row, and empty-collection semantics

#### Scenario: Real-browser readiness smoke check
- **WHEN** the packaged sample page is loaded in a real browser during final verification
- **THEN** it reaches `data-state="ready"`, renders the representative visible and disabled states, suppresses hidden semantic content, displays the sectioned reference presentation, and publishes semantic event and collection diagnostics
- **AND** its GraphQL requests succeed without browser console errors
- **AND** automated accessibility auditing reports no configured accessibility failures
