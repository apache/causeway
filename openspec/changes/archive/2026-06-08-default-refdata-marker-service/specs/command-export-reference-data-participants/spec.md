## MODIFIED Requirements

### Requirement: Applications classify replay reference data through an SPI
The command-log extension SHALL provide a public SPI that allows a consuming application to decide whether a bookmarked entity is replay reference data.
The SPI SHALL receive the entity bookmark or equivalent logical type and identifier information needed to classify the entity without requiring the entity to have appeared on the exported dotted path.
The command-log extension SHALL register its built-in `RefData` marker classifier as a default SPI implementation.
The system SHALL treat an entity as replay reference data when at least one registered SPI implementation classifies its bookmark as reference data.
The system MUST NOT treat an entity as replay reference data when no registered SPI implementation classifies it as reference data.
The system SHALL allow zero, one, or many application-provided SPI implementations to be registered in addition to the default marker classifier.

#### Scenario: Single implementation classifies a bookmark as reference data
- **GIVEN** a consuming application registers one replay reference-data SPI implementation
- **AND** the implementation classifies bookmark `demoCategory:STD` as reference data
- **WHEN** command export validation asks whether `demoCategory:STD` is reference data
- **THEN** the system treats `demoCategory:STD` as replay reference data

#### Scenario: Default marker implementation classifies a bookmark as reference data
- **GIVEN** bookmark `demoCategory:STD` resolves to a domain class that implements `RefData`
- **WHEN** command export validation asks whether `demoCategory:STD` is reference data
- **THEN** the system treats `demoCategory:STD` as replay reference data

#### Scenario: Multiple implementations are consulted
- **GIVEN** a consuming application registers two replay reference-data SPI implementations
- **AND** the default marker classifier does not classify bookmark `demoCategory:STD` as reference data
- **AND** the first application implementation does not classify bookmark `demoCategory:STD` as reference data
- **AND** the second application implementation classifies bookmark `demoCategory:STD` as reference data
- **WHEN** command export validation asks whether `demoCategory:STD` is reference data
- **THEN** the system treats `demoCategory:STD` as replay reference data

#### Scenario: No implementation accepts a bookmark
- **GIVEN** a consuming application registers replay reference-data SPI implementations
- **AND** the default marker classifier does not classify bookmark `demoCustomer:1` as reference data
- **AND** none of the application implementations classify bookmark `demoCustomer:1` as reference data
- **WHEN** command export validation asks whether `demoCustomer:1` is reference data
- **THEN** the system does not treat `demoCustomer:1` as replay reference data

#### Scenario: No application implementations are registered
- **GIVEN** no application-provided replay reference-data SPI implementation is registered
- **AND** the default marker classifier does not classify bookmark `demoCategory:STD` as reference data
- **WHEN** command export validation asks whether bookmark `demoCategory:STD` is reference data
- **THEN** the system does not treat `demoCategory:STD` as replay reference data
