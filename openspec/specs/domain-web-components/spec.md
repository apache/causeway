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
The standard action prompt SHALL support parameter hidden state, disabled state, defaults, choices, autocomplete, validation, and focus-completion timing when those capabilities are present in the rich schema.
Initial structural preparation SHALL remain GraphQL-authoritative, while parameter validity presentation and dependent-value recomputation SHALL be deferred until focus leaves the actively edited field or the user invokes the action.

#### Scenario: Prompt initializes
- **WHEN** a parameterized action prompt opens
- **THEN** parameters are presented in schema order
- **AND** advertised hidden, disabled, default, and choice semantics are resolved using the current pending preceding arguments
- **AND** untouched parameter validity reasons are not presented as errors

#### Scenario: User edits the focused parameter
- **WHEN** input or change events update the parameter that still owns focus
- **THEN** the controller retains the latest codec-produced pending value
- **AND** does not request parameter-state recomputation or present that parameter's validity reason while editing continues
- **AND** autocomplete search may continue through its independently advertised capability

#### Scenario: Edited parameter loses focus
- **WHEN** focus leaves an edited action parameter for another prompt control
- **THEN** the controller recomputes authoritative parameter state using the current pending argument set
- **AND** presents any mapped validity reason for that completed parameter
- **AND** retains focus on the newly selected control

#### Scenario: Earlier parameter changes
- **WHEN** an earlier parameter value is completed by focus departure
- **THEN** later parameter semantics whose GraphQL fields accept preceding arguments are invalidated
- **AND** recomputed using the current pending argument set

#### Scenario: Invoke occurs before focus departure
- **WHEN** the user invokes an action while the current parameter has not otherwise lost focus
- **THEN** the controller validates the complete latest pending argument set
- **AND** reveals applicable mapped parameter reasons before blocking invalid invocation
- **AND** invokes only when authoritative whole-action validation succeeds

#### Scenario: Parameter autocomplete becomes obsolete
- **WHEN** an autocomplete response belongs to an older search or pending-argument generation
- **THEN** the prompt discards that response without replacing newer suggestions

### Requirement: Action argument validation and invocation
The action interaction controller SHALL validate the pending argument set and invoke an object or service action through the semantically correct nested query or top-level mutation capability exposed by targeted introspection.
The context MUST emit only arguments declared by the selected field, MUST supply object target identity only through an advertised target argument, and MUST NOT issue an invalid operation when no executable capability exists.

#### Scenario: Invalid argument set
- **WHEN** rich action validation rejects one or more pending arguments
- **THEN** the prompt presents the mapped validation reasons
- **AND** does not invoke the action

#### Scenario: Parameterless nested service action
- **WHEN** a parameterless service action advertises a nested safe or idempotent invocation field
- **THEN** the service context executes that field beneath the advertised service query path without manufacturing variables or an object target
- **AND** publishes the resulting semantic outcome instead of `Action invocation failed`

#### Scenario: Parameterized nested object action
- **WHEN** a valid object action advertises a nested safe or idempotent invocation field with declared parameters
- **THEN** the object context sends the codec-produced pending values only through matching declared arguments
- **AND** preserves the current object identity through the enclosing object lookup

#### Scenario: Mutating object action has a top-level mutation
- **WHEN** a valid mutating object action advertises a generated top-level mutation field
- **THEN** the context executes that mutation and supplies the current bookmark through its declared target argument
- **AND** does not select a legacy nested mutating query merely because `invokeNonIdempotent` is also advertised

#### Scenario: Mutating service action has a top-level mutation
- **WHEN** a valid mutating service action advertises a generated top-level mutation field
- **THEN** the service context executes that mutation with only its declared action arguments
- **AND** does not manufacture an object target

#### Scenario: Only a legacy mutating invocation is available
- **WHEN** a supported API variant exposes `invokeNonIdempotent` but no corresponding top-level mutation
- **THEN** the context may use the legacy nested capability as an explicit compatibility fallback
- **AND** reports the selected placement through its inspectable operation result

#### Scenario: Advertised action has no executable placement
- **WHEN** targeted introspection exposes action state or parameter fields but no supported invocation field or mutation
- **THEN** preparation or invocation returns a bounded unsupported interaction result
- **AND** no invalid GraphQL request is issued

### Requirement: Semantic action outcomes
The interaction layer SHALL derive action result selection and extraction from the advertised output type and SHALL normalize object, collection, scalar, and void outcomes through framework-neutral semantic events.
It MUST NOT request a synthetic `results`, `target`, metadata, or child field that the effective result type does not advertise.

#### Scenario: Invocation uses a results envelope
- **WHEN** an advertised nested invocation output contains a `results` field
- **THEN** the context selects and extracts that field using its described type
- **AND** normalizes the extracted value through the semantic result contract

#### Scenario: Invocation returns a direct value
- **WHEN** an advertised invocation or mutation returns a scalar, enum, object, collection, or void value directly
- **THEN** the context selects only children valid for that direct type and extracts the selected field value without assuming an envelope
- **AND** preserves the documented scalar, object, collection, or void result kind

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
- **AND** invalidates the owning object context when applicable

#### Scenario: Result type lacks navigable metadata
- **WHEN** a valid direct or polymorphic result can currently expose only a bounded non-navigable projection
- **THEN** invocation success and the available typed result remain visible
- **AND** the controller does not invent an object bookmark or claim navigable identity

### Requirement: Interaction concurrency and cancellation
The object context SHALL serialize mutating commands for one object and SHALL prevent obsolete transient semantic responses from replacing newer interaction state.

#### Scenario: Two mutations are submitted
- **WHEN** two mutating commands are submitted concurrently through one object context
- **THEN** the context executes them serially in submission order

#### Scenario: Validation response arrives late
- **WHEN** a validation response completes after its pending value generation has been superseded
- **THEN** the obsolete response is discarded

### Requirement: Local interaction error mapping
Property and action interactions SHALL retain GraphQL validation, member, parameter, planning, invocation, and transport errors at the narrowest corresponding interaction scope.
Expected action planning and execution failures MUST produce bounded safe interaction results rather than escaping solely to the controller's generic exception fallback.

#### Scenario: Parameter-specific GraphQL error
- **WHEN** an error path identifies one action parameter semantic field
- **THEN** the prompt associates the error with that parameter
- **AND** preserves successful state for other parameters

#### Scenario: Invocation-level GraphQL error
- **WHEN** an action invocation fails without a more specific member path
- **THEN** the prompt presents a bounded mapped error at action level and remains available for correction or retry
- **AND** preserves the GraphQL path and safe error code for diagnostics

#### Scenario: Dispatch plan cannot satisfy the schema
- **WHEN** the selected operation shape lacks a required declared argument or safe result projection
- **THEN** the context returns a bounded action-planning error before execution
- **AND** does not submit a partial or speculative operation

#### Scenario: Protected parameter dispatch fails
- **WHEN** planning, GraphQL execution, transport, cancellation, or result extraction fails after a protected parameter was entered
- **THEN** rendered errors, semantic events, diagnostics, and operation summaries omit the submitted protected value
- **AND** the prompt retains only the existing safe write-only pending representation

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

#### Scenario: Property validation replaces focused controls
- **WHEN** a user tabs from a changed property editor to an owned Clear, Save, or Cancel control and validation-driven rendering replaces that focused control
- **THEN** focus remains on the equivalent newly rendered control, including while an internal adapter upgrades asynchronously
- **AND** subsequent Tab or Shift+Tab navigation continues from that control without restarting at the editor

#### Scenario: Focus leaves the property during validation
- **WHEN** focus genuinely moves beyond the property before validation rendering completes
- **THEN** the property does not restore an obsolete internal focus intent
- **AND** external focus remains unchanged

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
- **THEN** the generated `<cw-property>` uses the same shared object context, editing, validation, mutation, loading, and error contracts as an authored property element

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
The component library SHALL provide `<cw-menubars>`, `<cw-menubar-primary>`, `<cw-menubar-secondary>`, and `<cw-menubar-tertiary>` as framework-neutral semantic application-menu components.

#### Scenario: Composite menu bars connect
- **WHEN** `<cw-menubars>` connects beneath a configured GraphQL client
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

### Requirement: Optional semantic reference widget implementation
The semantic editor registry SHALL use the qualified internal Vaadin implementation by default for eligible single-reference and multi-reference inputs without changing the public Causeway element, context, identity, validation, or event contracts.
The registry MUST retain the native editor for explicit native policy, unsupported descriptors, failed qualification gates, load failure, and rollback.

#### Scenario: Eligible single-reference editor uses the default
- **WHEN** an introspected single-reference property or action parameter is eligible and the resolved toolkit policy is Vaadin
- **THEN** the registry renders the internal control beneath the existing semantic Causeway component
- **AND** labels, required state, disabled reason, current value, choices, validation, and semantic changes remain Causeway-owned

#### Scenario: Eligible multi-reference editor uses the default
- **WHEN** an introspected member exposes supported multi-reference pending-value and validation semantics and the resolved toolkit policy is Vaadin
- **THEN** the registry renders internal tokenized reference selection with stable identities and deterministic ordering
- **AND** application markup and listeners do not require toolkit-specific tags or events

#### Scenario: Reference descriptor is unsupported
- **WHEN** a reference input lacks stable identity, authoritative list semantics, bounded choices, or another required adapter capability
- **THEN** the registry selects the native editor or presents the existing semantic unsupported state
- **AND** does not expose a partially functional raw toolkit control

#### Scenario: Native policy is explicit
- **WHEN** the resolved toolkit policy is native
- **THEN** the registry selects the native reference editor without importing Vaadin
- **AND** the public semantic contract remains identical

### Requirement: Toolkit-neutral reference interaction lifecycle
Candidate-backed reference editors SHALL follow existing Causeway cancellation, route generation, connection, disconnection, focus, validation, and pending-value lifecycle behavior.
Toolkit overlays, listeners, callbacks, and caches MUST remain subordinate to the owning semantic component and disposable route context.

#### Scenario: Candidate-backed editor disconnects
- **WHEN** HTMX replacement, custom-fragment navigation, or ordinary DOM removal disconnects the editor
- **THEN** pending lookup work, toolkit listeners, and overlays are cancelled or removed
- **AND** late callbacks cannot mutate the current route or retain hidden focus

#### Scenario: Validation rejects a candidate value
- **WHEN** Causeway conversion or server validation rejects the selected reference value
- **THEN** the semantic component presents the authoritative error and reconciles the internal control to the accepted pending state
- **AND** the toolkit does not independently commit the rejected value

### Requirement: Toolkit-neutral reversible value codecs
The semantic editor registry SHALL select a toolkit-neutral value codec for every editable property and action parameter before selecting an internal control.
A codec MUST preserve authoritative GraphQL values, control values, validation values, submitted variables, nullability, and error recovery without lossy implicit conversion.

#### Scenario: Exact numeric value is edited
- **WHEN** a `Long`, `BigInteger`, or `BigDecimal` value is rendered, edited, validated, and submitted
- **THEN** its sign, digits, decimal scale, and accepted lexical precision are preserved without conversion through JavaScript `Number`
- **AND** cancellation or rejection restores or retains the exact prior or pending lexical value as applicable

#### Scenario: Toolkit implementation changes
- **WHEN** a native control or qualified toolkit adapter edits the same advertised input
- **THEN** both controls use the same codec and submit the same semantic GraphQL value
- **AND** the public Causeway element and semantic event contract remain unchanged

#### Scenario: No reversible codec exists
- **WHEN** an advertised input shape has no registered reversible codec
- **THEN** the component renders a Causeway-owned bounded unsupported state
- **AND** does not submit a raw string, display formatter output, or partially converted value

### Requirement: Null-preserving boolean editing
The component library SHALL distinguish nullable Boolean `null`, `true`, and `false` throughout defaults, editing, validation, submission, reconciliation, and cancellation.
A required Boolean MAY use a two-state control only when `null` is not a valid semantic value.

#### Scenario: Nullable Boolean remains null
- **WHEN** an optional Boolean has authoritative or default value `null` and the user submits without selecting true or false
- **THEN** validation and invocation receive `null`
- **AND** the control does not silently convert it to `false`

#### Scenario: Nullable Boolean is changed
- **WHEN** the user explicitly selects true, false, or no value
- **THEN** the corresponding `true`, `false`, or `null` value is submitted exactly
- **AND** a later authoritative refresh renders that same state

### Requirement: Type-specific temporal editing
The component library SHALL support each accepted GraphQL temporal input through a type-specific reversible codec.
Local, offset, and named-zone values MUST preserve their respective date, time, offset, zone identifier, and accepted fractional precision without implicit browser-timezone conversion.

#### Scenario: Local temporal value is edited
- **WHEN** a local date, local time, or local date-time value is edited
- **THEN** the submitted value preserves its local semantics and accepted fractional precision
- **AND** no UTC offset or browser timezone is added implicitly

#### Scenario: Offset or zoned value is edited
- **WHEN** an offset date-time, offset time, or zoned date-time value is edited
- **THEN** the codec validates and preserves the advertised offset or zone representation
- **AND** a control incapable of preserving that information is not selected

#### Scenario: Temporal value is malformed
- **WHEN** the pending lexical value violates the advertised temporal grammar
- **THEN** local validation prevents invocation and presents a field-specific error
- **AND** the pending value remains available for correction

### Requirement: Protected and resource input capability enforcement
Semantic editors SHALL enable URL, password or protected-value, Blob, Clob, and local-resource input only when public metadata advertises a reversible authorized strategy and all required constraints.
Display-only or undisclosed representations MUST NOT become editable through generic string fallback.

#### Scenario: Protected value is write-only
- **WHEN** an authorized member advertises a write-only protected input
- **THEN** the editor permits a new value without revealing the prior value
- **AND** automation, errors, events, and reconciliation do not expose the submitted secret

#### Scenario: Resource constraints are complete
- **WHEN** a resource input advertises accepted media, size, and representation constraints
- **THEN** the editor enforces those constraints before GraphQL submission
- **AND** submits only the public resource representation

#### Scenario: Resource capability is incomplete
- **WHEN** required authorization, media, size, path, or open-strategy metadata is absent
- **THEN** the editor remains visibly unsupported or read-only
- **AND** no file bytes, characters, path, or URL are submitted speculatively

### Requirement: Authoritative value reconciliation
Property and action interaction flows SHALL keep pending codec values separate from authoritative object state.
Only successful server results may replace authoritative state, and obsolete, cancelled, disconnected, or route-replaced work MUST NOT overwrite newer state.

#### Scenario: Validation rejects an exact pending value
- **WHEN** local or GraphQL validation rejects a pending exact, temporal, nullable, protected, resource, or custom value
- **THEN** the editor remains open with a safe representation of the pending value and mapped error
- **AND** the last authoritative value remains unchanged

#### Scenario: Mutation succeeds
- **WHEN** the codec value is accepted and mutation succeeds
- **THEN** the owning context reconciles from authoritative server data
- **AND** display formatting does not reuse a stale control value

#### Scenario: Pending work becomes obsolete
- **WHEN** a newer edit, cancellation, disconnect, or route replacement supersedes validation or mutation work
- **THEN** the obsolete result is ignored or aborted
- **AND** no stale value or protected input leaks into the current component state

### Requirement: Advertised semantic identity projection
The component library SHALL derive metadata selection for concrete object values and rows from the effective metadata type advertised by targeted introspection.
It MUST preserve advertised identity and presentation fields, MUST treat `version` as optional, and MUST NOT request, synthesize, or infer absent metadata fields.

#### Scenario: Versioned object value is read
- **WHEN** a concrete property, choice, autocomplete result, preparation value, action result, or collection row advertises `id`, `logicalTypeName`, `title`, and `version`
- **THEN** the operation selects those advertised fields
- **AND** the semantic value preserves the returned version

#### Scenario: Versionless object value is read
- **WHEN** a concrete property, choice, autocomplete result, preparation value, action result, or collection row advertises `id`, `logicalTypeName`, and `title` but not `version`
- **THEN** the operation omits `version`
- **AND** the value remains available for supported rendering, pending-value conversion, validation, hydration, refresh, and navigation

#### Scenario: Metadata lacks semantic identity minimums
- **WHEN** an effective metadata type does not advertise both `id` and `logicalTypeName`
- **THEN** the component does not claim a navigable bookmark or hydratable object identity
- **AND** it exposes only a valid bounded projection or the existing local unsupported state

#### Scenario: Versionless value participates in preparation
- **WHEN** a property or action parameter preparation field returns a concrete versionless object through default, choices, autocomplete, validity, or datatype semantics
- **THEN** the context selects only metadata fields advertised for that object
- **AND** cancellation and stale-response protection remain unchanged

#### Scenario: Versionless collection rows are loaded
- **WHEN** a concrete collection element type advertises semantic identity without `version`
- **THEN** row selection merges the advertised identity fields with requested semantic columns
- **AND** the rows remain hydratable without manufacturing a concurrency token

#### Scenario: Abstract row projection remains unsupported
- **WHEN** an interface or union row exceeds the bounded projection policy or cannot be resolved through advertised concrete fragments
- **THEN** the component retains a bounded non-navigable or partial-error result
- **AND** does not manufacture a concrete identity

### Requirement: Bounded polymorphic value projection
The component library SHALL derive valid inline-fragment selections for advertised GraphQL interface and union values without selecting concrete fields directly on the abstract type.
It MUST validate fragment membership and nested fields through targeted introspection, MUST bound fragment and introspection cost, and MUST NOT repeat a mutating interaction to discover its result type.

#### Scenario: Small abstract result has described possible types
- **WHEN** an interface or union advertises a bounded set of described concrete object types
- **THEN** the operation selects `__typename` and valid inline fragments for those advertised types
- **AND** each fragment requests only metadata and children valid for its concrete type

#### Scenario: Broad collection union is activated
- **WHEN** an activated side-effect-free collection exposes an abstract row type whose complete possible-type set exceeds the direct expansion limit
- **THEN** the context issues one bounded typename-only probe and describes only distinct observed advertised concrete types
- **AND** reissues the same list or window read once with valid fragments for those types

#### Scenario: Polymorphic rows expose common identity
- **WHEN** concrete fragment results contain `id` and `logicalTypeName`
- **THEN** each row retains its exact typename, metadata, semantic object link, and hydrated row context
- **AND** optional title and version behavior follows the concrete metadata type

#### Scenario: Requested column differs by concrete type
- **WHEN** a declared semantic column is present on some observed concrete types and absent on another
- **THEN** each fragment includes the column only where its wrapper and child selection are advertised
- **AND** the missing cell remains local without invalidating successfully projected rows

#### Scenario: Replay returns an unobserved concrete type
- **WHEN** a collection changes between typename probe and fragment replay and returns an advertised type not included in the replay fragments
- **THEN** that row remains bounded to its available typename projection and is reported locally
- **AND** the context does not enter an unbounded probe loop

#### Scenario: Returned typename is not advertised
- **WHEN** a response typename is not an advertised possible type of the abstract field
- **THEN** the context rejects it as a bounded schema mismatch
- **AND** does not construct or submit a fragment from that value

#### Scenario: Broad mutating action result
- **WHEN** a mutating action returns an abstract type that cannot be projected within the direct fragment bound
- **THEN** the action executes exactly once and retains its successful bounded typename-only outcome
- **AND** the controller does not invent navigable identity or repeat the mutation

#### Scenario: Polymorphic request becomes obsolete
- **WHEN** a probe or fragment replay belongs to a disconnected, cancelled, or superseded collection generation
- **THEN** its remaining work is aborted or discarded
- **AND** it cannot replace the current row state

### Requirement: Semantic autocomplete window consumption
Semantic property and action-parameter editors SHALL prefer an advertised rich GraphQL autocomplete window while preserving Causeway-owned pending values, validation, semantic events, cancellation, and toolkit neutrality.
They MUST retain a bounded honest fallback when the server exposes only legacy autocomplete.

#### Scenario: Windowed property search begins
- **WHEN** an editable reference property advertises `autoCompleteWindow` and the search reaches its minimum length
- **THEN** the property context requests offset zero with a bounded page size
- **AND** publishes the returned items and continuation metadata only for the current filter generation

#### Scenario: Windowed action parameter depends on earlier values
- **WHEN** an object or service action parameter requests a page using current preceding arguments
- **THEN** the semantic context sends only arguments declared by the advertised window field
- **AND** changing an earlier value invalidates outstanding pages and restarts the affected search at offset zero

#### Scenario: Later page is requested
- **WHEN** the active semantic editor requests an offset not yet loaded for the current filter
- **THEN** the context obtains that authoritative server window without downloading or locally slicing the complete result
- **AND** semantic identities deduplicate items at their requested positions

#### Scenario: Window request becomes obsolete
- **WHEN** filter text, dependent arguments, prompt state, route generation, connection, or component lifetime supersedes an outstanding page
- **THEN** the request is aborted or ignored
- **AND** stale items, totals, validation, selection, focus, and errors cannot replace current state

#### Scenario: Native editor receives a partial first window
- **WHEN** native fallback presentation has items with `hasNext` true
- **THEN** it exposes the bounded current choices and an accessible additional-results or refine-search indication
- **AND** does not claim the first window is the complete result

#### Scenario: Server has only legacy autocomplete
- **WHEN** targeted introspection finds `autoComplete` but no `autoCompleteWindow`
- **THEN** the context uses the existing single-response operation within its configured bound
- **AND** an over-bound response retains the established visible limitation state without silent truncation

### Requirement: Toolkit-backed semantic scalar editors
Semantic property and action-parameter components SHALL use qualified internal toolkit adapters by default for existing scalar, bounded-choice, numeric, and local-temporal editor contracts.
Selection MUST remain introspection-driven and codec-driven and MUST preserve the same pending values, validation, GraphQL variables, and semantic events as native editors.

#### Scenario: Internal adapter is selected
- **WHEN** the resolved Vaadin policy is active and the advertised input type and codec satisfy a qualified family
- **THEN** the editor registry renders a Causeway-owned adapter element
- **AND** no application component, event listener, or GraphQL context depends on a toolkit-specific tag or event

#### Scenario: Property input rerenders during validation
- **WHEN** a toolkit-backed text input causes debounced validation and its host rerenders
- **THEN** the pending value and recoverable text selection or focus remain Causeway-owned
- **AND** stale upgrade work from the replaced control cannot alter current state

#### Scenario: Action parameter changes dependencies
- **WHEN** a toolkit-backed parameter changes and later parameters depend on it
- **THEN** the existing interaction controller invalidates and prepares affected parameters
- **AND** toolkit state cannot preserve an obsolete dependent value

#### Scenario: Native editor is explicitly requested
- **WHEN** the resolved common toolkit policy is native
- **THEN** the same semantic component renders its established native editor
- **AND** public value, validation, event, focus, and submission contracts remain compatible

### Requirement: Protected toolkit adapter boundary
A toolkit-backed protected editor SHALL use the existing sensitive codec and SHALL keep both prior and pending protected values outside observable presentation and diagnostics.

#### Scenario: Protected editor is prepared
- **WHEN** a protected property or action parameter uses an internal field adapter
- **THEN** the control receives no prior value and markup contains no protected value
- **AND** semantic interaction state exposes no value

#### Scenario: Protected operation fails
- **WHEN** parsing, validation, module loading, or GraphQL submission fails for protected input
- **THEN** the bounded error contains no protected value or serialized operation variables
- **AND** native fallback also initializes without the value

### Requirement: Declarative member-associated action composition
The domain components SHALL treat each direct `<cw-action>` child of `<cw-property>` or `<cw-collection>` as an ordered presentation association with that owner member.

#### Scenario: Property declares an associated action
- **WHEN** authored HTML places `<cw-action id="updateName">` directly beneath `<cw-property id="name">`
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
- **WHEN** `<cw-object>` renders property- or collection-associated actions from an effective grid
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

### Requirement: Native domain member identifier
`<cw-property>`, `<cw-action>`, `<cw-collection>`, and `<cw-collection-column>` SHALL identify their represented domain member through the standard HTML `id` attribute and its native reflected `id` property.
They MUST NOT read, observe, reflect, or alias a `member` attribute or custom `member` element property.
Internal GraphQL descriptors, context requirements, layout plans, semantic payloads, and collection-column configuration MAY continue to use `member` as domain terminology.

#### Scenario: Authored component identifies a domain member
- **WHEN** authored HTML contains `<cw-property id="firstName">`
- **THEN** the property resolves and renders the `firstName` domain property
- **AND** its native `id` property equals `firstName`

#### Scenario: Generated component identifies a domain member
- **WHEN** a fallback or effective-grid layout generates a property, action, or collection component
- **THEN** the generated host's `id` equals its represented Causeway member identifier
- **AND** no `member` attribute is emitted

#### Scenario: Declarative collection column identifies a projected member
- **WHEN** a collection directly contains `<cw-collection-column id="name">`
- **THEN** the collection projects the `name` field using its established internal column configuration
- **AND** the column host exposes `name` through its native `id` property

#### Scenario: Identifier changes while connected
- **WHEN** application code changes the native `id` of a connected property, action, or collection
- **THEN** the component reconnects its context requirement for the new domain member
- **AND** stale state for the former identifier cannot become current

#### Scenario: Former member attribute is present
- **WHEN** a member-bearing component has `member="firstName"` but no `id`
- **THEN** it does not resolve `firstName` through that obsolete attribute
- **AND** no custom `member` element property supplies a compatibility alias

### Requirement: Toolkit-backed read-only scalar presentation
A `<cw-property>` whose selected standard renderer and semantic value shape are qualified SHALL use the resolved internal component toolkit for read-only presentation without changing the public property, value-renderer, accessibility, or semantic-event contract.
Application renderer precedence and established non-field renderers MUST remain authoritative.

#### Scenario: Qualified standard scalar is visible
- **WHEN** the resolved component policy is Vaadin and a visible standard text, Boolean, enum, bounded-choice, numeric, or qualified local-temporal value satisfies its read-only family contract
- **THEN** the property renders that value through the internal qualified read-only field adapter
- **AND** its Causeway label, description, disabled reason, errors, alignment, responsive layout, classes, and events remain authoritative

#### Scenario: Application renderer wins precedence
- **WHEN** an application-specific renderer is selected for an otherwise eligible scalar
- **THEN** the application renderer remains visible
- **AND** toolkit selection does not replace or wrap it with a standard Vaadin field

#### Scenario: Value is not field-qualified
- **WHEN** the selected value is protected, null, reference, resource, LOB, offset-bearing, zoned, legacy temporal, custom, collection, unsupported, or otherwise unqualified
- **THEN** its established native or application renderer remains authoritative
- **AND** the property does not display an approximate disabled or generic field

#### Scenario: Native component policy is selected
- **WHEN** the resolved component policy is native
- **THEN** the property uses its established native value renderer and interaction controls
- **AND** application markup, GraphQL state, and semantic events remain unchanged

### Requirement: Toolkit-backed ordinary action affordance
A visible ordinary `<cw-action>` SHALL use the qualified internal action control selected by the resolved component toolkit while retaining Causeway ownership of action identity, visibility, usability, descriptions, request publication, interaction, results, and focus policy.

#### Scenario: Vaadin action is enabled
- **WHEN** the resolved component policy is Vaadin and a visible enabled ordinary action renders
- **THEN** an internal qualified Vaadin Button presents the action
- **AND** keyboard or pointer activation publishes the established Causeway action request exactly once

#### Scenario: Vaadin action is disabled
- **WHEN** a visible ordinary action is disabled with a bounded reason
- **THEN** the internal action control cannot activate
- **AND** Causeway-owned accessible presentation exposes its name, description, disabled state, and reason

#### Scenario: Vaadin action is hidden
- **WHEN** an ordinary action becomes hidden while an adapter is loading or connected
- **THEN** no native or Vaadin action control remains visible, focusable, or actionable
- **AND** late adapter work cannot restore it

#### Scenario: Non-ordinary control is rendered
- **WHEN** the control is a property edit, save, cancel, clear, action-prompt, shell, or another affordance outside ordinary `<cw-action>` qualification
- **THEN** its established native control remains authoritative
- **AND** the ordinary action adapter does not broaden its scope

### Requirement: Toolkit-backed qualified collection presentation
The `<cw-collection>` component SHALL select an internal Grid only for semantically qualified active wide collections while preserving its public markup, state, events, renderers, navigation, and associated actions.
Causeway SHALL remain the owner of collection loading, rows, columns, totals, ordering, paging, errors, focus, and lifecycle.

#### Scenario: Qualified wide collection renders
- **WHEN** a visible active collection has bounded windows, deterministic ordering, supported semantic columns, wide layout, and a healthy enabled Grid family
- **THEN** `<cw-collection>` may render one internal Grid presentation
- **AND** application-authored `<cw-collection-column>` and `<cw-action>` children remain the authoritative declarations

#### Scenario: Stable total supports virtualization
- **WHEN** a qualified collection reports a safely available stable total
- **THEN** Causeway may serve bounded ranges to Grid virtualization
- **AND** no Grid callback constructs GraphQL, domain identity, authorization, or navigation independently

#### Scenario: Total is unavailable
- **WHEN** a qualified deterministic window reports unavailable total count
- **THEN** the collection renders only its current bounded window through Grid with Causeway-owned previous and next controls
- **AND** it does not invent a total or silently traverse beyond normalized window metadata

#### Scenario: Collection is narrow or unqualified
- **WHEN** container width is at most 48rem or window, ordering, column, renderer, policy, or family qualification fails
- **THEN** the established native collection presentation remains authoritative
- **AND** no partial Grid or unsupported mixed presentation remains

#### Scenario: Native toolkit is explicit
- **WHEN** the common component toolkit resolves to native
- **THEN** collections use established native presentation without a Grid request or style hash
- **AND** GraphQL operations, routes, data, semantic events, and application markup are unchanged

### Requirement: Toolkit-neutral collection range lifecycle
Collection range work SHALL remain bounded by the current Causeway route, object generation, member identity, column selection, responsive mode, toolkit policy, renderer registry, and connection lifetime.
Hydrated row contexts and focus restoration MUST follow domain identity rather than recycled presentation nodes.

#### Scenario: Grid requests an additional range
- **WHEN** a still-current virtual Grid requests a bounded range
- **THEN** the collection host coordinates it through the existing object-context window operation
- **AND** identical current work is deduplicated while accepted concurrent work remains independently cancellable

#### Scenario: Collection lifetime supersedes range work
- **WHEN** route replacement, refresh, column change, width change, policy revision, member change, or disconnect makes a request obsolete
- **THEN** the request and its row contexts are aborted, disconnected, or ignored
- **AND** stale rows, cells, errors, total, paging, focus, and controls cannot alter the current component

#### Scenario: Cell is recycled
- **WHEN** Grid reuses a presentation node for another row or column
- **THEN** Causeway repopulates it from the current frozen semantic descriptor and cleans previous relationships
- **AND** semantic events and focus identity refer to current domain row and member identity

#### Scenario: Associated actions remain composed
- **WHEN** a collection uses Grid and contains associated actions
- **THEN** those actions remain Causeway siblings outside Grid in declaration order
- **AND** their visibility, usability, prompting, execution, navigation, and focus lifecycles remain independent

### Requirement: Toolkit-backed qualified application menus
The semantic application-menu components SHALL permit an internal Menu Bar presentation only when the complete tier preserves established hierarchy, order, state, interaction, responsive, and accessibility contracts.
The public element vocabulary, semantic events, menu state, action state, and host policy MUST remain Causeway-owned.

#### Scenario: Qualified semantic tier renders
- **WHEN** a connected non-empty primary, secondary, or tertiary tier is representable under the Vaadin component policy
- **THEN** that public tier may host one internal Menu Bar without changing application markup or selectors
- **AND** menus, sections, actions, labels, descriptions, icon hints, order, visibility, usability, and disabled reasons remain equivalent

#### Scenario: Service action is selected
- **WHEN** an enabled current action item is activated by keyboard or pointer
- **THEN** the existing Causeway interaction-controller path executes its service logical type and action ID exactly once
- **AND** established validation, cancellation, result, navigation, refresh, and semantic-event policy remains authoritative

#### Scenario: Tier is empty or unqualified
- **WHEN** a tier is empty, disconnected, hidden, unsupported, stale, responsively disqualified, failed, or governed by native policy
- **THEN** it remains hidden or uses the complete established native presentation
- **AND** no approximate or mixed toolkit hierarchy is presented

#### Scenario: Three tiers qualify
- **WHEN** primary, secondary, and tertiary bars are all non-empty and qualified
- **THEN** each retains one independently identified internal control in original semantic order
- **AND** the tiers are not merged into one application-facing or internal menu tree

### Requirement: Toolkit-neutral menu adapter lifecycle
Application-menu generation, preparation, refresh, activation, result, focus, and responsive state SHALL remain valid independently of native or toolkit presentation.
Superseded presentation work MUST NOT mutate current menu state or invoke a stale action.

#### Scenario: Menu generation changes
- **WHEN** refresh, authorization, preparation, metadata, or resource loading produces a newer accepted generation
- **THEN** current connected tiers project only that generation into native or toolkit presentation
- **AND** stale items, events, focus targets, definitions, and render completions are ignored

#### Scenario: Presentation mode changes
- **WHEN** policy, width, hierarchy, family health, connection, or visibility changes presentation mode
- **THEN** the tier rebuilds from current immutable Causeway state without another semantic request
- **AND** generated controls, listeners, observers, items, and transient focus state are cleaned exactly once

#### Scenario: Disabled action remains explained
- **WHEN** current preparation marks a visible service action unusable with a bounded reason
- **THEN** both native and qualified presentation expose equivalent disabled and described semantics
- **AND** neither presentation can invoke it

#### Scenario: Family failure occurs
- **WHEN** Menu Bar loading, definition, projection, rendering, event translation, or CSP fails
- **THEN** current menu tiers remain usable through native presentation
- **AND** references, fields, actions, collections, GraphQL, routing, authentication, and menu state remain unaffected

### Requirement: Semantic menu focus continuity
Application menus SHALL track focus and dismissal by current tier and semantic menu, section, or action identity rather than toolkit DOM identity.
Focus restoration MUST remain safe when overflow, refresh, responsive switching, action completion, or fallback recreates controls.

#### Scenario: Expanded menu is dismissed
- **WHEN** the user presses Escape in a native or toolkit nested or overflow menu
- **THEN** the transient menu closes and focus returns to its current semantic origin
- **AND** other tiers remain independently scoped

#### Scenario: Focused action disappears
- **WHEN** refresh removes, hides, disables, or relocates the focused action
- **THEN** focus moves to a safe current tier or shell target
- **AND** no stale, hidden, disconnected, or toolkit-internal node regains focus

### Requirement: Canonical property presentation metadata
The rich GraphQL property contract SHALL expose canonical friendly name, description, multiline row count, and label position metadata when those facets are available.
A `<cw-property>` read SHALL request the supported presentation metadata together with its existing semantic state and value projection.

#### Scenario: Property presentation facets are available
- **WHEN** a property declares `@PropertyLayout(named)`, `@PropertyLayout(describedAs)`, `@PropertyLayout(multiLine)`, or `@PropertyLayout(labelPosition)`
- **THEN** the rich property metadata exposes the corresponding canonical values
- **AND** a directly authored `<cw-property>` can render them without loading the effective-grid resource

#### Scenario: Presentation metadata field is unavailable
- **WHEN** an older compatible rich schema does not expose one or more presentation metadata fields
- **THEN** the property requests only supported fields
- **AND** missing values use the established fallback presentation without issuing an invalid GraphQL operation

### Requirement: Shared member description tooltip presentation
`<cw-property>` and `<cw-collection>` SHALL provide a framework-neutral tooltip presentation for effective descriptions without changing their metadata source or precedence.
Tooltip content MUST be escaped, bounded, pointer-discoverable, keyboard-reachable, responsive, and associated with its member through accessible description semantics.

#### Scenario: Tooltip mode is selected
- **WHEN** a property or collection with a non-blank effective description declares `description-as="tooltip"` ignoring case
- **THEN** the description does not consume visible layout space
- **AND** the semantic property label, property field when its label is suppressed, or collection heading exposes the description by pointer and keyboard
- **AND** assistive technology retains access to the effective description

#### Scenario: Label mode is selected or implied
- **WHEN** `description-as` is absent, blank, `label`, or unsupported
- **THEN** the effective description retains the established visible label-adjacent presentation
- **AND** unsupported input does not create an unknown presentation state

#### Scenario: Description and disabled reason share a tooltip
- **WHEN** a property or collection has both an effective description and a disabled reason
- **THEN** one tooltip presents a bounded description section above a distinct bounded disabled-reason section
- **AND** accessible description references preserve the same description-then-reason order

#### Scenario: Only a disabled reason is available
- **WHEN** a disabled property or collection has no effective non-redundant description
- **THEN** its semantic tooltip trigger exposes the bounded disabled reason without an empty description section
- **AND** the reason is not added as ordinary visible page text

#### Scenario: Presentation attribute changes
- **WHEN** `description-as` changes while the component is connected
- **THEN** the current member rerenders using the newly resolved presentation
- **AND** member data is not independently reloaded solely for the presentation change

### Requirement: Authored property presentation overrides
`<cw-property>` SHALL support `named`, `described-as`, `description-as`, `multi-line`, and `label-position` attributes as explicit overrides of canonical property presentation metadata or its presentation.
Canonical authored attributes MUST take precedence over compatibility aliases, which MUST take precedence over metadata and fallback values.

#### Scenario: Property name is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" named="Given name">`
- **THEN** the visible and accessible property name is `Given name`
- **AND** another property without `named` continues to use canonical metadata or its humanized member ID

#### Scenario: Property description is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" described-as="The given or first name of this customer">`
- **THEN** that text is the property's effective visible or tooltip description according to `description-as`
- **AND** it takes precedence over any canonical property description

#### Scenario: Description presentation is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" description-as="tooltip">`
- **THEN** the effective description uses tooltip presentation regardless of whether its text came from `described-as` or canonical metadata
- **AND** changing presentation does not change description-text precedence

#### Scenario: Multiline rows are overridden
- **WHEN** authored HTML contains `<cw-property id="notes" multi-line="5">`
- **THEN** a supported string view or editor uses five multiline rows
- **AND** canonical metadata does not replace the authored row count

#### Scenario: Invalid multiline override
- **WHEN** `multi-line` is malformed, no greater than one, or exceeds the supported maximum
- **THEN** the component ignores the malformed or non-multiline value or caps an excessive value at the supported maximum
- **AND** unrelated property rendering remains available

#### Scenario: Label position is overridden
- **WHEN** authored HTML contains `<cw-property id="firstName" label-position="TOP">`
- **THEN** the property uses the `TOP` presentation regardless of its canonical label-position facet

#### Scenario: Invalid label-position override
- **WHEN** an authored `label-position` is not `LEFT`, `TOP`, or `NONE` ignoring case
- **THEN** the component falls back to canonical metadata and then `LEFT`
- **AND** exposes no broken or unknown layout mode

#### Scenario: Compatibility aliases remain usable
- **WHEN** existing markup uses `label` or `multiline`
- **THEN** the component preserves the established override behavior
- **AND** a simultaneously supplied canonical `named` or `multi-line` attribute wins deterministically

### Requirement: Property label-position presentation
`<cw-property>` SHALL honour the effective property label position `LEFT`, `TOP`, or `NONE` in view, loading, error, disabled, and edit states.
Properties using `LEFT` in the same field-set-like container MUST use a consistent configurable label-to-field ratio.
Effective multiline presentation from canonical HTML, compatibility HTML, or member metadata MUST drive the same explicit responsive shell layout.
Description layout MUST follow the effective `description-as` mode without weakening accessible naming or description.

#### Scenario: Label is positioned left
- **WHEN** the effective label position is `LEFT`
- **THEN** the visible label is placed to the left of the property field using the container's consistent label-column ratio
- **AND** a label-presented description appears in smaller text below the label
- **AND** the field uses the remaining width

#### Scenario: Label is positioned top
- **WHEN** the effective label position is `TOP`
- **THEN** the visible label is placed above the property field
- **AND** a label-presented description appears in smaller text below the label
- **AND** the field uses the full available width

#### Scenario: Label is suppressed
- **WHEN** the effective label position is `NONE`
- **THEN** no visible label or label-presented description is rendered
- **AND** the field uses the full available width
- **AND** interactive controls retain a meaningful accessible name
- **AND** a tooltip-presented description remains available from the property field by pointer and keyboard

#### Scenario: Property description comes from the facet
- **WHEN** `@PropertyLayout(describedAs)` supplies a non-redundant description and no HTML text override is present
- **THEN** the description appears in smaller text immediately below the visible label by default or in the tooltip when selected
- **AND** the value or editor is associated with it through accessible description semantics

#### Scenario: Narrow presentation stacks safely
- **WHEN** a multiline `LEFT` property resolved from `multi-line`, legacy `multiline`, or `metadata.multiLine` cannot retain its configured columns at a narrow inline size
- **THEN** label and any label-presented description occupy explicit successive rows followed by an explicit full-width field row in meaningful document order
- **AND** the field's value and bounded edit control align at the start of that row without overlap or implicit-grid displacement
- **AND** the property introduces no horizontal overflow or clipping

### Requirement: Generated property presentation equivalence
`<cw-object>` SHALL carry supported effective-grid property name, description, multiline, and label-position hints into generated `<cw-property>` elements through the canonical public attributes.

#### Scenario: Effective grid supplies property presentation
- **WHEN** an effective-grid property reference supplies `named`, `describedAs`, `multiLine`, or `labelPosition`
- **THEN** the generated property receives the equivalent `named`, `described-as`, `multi-line`, or `label-position` attribute
- **AND** direct and generated property elements resolve presentation through the same component logic

#### Scenario: Effective grid presentation is partial
- **WHEN** an effective-grid property reference supplies only some supported presentation hints
- **THEN** supplied hints override their corresponding metadata values
- **AND** omitted hints continue to use canonical metadata or fallback values

### Requirement: Executable Petclinic property presentation examples
The Petclinic sample SHALL demonstrate annotation-derived and HTML-overridden property presentation selectively rather than applying every presentation form to every property.

#### Scenario: Maintainer inspects Petclinic property declarations
- **WHEN** a maintainer inspects the Petclinic domain and page resources
- **THEN** some but not all properties declare `@PropertyLayout(describedAs)`
- **AND** at least one property demonstrates annotation-derived `labelPosition=TOP`
- **AND** at least one property uses an authored `named` override
- **AND** some but not all appropriate properties use authored `described-as`, `multi-line`, or `label-position` overrides

#### Scenario: Browser renders Petclinic examples
- **WHEN** the Petclinic owner page reaches its ready state
- **THEN** the selected annotation-derived and HTML-overridden names, descriptions, multiline fields, and label positions are visible and accessible
- **AND** non-overridden properties retain their metadata-driven or fallback presentation

### Requirement: Semantic object breadcrumbs
The component library SHALL provide a framework-neutral `<cw-breadcrumbs>` custom element that consumes the nearest object context's semantic breadcrumbs requirement.
When valid navigable ancestors exist, it SHALL render the ancestor chain followed by the current object.
When no valid navigable ancestors exist in ready state, it SHALL omit breadcrumb presentation.

#### Scenario: Object has multiple ancestors
- **WHEN** `<cw-breadcrumbs>` receives root-to-parent entries and current-object metadata
- **THEN** it renders each ancestor as a semantic `<cw-object-link>` in the supplied order
- **AND** renders the escaped current title as the final non-link item marked `aria-current="page"`

#### Scenario: Object has no ancestors
- **WHEN** the breadcrumb list is empty and current-object metadata is available
- **THEN** the component host is hidden and contains no breadcrumb landmark or current-only item

#### Scenario: Ancestor entry is malformed
- **WHEN** an entry lacks a logical type name, identifier, or title
- **THEN** the component omits that entry defensively
- **AND** does not construct a partial navigation target
- **AND** hides ready-state breadcrumb presentation when no valid ancestor remains

### Requirement: Accessible breadcrumb presentation
When one or more valid ancestors exist, `<cw-breadcrumbs>` SHALL render keyboard-operable, escaped, responsive light-DOM markup with a `nav` landmark labelled `Breadcrumb`, an ordered list, documented semantic classes, and ordinary link focus behavior.
A ready root-only state MUST NOT expose an empty or current-only breadcrumb landmark to the accessibility tree.

#### Scenario: Assistive technology inspects descendant breadcrumbs
- **WHEN** breadcrumb state is ready with at least one valid ancestor
- **THEN** the navigation landmark and list communicate hierarchy order
- **AND** exactly one final current item exposes `aria-current="page"`

#### Scenario: Assistive technology inspects a root object
- **WHEN** breadcrumb state is ready with no valid ancestors
- **THEN** no breadcrumb navigation landmark or current item is exposed

#### Scenario: Breadcrumb title is long or contains markup-like text
- **WHEN** an ancestor or current title contains long text or HTML-significant characters
- **THEN** the title is escaped as text and wraps without causing horizontal page overflow

### Requirement: Existing semantic navigation ownership
Ancestor activation SHALL use the existing bubbling and composed `causeway-navigation-request` contract produced by `<cw-object-link>` and SHALL NOT impose a URL, router, HTMX, or application callback API.

#### Scenario: User activates an ancestor
- **WHEN** the user follows an ancestor breadcrumb by pointer or keyboard
- **THEN** one semantic navigation event identifies that ancestor's logical type, id, and title
- **AND** the host remains responsible for route handling

### Requirement: Local breadcrumb states
`<cw-breadcrumbs>` SHALL render loading, unsupported, partial-error, and terminal-error states locally without suppressing successful sibling components.

#### Scenario: Breadcrumb traversal is loading
- **WHEN** the semantic requirement is awaiting schema or object data
- **THEN** the component exposes a bounded busy status within its own landmark

#### Scenario: Breadcrumb traversal fails
- **WHEN** the requirement reports unsupported, partial-error, or terminal-error state
- **THEN** the component renders a bounded accessible local diagnostic
- **AND** does not render stale ancestor links

### Requirement: Public breadcrumb component contract
The component registry, ECMAScript exports, semantic element-name constants, stylesheet surfaces, usage documentation, fixtures, and automation hooks SHALL include `<cw-breadcrumbs>` without changing existing component contracts.

#### Scenario: Application uses plain HTML
- **WHEN** an application imports the standard foundation entry module and writes `<cw-breadcrumbs>` beneath an object context
- **THEN** the element is registered and operates without a frontend framework or direct GraphQL document construction

### Requirement: Semantic collection headings
The `<cw-collection>` component SHALL render one semantic collection heading from an explicit HTML name, canonical member metadata, or a safe member-id fallback.
It SHALL render or tooltip-present a distinct available description according to `description-as` and associate the description with every collection presentation state.

#### Scenario: HTML supplies text and presentation overrides
- **WHEN** a collection declares non-blank `named` and `described-as` attributes and a supported `description-as` value
- **THEN** those text values are the effective collection name and description
- **AND** they override canonical friendly-name and description metadata
- **AND** the description uses the selected visible-label or tooltip presentation

#### Scenario: Canonical metadata supplies heading text
- **WHEN** no applicable HTML text override exists and the collection response supplies canonical `metadata.friendlyName` or `metadata.description`
- **THEN** each available value is used independently
- **AND** a missing value falls back to a humanized member id for the name or no description for the description

#### Scenario: Legacy label remains present
- **WHEN** a collection declares the existing `label` attribute without `named`
- **THEN** the label remains the effective name for backward compatibility
- **AND** `named` takes precedence when both are non-blank

#### Scenario: Description duplicates resolved name
- **WHEN** the candidate description equals the resolved name after trimming and case-insensitive comparison
- **THEN** no duplicate description is rendered, tooltip-presented, or referenced

#### Scenario: Heading attributes change
- **WHEN** `named`, `described-as`, `description-as`, `label`, or the member id changes while the component is connected
- **THEN** the current collection shell rerenders with the newly resolved accessible heading and description presentation
- **AND** collection data is not independently reloaded solely for the text or presentation change

### Requirement: Quiet read-only collection presentation
Collection wrappers SHALL remain read-only and authorization-aware without rendering their disabled or unmodifiable boolean or reason as ordinary visible explanatory text or a label.
A non-blank collection-level reason MUST remain available on demand from the semantic heading tooltip and through accessible description semantics.

#### Scenario: Mixed-in collection is unmodifiable
- **WHEN** a readable mixed-in collection reports a disabled reason such as “Cannot edit a mixed-in collection”
- **THEN** its rows, heading, description, paging, and applicable associated actions remain available
- **AND** the collection-level reason is absent from ordinary visible layout but available from the heading tooltip

#### Scenario: Collection has description and unmodifiable reason
- **WHEN** a collection has both an effective description and a disabled or unmodifiable reason
- **THEN** the heading tooltip presents the description above the reason as distinct sections
- **AND** the description remains visibly rendered as well when `description-as` resolves to `label`

#### Scenario: Member or associated action is disabled
- **WHEN** an individual row member or associated action has its own disabled state and reason
- **THEN** that control retains its established semantic state and explanation
- **AND** collection-level tooltip presentation does not broaden authorization or enable mutation

### Requirement: Declarative collection paging
`<cw-collection>` SHALL accept an optional `paged` attribute containing a bounded positive integer page size.
A valid override SHALL apply consistently to initial loading, previous and next navigation, refresh, and current-generation reloads.

#### Scenario: Valid page size is declared
- **WHEN** an active collection declares `paged="10"`
- **THEN** its first collection-window request starts at offset zero with size ten
- **AND** previous and next requests preserve the authoritative normalized page size

#### Scenario: Page size is absent
- **WHEN** a collection does not declare `paged`
- **THEN** established default collection loading and Grid qualification remain unchanged
- **AND** no paging controls are introduced solely by this change

#### Scenario: Page size is invalid
- **WHEN** `paged` is blank, malformed, zero, negative, or above the accepted public bound
- **THEN** the component treats it as absent
- **AND** does not issue an unbounded or invalid request from that attribute

#### Scenario: Page size changes reactively
- **WHEN** a connected collection's effective `paged` value changes
- **THEN** current loading and range generations are retired before loading offset zero with the new effective size
- **AND** stale rows, focus, errors, or paging controls cannot replace the new state

### Requirement: Declarative collection Grid controls
`<cw-collection>` SHALL accept optional `resizable-columns` and `reorderable-columns` attributes for qualified Grid presentation.
The attributes MUST NOT expose toolkit elements, events, or item objects and MUST NOT alter native fallback semantics.

#### Scenario: Resizing is enabled
- **WHEN** a qualified Grid collection declares `resizable-columns`
- **THEN** each accepted generated Grid column is resizable
- **AND** Causeway cell rendering and column selection remain authoritative

#### Scenario: Reordering is enabled
- **WHEN** a qualified Grid collection declares `reorderable-columns`
- **THEN** the current Grid permits pointer and keyboard-supported column reordering provided by the toolkit
- **AND** declarative order is restored after adapter replacement, native fallback, refresh, or navigation

#### Scenario: Controls are absent
- **WHEN** neither Grid-control attribute is declared
- **THEN** resizing and reordering remain disabled
- **AND** existing collection presentation is unchanged

#### Scenario: Native presentation is active
- **WHEN** responsive, policy, capability, or failure qualification selects native collection presentation
- **THEN** Grid-control attributes do not create toolkit controls or alter the native table or list
- **AND** later safe Grid qualification may apply the current attributes once

### Requirement: Declarative collection sorting and filtering
`<cw-collection>` SHALL accept optional `sortable` and `filterable` attributes that activate only server-advertised collection-wide criteria.
Both attributes SHALL default off and MUST NOT transform only the currently loaded rows.

#### Scenario: Sorting is enabled
- **WHEN** a collection declares `sortable` and its window advertises one or more declared columns as sortable
- **THEN** accessible column-header controls visibly indicate their sortable affordance before a direction is selected
- **AND** they can select one ascending or descending server criterion
- **AND** the collection reloads offset zero before paging or virtual ranges continue with that criterion

#### Scenario: Filtering is enabled
- **WHEN** a collection declares `filterable` and its window advertises quick-search support
- **THEN** one labelled bounded search control applies the server's collection-wide filtering semantics
- **AND** filtering resets to offset zero and preserves normalized paging

#### Scenario: Capability is unavailable
- **WHEN** the server does not advertise sortable declared columns or quick-search support
- **THEN** the corresponding opt-in attribute does not expose a misleading active control
- **AND** established collection loading and presentation remain available

#### Scenario: Criteria change reactively
- **WHEN** sorting, search text, or either opt-in attribute changes
- **THEN** current loading, row contexts, range cache, focus intent, and paging state are retired or reconciled before offset-zero reload
- **AND** stale rows, totals, errors, controls, or criteria cannot replace the new state

#### Scenario: Native fallback is active
- **WHEN** responsive, policy, ordering, capability, or adapter failure selects native presentation
- **THEN** the same Causeway sorting and filtering criteria remain operable through native semantic controls
- **AND** no toolkit element or event becomes an application dependency

#### Scenario: Sorting preserves the established presentation
- **WHEN** an initially unstable collection receives deterministic requested ordering from a sort criterion
- **THEN** the criterion does not by itself upgrade native presentation to Grid
- **AND** header font, column allocation, and control affordances remain stable across ascending, descending, and unordered states

### Requirement: Collection sorting and filtering remain collection-wide concerns
The component SHALL expose sorting or filtering only when the collection-window contract can apply the selected criteria across the complete authorized execution-time collection.
It MUST NOT apply criteria only to loaded rows while presenting them as collection-wide.

#### Scenario: Server contract advertises sort and search inputs
- **WHEN** the collection window advertises bounded sorting or quick-search capability
- **THEN** opted-in controls send normalized criteria with every initial, paging, refresh, and virtual-range request
- **AND** deterministic server results remain authoritative across windows

#### Scenario: Server contract lacks criteria inputs
- **WHEN** the discovered collection-window operation accepts offset and size but no ordering or search criteria
- **THEN** the component does not enable sorting or filtering controls
- **AND** established server ordering remains authoritative across windows

### Requirement: Authored and canonical action naming
`<cw-action>` SHALL accept an optional `named` attribute and reflected property for its visible action name.
The effective action name MUST use `named`, then compatible `label`, then canonical metadata, then the humanized action identifier.

#### Scenario: HTML name is supplied
- **WHEN** an action element declares `named="Place a new order"`
- **THEN** its ordinary control and any parameterized prompt use `Place a new order`
- **AND** the semantic action identifier remains unchanged

#### Scenario: Compatible label is supplied
- **WHEN** `named` is absent and an existing action element declares `label`
- **THEN** the compatible label remains the visible name
- **AND** canonical metadata and humanized fallback do not override it

#### Scenario: No authored name is supplied
- **WHEN** neither `named` nor `label` supplies a name
- **THEN** canonical action metadata supplies the visible name when available
- **AND** a humanized action identifier remains the bounded fallback for older schemas

### Requirement: Action tooltip composition
An ordinary action control SHALL present a bounded canonical description as an accessible tooltip.
When the action is disabled, its bounded disabled reason MUST appear in the tooltip as a separate section without replacing an available description.

#### Scenario: Enabled described action renders
- **WHEN** a visible enabled action has a canonical description
- **THEN** pointer and keyboard users can obtain that description from the action control tooltip
- **AND** the description remains escaped and associated through accessible descriptive semantics

#### Scenario: Disabled action has description and reason
- **WHEN** a visible action has both a canonical description and a disabled reason
- **THEN** its non-invoking control presents the description and disabled reason as separate tooltip sections
- **AND** activation remains unavailable

#### Scenario: Disabled action has only a reason
- **WHEN** a visible action has no description and has a disabled reason
- **THEN** the tooltip contains the disabled reason without an empty section
- **AND** the reason remains available to assistive technology

### Requirement: Parameterized action prompt presentation
A parameterized action prompt SHALL present the effective action name as its heading and a non-duplicate canonical action description as quiet explanatory text immediately below.

#### Scenario: Described parameterized action opens
- **WHEN** action preparation returns one or more parameters and current action presentation includes a description
- **THEN** the dialog heading uses the effective action name
- **AND** the escaped description appears below the heading and participates in the dialog description

#### Scenario: Parameterless action executes
- **WHEN** action preparation returns no parameters
- **THEN** established direct invocation remains in effect
- **AND** no presentation-only prompt is introduced

### Requirement: Font Awesome action icon presentation
Ordinary action controls SHALL render applicable static Font Awesome metadata as a decorative icon before or after the action name according to its canonical position.
Icon metadata MUST be bounded and tokenized and MUST NOT inject markup or alter action semantics.

#### Scenario: Left-positioned icon is supplied
- **WHEN** action metadata supplies accepted Font Awesome classes with `LEFT` position
- **THEN** a decorative icon precedes the action name
- **AND** the accessible name remains the textual action name

#### Scenario: Right-positioned icon is supplied
- **WHEN** action metadata supplies accepted Font Awesome classes with `RIGHT` position
- **THEN** a decorative icon follows the action name
- **AND** native and Vaadin action controls retain equivalent order

#### Scenario: Icon metadata is absent or invalid
- **WHEN** action metadata has no accepted static Font Awesome classes or position
- **THEN** no icon is rendered
- **AND** the action name, usability, invocation, and focus behavior remain unchanged

### Requirement: Field-aligned property-associated actions
The component library SHALL place actions authored directly beneath a property in visual association with that property's field rather than its label while preserving semantic action behavior and light-DOM ownership.

#### Scenario: Associated action renders at wide width
- **WHEN** a property with a left-positioned label renders one or more associated actions at a wide layout width
- **THEN** each associated action begins at the logical start of the property's field column
- **AND** the label column remains free of action controls

#### Scenario: Associated action renders at narrow width
- **WHEN** responsive presentation stacks the property label and field
- **THEN** associated actions begin at the same logical start as the stacked field
- **AND** controls do not overlap, clip, or introduce horizontal page overflow

#### Scenario: Associated action is activated
- **WHEN** a user activates a field-aligned associated action
- **THEN** the established semantic action request, toolkit selection, focus, disabled state, tooltip, and invocation behavior remain unchanged

### Requirement: Declarative action-parameter presentation
The component library SHALL provide a framework-neutral `<cw-parameter>` configuration component that MAY be nested directly beneath `<cw-action>` to supply presentation hints for the authoritative action parameter with the same semantic id.
Nested parameter declarations MUST NOT add, remove, reorder, hide, disable, default, validate, or invoke parameters.

#### Scenario: Selected parameter has authored presentation
- **WHEN** an authored action contains `<cw-parameter id="firstName" named="Given name" described-as="The person's given name" description-as="label" multi-line="3">`
- **AND** authoritative action preparation returns a compatible parameter with id `firstName`
- **THEN** the standard prompt uses the authored name and description and a bounded multiline editor
- **AND** hidden, disabled, default, choice, autocomplete, validity, order, and invocation semantics remain authoritative

#### Scenario: Parameter declaration is partial
- **WHEN** an action declares presentation for only some authoritative parameters
- **THEN** matching declared parameters use only their explicitly supplied overrides
- **AND** undeclared parameters and non-overridden presentation fields retain established canonical behavior

#### Scenario: Declared parameter is not authoritative
- **WHEN** a nested parameter id is absent from authoritative action preparation
- **THEN** the controller ignores that declaration
- **AND** it does not create an editor, argument, validation request, or invocation value for that id

#### Scenario: Action declares no parameters
- **WHEN** an existing authored or generated action has no nested `<cw-parameter>` children
- **THEN** parameter preparation, prompt presentation, validation, and invocation remain unchanged

### Requirement: Action-parameter name and description overrides
`<cw-parameter>` SHALL support optional `named`, `described-as`, and `description-as` attributes with normalization and accessibility behavior consistent with property presentation.
`description-as` MUST accept `label` and `tooltip`, MUST default to `label`, and MUST fall back safely for blank or unsupported values.

#### Scenario: Visible authored description
- **WHEN** a matching declaration supplies `described-as` and `description-as="label"`
- **THEN** the effective description appears in the parameter layout
- **AND** the editor is associated with that description accessibly

#### Scenario: Tooltip authored description
- **WHEN** a matching declaration supplies `described-as` and `description-as="tooltip"`
- **THEN** the effective description is available from the parameter label by pointer and keyboard
- **AND** hidden accessible description text remains associated with the editor

#### Scenario: Authored description duplicates effective name
- **WHEN** the effective description equals the effective parameter name ignoring case and surrounding whitespace
- **THEN** the prompt suppresses redundant visible and tooltip description presentation

### Requirement: Action-parameter multiline override
`<cw-parameter>` SHALL support an optional bounded `multi-line` attribute that requests the established multiline string editor without changing parameter value parsing or validation.

#### Scenario: Valid multiline hint
- **WHEN** a matching string parameter declaration supplies an integer `multi-line` value greater than one within or above the supported bound
- **THEN** the standard editor uses a textarea with the bounded effective row count
- **AND** pending-value parsing, focus, validation, and invocation remain identical to an ordinary string parameter

#### Scenario: Invalid or incompatible multiline hint
- **WHEN** `multi-line` is absent, malformed, non-positive, one, or incompatible with the authoritative input shape
- **THEN** the parameter retains its established qualified editor
- **AND** unrelated parameter presentation remains available

### Requirement: Distinct customizable tooltip presentation
Component-owned action, member, collection, parameter, and disabled-state tooltips SHALL use a shared high-contrast explanatory surface that is visually distinct from adjacent action controls.
The default treatment MUST use a light neutral background, dark text, a visible boundary, and stable `--causeway-*` customization variables without changing tooltip semantics or activation.

#### Scenario: Tooltip appears beside a filled action
- **WHEN** pointer or keyboard interaction reveals a tooltip adjacent to a filled action control
- **THEN** the tooltip uses the light neutral explanatory surface and dark text
- **AND** its border or elevation distinguishes it from both the page and the action control

#### Scenario: Different component tooltip families render
- **WHEN** an action description, member description, collection description, action-parameter description, or disabled reason is presented as a tooltip
- **THEN** each tooltip consumes the same background, text, border, and shadow presentation tokens
- **AND** existing content, sections, positioning, responsive bounds, pointer access, keyboard access, and accessible associations remain unchanged

#### Scenario: Application customizes tooltip presentation
- **WHEN** an application overrides the documented tooltip variables
- **THEN** component-owned tooltip background, text, border, and shadow presentation use those values
- **AND** no component markup or semantic event customization is required

#### Scenario: Optional theme is absent
- **WHEN** an application installs only the structural component styles
- **THEN** equivalent high-contrast light tooltip fallback values remain effective
- **AND** tooltip content remains readable and visually bounded

### Requirement: Locale-aware local-date presentation
Qualified local-date editors SHALL format and parse visible calendar dates according to the active document language, falling back to the browser locale and then a safe default when no valid document language is available.
Locale presentation MUST NOT change the ISO lexical value retained by the temporal codec, pending interaction state, validation, or GraphQL submission.

#### Scenario: Document declares a non-US date convention
- **WHEN** a qualified `LocalDate` or `LocalDateTime` date portion is presented under a document language whose numeric date order differs from the default US order
- **THEN** the visible date uses that document language's numeric order and separators
- **AND** the underlying semantic value remains the same ISO local date

#### Scenario: User enters a localized date
- **WHEN** the user enters a valid date in the active locale's displayed numeric format
- **THEN** the picker resolves the corresponding calendar date
- **AND** the existing temporal codec receives its ISO lexical representation without timezone conversion

#### Scenario: Locale is absent or invalid
- **WHEN** the document has no valid language declaration
- **THEN** the adapter uses the browser locale when available and otherwise its safe default
- **AND** the field family does not fail solely because locale metadata is absent or malformed

#### Scenario: Localized calendar opens
- **WHEN** the platform exposes localized month names, weekday names, and first-day-of-week information
- **THEN** the date chooser uses those locale settings
- **AND** date selection still produces the same ISO local semantic value

### Requirement: Canonical are-you-sure action confirmation
The standard interaction controller SHALL require explicit accessible confirmation before invoking an action whose canonical rich GraphQL metadata reports `areYouSure` as true.
HTML-authored component presentation MUST NOT suppress, fabricate, or override that canonical requirement.

#### Scenario: Parameterless action requires confirmation
- **WHEN** an enabled parameterless action with `areYouSure` metadata is activated
- **THEN** the controller presents confirmation before invoking the action
- **AND** the action is invoked only after the user explicitly confirms

#### Scenario: Parameterized action requires confirmation
- **WHEN** an action with `areYouSure` metadata has parameters
- **AND** the user submits values that pass authoritative parameter preparation and whole-action validation
- **THEN** the controller presents confirmation with the validated pending values retained
- **AND** invokes only after the user explicitly confirms

#### Scenario: Parameterized confirmation is declined
- **WHEN** a user cancels or presses Escape from confirmation for a parameterized action
- **THEN** the controller returns to the parameter prompt with pending values intact
- **AND** does not invoke the action

#### Scenario: Parameterless confirmation is declined
- **WHEN** a user cancels or presses Escape from confirmation for a parameterless action
- **THEN** the controller closes the prompt without invocation
- **AND** restores focus to the originating action affordance

#### Scenario: Ordinary action is activated
- **WHEN** an enabled action's canonical metadata reports `areYouSure` as false or the compatible schema does not expose the field
- **THEN** the controller retains the established parameterless or parameterized interaction flow
- **AND** does not infer confirmation from presentation or operation placement

#### Scenario: Confirmation is presented accessibly
- **WHEN** an action enters confirmation state
- **THEN** one controller-owned modal dialog identifies the effective action name and destructive decision
- **AND** exposes keyboard-operable Confirm and Cancel controls with deterministic focus and stable automation hooks

#### Scenario: Confirmation is activated repeatedly
- **WHEN** the user confirms an action and invocation begins
- **THEN** confirmation controls are gated against repeated activation
- **AND** the controller invokes the action at most once for that prompt generation

#### Scenario: Action is disabled
- **WHEN** authoritative action state supplies a disabled reason
- **THEN** activation remains unavailable and the reason remains accessible
- **AND** no confirmation prompt opens
