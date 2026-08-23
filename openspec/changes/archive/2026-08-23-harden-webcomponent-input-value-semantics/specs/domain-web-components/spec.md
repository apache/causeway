## ADDED Requirements

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
