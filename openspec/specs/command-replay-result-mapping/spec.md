## Purpose

Define the command replay mapping SPI that lets applications remap replay inputs and observe mappings from recorded returned object bookmarks to actual replay result bookmarks.
## Requirements
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

### Requirement: Replay input remapping does not mutate recorded command data
The system SHALL preserve the imported command log entry's recorded command DTO when applying replay-time target or reference parameter remapping.
Replay input remapping SHALL affect the command DTO supplied to replay execution, not the recorded command DTO retained for audit and inspection.
When target remapping supplies a replacement bookmark, the replay execution DTO SHALL use the replacement target bookmark and the recorded command DTO SHALL retain the original target bookmark.
When reference parameter remapping supplies a replacement bookmark, the replay execution DTO SHALL use the replacement parameter reference bookmark and the recorded command DTO SHALL retain the original parameter reference bookmark.
Replay retry SHALL start from the recorded command DTO and MUST NOT reuse a previously remapped execution DTO as recorded input.
Replay failure MUST NOT leave remapped target or reference parameter values in the recorded command DTO.
Lifecycle synchronization for replay entries SHALL update execution timing metadata without overwriting recorded command DTO, target, member, result, or exception data.

#### Scenario: Remapped replay target preserves recorded command
- **WHEN** command replay remaps recorded target bookmark `demoCustomer:1` to actual target bookmark `demoCustomer:2`
- **THEN** the replay execution receives target bookmark `demoCustomer:2`
- **AND** the imported command log entry still records target bookmark `demoCustomer:1`

#### Scenario: Remapped replay reference parameter preserves recorded command
- **WHEN** command replay remaps action reference parameter `customer` from recorded bookmark `demoCustomer:1` to actual bookmark `demoCustomer:2`
- **THEN** the replay execution receives reference parameter `customer` with bookmark `demoCustomer:2`
- **AND** the imported command log entry still records reference parameter `customer` with bookmark `demoCustomer:1`

#### Scenario: Replay retry starts from recorded target bookmark
- **GIVEN** a first replay remaps recorded target bookmark `demoCustomer:1` to actual target bookmark `demoCustomer:2`
- **WHEN** the command is replayed or retried again
- **THEN** replay input remapping is evaluated against recorded target bookmark `demoCustomer:1`
- **AND** the stored command DTO does not use `demoCustomer:2` as the recorded target

#### Scenario: Replay retry starts from recorded reference parameter bookmark
- **GIVEN** a first replay remaps action reference parameter `customer` from recorded bookmark `demoCustomer:1` to actual bookmark `demoCustomer:2`
- **WHEN** the command is replayed or retried again
- **THEN** replay input remapping is evaluated against recorded parameter bookmark `demoCustomer:1`
- **AND** the stored command DTO does not use `demoCustomer:2` as the recorded parameter reference

#### Scenario: Replay failure preserves recorded command DTO
- **WHEN** command replay remaps a recorded target or reference parameter bookmark before execution
- **AND** replay execution fails
- **THEN** the imported command log entry still records the original target and reference parameter bookmarks

#### Scenario: Replay lifecycle sync preserves recorded replay entry data
- **WHEN** command subscriber lifecycle synchronization receives an in-memory replay command with execution timing, command DTO, target, member, result, and exception data
- **THEN** the replay command log entry records the execution timing metadata
- **AND** the replay command log entry still retains its recorded command DTO, target, member, result, and exception data

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
The system SHALL replay imported safe action command log entries as replayable commands only when they are eligible replayable command candidates with a single recorded result bookmark.
After an eligible logged safe action replay succeeds, the system SHALL notify the command replay mapping SPI when the imported command log entry has a recorded returned object bookmark and replay execution returns an actual result bookmark.
The notification SHALL use the existing replay result mapping SPI contract and SHALL include the recorded result bookmark and the actual replay result bookmark.
The system MUST NOT notify the SPI for a logged safe action replay when either the recorded or actual result bookmark is unavailable.
The system MUST NOT replay or notify replay result mapping for logged safe action entries that have no recorded result bookmark.

#### Scenario: Replayed safe action maps recorded result to actual result
- **GIVEN** an imported safe action command log entry has recorded result bookmark `demoCustomer:1`
- **WHEN** command replay executes that safe action and obtains actual result bookmark `demoCustomer:2`
- **THEN** the system notifies the command replay mapping SPI with recorded bookmark `demoCustomer:1` and actual bookmark `demoCustomer:2`

#### Scenario: Replayed safe action result is unavailable
- **GIVEN** an imported safe action command log entry has no recorded result bookmark
- **WHEN** command replay evaluates whether the entry is replayable
- **THEN** the system does not expose the entry as a replayable command
- **AND** the system does not notify the command replay mapping SPI for that action result

#### Scenario: Imported safe action with multiple results is not replayed
- **GIVEN** an imported safe action command log entry represents a safe action that returned multiple objects
- **AND** the imported command log entry has no single recorded result bookmark
- **WHEN** command replay evaluates whether the entry is replayable
- **THEN** the system does not expose the entry as a replayable command
- **AND** the system does not notify the command replay mapping SPI for that action result

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

### Requirement: Replay result mapping observes command interaction id
The system SHALL make the replayed command interaction id available when a replay result mapping observation is handled.
The built-in replay mapping listener implementations SHALL capture the interaction id from the `CommandLogEntry` supplied to `onReplayResult(...)` when they create a new mapping.
When the supplied command log entry has no interaction id, the system SHALL still record or retain the replay result mapping without an interaction id.
The system MUST NOT require a `CommandReplayMappingListener` SPI signature change to expose this interaction id.

#### Scenario: Replay result observation includes command interaction id
- **WHEN** command replay succeeds for an imported command whose interaction id is `11111111-1111-1111-1111-111111111111`
- **AND** the command has recorded result bookmark `demoInvoice:1`
- **AND** replay execution returns actual result bookmark `demoInvoice:2`
- **THEN** the built-in replay mapping listener records a replay result mapping associated with command interaction id `11111111-1111-1111-1111-111111111111`

#### Scenario: Replay result observation has no command interaction id
- **WHEN** command replay notifies a built-in replay mapping listener with recorded result bookmark `demoInvoice:1` and actual result bookmark `demoInvoice:2`
- **AND** the supplied command log entry has no interaction id
- **THEN** the built-in replay mapping listener records or retains the replay result mapping without a command interaction id

#### Scenario: Existing SPI signature is preserved
- **WHEN** an application implements `CommandReplayMappingListener#onReplayResult(Bookmark, Bookmark, CommandLogEntry)`
- **THEN** the implementation remains source-compatible with the replay result mapping SPI

### Requirement: Replayable command inspection surfaces relevant replay mappings
The system SHALL make replay mapping data relevant to a replayable command visible from that replayable command's UI.
The visible data SHALL include target bookmark replacements discovered through replay mapping lookup.
The visible data SHALL include reference parameter bookmark replacements discovered through replay mapping lookup.
The visible data SHALL include recorded target, reference parameter, and result bookmarks before replay.
The visible data SHALL include actual target, reference parameter, and result bookmarks after the replayable command has successfully replayed.
The system SHALL preserve the existing replay mapping SPI contracts while surfacing this data.

#### Scenario: User inspects replay input participants
- **WHEN** a replayable command has a recorded target or reference parameter bookmark
- **THEN** the replayable command UI exposes the recorded bookmark for that participant

#### Scenario: User inspects replay input remappings after success
- **WHEN** a replayable command target or reference parameter is remapped by the replay mapping lookup flow
- **AND** the replayable command has successfully replayed
- **THEN** the replayable command UI exposes the recorded bookmark and actual bookmark for that participant

#### Scenario: User inspects replay result remapping after success
- **WHEN** a replayable command has replayed successfully
- **AND** the command has a recorded result bookmark mapped to an actual result bookmark
- **THEN** the replayable command UI exposes the recorded result bookmark and actual result bookmark

#### Scenario: Replay mapping SPI remains source-compatible
- **WHEN** an application implements `CommandReplayMappingListener`
- **THEN** the implementation remains source-compatible with the replay mapping SPI

### Requirement: Replay result mappings can be deleted from the command log menu
The system SHALL provide a command log menu action that deletes all persisted command replay result mappings.
The delete action SHALL use an `ARE_YOU_SURE` action semantic.
The delete action SHALL be idempotent so invoking it when no mappings exist leaves the system with no mappings and no error.
The delete action SHALL report the number of deleted mappings to the user.
The delete action SHALL hide when no command replay result mapping repository is available.
The delete action MUST NOT delete command log entries or imported replay commands.

#### Scenario: User deletes all replay result mappings
- **GIVEN** command replay result mappings are persisted
- **WHEN** the user confirms the delete-all replay result mappings action
- **THEN** all command replay result mappings are removed
- **AND** the user is informed that two mappings were deleted
- **AND** command log entries are not removed

#### Scenario: Delete action is hidden without repository
- **GIVEN** no command replay result mapping repository is available
- **WHEN** the command log menu is rendered
- **THEN** the delete-all replay result mappings action is hidden

#### Scenario: Delete action is idempotent when no mappings exist
- **GIVEN** no command replay result mappings are persisted
- **WHEN** the user confirms the delete-all replay result mappings action
- **THEN** no command replay result mappings are persisted
- **AND** the action completes without error

### Requirement: Command replay menu actions are ordered for replay workflow
The command log menu SHALL order the command export and replay actions before replay result mapping finder actions.
The command log menu SHALL order `exportManager` before `replayManager`.
The command log menu SHALL order replay result mapping finder actions after `replayManager`.
The command log menu SHALL order the delete-all replay result mappings action after the replay result mapping finder actions.
The ordering SHALL be expressed with `@ActionLayout(sequence)` values on the menu actions.

#### Scenario: Export manager appears before replay manager
- **WHEN** the command log menu actions are ordered by their layout sequences
- **THEN** `exportManager` appears before `replayManager`

#### Scenario: Replay mapping finders appear after replay manager
- **WHEN** the command log menu actions are ordered by their layout sequences
- **THEN** each replay result mapping finder action appears after `replayManager`

#### Scenario: Delete action appears after replay mapping finders
- **WHEN** the command log menu actions are ordered by their layout sequences
- **THEN** the delete-all replay result mappings action appears after the replay result mapping finder actions

