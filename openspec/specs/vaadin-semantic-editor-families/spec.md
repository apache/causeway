# Vaadin Semantic Editor Families Specification

## Purpose

Define qualified internal Vaadin free-core adapters for reversible basic, numeric, and local-temporal semantic editors while preserving Causeway-owned contracts, strict delivery policy, and native fallback.

## Requirements

### Requirement: Codec-qualified internal field adapters
The viewer SHALL use internal Vaadin free-core adapters by default only for semantic input families whose existing Causeway value codec is reversible and whose candidate control preserves the advertised value shape.
Application markup and events MUST remain Causeway-owned and MUST NOT require raw Vaadin APIs.

#### Scenario: Basic scalar family is eligible
- **WHEN** the resolved Vaadin policy is active and a semantic editor advertises text, multiline text, protected text, Boolean, enum, or bounded scalar choices with a reversible codec
- **THEN** the registry selects the internal basic adapter
- **AND** the application continues to observe the existing Causeway element and semantic interaction contract

#### Scenario: Numeric family is eligible
- **WHEN** the resolved Vaadin policy is active and a semantic editor advertises an exact or machine numeric codec
- **THEN** exact numbers retain lexical text handling while machine numbers may use numeric controls
- **AND** GraphQL receives the value produced by the existing codec without additional JavaScript number coercion

#### Scenario: Local temporal family is eligible
- **WHEN** the resolved Vaadin policy is active and the value is `LocalDate`, or a `LocalTime` or `LocalDateTime` representable at millisecond precision
- **THEN** the registry selects the corresponding internal picker
- **AND** local value and supported fractional precision survive the semantic pending-value and GraphQL path

#### Scenario: Shape is not qualified
- **WHEN** the value has local temporal precision beyond milliseconds, is offset-bearing, zoned, legacy temporal, resource, custom, reference, collection, or lacks a reversible codec
- **THEN** the field-family adapter is ineligible
- **AND** the existing native or explicit unsupported presentation remains authoritative

#### Scenario: Native policy is selected
- **WHEN** the resolved common toolkit policy is native
- **THEN** every basic, numeric, and local-temporal descriptor is ineligible for the Vaadin adapter
- **AND** no field closure is imported

### Requirement: Causeway-owned interaction semantics
Each field adapter SHALL map Causeway labels, descriptions, required and disabled state, validation, pending values, focus, keyboard clearing, cancellation, and semantic events into its internal control.
It MUST NOT establish a second interaction or validation state machine.

#### Scenario: User changes an editable property
- **WHEN** an internal control emits a value change
- **THEN** the owning Causeway property interaction parses, validates, and saves through its existing context
- **AND** success, failure, cancellation, focus restoration, and semantic events match native behavior

#### Scenario: User changes an action parameter
- **WHEN** an internal control changes a prepared object or service action parameter
- **THEN** the interaction controller updates that parameter through the existing argument negotiation path
- **AND** dependent parameter preparation and submission emit only advertised arguments

#### Scenario: Optional field exposes clearing
- **WHEN** a qualified optional non-protected field has a non-empty value and presents a visible clear affordance
- **THEN** keyboard users can reach that affordance through normal forward and reverse Tab navigation
- **AND** its accessible name identifies the semantic field being cleared
- **AND** keyboard or pointer activation clears through the existing Causeway pending-value path

#### Scenario: Keyboard user clears a field
- **WHEN** the keyboard user activates the Causeway-owned clear affordance
- **THEN** the now-empty clear affordance is removed from the tab sequence
- **AND** focus returns to the field without cancelling the interaction, submitting it, or exposing a second value state

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
Each field family SHALL fail closed independently to the existing native semantic editor on unsupported shape, common native policy, module failure, definition failure, or policy rejection.
Fallback MUST preserve Causeway-owned pending values and MUST require no GraphQL, route, persisted-data, or application-markup migration.

#### Scenario: Native policy is configured
- **WHEN** the common toolkit policy resolves to native
- **THEN** every qualified field family uses its established native editor
- **AND** no field-family asset is requested

#### Scenario: Family module fails to load
- **WHEN** an enabled field closure cannot load or define its required control
- **THEN** only that family is disabled for the current document and the host rerenders natively
- **AND** current pending value, required state, validation, semantic events, and recoverable focus are retained

#### Scenario: Reference family remains independent
- **WHEN** a field family fails while the default reference adapter remains eligible
- **THEN** reference loading and behavior remain unchanged
- **AND** the failure does not broaden or eagerly request another closure

#### Scenario: Deprecated subset compatibility is active
- **WHEN** the common property is absent and old family configuration enables only a subset
- **THEN** only that normalized subset remains eligible
- **AND** omitted families preserve the former native behavior during the compatibility period

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
