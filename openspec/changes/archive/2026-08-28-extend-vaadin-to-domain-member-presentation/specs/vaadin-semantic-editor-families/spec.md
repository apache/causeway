## MODIFIED Requirements

### Requirement: Independent lazy family closures
Basic, numeric, and local-temporal Vaadin controls SHALL be delivered as independently lazy same-origin ESM closures with pinned deterministic policy metadata.
An unaffected route with no eligible read-only presentation or editor MUST request none of those closures.

#### Scenario: One family is first used by an editor
- **WHEN** an enabled eligible editor from one family connects before any read-only member from that family
- **THEN** only that family's closure is requested and upgraded
- **AND** checksum, gzip budget, entry points, licenses, telemetry opt-out, and exact style hashes match reviewed policy

#### Scenario: One family is first used by read-only presentation
- **WHEN** an enabled eligible read-only property from one family connects before any editor from that family
- **THEN** the same independently packaged family closure is requested and upgraded
- **AND** editor code does not require a second copy or closure for that family

#### Scenario: Other families are unused
- **WHEN** the active route uses no eligible read-only presentation or editor from another enabled family
- **THEN** the browser makes zero requests for that other closure
- **AND** route readiness does not wait for it

#### Scenario: Closure input drifts
- **WHEN** a direct or transitive version, integrity, license, vulnerability result, generated checksum, compressed size, entry point, telemetry behavior, or style hash differs from policy
- **THEN** verification fails with the changed closure identified
- **AND** packaging cannot silently publish the drifted asset

### Requirement: Family-scoped fallback and rollback
Each field family SHALL fail closed independently to the existing native semantic presentation or editor on unsupported shape, native component policy, module failure, definition failure, or policy rejection.
Fallback MUST preserve Causeway-owned authoritative and pending values and MUST require no GraphQL, route, persisted-data, or application-markup migration.

#### Scenario: Native component policy is configured
- **WHEN** the common component toolkit policy resolves to native
- **THEN** every qualified field family uses its established native read-only presentation and native editor
- **AND** no field-family asset is requested

#### Scenario: Family module fails for a read-only value
- **WHEN** an enabled field closure cannot load or define its required read-only control
- **THEN** only that family is disabled for the current document and the host rerenders through its authoritative native value renderer
- **AND** current value, description, errors, semantic events, and recoverable focus remain correct

#### Scenario: Family module fails for an editor
- **WHEN** an enabled field closure cannot load or define its required editor control
- **THEN** only that family is disabled for the current document and the host rerenders through its native semantic editor
- **AND** current pending value, required state, validation, semantic events, and recoverable focus are retained

#### Scenario: Reference and action families remain independent
- **WHEN** a field family fails while reference and action adapters remain eligible
- **THEN** reference and action loading and behavior remain unchanged
- **AND** the failure does not broaden or eagerly request another closure

#### Scenario: Deprecated subset compatibility is active
- **WHEN** the component and editor common properties are absent and old family configuration enables only a subset
- **THEN** only that normalized editor subset remains eligible
- **AND** read-only presentation and omitted editor families preserve native behavior during the compatibility period

### Requirement: Strict security accessibility and presentation qualification
Every adopted family SHALL pass enforcing strict-CSP, read-only and editor accessibility, keyboard, responsive, theme, reduced-motion, forced-colors, lifecycle, console, page-error, external-request, and overflow qualification.
The policy MUST retain `style-src-attr 'none'` and MUST NOT add blanket `unsafe-inline`.

#### Scenario: Read-only and editor qualification matrix runs
- **WHEN** controls connect, display, focus, enter and leave editing, validate, disable, reconnect, switch theme, and encounter representative errors
- **THEN** there are zero unexpected CSP, accessibility, console, page, external-request, duplicate-control, stale-state, or overflow failures
- **AND** accessible naming, description, keyboard, focus, authoritative values, and fallback behavior match the semantic contract

#### Scenario: Candidate requires unapproved policy
- **WHEN** a control requires blanket inline style permission, external assets, telemetry, Pro code, Flow state, or another excluded capability
- **THEN** that family fails qualification and remains native
- **AND** no broader policy is enabled to make the candidate appear successful
