# Domain Web Components Specification

## Purpose

Define framework-neutral semantic web components for composing Causeway domain pages from the rich GraphQL schema, including read-only values, actions, links, collections, semantic property and action interactions, accessibility, and executable vanilla-HTML verification.

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

### Requirement: Semantic object command API
The object context SHALL expose semantic commands for property validation and update, action parameter negotiation and validation, and action invocation without requiring components to construct GraphQL operations.

#### Scenario: Component executes a supported command
- **WHEN** a component submits a semantic command using a member identifier and values
- **THEN** the context resolves the corresponding introspected rich-schema fields, arguments, input types, and operation placement
- **AND** returns a semantic interaction result

#### Scenario: Command is unavailable
- **WHEN** the configured schema or API variant does not expose the capability required by a semantic command
- **THEN** the context returns an unsupported interaction result without issuing an invalid GraphQL operation

### Requirement: Property editing semantics
An editable `<causeway-property>` SHALL provide view, preparing, editing, validating, saving, success, and failed interaction states driven by its rich-schema capabilities.

#### Scenario: User starts editing
- **WHEN** a visible and enabled property advertises a supported update operation and the user activates edit
- **THEN** the component selects an editor from the semantic editor registry
- **AND** lazily obtains the supported editor semantics needed for the current property

#### Scenario: User cancels editing
- **WHEN** the user cancels before a successful update
- **THEN** the component restores its authoritative context value
- **AND** does not execute an update command

#### Scenario: Property is not editable
- **WHEN** the property is hidden, disabled, or lacks an update capability
- **THEN** the component does not expose an enabled edit affordance

### Requirement: Extensible semantic editor selection
The component library SHALL select property and action-parameter editors through a deterministic registry based on the introspected input type and semantic member descriptor.

#### Scenario: Standard supported input
- **WHEN** a property or parameter uses a supported scalar, enum, object-reference, or choice-based input shape
- **THEN** the registry supplies the corresponding standard editor

#### Scenario: Application editor override
- **WHEN** an application registers a more-specific semantic editor
- **THEN** the application editor receives pending value and interaction state while GraphQL execution remains owned by the context

### Requirement: Property choices autocomplete and validation
A property editor SHALL use choices, autocomplete, and validation only when the introspected rich property wrapper advertises the corresponding capability.

#### Scenario: Property has choices
- **WHEN** editing begins for a property whose wrapper exposes choices
- **THEN** the component obtains the current choices through the object context and constrains the standard editor accordingly

#### Scenario: Property supports autocomplete
- **WHEN** the user changes the search text for an autocomplete property
- **THEN** the component starts a debounced cancellable autocomplete command
- **AND** displays only the latest non-obsolete result

#### Scenario: Proposed value is invalid
- **WHEN** rich property validation rejects the pending value
- **THEN** the editor presents the validation reason accessibly
- **AND** prevents saving while that value remains invalid

### Requirement: Property mutation reconciliation
The component library SHALL execute property updates through the semantically appropriate GraphQL mutation capability and reconcile the owning object context after success.

#### Scenario: Property update succeeds
- **WHEN** a valid pending value is saved and the GraphQL update succeeds
- **THEN** the context publishes the successful command result
- **AND** refreshes or fully satisfies its complete active read projection
- **AND** the property returns to view state with authoritative server data

#### Scenario: Property update fails
- **WHEN** the GraphQL update returns an interaction or transport error
- **THEN** the editor remains open with the submitted value and mapped error
- **AND** successful sibling object state remains available

### Requirement: Standard action interaction controller
The library SHALL provide a standard controller that handles unclaimed semantic action-request events and orchestrates parameterless invocation or a parameter prompt.

#### Scenario: Application overrides action handling
- **WHEN** an application claims or cancels an action-request event
- **THEN** the standard controller does not open a prompt or invoke the action

#### Scenario: Parameterless enabled action
- **WHEN** an unclaimed request identifies an enabled action with no parameters
- **THEN** the standard controller asks the object context to invoke the action without opening a parameter form

#### Scenario: Parameterized action
- **WHEN** an unclaimed request identifies an action with parameters
- **THEN** the standard controller opens an accessible prompt built from the introspected action parameter wrappers

### Requirement: Rich action parameter negotiation
The standard action prompt SHALL support parameter hidden state, disabled state, defaults, choices, autocomplete, and validation when those capabilities are present in the rich schema.

#### Scenario: Prompt initializes
- **WHEN** a parameterized action prompt opens
- **THEN** parameters are presented in schema order
- **AND** advertised hidden, disabled, default, and choice semantics are resolved using the current pending preceding arguments

#### Scenario: Earlier parameter changes
- **WHEN** an earlier parameter value changes
- **THEN** later parameter semantics whose GraphQL fields accept preceding arguments are invalidated
- **AND** recomputed using the current pending argument set

#### Scenario: Parameter autocomplete becomes obsolete
- **WHEN** an autocomplete response belongs to an older search or pending-argument generation
- **THEN** the prompt discards that response without replacing newer suggestions

### Requirement: Action argument validation and invocation
The action interaction controller SHALL validate the pending argument set and invoke the action through the semantically correct query or mutation capability exposed by the configured schema.

#### Scenario: Invalid argument set
- **WHEN** rich action validation rejects one or more pending arguments
- **THEN** the prompt presents the mapped validation reasons
- **AND** does not invoke the action

#### Scenario: Safe action invocation
- **WHEN** a valid safe action is submitted
- **THEN** the context executes the action through its advertised safe invocation capability

#### Scenario: Mutating action invocation
- **WHEN** a valid mutating action is submitted and a top-level mutation is available
- **THEN** the context executes that mutation rather than relying on a non-spec-compliant mutating query

### Requirement: Semantic action outcomes
The interaction layer SHALL normalize object, collection, scalar, and void action outcomes and publish them through framework-neutral semantic events.

#### Scenario: Action returns an object
- **WHEN** an invocation returns an object bookmark and metadata
- **THEN** the controller publishes a semantic object result containing that identity
- **AND** host navigation policy decides whether to navigate

#### Scenario: Action returns a scalar or collection
- **WHEN** an invocation returns a scalar or collection
- **THEN** the controller publishes a typed semantic result suitable for host or standard result presentation

#### Scenario: Action returns void
- **WHEN** an invocation succeeds without a value
- **THEN** the controller publishes a successful void result
- **AND** invalidates the owning object context

### Requirement: Interaction concurrency and cancellation
The object context SHALL serialize mutating commands for one object and SHALL prevent obsolete transient semantic responses from replacing newer interaction state.

#### Scenario: Two mutations are submitted
- **WHEN** two mutating commands are submitted concurrently through one object context
- **THEN** the context executes them serially in submission order

#### Scenario: Validation response arrives late
- **WHEN** a validation response completes after its pending value generation has been superseded
- **THEN** the obsolete response is discarded

### Requirement: Local interaction error mapping
Property and action interactions SHALL retain GraphQL validation, member, parameter, invocation, and transport errors at the narrowest corresponding interaction scope.

#### Scenario: Parameter-specific GraphQL error
- **WHEN** an error path identifies one action parameter semantic field
- **THEN** the prompt associates the error with that parameter
- **AND** preserves successful state for other parameters

#### Scenario: Invocation-level error
- **WHEN** an action invocation fails without a more specific member path
- **THEN** the prompt presents the error at action level and remains available for correction or retry

### Requirement: Explicit interaction enablement
The interaction layer SHALL preserve existing read-only rendering unless an application explicitly enables property editing or installs a standard interaction controller.

#### Scenario: Existing read-only composition
- **WHEN** a page uses the established property and action components without enabling interactions
- **THEN** property values remain read-only
- **AND** action affordances continue to publish claimable semantic action-request events without automatic invocation

#### Scenario: Standard interaction layer enabled
- **WHEN** a page enables property interaction and installs the standard action interaction controller
- **THEN** supported enabled properties expose edit affordances
- **AND** unclaimed enabled action requests use the standard invocation or prompt flow

### Requirement: Accessible standard interaction presentation
Standard property editors and the standard action prompt SHALL expose labelled controls, associated validation, pending and error announcements, deterministic focus behavior, keyboard operation, and application styling through light-DOM semantic markup.

#### Scenario: Parameterized prompt opens and closes
- **WHEN** a keyboard user opens a parameterized action prompt
- **THEN** focus moves to the first operable prompt control
- **AND** Escape cancels the prompt without invocation
- **AND** focus returns to the originating action affordance

#### Scenario: Invalid property value
- **WHEN** server validation rejects a pending property value
- **THEN** the editor associates the reason with its input and announces it accessibly
- **AND** retains the pending value for correction or cancellation

### Requirement: Executable vanilla-HTML interaction acceptance composition
The interaction slice SHALL extend the existing `sample-html` application as an executable acceptance fixture using the packaged web-component artifact and the real same-origin rich GraphQL endpoint.

#### Scenario: Existing sample contract remains stable
- **WHEN** the interaction-enabled sample application starts
- **THEN** `/sample-html/index.html`, `/causeway-webcomponents/index.mjs`, `/graphql`, root bookmark `s_sample-1`, established read-only selectors, and `data-state` readiness remain valid
- **AND** the application remains runnable through the `run-sample-html` Maven profile without npm, HTMX, Playwright, or a frontend framework

#### Scenario: Representative property interactions
- **WHEN** the sample reaches `data-state="ready"`
- **THEN** deterministic editable scalar and enum properties support edit, cancel, final validation, successful save, and authoritative context refresh
- **AND** deliberate disabled and hidden members remain unavailable for editing

#### Scenario: Representative action interactions
- **WHEN** an enabled deterministic sample action is selected
- **THEN** the standard controller invokes a parameterless action or presents the parameterized prompt as appropriate
- **AND** safe and mutating operations use their advertised GraphQL placement
- **AND** typed semantic result diagnostics expose representative object, collection, scalar, or void outcomes without automatic navigation

### Requirement: Stable interaction automation contract
The interaction-enabled sample SHALL expose additive stable browser hooks for property editors, action prompts, action outcomes, and semantic interaction diagnostics without depending on incidental generated markup.

#### Scenario: Automation observes property interaction
- **WHEN** automation edits a representative sample property
- **THEN** it can address the editor, validation reason, save, and cancel controls through documented `data-testid` hooks
- **AND** the existing property host selector remains valid

#### Scenario: Automation observes action interaction
- **WHEN** automation opens or submits the standard action interaction flow
- **THEN** it can address the interaction controller, prompt, parameters, submit, cancel, result, and latest interaction event through documented `data-testid` hooks

### Requirement: Automated executable interaction verification
The Maven build SHALL exercise semantic property and action interactions against deterministic fixtures and the running sample's real rich GraphQL endpoint.

#### Scenario: Real property mutation contract
- **WHEN** random-port integration verification validates and updates a deterministic editable property
- **THEN** the advertised rich GraphQL validation and mutation capabilities accept valid input and reject invalid input with mapped reasons
- **AND** a subsequent object read returns the authoritative updated value

#### Scenario: Real action invocation contract
- **WHEN** random-port integration verification negotiates and invokes representative safe and mutating actions
- **THEN** parameter semantics and operation placement match targeted introspection
- **AND** deterministic outcomes and post-command object state are returned without a bad-faith introspection rejection

### Requirement: High-level semantic object component
The component library SHALL provide `<causeway-object>` as a framework-neutral high-level projection that composes a complete object from established semantic child components.

#### Scenario: Object component connects
- **WHEN** `<causeway-object>` connects beneath an authoritative object context
- **THEN** it discovers the logical type's members through the context's targeted schema description
- **AND** composes the object's supported layout without constructing GraphQL documents itself
- **AND** only generated properties are affected by the component's optional `editable` attribute

#### Scenario: Object context is absent
- **WHEN** the component has no usable object context
- **THEN** it presents a local actionable configuration error
- **AND** does not create an unrelated second state owner

### Requirement: Causeway grid interpretation
`<causeway-object>` SHALL interpret a documented safe subset of the effective Causeway grid resource for semantic object composition.

#### Scenario: Supported grid is available
- **WHEN** object metadata references an accessible grid containing supported rows, columns, tabs, field sets, domain-object placement, and member references
- **THEN** the context retrieves the opaque origin-relative resource using same-origin no-store semantics
- **AND** the component maps it into responsive semantic layout regions

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

### Requirement: Canonical fallback object layout
The component SHALL provide deterministic fallback composition modeled on Causeway's `GridFallbackLayout.xml` when no usable effective grid is available.

#### Scenario: No usable grid exists
- **WHEN** the grid is absent, forbidden, unreachable, or wholly malformed
- **THEN** the component renders header and actions followed by conventional property field-set and collection regions
- **AND** uses framework-neutral CSS Grid rather than requiring Bootstrap

#### Scenario: Conventional region is empty
- **WHEN** no visible member belongs to a fallback field set or collection region
- **THEN** the empty region collapses without leaving an unlabelled interactive container

### Requirement: Decomposition into established components
The object component SHALL generate existing semantic header, property, action, and collection elements rather than duplicate their runtime behavior.

#### Scenario: Layout references a property
- **WHEN** a valid property reference is placed
- **THEN** the generated `<causeway-property>` uses the same shared object context, editing, validation, mutation, loading, and error contracts as an authored property element

#### Scenario: Layout references an action or collection
- **WHEN** a valid action or collection reference is placed
- **THEN** the generated established component retains its standard hidden, disabled, interaction, result, lazy-read, and error semantics

### Requirement: Deterministic member allocation
Every introspected member claimed by automatic composition SHALL be placed at most once according to explicit references, unreferenced markers, and fallback policy.

#### Scenario: Explicit member is also eligible as unreferenced
- **WHEN** a member has already been claimed by an explicit layout reference
- **THEN** an unreferenced marker does not place it again

#### Scenario: Member is intentionally omitted
- **WHEN** a usable explicit grid neither references a member nor provides the corresponding unreferenced marker
- **THEN** automatic composition does not invent placement for that member

#### Scenario: Layout reference is stale or wrong-kind
- **WHEN** the layout names a missing member or uses a member in an incompatible role
- **THEN** the component records a diagnostic
- **AND** does not create a broken or duplicate semantic child

### Requirement: Accessible responsive object composition
Generated object layout SHALL provide accessible groups and tabs and SHALL adapt to narrow viewports without changing semantic member behavior.

#### Scenario: User operates a tab group by keyboard
- **WHEN** focus is within generated tabs
- **THEN** documented arrow, Home, End, Enter, and Space behavior updates the selected tab and associated panel accessibly

#### Scenario: Viewport becomes narrow
- **WHEN** the available width crosses the documented narrow-layout threshold
- **THEN** columns stack in meaningful document order
- **AND** controls retain visible focus and keyboard operation

### Requirement: Observable and customizable composition
Generated light DOM SHALL expose documented stable styling, region, lifecycle, and diagnostic hooks while preserving explicit low-level composition as an alternative.

#### Scenario: Application themes generated layout
- **WHEN** an application supplies supported CSS variables and selectors
- **THEN** it can style regions and established child components without replacing object semantics

#### Scenario: Layout state changes
- **WHEN** effective-grid loading, fallback, successful planning, or bounded diagnostics occur
- **THEN** documented `data-layout-state`, layout state events, and redacted diagnostic events expose the transition
- **AND** an explicit `refreshLayout()` can re-evaluate locale-sensitive or authorization-sensitive layout context

#### Scenario: Application needs custom structure
- **WHEN** automatic layout is not suitable
- **THEN** the application can continue composing established lower-level components directly beneath the same object context

### Requirement: Causeway menu-bar component vocabulary
The component library SHALL provide `<causeway-menubars>`, `<causeway-menubar-primary>`, `<causeway-menubar-secondary>`, and `<causeway-menubar-tertiary>` as framework-neutral semantic application-menu components.

#### Scenario: Composite menu bars connect
- **WHEN** `<causeway-menubars>` connects beneath a configured GraphQL client
- **THEN** it discovers the optional application menu capability through targeted introspection
- **AND** coordinates present primary, secondary, and tertiary bar components in semantic order

#### Scenario: One bar is used independently
- **WHEN** an application uses a primary, secondary, or tertiary bar without the composite
- **THEN** that component can obtain the same authorized effective resource and render only its semantic bar
- **AND** does not require a public application-context element

### Requirement: Generation-scoped application-menu coordination
The composite SHALL own one bounded application-menu generation shared by its bars without caching effective content across users, client instances, or explicit refresh generations.

#### Scenario: Composite loads its current generation
- **WHEN** application metadata exposes a safe effective menu resource descriptor
- **THEN** the coordinator reads that metadata once, fetches the opaque resource once with same-origin no-store semantics, and shares one immutable plan with child bars

#### Scenario: Application menu capability is unavailable
- **WHEN** targeted introspection shows that `application` or `menuBars` is absent
- **THEN** the component reports a local bounded unsupported state
- **AND** does not issue an invalid GraphQL operation or invent a client-side menu hierarchy

#### Scenario: Application refreshes menu state
- **WHEN** the application calls `refresh()`
- **THEN** a new generation re-evaluates application metadata, resource content, and current service-action state
- **AND** superseded metadata, resource, and action-state responses cannot replace the newer generation

#### Scenario: Multiple contexts start against a cold GraphQL viewer
- **WHEN** object and menu contexts issue concurrent initial requests before the GraphQL execution source exists
- **THEN** the viewer initializes one complete execution source without corrupting shared schema registries
- **AND** this initialization guard does not introduce parallel data-fetcher or mutation execution

### Requirement: Declarative and generated bar composition
The composite SHALL preserve declaratively supplied semantic bar children and generate only missing effective non-empty bar roles.

#### Scenario: Children exist before custom-element upgrade
- **WHEN** declarative primary or tertiary children are parsed before registration
- **THEN** the composite captures and reuses them after upgrade
- **AND** does not generate duplicate bars for those roles

#### Scenario: Effective bar is absent
- **WHEN** the effective menu resource contains no visible entries for a bar
- **THEN** the composite does not generate a missing child for that role
- **AND** a declarative child for that role exposes no empty interactive landmark

### Requirement: Secure effective menu structure rendering
Each bar SHALL use shared bounded structural-resource and XML safety rules to preserve the effective ordered menus, sections, service-action references, labels, descriptions, icon hints, and supported presentation data.

#### Scenario: Menu resource is parsed
- **WHEN** a bar receives an authorized menu resource in the documented Causeway namespace
- **THEN** parsing rejects document types, entities, executable markup, cross-origin expansion, malformed nesting, and configured size or complexity limit violations
- **AND** arbitrary response markup is never inserted into the component DOM

#### Scenario: Bar contains multiple menus and sections
- **WHEN** a bar is rendered
- **THEN** menus, sections, and visible service actions follow effective Causeway document order
- **AND** optional presentation metadata is exposed only as text-safe documented light-DOM hooks

#### Scenario: Menu content is partially unsupported
- **WHEN** one local node or action reference is unknown, malformed, stale, or wrong-kind
- **THEN** the bar records a bounded redacted local diagnostic
- **AND** retains unrelated recognized menus, sections, and actions

### Requirement: Coordinated service-action state
Menu components SHALL resolve current hidden and disabled state through established rich service-action wrappers while coordinating reads by logical service type.

#### Scenario: Current menu action state is loaded
- **WHEN** one generation contains actions from one or more logical service types
- **THEN** targeted schema descriptions are cached by the GraphQL client
- **AND** hidden and disabled state reads are grouped by logical service type rather than issued once per action

#### Scenario: Action is hidden
- **WHEN** a canonical service-action wrapper reports hidden for the current interaction
- **THEN** no visible menu entry, label, description, icon hint, or authorization metadata for that action is rendered

#### Scenario: Action is disabled
- **WHEN** a service action is visible but disabled
- **THEN** its entry remains represented and counted as visible
- **AND** it is non-invokable and exposes its established disabled reason accessibly where available

#### Scenario: Group becomes empty
- **WHEN** current state removes every visible action from a section, menu, or bar
- **THEN** empty sections, menus, and bars collapse in that order without an empty interactive landmark

### Requirement: Service-action interaction reuse
Menu entries SHALL reuse established semantic parameter, editor, choices, autocomplete, validation, invocation, cancellation, stale-response, mutation-serialization, and typed-result behavior for rich service actions.

#### Scenario: Service action requires parameters
- **WHEN** a user activates a parameterized menu action
- **THEN** the standard accessible interaction presentation negotiates and validates parameters through a service-bound adapter
- **AND** invokes the advertised rich service-action operation with typed values and no manufactured object target

#### Scenario: Service action is mutating
- **WHEN** a mutating service action is submitted through one application-menu coordinator
- **THEN** it uses the existing top-level mutation field
- **AND** mutating submissions are serialized in submission order

#### Scenario: Service action returns a result
- **WHEN** invocation returns scalar, object, collection, or void semantics
- **THEN** the menu component publishes the established semantic result event and typed result
- **AND** additive target detail identifies the public service logical type without pretending it is an object bookmark

### Requirement: Accessible menu disclosure behavior
Menu bars SHALL use labelled navigation landmarks and keyboard-operable native disclosure and action controls with documented traversal, closing, sibling coordination, and focus restoration.

#### Scenario: User opens and closes a menu by keyboard
- **WHEN** the user operates a menu disclosure with Enter or Space and later presses Escape
- **THEN** the menu opens and closes without hover dependency
- **AND** focus returns to the originating disclosure button

#### Scenario: User traverses controls
- **WHEN** the user uses Tab, Shift+Tab, Home, End, or documented arrow keys
- **THEN** focus follows native document order or documented peer-disclosure order without becoming trapped
- **AND** opening one menu closes sibling menus in the same bar

#### Scenario: Assistive technology encounters a bar
- **WHEN** a non-empty bar is rendered
- **THEN** it exposes a labelled navigation landmark, native buttons, `aria-expanded`, and `aria-controls`
- **AND** it does not misuse ARIA application-menu roles for ordinary page navigation

### Requirement: Responsive semantic menu bars
Menu bars SHALL adapt to wide and narrow layouts without changing semantic bar, menu, section, action order, interaction state, or event contracts.

#### Scenario: Narrow layout is active
- **WHEN** available width crosses the documented narrow threshold
- **THEN** non-empty bars and menus use accessible disclosures in unchanged document order
- **AND** every visible action remains keyboard operable with visible focus and no horizontal page overflow

### Requirement: Observable and customizable menu composition
Generated light DOM SHALL expose stable bar, menu, section, action, state, lifecycle, diagnostic, data-attribute, CSS-variable, and styling hooks without exposing sensitive remote content.

#### Scenario: Application themes menu bars
- **WHEN** an application supplies documented CSS variables and selectors
- **THEN** it can style primary, secondary, tertiary, menu, section, and action regions without replacing semantic behavior

#### Scenario: Partial menu error occurs
- **WHEN** a capability, resource, reference, or service-state operation fails locally
- **THEN** bounded lifecycle and diagnostic events identify the safe failure scope
- **AND** diagnostics omit response bodies, credentials, authorization rules, submitted values, and remote exception text

### Requirement: Host-controlled menu policy
Menu components SHALL leave routes, browser history, authentication chrome, automatic home-page behavior, shell-closing behavior, and action-result navigation or presentation to the host.

#### Scenario: Host receives a service object result
- **WHEN** a service action publishes a semantic object result
- **THEN** the host may navigate, render it, close a shell menu, or do nothing according to policy
- **AND** the menu component does not assume HTMX, a canonical route, or an automatic home action
