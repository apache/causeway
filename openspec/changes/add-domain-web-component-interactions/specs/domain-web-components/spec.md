## ADDED Requirements

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
