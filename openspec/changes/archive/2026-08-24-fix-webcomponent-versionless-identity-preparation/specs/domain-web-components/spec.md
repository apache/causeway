## ADDED Requirements

### Requirement: Advertised semantic identity projection
The component library SHALL derive metadata selection for concrete object values and rows from the effective metadata type advertised by targeted introspection.
It MUST preserve advertised identity and presentation fields, MUST treat `version` as optional, and MUST NOT request, synthesize, or infer absent metadata fields.

#### Scenario: Versioned object value is read
- **WHEN** a concrete property, choice, autocomplete result, preparation value, action result, or collection row advertises `id`, `logicalTypeName`, `title`, and `version`
- **THEN** the operation selects those advertised fields
- **AND** the semantic value preserves the returned version

#### Scenario: Versionless object value is read
- **WHEN** a concrete property, choice, autocomplete result, preparation value, action result, or collection row advertises `id`, `logicalTypeName`, and `title` but not `version`
- **THEN** the operation omits `version`
- **AND** the value remains available for supported rendering, pending-value conversion, validation, hydration, refresh, and navigation

#### Scenario: Metadata lacks semantic identity minimums
- **WHEN** an effective metadata type does not advertise both `id` and `logicalTypeName`
- **THEN** the component does not claim a navigable bookmark or hydratable object identity
- **AND** it exposes only a valid bounded projection or the existing local unsupported state

#### Scenario: Versionless value participates in preparation
- **WHEN** a property or action parameter preparation field returns a concrete versionless object through default, choices, autocomplete, validity, or datatype semantics
- **THEN** the context selects only metadata fields advertised for that object
- **AND** cancellation and stale-response protection remain unchanged

#### Scenario: Versionless collection rows are loaded
- **WHEN** a concrete collection element type advertises semantic identity without `version`
- **THEN** row selection merges the advertised identity fields with requested semantic columns
- **AND** the rows remain hydratable without manufacturing a concurrency token

#### Scenario: Abstract row type requires fragments
- **WHEN** an interface or union row cannot expose semantic metadata without concrete fragments
- **THEN** the component retains a bounded non-navigable or partial-error result
- **AND** does not treat versionless concrete-object support as general union-projection support
