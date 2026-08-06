# Command Export Command Exclusion Specification

## Purpose

Define unified-manager exclusion, restoration, and permanent deletion of baseline-bounded command-log entries.
## Requirements
### Requirement: Unified manager excludes selected sequence commands
When command-log recording support is `ENABLED`, the unified manager SHALL provide an `excludeCommands` action associated with `commandsInSequence`. The action SHALL accept one or more commands from the current baseline-and-limit-bounded sequence, set every selected backing command-log entry to replay state `EXCLUDED`, retain the entries, and return the current manager. Its default selection SHALL contain exactly the current sequence commands whose contextual `knownParticipants` value is false. The action SHALL be disabled when recording support is `DISABLED` or the sequence is empty. Direct invocation MUST reject disabled recording support, an empty selection, or any selected interaction id outside the current sequence, and validation MUST complete before any replay state changes.

#### Scenario: Unknown commands are selected and excluded by default
- **GIVEN** recording support is enabled and current sequence commands `A` and `B` respectively have unknown and known participants
- **WHEN** exclusion defaults are requested and the default selection is submitted
- **THEN** only command `A` is selected and changed to `EXCLUDED`
- **AND** its command-log entry still exists

#### Scenario: Exclusion rejects a stale or foreign selection atomically
- **GIVEN** command `A` is in the current sequence and command `B` is outside it
- **WHEN** direct invocation selects both commands for exclusion
- **THEN** the invocation is rejected
- **AND** neither replay state changes

#### Scenario: Exclusion requires recording support
- **GIVEN** command-log recording support is disabled
- **WHEN** exclusion availability or direct invocation is evaluated
- **THEN** exclusion is disabled or rejected with a recording-support reason
- **AND** no replay state changes

### Requirement: Unified manager restores selected excluded commands
The unified manager SHALL provide an `unexcludeCommands` action associated with `excluded`. The action SHALL accept one or more commands from the manager's current baseline-bounded `excluded` collection and a replay state chosen from every `ReplayState` value except `EXCLUDED`. It SHALL set every selected backing entry to the chosen state, retain the entries, and return the current manager. The action SHALL be disabled when recording support is `DISABLED` or `excluded` is empty. Direct invocation MUST reject an empty selection, an `EXCLUDED` or null destination state, or any selected interaction id outside the current `excluded` collection, and validation MUST complete before any replay state changes.

#### Scenario: Excluded commands are restored to recorded state
- **GIVEN** recording support is enabled and excluded commands `A` and `B` are in the current manager collection
- **WHEN** both are restored to `UNDEFINED`
- **THEN** both entries have replay state `UNDEFINED`
- **AND** neither entry is deleted

#### Scenario: Excluded command can become pending replay work
- **GIVEN** command `A` is in the current `excluded` collection
- **WHEN** it is restored to `PENDING`
- **THEN** its replay state is `PENDING`
- **AND** it no longer appears in `excluded`

#### Scenario: Restoration rejects active commands and excluded destination
- **GIVEN** excluded command `A` and active command `B`
- **WHEN** direct invocation attempts to restore both commands or chooses destination `EXCLUDED`
- **THEN** the invocation is rejected before either replay state changes

### Requirement: Unified manager permanently deletes only selected excluded commands
The unified manager SHALL provide a `deleteCommands` action associated with `excluded`, with choices drawn from the current baseline-bounded excluded collection. The action SHALL validate one or more selected interaction ids, permanently remove each selected backing command-log entry through the existing persistence service, and return the current manager. It SHALL be disabled when `excluded` is empty. It MUST reject an empty selection or any selected command outside the current excluded collection before deleting anything, and it MUST NOT delete active, pending, failed, recorded, or replayed entries merely because a caller constructs their view models.

#### Scenario: Selected excluded commands are deleted
- **GIVEN** excluded commands `A` and `B` are in the current manager collection
- **WHEN** both are submitted to `deleteCommands`
- **THEN** both backing command-log entries are permanently removed
- **AND** the current manager is returned

#### Scenario: Active command cannot be deleted through excluded workflow
- **GIVEN** excluded command `A` and active command `B`
- **WHEN** direct invocation submits both to `deleteCommands`
- **THEN** the invocation is rejected
- **AND** neither entry is deleted

#### Scenario: Delete choices and availability follow excluded collection
- **WHEN** the manager has excluded command `A` and active command `B`
- **THEN** deletion is enabled and offers `A` but not `B`
- **AND** deletion becomes disabled when no excluded commands remain
