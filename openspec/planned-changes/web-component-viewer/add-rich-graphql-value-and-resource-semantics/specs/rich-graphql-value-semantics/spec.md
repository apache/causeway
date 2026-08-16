## ADDED Requirements

### Requirement: Reversible advertised input values
Every rich GraphQL property or action input advertised as supported SHALL reconstruct the declared Causeway domain value from its GraphQL representation.

#### Scenario: Supported value round-trips
- **WHEN** a client submits the documented canonical representation for a supported value
- **THEN** GraphQL supplies the declared Java type to the domain member
- **AND** subsequent output uses the documented canonical representation

#### Scenario: No reversible strategy exists
- **WHEN** an input-capable member uses a value type without a reversible strategy
- **THEN** schema construction or capability discovery reports it as unsupported
- **AND** GraphQL does not pass an arbitrary raw string as the domain value

### Requirement: Discoverable editor-neutral value semantics
Rich datatype metadata SHALL identify logical type, representation category, GraphQL input and output shape, canonical format, constraints, resource behavior, and extension ownership.

#### Scenario: Client inspects a datatype
- **WHEN** targeted introspection reaches a property or parameter datatype descriptor
- **THEN** the client can distinguish text, boolean, numeric, temporal, enum, object-reference, resource, composite, and opaque semantics
- **AND** no frontend widget is prescribed

### Requirement: Canonical standard datatype support
The default viewer SHALL provide deterministic coercion and serialization for the standard datatype set confirmed by the reference-app analysis.

#### Scenario: Temporal or URL value is valid
- **WHEN** a confirmed temporal or URL value uses its canonical representation
- **THEN** input and output preserve the documented value, precision, and timezone semantics

#### Scenario: Standard value is malformed
- **WHEN** a client submits malformed canonical input
- **THEN** GraphQL returns a typed coercion error without invoking the domain member

### Requirement: Extensible custom values
Applications SHALL be able to register an explicit reversible marshaller and datatype descriptor for a custom value.

#### Scenario: Custom strategy is registered
- **WHEN** an application registers a compatible strategy
- **THEN** the rich schema advertises and uses that strategy for supported reads and inputs

#### Scenario: Custom strategy is absent
- **WHEN** an opaque custom value has no registered strategy
- **THEN** the schema reports explicit unsupported capability rather than serializing it through `toString()`

### Requirement: Consistent resource-value capability
Supported resource values SHALL use a consistent metadata and transfer contract across property reads, updates, action parameters, and action results.

#### Scenario: Resource metadata is requested
- **WHEN** a client inspects a supported resource value
- **THEN** filename, media type, size policy, transfer mode, and applicable acceptance constraints are available without transferring content

#### Scenario: Resource input is submitted
- **WHEN** authorized resource input satisfies configured media and size constraints
- **THEN** GraphQL reconstructs the declared resource value and invokes the member

#### Scenario: Resource input is forbidden or unsupported
- **WHEN** authorization, sensitivity, size, media, or strategy policy rejects a resource
- **THEN** no content is disclosed or passed to the domain member
- **AND** the client receives an explicit capability or validation outcome

### Requirement: Sensitive values remain undisclosed
Passwords, hidden values, and other configured sensitive values SHALL NOT be exposed through generic serialization, metadata, diagnostics, or errors.

#### Scenario: Sensitive value lacks a public representation
- **WHEN** GraphQL encounters a sensitive value
- **THEN** it does not serialize the value through fallback text
- **AND** any diagnostic identifies only the logical type and capability outcome permitted by policy

### Requirement: Value compatibility policy
Changes to generic fallback behavior SHALL include compatibility diagnostics and a documented migration path.

#### Scenario: Existing application relies on fallback input
- **WHEN** strict reversible-input enforcement detects an affected logical type
- **THEN** the application receives an actionable diagnostic and registration path
- **AND** the configured compatibility behavior is deterministic
