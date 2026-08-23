## ADDED Requirements

### Requirement: Advertised scalar and runtime value agreement
For every input capability advertised by rich GraphQL, the runtime SHALL unmarshal the accepted GraphQL representation into the declared domain Java value type before validation or invocation.
The schema and runtime MUST reject malformed input consistently and MUST NOT advertise a scalar representation that reaches domain invocation as an incompatible raw value.

#### Scenario: Advertised exact numeric string is submitted
- **WHEN** introspection advertises a string input for `Long`, `BigInteger`, or `BigDecimal` and a client submits a valid canonical string
- **THEN** validation and invocation receive the corresponding exact Java numeric type
- **AND** no intermediate binary floating-point conversion changes the value

#### Scenario: Advertised temporal scalar is submitted
- **WHEN** a client submits a valid value through the temporal scalar advertised by introspection
- **THEN** validation and invocation receive the declared local, offset, or zoned Java temporal type
- **AND** its documented offset, zone, and precision semantics are preserved

#### Scenario: Runtime conversion cannot satisfy the declaration
- **WHEN** the accepted GraphQL representation cannot be converted to the declared domain type
- **THEN** GraphQL returns a bounded input or validation error before domain invocation
- **AND** does not expose implementation exceptions or invoke with an incompatible value

### Requirement: Canonical exact numeric input contract
Rich GraphQL SHALL define and enforce canonical lexical input contracts for `Long`, `BigInteger`, and `BigDecimal` that preserve every accepted domain value.
The contract MUST state sign, digit, decimal, exponent, scale, range, and null behavior for each type.

#### Scenario: Boundary integer is submitted
- **WHEN** a client submits an accepted `Long` or `BigInteger` boundary outside JavaScript's safe integer range
- **THEN** GraphQL round-trips the exact integer without digit loss
- **AND** the domain receives the exact declared integer type

#### Scenario: Precision-sensitive decimal is submitted
- **WHEN** a client submits a decimal with significant trailing zeros or precision beyond binary floating point
- **THEN** GraphQL preserves the accepted lexical scale and exact decimal value through validation and invocation
- **AND** authoritative output follows the documented normalization policy

#### Scenario: Exact numeric input is malformed
- **WHEN** a numeric lexical value violates the declared grammar or range
- **THEN** GraphQL rejects it with a bounded input error
- **AND** no truncated, rounded, overflowed, or partially parsed value reaches domain code

### Requirement: Complete temporal input catalogue
Rich GraphQL SHALL advertise a reversible input contract for every built-in temporal type classified as input-capable.
The contract MUST distinguish local values, offset-bearing values, named-zone values, legacy values, and configured formats.

#### Scenario: Offset is significant
- **WHEN** an offset-bearing value is submitted
- **THEN** GraphQL preserves the submitted instant and offset according to the advertised scalar contract
- **AND** does not replace the offset with the server or client default timezone

#### Scenario: Zone identifier is significant
- **WHEN** a named-zone value is submitted
- **THEN** GraphQL preserves or deterministically normalizes both the instant and zone identifier according to the advertised format
- **AND** rejects a value that omits required zone information

#### Scenario: Temporal precision is significant
- **WHEN** a temporal value contains accepted fractional seconds
- **THEN** GraphQL preserves that precision through unmarshalling, validation, invocation, and authoritative output

### Requirement: Nullable and protected input contract
Rich GraphQL SHALL preserve nullable Boolean `null` independently from `false` and SHALL expose protected values only through explicitly authorized input capabilities.
Protected output and errors MUST remain undisclosed.

#### Scenario: Nullable Boolean is submitted
- **WHEN** the declared Boolean input is nullable and the client submits `null`
- **THEN** domain validation and invocation receive `null`
- **AND** no default coercion changes it to `false`

#### Scenario: Protected input is submitted
- **WHEN** an authorized write-only protected input receives a new value
- **THEN** GraphQL invokes the declared member without returning the prior or submitted secret
- **AND** validation and error payloads contain only safe bounded messages

### Requirement: Reversible resource and custom input strategies
Rich GraphQL SHALL advertise Blob, Clob, URL, local-resource, and custom-value input only when a registered strategy can reconstruct the declared domain value under the public authorization, media, size, and representation constraints.
Output formatting alone MUST NOT imply input capability.

#### Scenario: Resource input satisfies its contract
- **WHEN** an authorized client submits a resource value within advertised media and size constraints
- **THEN** GraphQL reconstructs the declared resource domain type and invokes the member
- **AND** authoritative output uses the existing safe resource-link policy

#### Scenario: Custom reversible strategy is registered
- **WHEN** a custom value strategy explicitly supports reversible input and the client submits its advertised representation
- **THEN** GraphQL reconstructs the declared custom domain type before invocation
- **AND** the strategy's bounded validation errors are preserved

#### Scenario: Input strategy is absent
- **WHEN** a resource or custom value has output behavior but no reversible input strategy
- **THEN** schema construction or capability discovery marks it output-only or unsupported
- **AND** GraphQL never passes generic map, string, byte, or character data to the domain member speculatively
