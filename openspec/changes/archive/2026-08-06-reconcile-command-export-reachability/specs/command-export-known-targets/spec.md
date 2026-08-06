## ADDED Requirements

### Requirement: Export participants follow baseline-bounded reachability
When command-log recording support is `ENABLED`, the system SHALL classify every target and reference-valued action parameter of a command in the unified manager context as known or unknown. A participant SHALL be known only when its bookmark is an export root, is application-declared replay reference data, or is the recorded result of an eligible, non-excluded command earlier in the manager's current baseline-bounded command order. A later result and a result before the baseline MUST NOT establish knowledge for the evaluated command. Scalar parameters MUST NOT be treated as reachability participants. Classification MUST NOT block command recording or mutate command state.

#### Scenario: Earlier result establishes a target
- **GIVEN** recording support is enabled and a finder in the manager sequence records result `demo.Customer:1`
- **WHEN** a later command in that sequence targets `demo.Customer:1`
- **THEN** the target is known

#### Scenario: Later result does not establish an earlier parameter
- **GIVEN** recording support is enabled and a command has reference parameter `customer=demo.Customer:1`
- **WHEN** only a later command in manager order records result `demo.Customer:1`
- **THEN** the parameter is unknown for the earlier command

#### Scenario: Result before baseline is unavailable
- **GIVEN** a command before the manager baseline records result `demo.Customer:1`
- **WHEN** a command at or after the baseline targets `demo.Customer:1` and no root rule applies
- **THEN** the target is unknown

#### Scenario: Reference data is immediately known
- **GIVEN** a registered replay reference-data classifier accepts `demo.Category:STD`
- **WHEN** a command targets or receives reference parameter `demo.Category:STD`
- **THEN** that participant is known without an earlier result

#### Scenario: Scalar parameter creates no reachability edge
- **WHEN** an action command has scalar parameter `quantity=2`
- **THEN** the scalar parameter does not require participant classification

### Requirement: Export roots are domain-service logical types
A bookmark SHALL be an export root when its logical type resolves to a Causeway metamodel specification classified as a domain service. Root classification SHALL use logical-type metadata and MUST NOT load the bookmarked object. An ordinary persisted object MUST NOT become an export root merely because it resolves locally or appears in the service registry. Missing logical-type metadata SHALL produce non-root classification unless the reference-data SPI independently accepts the bookmark.

#### Scenario: Domain service is an export root
- **GIVEN** bookmark `demo.CustomerMenu:1` identifies a metamodel type classified as a domain service
- **WHEN** the bookmark is classified for reachability
- **THEN** it is an export root without an earlier command result

#### Scenario: Resolvable entity is not an export root
- **GIVEN** bookmark `demo.Customer:1` identifies an ordinary persisted object that can resolve locally
- **WHEN** no earlier result or reference-data classifier establishes the bookmark
- **THEN** it remains unknown

#### Scenario: Root classification does not load an object
- **WHEN** the system classifies a bookmark whose logical type is present in the metamodel
- **THEN** it uses the type's domain-service classification without resolving the bookmark instance

### Requirement: Participant validation identifies the first failure
The reachability validator SHALL evaluate target participants before reference-parameter participants and SHALL report the first unknown participant in command order. A target failure SHALL identify the command and target bookmark. A parameter failure SHALL identify the command, parameter name or stable positional fallback, and bookmark. The result SHALL be reusable by later export enforcement without changing existing YAML actions in R2.

#### Scenario: Unknown target is identified
- **WHEN** a command target is neither a root, reference data, nor an earlier result
- **THEN** validation identifies that command and target bookmark as the first failure

#### Scenario: Unknown parameter is identified
- **WHEN** a command's named reference parameter is neither a root, reference data, nor an earlier result
- **THEN** validation identifies the command, parameter name, and parameter bookmark

#### Scenario: R2 does not enforce YAML export
- **WHEN** reachability is computed during R2
- **THEN** existing YAML export and import behavior remains unchanged
