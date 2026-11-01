## ADDED Requirements

### Requirement: Versionless identity regression coverage
The pinned Reference Application suite SHALL exercise representative concrete view models and collection rows whose rich metadata omits `version` while preserving stable semantic identity.
It MUST distinguish corrected versionless projection behavior from retained polymorphic-union and opaque-route gaps.

#### Scenario: Versionless preparation target is exercised
- **WHEN** the browser prepares representative action or property interaction semantics that return concrete versionless values
- **THEN** defaults, choices, autocomplete, validation, and successful submission remain usable without a missing-version GraphQL error
- **AND** invalid, cancelled, and stale interactions retain their established behavior

#### Scenario: Versionless property or action result is rendered
- **WHEN** a representative concrete view model is returned as a property value or typed action outcome
- **THEN** its advertised identity and title remain renderable and navigable through existing semantic contracts
- **AND** no `_meta.version` selection is submitted for its versionless metadata type

#### Scenario: Versionless collection row is rendered
- **WHEN** a representative concrete view-model collection is activated
- **THEN** its row window reaches the supported ready or documented partial state with stable identities and requested columns
- **AND** a missing version field is not the cause of any row error

#### Scenario: Unrelated retained gaps remain classified
- **WHEN** the same qualification run reaches a polymorphic union target or a long opaque composite bookmark
- **THEN** the suite retains the corresponding bounded known-gap assertion until its focused change is implemented
- **AND** does not count it as evidence that concrete versionless identity failed
