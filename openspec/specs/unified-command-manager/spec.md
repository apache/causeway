# Unified Command Manager Specification

## Purpose

Define the unified commandlog manager's identity, state, controls, foreground review collections, compatibility path, and contextual participant-reachability presentation.
## Requirements
### Requirement: Unified command manager has stable baseline and limit state
The system SHALL provide a command-recording-suppressed `CommandManager` view model with logical type `causeway.ext.commandLog.CommandManager`.
The manager SHALL carry an inclusive baseline timestamp and a positive page limit, with a default limit of 100.
Its canonical memento SHALL encode `<timestamp>--<limit>` and SHALL NOT encode an export or replay mode.
The framework constructor SHALL also accept a timestamp-only memento and use the default limit.
Missing or malformed timestamp and limit components SHALL fall back independently to the current timestamp and default limit, and a non-positive limit SHALL use the default limit.

#### Scenario: Canonical memento round-trips state
- **WHEN** a manager with baseline `2026-08-06 10:00:00` and limit `50` is converted to and reconstructed from its memento
- **THEN** the reconstructed manager has the same baseline and limit
- **AND** the memento contains no workflow mode

#### Scenario: Timestamp-only state remains usable
- **WHEN** the manager is constructed from a valid timestamp-only memento
- **THEN** it uses that timestamp as its baseline
- **AND** it uses page limit 100

#### Scenario: Invalid components fall back independently
- **WHEN** a manager memento contains a valid timestamp and a non-positive or malformed limit
- **THEN** it retains the supplied timestamp and uses page limit 100
- **AND** a malformed timestamp does not prevent a valid positive limit from being retained

### Requirement: Manager state controls are safe and context preserving
The manager SHALL provide previous-hour, next-hour, change-baseline, and change-limit controls.
Each control SHALL have safe semantics with command publishing and execution publishing disabled.
Each control SHALL return a new `CommandManager`, changing only its designated state component and retaining the other component.
Invoking a state control MUST NOT mutate a command log entry.

#### Scenario: Moving the baseline retains the limit
- **WHEN** previous-hour is invoked on a manager with baseline `2026-08-06 10:00:00` and limit `50`
- **THEN** the returned manager has baseline `2026-08-06 09:00:00` and limit `50`
- **AND** no command is recorded or modified

#### Scenario: Changing the limit retains the baseline
- **WHEN** change-limit selects `200` on a manager with baseline `2026-08-06 10:00:00`
- **THEN** the returned manager has the same baseline and limit `200`
- **AND** no command is recorded or modified

### Requirement: Commands in sequence are bounded ordered foreground work
`commandsInSequence` SHALL query foreground commands at or after the manager baseline using the manager page limit and established commandlog ordering.
It SHALL exclude entries in replay state `EXCLUDED` and apply the general replayable-command eligibility policy before wrapping entries.
Eligible entries in `UNDEFINED`, `EXPORTED`, `PENDING`, `OK`, and `FAILED` states SHALL remain visible.
Reading the collection MUST NOT mutate, reorder, or change the replay state of any command.

#### Scenario: Sequence includes every eligible non-excluded state
- **WHEN** eligible foreground entries in `UNDEFINED`, `EXPORTED`, `PENDING`, `OK`, `FAILED`, and `EXCLUDED` states occur at or after the baseline
- **THEN** `commandsInSequence` contains the first five in repository order
- **AND** it does not contain the `EXCLUDED` entry

#### Scenario: Sequence applies page limit at the repository boundary
- **WHEN** more eligible non-excluded foreground entries exist than the manager page limit
- **THEN** `commandsInSequence` returns at most the configured number in established commandlog order

#### Scenario: Ineligible general work is omitted without mutation
- **WHEN** the bounded query returns a safe action with no result bookmark
- **THEN** `commandsInSequence` does not wrap that entry
- **AND** the persisted entry remains unchanged

### Requirement: Unified manager exposes focused replay-state collections
The manager SHALL expose baseline-bounded `excluded`, `pendingOrFailed`, and `recordedOrReplayed` collections in established commandlog order.
`excluded` SHALL contain entries in `EXCLUDED` state and apply the general replayable-command eligibility policy.
`pendingOrFailed` SHALL contain entries in `PENDING` or `FAILED` state and wrap every repository result without applying general eligibility.
`recordedOrReplayed` SHALL contain entries in `UNDEFINED`, legacy `EXPORTED`, or `OK` state and apply general eligibility.
These collections SHALL NOT be truncated by the `commandsInSequence` page limit and MUST NOT mutate command entries.

#### Scenario: Excluded review contains eligible excluded work
- **WHEN** eligible and ineligible foreground commands in `EXCLUDED` state occur at or after the baseline
- **THEN** `excluded` contains only the eligible commands in repository order

#### Scenario: Pending work retains the eligibility exception
- **WHEN** the pending-or-failed query returns a resultless safe action in `PENDING` state
- **THEN** `pendingOrFailed` wraps it as a `ReplayableCommand`

#### Scenario: Recorded history includes legacy exported work
- **WHEN** eligible commands in `UNDEFINED`, `EXPORTED`, and `OK` states occur at or after the baseline
- **THEN** `recordedOrReplayed` contains all three in repository order

#### Scenario: Focused collections ignore the sequence limit
- **WHEN** a focused collection contains more entries than the manager page limit
- **THEN** all matching entries at or after the baseline remain available in that focused collection

### Requirement: Unified manager is the primary compatible entry point
The standard commandlog menu SHALL expose one prototyping action that opens `CommandManager` with the current hour as baseline and page limit 100.
The legacy export-manager and replay-manager menu action identifiers SHALL remain registered but SHALL be hidden from the standard menu.
`CommandExportManager` and `CommandReplayManager` SHALL retain their logical types, timestamp-only memento constructors, existing behavior, and direct construction compatibility.
Each legacy manager SHALL expose a safe, command- and execution-publishing-disabled `openCommandManager` action that transfers its baseline and uses page limit 100.
Existing legacy bookmarks MUST NOT be rewritten or invalidated.

#### Scenario: Standard menu opens one unified manager
- **WHEN** the commandlog prototyping menu is rendered
- **THEN** it offers the unified command-manager action
- **AND** the legacy export-manager and replay-manager launchers are hidden

#### Scenario: Legacy export bookmark migrates safely
- **WHEN** an existing `CommandExportManager` bookmark is opened and `openCommandManager` is invoked
- **THEN** a `CommandManager` is returned with the legacy manager baseline and page limit 100
- **AND** no command entry is modified

#### Scenario: Legacy replay bookmark remains loadable
- **WHEN** an existing timestamp-only `CommandReplayManager` bookmark is resolved
- **THEN** the legacy manager retains its previous behavior
- **AND** it offers safe navigation to the unified manager

### Requirement: Unified manager supplies participant-reachability context
The unified manager SHALL supply each replayable command it creates with the manager instance's current baseline, limit, eligible non-excluded command order, recording-support state, domain-service classification, and reference-data classifiers. For a command at a given interaction id, only recorded results before that command in the same bounded order SHALL be available as prior knowledge. Replay state alone MUST NOT remove an otherwise eligible, non-excluded earlier result from this context. Manager mementos and repository entries MUST remain unchanged.

#### Scenario: Manager ordering controls prior knowledge
- **GIVEN** two eligible non-excluded commands occur at or after the manager baseline
- **WHEN** the first records a bookmark used by the second
- **THEN** the manager context makes that bookmark available to the second but not vice versa

#### Scenario: Replayed result remains prior knowledge
- **GIVEN** an eligible earlier command has replay state `OK` and a recorded result bookmark
- **WHEN** a later command's participants are evaluated
- **THEN** that recorded result is available as prior knowledge

#### Scenario: Context does not change manager identity
- **WHEN** the manager supplies reachability context
- **THEN** its baseline-and-limit memento remains unchanged

### Requirement: Unified manager replays pending work with background completion gates
The unified manager SHALL provide prototyping `replayOrRetryNext` and `replayOrRetryMultiple` actions associated with `pendingOrFailed`, with command and execution publishing disabled. Replay-next SHALL execute only the oldest pending-or-failed command when it is in the current sequence with known participants. Replay-multiple SHALL process current pending-or-failed commands in manager order up to a selected bound, defaulting to 10, and SHALL stop after a replay failure or newly pending background work. Both actions MUST reject direct invocation while background commands are already pending and MUST return the same manager state.

#### Scenario: Replay next retains manager state
- **WHEN** replay-next successfully executes the oldest known pending command
- **THEN** it returns a manager with the same baseline, limit, and memento

#### Scenario: Replay multiple stops at its selected bound
- **GIVEN** more pending-or-failed commands than the selected replay bound
- **WHEN** replay-multiple succeeds without creating background work
- **THEN** it processes only the bounded prefix in manager order
- **AND** it returns a manager with unchanged state

#### Scenario: Pending background work guards direct manager invocation
- **GIVEN** at least one background command remains pending execution
- **WHEN** either unified replay action is invoked directly
- **THEN** no command is replayed
- **AND** manager state remains unchanged

### Requirement: Fallback presentation exposes P2 review metadata only
The manager fallback layout SHALL expose baseline and limit with their state controls and SHALL present all four collections as sequence and replay review surfaces. Manager collection tables SHALL identify commands using interaction id, timestamp, member, replay state, result presence, and known-participants status, with known participants immediately after result presence. The layout SHALL expose E1 sequence-export and replay-import actions. It SHALL expose W1 exclusion and movement actions with `commandsInSequence` and W1 restoration and deletion actions with `excluded`. It SHALL expose B2 replay-next and replay-multiple actions with `pendingOrFailed` while retaining import on that collection.

#### Scenario: Unified manager layout includes completed reconciliation workflows
- **WHEN** the manager is rendered from fallback layout metadata
- **THEN** baseline, limit, all four collections, P1 identification columns, R2 known-participants status, E1 export/import actions, W1 exclusion, restoration, deletion, and movement actions, and B2 replay controls are visible
- **AND** replay-next and replay-multiple are associated with `pendingOrFailed`

### Requirement: Unified manager exports a result-bearing command sequence
The unified manager SHALL provide a prototyping `exportSequence` action associated with `commandsInSequence`. The action SHALL serialize the implicit known-participant sequence in manager order as multi-document `CommandExportDto` YAML, embedding each recorded command and its optional result bookmark. It SHALL support a filename prefix defaulting to `commands`, an optional sanitized timestamp suffix derived from the first exported command, and optional export remapping. Export MUST NOT mutate command DTOs, results, timestamps, replay states, repository membership, baseline, or limit.

#### Scenario: Export emits ordered command envelopes
- **GIVEN** two known commands occur in manager order and the first has a recorded result
- **WHEN** `exportSequence` is invoked
- **THEN** the YAML contains two documents in manager order
- **AND** the first document contains its embedded command and result identity

#### Scenario: Export leaves legacy replay state unchanged
- **GIVEN** an exported command has replay state `UNDEFINED` or legacy `EXPORTED`
- **WHEN** `exportSequence` completes
- **THEN** that replay state is unchanged
- **AND** the command remains in the same manager review collections

#### Scenario: Timestamped filename is safe
- **WHEN** timestamp suffixing is enabled for sequence export
- **THEN** the output filename starts with the configured prefix
- **AND** its suffix is derived from the first exported timestamp with unsafe filename characters replaced

### Requirement: Unified manager imports replay command envelopes
The unified manager SHALL provide a prototyping, command-recording-suppressed `importCommands` action accepting `.yml` and `.yaml` content through the replay-import decoder. It SHALL persist every decoded command through `saveForReplay` and attach optional result bookmarks. The action SHALL default to moving the returned manager baseline to the oldest non-null imported command timestamp while preserving the current limit. When baseline movement is disabled, no usable timestamp exists, or no command is imported, it SHALL return the current manager state.

#### Scenario: Import stores commands and results for replay
- **WHEN** the manager imports supported YAML containing commands and result metadata
- **THEN** each command is saved with replay repository semantics
- **AND** each supplied result bookmark is attached to its corresponding entry

#### Scenario: Import moves baseline and retains limit
- **GIVEN** the manager limit is 50
- **AND** imported commands have timestamps 10:00 and 09:00
- **WHEN** import uses the default baseline movement
- **THEN** the returned manager baseline is 09:00 and its limit is 50

#### Scenario: Import can retain current baseline
- **WHEN** import disables baseline movement
- **THEN** the returned manager retains its original baseline and limit

#### Scenario: Import without usable timestamp is safe
- **WHEN** supported imported command data contains no non-null timestamp
- **THEN** the commands are still persisted
- **AND** the returned manager retains its original baseline and limit
