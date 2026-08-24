## MODIFIED Requirements

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
