## Purpose

Define the command replay mapping SPI that lets applications remap replay inputs and observe mappings from recorded returned object bookmarks to actual replay result bookmarks.
## Requirements
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
The system SHALL notify the SPI in the same transaction as replay command execution.
The system SHALL propagate result-mapping notification exceptions so the replayed command execution fails and rolls back.

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
- **AND** a command replay mapping SPI throws while handling the result mapping notification
- **THEN** the replayed command execution fails
- **AND** the replayed command transaction rolls back

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

### Requirement: Replay mapping SPI has a conditional default listener
The system SHALL autoconfigure a default `CommandReplayMappingListener` bean when no application-defined `CommandReplayMappingListener` bean is present.
The system MUST NOT instantiate the default listener bean when another `CommandReplayMappingListener` bean is already defined.
The conditional default listener SHALL participate in the same replay mapping listener collection used for replay input remapping and replay result mapping notifications.

#### Scenario: Default listener is autoconfigured when missing
- **WHEN** the application context contains no `CommandReplayMappingListener` bean
- **THEN** the command log autoconfiguration provides the default `CommandReplayMappingListener` bean

#### Scenario: Default listener backs off for custom listener
- **WHEN** the application context already contains a custom `CommandReplayMappingListener` bean
- **THEN** the command log autoconfiguration does not provide the default `CommandReplayMappingListener` bean

#### Scenario: Autoconfigured listener participates in replay
- **WHEN** the command log autoconfiguration provides the default `CommandReplayMappingListener` bean
- **THEN** command replay includes that listener when requesting input remaps and sending result mapping notifications

### Requirement: Replay result mapping includes logged safe action results
The system SHALL replay imported safe action command log entries as replayable commands when they are present in the imported command stream.
After a logged safe action replay succeeds, the system SHALL notify the command replay mapping SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL use the existing replay result mapping SPI contract and SHALL include the recorded result bookmark and the actual replay result bookmark.
The system MUST NOT notify the SPI for a logged safe action replay when either the recorded or actual result bookmark is unavailable.

#### Scenario: Replayed safe action maps recorded result to actual result
- **GIVEN** an imported safe action command log entry has recorded result bookmark `demoCustomer:1`
- **WHEN** command replay executes that safe action and obtains actual result bookmark `demoCustomer:2`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoCustomer:1` and actual bookmark `demoCustomer:2`

#### Scenario: Replayed safe action result is unavailable
- **GIVEN** an imported safe action command log entry has no recorded result bookmark
- **WHEN** command replay executes that safe action
- **THEN** the system does not notify the command replay mapping SPI for that action result

### Requirement: Safe action result mappings can remap later replay inputs
The system SHALL make replay result mappings produced by logged safe action commands available to the same replay input remapping flow used by other replayed commands.
When a later replayed command target or reference parameter uses a recorded bookmark that was mapped by an earlier safe action replay, the system SHALL allow the command replay mapping SPI to replace that recorded bookmark with the actual replay bookmark.

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

