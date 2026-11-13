# Command Export Reference-Data Marker Specification

## Purpose

Define the public reference-data marker, its metamodel-backed default command-replay classifier, and the built-in SecMan identities that opt into stable cross-environment classification.
## Requirements
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

The SecMan applib `ApplicationUser`, `ApplicationRole`, `ApplicationTenancy`, and `ApplicationPermission` domain abstractions SHALL implement `RefData`, and the permission-feature reference view-model `ApplicationFeatureChoices.AppFeat` SHALL also implement `RefData`. No persistence field or schema change SHALL be required by these marker declarations. A bookmark whose logical type resolves to any of these designated SecMan types SHALL therefore be classified as replay reference data by the default classifier.

#### Scenario: SecMan domain type is classified by the default service

- **WHEN** a bookmark resolves to any of the four designated SecMan domain abstractions
- **THEN** the default classifier identifies it as replay reference data

#### Scenario: Permission-feature reference view-model is classified as reference data

- **WHEN** a bookmark resolves to the `ApplicationFeatureChoices.AppFeat` permission-feature reference view-model
- **THEN** the default classifier identifies it as replay reference data
- **AND** no domain object is loaded to make that classification

#### Scenario: Permission-feature command is a known export participant

- **GIVEN** a command whose target or reference parameter is an `AppFeat` bookmark
- **WHEN** export reachability is evaluated
- **THEN** the `AppFeat` participant is a known export participant with no prior result establishing that bookmark

#### Scenario: Marker declarations require no schema change

- **WHEN** the SecMan abstractions and the `AppFeat` view-model implement `RefData`
- **THEN** no persistence field or schema change is required

