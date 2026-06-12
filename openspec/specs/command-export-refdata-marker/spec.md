# command-export-refdata-marker Specification

## Purpose
TBD - created by archiving change default-refdata-marker-service. Update Purpose after archive.
## Requirements
### Requirement: Marker interface declares replay reference data
The command-log extension SHALL provide a public `RefData` marker interface for domain classes whose bookmarked instances are replay reference data.
A class that implements `RefData` SHALL declare that its instances have stable logical type and identifier values expected to exist in replay environments.
The marker interface MUST NOT require methods or entity loading during command export validation.

#### Scenario: Domain class implements marker
- **GIVEN** a domain class implements `RefData`
- **WHEN** command export validation classifies a bookmark whose logical type resolves to that class
- **THEN** the bookmark is classified as replay reference data

#### Scenario: Domain class does not implement marker
- **GIVEN** a domain class does not implement `RefData`
- **WHEN** command export validation classifies a bookmark whose logical type resolves to that class
- **THEN** the marker-based classifier does not classify the bookmark as replay reference data

### Requirement: Default service classifies marker implementations using the metamodel
The command-log extension SHALL provide a default `CommandReplayReferenceDataService` implementation for the `RefData` marker interface.
The default implementation SHALL extract the bookmark logical type name and use `SpecificationLoader` to obtain the corresponding `ObjectSpecification`.
The default implementation SHALL obtain the `ObjectSpecification` corresponding class and classify the bookmark as replay reference data when that class implements `RefData`.
The default implementation MUST NOT load, instantiate, or resolve the bookmarked entity instance.
When the logical type cannot be resolved to an object specification or corresponding class, the default implementation MUST NOT classify the bookmark as replay reference data.

#### Scenario: Bookmark resolves to marker class
- **GIVEN** bookmark `demoCategory:STD` has logical type `demoCategory`
- **AND** `SpecificationLoader` resolves `demoCategory` to an object specification whose corresponding class implements `RefData`
- **WHEN** the default service classifies `demoCategory:STD`
- **THEN** the service returns `true`

#### Scenario: Bookmark resolves to non-marker class
- **GIVEN** bookmark `demoCustomer:1` has logical type `demoCustomer`
- **AND** `SpecificationLoader` resolves `demoCustomer` to an object specification whose corresponding class does not implement `RefData`
- **WHEN** the default service classifies `demoCustomer:1`
- **THEN** the service returns `false`

#### Scenario: Bookmark logical type is unknown
- **GIVEN** bookmark `missingType:1` has a logical type that `SpecificationLoader` cannot resolve to an object specification
- **WHEN** the default service classifies `missingType:1`
- **THEN** the service returns `false`

#### Scenario: Entity instance is not loaded
- **GIVEN** bookmark `demoCategory:STD` has logical type `demoCategory`
- **WHEN** the default service classifies the bookmark
- **THEN** the service uses metamodel type information only
- **AND** the service does not load the entity identified by `STD`

