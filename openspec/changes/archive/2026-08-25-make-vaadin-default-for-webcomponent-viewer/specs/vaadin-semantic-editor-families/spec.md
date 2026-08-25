## MODIFIED Requirements

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
