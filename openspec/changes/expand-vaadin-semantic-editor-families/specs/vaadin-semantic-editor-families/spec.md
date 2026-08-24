## ADDED Requirements

### Requirement: Codec-qualified internal field adapters
The viewer SHALL provide internal Vaadin free-core adapters only for semantic input families whose existing Causeway value codec is reversible and whose candidate control preserves the advertised value shape.
Application markup and events MUST remain Causeway-owned and MUST NOT require raw Vaadin APIs.

#### Scenario: Basic scalar family is eligible
- **WHEN** explicit configuration enables the basic family and a semantic editor advertises text, multiline text, protected text, Boolean, enum, or bounded scalar choices with a reversible codec
- **THEN** the registry selects the internal basic adapter
- **AND** the application continues to observe the existing Causeway element and semantic interaction contract

#### Scenario: Numeric family is eligible
- **WHEN** explicit configuration enables the numeric family and a semantic editor advertises an exact or machine numeric codec
- **THEN** exact numbers retain lexical text handling while machine numbers may use numeric controls
- **AND** GraphQL receives the value produced by the existing codec without additional JavaScript number coercion

#### Scenario: Local temporal family is eligible
- **WHEN** explicit configuration enables the local-temporal family and the value is `LocalDate`, `LocalTime`, or `LocalDateTime`
- **THEN** the registry selects the corresponding internal picker
- **AND** local value and supported fractional precision survive the semantic pending-value and GraphQL path

#### Scenario: Shape is not qualified
- **WHEN** the value is offset-bearing, zoned, legacy temporal, resource, custom, reference, collection, or lacks a reversible codec
- **THEN** the field-family adapter is ineligible
- **AND** the existing native or explicit unsupported presentation remains authoritative

### Requirement: Causeway-owned interaction semantics
Each field adapter SHALL map Causeway labels, descriptions, required and disabled state, validation, pending values, focus, cancellation, and semantic events into its internal control.
It MUST NOT establish a second interaction or validation state machine.

#### Scenario: User changes an editable property
- **WHEN** an internal control emits a value change
- **THEN** the owning Causeway property interaction parses, validates, and saves through its existing context
- **AND** success, failure, cancellation, focus restoration, and semantic events match native behavior

#### Scenario: User changes an action parameter
- **WHEN** an internal control changes a prepared object or service action parameter
- **THEN** the interaction controller updates that parameter through the existing argument negotiation path
- **AND** dependent parameter preparation and submission emit only advertised arguments

#### Scenario: Validation rejects a value
- **WHEN** client codec or authoritative GraphQL validation rejects the pending value
- **THEN** the Causeway host owns the bounded error and invalid state
- **AND** the internal control cannot submit or display a successful-looking alternative value

#### Scenario: Protected text is edited
- **WHEN** a protected semantic editor is prepared, changed, validated, cancelled, fails, or submits
- **THEN** its prior or pending value is absent from markup, semantic events, errors, diagnostics, operation summaries, and route evidence
- **AND** the internal control initializes empty with appropriate password-autocomplete behavior

### Requirement: Independent lazy family closures
Basic, numeric, and local-temporal Vaadin controls SHALL be delivered as independently lazy same-origin ESM closures with pinned deterministic policy metadata.
An unaffected route MUST request none of those closures.

#### Scenario: One family is first used
- **WHEN** an enabled eligible editor from one family connects
- **THEN** only that family's closure is requested and upgraded
- **AND** checksum, gzip budget, entry points, licenses, telemetry opt-out, and exact style hashes match reviewed policy

#### Scenario: Other families are unused
- **WHEN** the active route uses no eligible editor from another enabled family
- **THEN** the browser makes zero requests for that other closure
- **AND** route readiness does not wait for it

#### Scenario: Closure input drifts
- **WHEN** a direct or transitive version, integrity, license, vulnerability result, generated checksum, compressed size, entry point, telemetry behavior, or style hash differs from policy
- **THEN** verification fails with the changed closure identified
- **AND** packaging cannot silently publish the drifted asset

### Requirement: Family-scoped fallback and rollback
Each field family SHALL fail closed independently to the existing native semantic editor on unsupported shape, explicit disablement, module failure, definition failure, or policy rejection.
Fallback MUST preserve Causeway-owned pending values and MUST require no GraphQL, route, persisted-data, or application-markup migration.

#### Scenario: Family is disabled
- **WHEN** configuration omits a field family
- **THEN** every member in that family uses its existing native editor
- **AND** no asset for the omitted family is requested

#### Scenario: Family module fails to load
- **WHEN** an enabled field closure cannot load or define its required control
- **THEN** only that family is disabled for the current document and the host rerenders natively
- **AND** current pending value, required state, validation, semantic events, and recoverable focus are retained

#### Scenario: Reference family remains independent
- **WHEN** a field family fails or is disabled while the separately configured reference adapter remains eligible
- **THEN** reference loading and behavior remain unchanged
- **AND** the failure does not broaden or eagerly request another closure

### Requirement: Strict security accessibility and presentation qualification
Every adopted family SHALL pass enforcing strict-CSP, accessibility, keyboard, responsive, theme, reduced-motion, forced-colors, lifecycle, console, page-error, external-request, and overflow qualification.
The policy MUST retain `style-src-attr 'none'` and MUST NOT add blanket `unsafe-inline`.

#### Scenario: Qualification matrix runs
- **WHEN** controls connect, focus, edit, validate, disable, reconnect, switch theme, and encounter representative errors
- **THEN** there are zero unexpected CSP, accessibility, console, page, external-request, or overflow failures
- **AND** keyboard and focus behavior remain operable at supported viewport and preference combinations

#### Scenario: Candidate requires unapproved policy
- **WHEN** a control requires blanket inline style permission, external assets, telemetry, Pro code, Flow state, or another excluded capability
- **THEN** that family fails qualification and remains native
- **AND** no broader policy is enabled to make the candidate appear successful
