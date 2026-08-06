## ADDED Requirements

### Requirement: Public marker declares stable replay reference data

The applib SHALL provide a public, dependency-neutral `RefData` marker interface for domain types whose bookmarked instances are stable replay reference data. The marker SHALL require no methods. Implementing it SHALL assert that the type's logical type and bookmark identifiers are well-known and expected to exist in every replay environment.

#### Scenario: Application domain type opts into reference-data classification

- **WHEN** an application domain type implements `RefData`
- **THEN** it declares its bookmarked instances to be stable replay reference data without depending on commandlog

### Requirement: Default classifier recognizes marker implementations using the metamodel

The commandlog extension SHALL provide and register a default `CommandReplayReferenceDataService` implementation for `RefData`. The classifier SHALL resolve a non-null bookmark through `SpecificationLoader`, inspect the corresponding class from its `ObjectSpecification`, and return `true` when `RefData` is assignable from that class. It MUST return `false` for null bookmarks, unknown logical types, absent specifications, or non-marker classes, and MUST NOT load or instantiate the bookmarked object.

#### Scenario: Bookmark resolves to a marker type

- **WHEN** a bookmark resolves to an object specification whose corresponding class implements `RefData`
- **THEN** the default classifier returns `true`

#### Scenario: Bookmark resolves to a non-marker type

- **WHEN** a bookmark resolves to an object specification whose corresponding class does not implement `RefData`
- **THEN** the default classifier returns `false`

#### Scenario: Bookmark type is unknown

- **WHEN** a bookmark cannot be resolved to an object specification and corresponding class
- **THEN** the default classifier returns `false`

#### Scenario: Classification uses type information only

- **WHEN** the default classifier evaluates a bookmark
- **THEN** it consults metamodel type information without loading or instantiating the bookmarked object

#### Scenario: Default and custom classifiers coexist

- **WHEN** the default marker classifier rejects a bookmark and an application classifier accepts it
- **THEN** composed reference-data classification returns `true`

### Requirement: Built-in SecMan identities are declared as reference data

The SecMan applib `ApplicationUser`, `ApplicationRole`, `ApplicationTenancy`, and `ApplicationPermission` domain abstractions SHALL implement `RefData`. No persistence field or schema change SHALL be required by these marker declarations.

#### Scenario: SecMan domain type is classified by the default service

- **WHEN** a bookmark resolves to any of the four designated SecMan domain abstractions
- **THEN** the default marker classifier returns `true`

#### Scenario: Marker declarations do not alter persistence

- **WHEN** SecMan domain abstractions implement `RefData`
- **THEN** their persistent state and datastore schemas remain unchanged
