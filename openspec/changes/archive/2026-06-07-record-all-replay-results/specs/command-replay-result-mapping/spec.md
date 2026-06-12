## MODIFIED Requirements

### Requirement: Replay mapping SPI can remap command targets before execution
The system SHALL ask the command replay mapping SPI's common lookup method whether each replayed command target bookmark has a replacement before command execution.
When the SPI provides a replacement target bookmark, the system SHALL execute the replayed command using the replacement target identifier.
When no SPI implementation exists or no replacement is provided, the system SHALL execute the replayed command using the recorded target identifier.
The system SHALL use the same lookup method for command targets and reference action parameters.

#### Scenario: Target bookmark is remapped
- **WHEN** command replay is about to execute a command whose recorded target bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement bookmark `demoCustomer:2` from its common lookup method
- **THEN** the system executes the replayed command against target bookmark `demoCustomer:2`

#### Scenario: Target bookmark is not remapped
- **WHEN** command replay is about to execute a command whose recorded target bookmark is `demoCustomer:1`
- **AND** no command replay mapping SPI provides a replacement bookmark from its common lookup method
- **THEN** the system executes the replayed command against target bookmark `demoCustomer:1`

### Requirement: Replay mapping SPI can remap reference action parameters before execution
The system SHALL ask the command replay mapping SPI's common lookup method whether each replayed action parameter represented as `type: "reference"` with a populated `reference` OID has a replacement before command execution.
When the SPI provides a replacement reference bookmark, the system SHALL execute the replayed command using the replacement parameter reference.
When no SPI implementation exists or no replacement is provided, the system SHALL execute the replayed command using the recorded parameter reference.
The system MUST leave non-reference action parameters unchanged by this reference remapping flow.
The system MUST NOT require the SPI to receive parameter name or parameter index metadata to remap a reference action parameter.

#### Scenario: Reference action parameter is remapped
- **WHEN** command replay is about to execute an action command with parameter `simpleObject` recorded as `reference.type: "simple.SimpleObject"` and `reference.id: "1"`
- **AND** the command replay mapping SPI returns replacement bookmark `simple.SimpleObject:2` from its common lookup method
- **THEN** the system executes the replayed command with parameter `simpleObject` set to `reference.type: "simple.SimpleObject"` and `reference.id: "2"`

#### Scenario: Reference action parameter is not remapped
- **WHEN** command replay is about to execute an action command with parameter `simpleObject` recorded as `reference.type: "simple.SimpleObject"` and `reference.id: "1"`
- **AND** no command replay mapping SPI provides a replacement bookmark from its common lookup method
- **THEN** the system executes the replayed command with parameter `simpleObject` set to `reference.type: "simple.SimpleObject"` and `reference.id: "1"`

#### Scenario: Non-reference action parameter is not remapped by reference flow
- **WHEN** command replay is about to execute an action command with a parameter that is not represented as `type: "reference"`
- **THEN** the system does not ask the command replay mapping SPI's common lookup method to replace that parameter

### Requirement: Replay result mapping SPI is notified after successful replay
The system SHALL provide a command replay mapping SPI that applications can implement to receive replay result bookmark observations and provide replay input bookmark lookups.
After a command replay succeeds, the system SHALL notify the SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL include the recorded returned object bookmark and the actual replay result bookmark.
The system SHALL notify the SPI even when the recorded returned object bookmark and actual replay result bookmark are equal.
The system SHALL notify the SPI in the same transaction as replay command execution.
The system SHALL propagate result notification exceptions so the replayed command execution fails and rolls back.

#### Scenario: Successful replay produces a different result bookmark
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:2`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:2`

#### Scenario: Successful replay produces the same result bookmark
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:1`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`

#### Scenario: Result mapping listener rejects the replay result
- **WHEN** command replay execution succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:2`
- **AND** a command replay mapping SPI throws while handling the result notification
- **THEN** the replayed command execution fails
- **AND** the replayed command transaction rolls back

### Requirement: Safe action result mappings can remap later replay inputs
The system SHALL make replay result observations produced by logged safe action commands available to the same replay input lookup flow used by other replayed commands.
When a later replayed command target or reference parameter uses a recorded bookmark that was observed by an earlier safe action replay, the system SHALL allow the command replay mapping SPI to replace that recorded bookmark with the actual replay bookmark.

#### Scenario: Later replay command target is remapped from safe action result
- **GIVEN** replay of a safe action command mapped recorded bookmark `demoCustomer:1` to actual bookmark `demoCustomer:2`
- **WHEN** replay is about to execute a later command whose recorded target bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement bookmark `demoCustomer:2`
- **THEN** the system executes the later replayed command against target bookmark `demoCustomer:2`

#### Scenario: Later replay command reference parameter is remapped from safe action result
- **GIVEN** replay of a safe action command mapped recorded bookmark `demoCustomer:1` to actual bookmark `demoCustomer:2`
- **WHEN** replay is about to execute a later command whose reference parameter bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement bookmark `demoCustomer:2`
- **THEN** the system executes the later replayed command with reference parameter bookmark `demoCustomer:2`
