## Purpose

Define the command replay mapping SPI that lets applications remap replay inputs and observe mappings from recorded returned object bookmarks to actual replay result bookmarks.

## Requirements

### Requirement: Replay mapping SPI can remap command targets before execution
The system SHALL ask the command replay mapping SPI whether each replayed command target bookmark should be remapped before command execution.
When the SPI provides a replacement target bookmark, the system SHALL execute the replayed command using the replacement target identifier.
When no SPI implementation exists or no replacement is provided, the system SHALL execute the replayed command using the recorded target identifier.

#### Scenario: Target bookmark is remapped
- **WHEN** command replay is about to execute a command whose recorded target bookmark is `demoCustomer:1`
- **AND** the command replay mapping SPI returns replacement target bookmark `demoCustomer:2`
- **THEN** the system executes the replayed command against target bookmark `demoCustomer:2`

#### Scenario: Target bookmark is not remapped
- **WHEN** command replay is about to execute a command whose recorded target bookmark is `demoCustomer:1`
- **AND** no command replay mapping SPI provides a replacement target bookmark
- **THEN** the system executes the replayed command against target bookmark `demoCustomer:1`

### Requirement: Replay mapping SPI can remap reference action parameters before execution
The system SHALL ask the command replay mapping SPI whether each replayed action parameter represented as `type: "reference"` with a populated `reference` OID should be remapped before command execution.
When the SPI provides a replacement reference bookmark, the system SHALL execute the replayed command using the replacement parameter reference.
When no SPI implementation exists or no replacement is provided, the system SHALL execute the replayed command using the recorded parameter reference.
The system MUST leave non-reference action parameters unchanged by this reference remapping flow.

#### Scenario: Reference action parameter is remapped
- **WHEN** command replay is about to execute an action command with parameter `simpleObject` recorded as `reference.type: "simple.SimpleObject"` and `reference.id: "1"`
- **AND** the command replay mapping SPI returns replacement reference bookmark `simple.SimpleObject:2`
- **THEN** the system executes the replayed command with parameter `simpleObject` set to `reference.type: "simple.SimpleObject"` and `reference.id: "2"`

#### Scenario: Reference action parameter is not remapped
- **WHEN** command replay is about to execute an action command with parameter `simpleObject` recorded as `reference.type: "simple.SimpleObject"` and `reference.id: "1"`
- **AND** no command replay mapping SPI provides a replacement reference bookmark
- **THEN** the system executes the replayed command with parameter `simpleObject` set to `reference.type: "simple.SimpleObject"` and `reference.id: "1"`

#### Scenario: Non-reference action parameter is not remapped by reference flow
- **WHEN** command replay is about to execute an action command with a parameter that is not represented as `type: "reference"`
- **THEN** the system does not ask the reference parameter remapping flow to replace that parameter

### Requirement: Replay input remapping does not mutate recorded command data
The system SHALL preserve the imported command log entry's recorded command DTO when applying replay-time target or reference parameter remapping.
Replay input remapping SHALL affect the command DTO supplied to replay execution, not the recorded command DTO retained for audit and inspection.

#### Scenario: Remapped replay preserves recorded command
- **WHEN** command replay remaps recorded target bookmark `demoCustomer:1` to actual target bookmark `demoCustomer:2`
- **THEN** the replay execution receives target bookmark `demoCustomer:2`
- **AND** the imported command log entry still records target bookmark `demoCustomer:1`

### Requirement: Replay result mapping SPI is notified after successful replay
The system SHALL provide a command replay mapping SPI that applications can implement to receive replay result bookmark mappings and provide replay input remappings.
After a command replay succeeds, the system SHALL notify the SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL include the recorded returned object bookmark and the actual replay result bookmark.

#### Scenario: Successful replay produces a different result bookmark
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:2`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:2`

#### Scenario: Successful replay produces the same result bookmark
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:1`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoInvoice:1` and actual bookmark `demoInvoice:1`

### Requirement: Replay result mapping SPI is not notified when mapping data is unavailable
The system MUST NOT notify the command replay mapping SPI of result mappings unless replay succeeds and both recorded and actual result bookmarks are available.
Replay input remapping methods MAY still be called before execution even if result mapping data is unavailable.

#### Scenario: Replay fails
- **WHEN** command replay fails for an imported command whose recorded result bookmark is available
- **THEN** the system does not notify the command replay mapping SPI of a result mapping

#### Scenario: Recorded result is absent
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is absent
- **THEN** the system does not notify the command replay mapping SPI of a result mapping

#### Scenario: Actual result is absent
- **WHEN** command replay succeeds for an imported command whose recorded result bookmark is available
- **AND** replay execution does not return an actual result bookmark
- **THEN** the system does not notify the command replay mapping SPI of a result mapping

### Requirement: Replay result mapping SPI uses bookmarks without resolving objects
The system SHALL pass recorded and actual result bookmarks to the command replay mapping SPI without requiring those bookmarks to resolve to live domain objects.
The system SHALL pass target bookmarks to the command replay mapping SPI without requiring those bookmarks to resolve to live domain objects.

#### Scenario: Recorded object is not resolvable
- **WHEN** command replay succeeds and the recorded result bookmark does not resolve to a local domain object
- **THEN** the system can still notify the command replay mapping SPI with the recorded bookmark and actual bookmark

#### Scenario: Target object is not resolvable before remapping
- **WHEN** command replay is about to execute a command whose recorded target bookmark does not resolve to a local domain object
- **THEN** the system can still ask the command replay mapping SPI for a replacement target bookmark
