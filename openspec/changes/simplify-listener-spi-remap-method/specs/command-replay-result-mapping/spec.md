## MODIFIED Requirements

### Requirement: Replay mapping SPI can remap command targets before execution
The system SHALL ask the command replay mapping SPI's common remap method whether each replayed command target bookmark should be remapped before command execution.
When the SPI provides a replacement target bookmark, the system SHALL execute the replayed command using the replacement target identifier.
When no SPI implementation exists or no replacement is provided, the system SHALL execute the replayed command using the recorded target identifier.
The system SHALL use the same remap method for command targets and reference action parameters.

#### Scenario: Target bookmark is remapped
- **WHEN** command replay is about to execute a command whose recorded target bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement bookmark `demoCustomer:2` from its common remap method
- **THEN** the system executes the replayed command against target bookmark `demoCustomer:2`

#### Scenario: Target bookmark is not remapped
- **WHEN** command replay is about to execute a command whose recorded target bookmark is `demoCustomer:1`
- **AND** no command replay mapping SPI provides a replacement bookmark from its common remap method
- **THEN** the system executes the replayed command against target bookmark `demoCustomer:1`

### Requirement: Replay mapping SPI can remap reference action parameters before execution
The system SHALL ask the command replay mapping SPI's common remap method whether each replayed action parameter represented as `type: "reference"` with a populated `reference` OID should be remapped before command execution.
When the SPI provides a replacement reference bookmark, the system SHALL execute the replayed command using the replacement parameter reference.
When no SPI implementation exists or no replacement is provided, the system SHALL execute the replayed command using the recorded parameter reference.
The system MUST leave non-reference action parameters unchanged by this reference remapping flow.
The system MUST NOT require the SPI to receive parameter name or parameter index metadata to remap a reference action parameter.

#### Scenario: Reference action parameter is remapped
- **WHEN** command replay is about to execute an action command with parameter `simpleObject` recorded as `reference.type: "simple.SimpleObject"` and `reference.id: "1"`
- **AND** the command replay mapping SPI returns replacement bookmark `simple.SimpleObject:2` from its common remap method
- **THEN** the system executes the replayed command with parameter `simpleObject` set to `reference.type: "simple.SimpleObject"` and `reference.id: "2"`

#### Scenario: Reference action parameter is not remapped
- **WHEN** command replay is about to execute an action command with parameter `simpleObject` recorded as `reference.type: "simple.SimpleObject"` and `reference.id: "1"`
- **AND** no command replay mapping SPI provides a replacement bookmark from its common remap method
- **THEN** the system executes the replayed command with parameter `simpleObject` set to `reference.type: "simple.SimpleObject"` and `reference.id: "1"`

#### Scenario: Non-reference action parameter is not remapped by reference flow
- **WHEN** command replay is about to execute an action command with a parameter that is not represented as `type: "reference"`
- **THEN** the system does not ask the command replay mapping SPI's common remap method to replace that parameter
