## ADDED Requirements

### Requirement: Reversible advertised input values
Every rich GraphQL property or action input advertised as supported SHALL reconstruct the declared Causeway domain value from its GraphQL representation.

#### Scenario: Supported value round-trips
- **WHEN** a client submits the documented canonical representation for a supported value
- **THEN** GraphQL supplies the declared Java type to the domain member
- **AND** subsequent output uses the documented canonical representation

#### Scenario: No reversible strategy exists
- **WHEN** an input-capable member uses a value type without a reversible strategy
- **THEN** schema construction or capability discovery reports it as output-only or unsupported
- **AND** GraphQL does not pass an arbitrary raw string as the domain value

### Requirement: Standard GraphQL value discovery
Supported rich value semantics SHALL be discoverable through generated GraphQL scalar, input, and output types together with the existing rich datatype identity.

#### Scenario: Client inspects a supported datatype
- **WHEN** targeted introspection reaches the GraphQL type used by a property or parameter
- **THEN** the type shape or scalar documentation identifies its canonical representation
- **AND** the rich datatype identity distinguishes declared semantics that share a scalar

#### Scenario: Client discovers value support
- **WHEN** a value has no input mapping
- **THEN** the generated member does not imply reversible input capability
- **AND** no duplicate global datatype catalogue is required

### Requirement: Canonical standard datatype support
The default viewer SHALL provide deterministic coercion and serialization for the standard datatype set accepted from matrix entry `REF-VALUE-02` and the remaining scalar-shaped Causeway built-ins selected by the closed classification.

#### Scenario: Temporal or URL value is valid
- **WHEN** an accepted temporal or URL value uses its canonical representation
- **THEN** input and output preserve the documented value, precision, date, offset, zone, and normalization semantics that apply

#### Scenario: Protected or presentation value is mapped
- **WHEN** a supported password, markup, or local-resource-path value is encountered
- **THEN** password output remains suppressed, markup remains output-only, and local resource paths preserve path and open strategy
- **AND** none receives broader capability through generic fallback

#### Scenario: Standard value is malformed
- **WHEN** a client submits malformed canonical input
- **THEN** GraphQL returns a typed coercion error without invoking the domain member

### Requirement: Extensible custom values
Applications SHALL be able to register an explicit reversible marshaller and GraphQL type mapping for a custom value.

#### Scenario: Custom strategy is registered
- **WHEN** an application registers compatible output behavior and explicitly opts into reversible input
- **THEN** the rich schema advertises and uses that strategy for supported reads and inputs

#### Scenario: Custom strategy does not opt into input
- **WHEN** an existing or new application marshaller does not explicitly declare reversible input capability
- **THEN** the marshaller remains available for its declared output
- **AND** rich GraphQL reports its input as unsupported before domain invocation

#### Scenario: Custom strategy is absent
- **WHEN** an opaque custom value has no registered strategy
- **THEN** input is reported as unsupported rather than serialized and reconstructed through `toString()` guesses

### Requirement: Consistent resource-value capability
Supported resource values SHALL use a consistent metadata and bounded transfer contract across property reads, updates, action parameters, and action results.

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

### Requirement: Closed built-in value classification
Every framework-provided Causeway value semantics SHALL have an explicit rich GraphQL classification as reversible, structured, protected, output-only, or unsupported.

#### Scenario: Framework value is known
- **WHEN** the framework supplies a value-semantics provider
- **THEN** GraphQL applies its reviewed classification instead of generic fallback
- **AND** automated coverage records that classification

#### Scenario: Framework adds a value semantics
- **WHEN** a new framework-provided value-semantics provider has no GraphQL classification
- **THEN** classification coverage fails before release
- **AND** the new value cannot silently gain input or textual output capability

### Requirement: Unknown values fail closed
An unknown value without an explicit GraphQL strategy SHALL NOT use implicit input reconstruction or arbitrary `toString()` output by default.

#### Scenario: Unknown value is used as input
- **WHEN** an unknown value is declared by an input-capable member
- **THEN** its GraphQL input type rejects coercion without invoking the domain member

#### Scenario: Unknown value is returned
- **WHEN** an unknown value has no explicit output strategy
- **THEN** GraphQL returns only the documented non-disclosing unsupported representation
- **AND** it does not invoke the unknown value's `toString()` method

#### Scenario: Legacy output migration is selected
- **WHEN** an application explicitly selects the legacy string-output policy
- **THEN** unknown output uses the previous deterministic string behavior
- **AND** input remains unsupported

### Requirement: Sensitive values remain undisclosed
Passwords, hidden values, and other configured sensitive values SHALL NOT be exposed through generic serialization, metadata, resources, diagnostics, or errors.

#### Scenario: Sensitive value lacks a public representation
- **WHEN** GraphQL encounters a sensitive value
- **THEN** it does not serialize the value through fallback text
- **AND** any diagnostic identifies only the logical type and capability outcome permitted by policy

### Requirement: Value compatibility policy
Changes to generic fallback behavior SHALL include compatibility diagnostics and a documented migration path.

#### Scenario: Existing application marshaller relied on implicit input
- **WHEN** strict reversible-input enforcement detects a marshaller that did not opt into input
- **THEN** the application receives an actionable diagnostic and registration path
- **AND** the marshaller continues to provide output without receiving unreviewed input capability

#### Scenario: Existing application relies on fallback output
- **WHEN** strict output enforcement detects a logical type without an explicit strategy
- **THEN** the application receives an actionable registration path
- **AND** any temporary legacy string behavior requires explicit configuration
