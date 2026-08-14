<!--
DRAFT DELTA — re-validate on promotion.
MODIFIES two existing requirements of unified-command-manager:
 (1) "Unified command manager has stable baseline and limit state" — add a maximum page limit of 320;
 (2) "Unified manager is the primary compatible entry point" — the standard menu opens at 320.
Both requirements are restated in full with only the limit-bound changes applied.
-->

## MODIFIED Requirements

### Requirement: Unified command manager has stable baseline and limit state

The system SHALL provide a command-recording-suppressed `CommandManager` view model with logical type `causeway.ext.commandLog.CommandManager`. The manager SHALL carry an inclusive baseline timestamp and a positive page limit that SHALL NOT exceed a maximum of 320, with a default limit of 100. Its canonical memento SHALL encode `<timestamp>--<limit>` and SHALL NOT encode an export or replay mode. The framework constructor SHALL also accept a timestamp-only memento and use the default limit. Missing or malformed timestamp and limit components SHALL fall back independently to the current timestamp and default limit; a non-positive limit SHALL use the default limit; and a limit greater than 320 SHALL be capped to 320.

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

#### Scenario: A limit above the maximum is capped to 320

- **WHEN** a manager is constructed with, or a memento encodes, a page limit greater than 320
- **THEN** the manager uses page limit 320

#### Scenario: Change-limit accepts only 1 to 320

- **WHEN** the change-limit control is validated with a value of 0 or a value greater than 320
- **THEN** validation rejects it
- **AND** validation accepts 1 and 320

### Requirement: Unified manager is the primary compatible entry point

The standard commandlog menu SHALL expose one prototyping action that opens `CommandManager` with the current hour as baseline and page limit 320 (its maximum). The legacy export-manager and replay-manager menu action identifiers SHALL remain registered but SHALL be hidden from the standard menu. `CommandExportManager` and `CommandReplayManager` SHALL retain their logical types, timestamp-only memento constructors, existing behavior, and direct construction compatibility. Each legacy manager SHALL expose a safe, command- and execution-publishing-disabled `openCommandManager` action that transfers its baseline and uses page limit 100. Existing legacy bookmarks MUST NOT be rewritten or invalidated.

#### Scenario: Standard menu opens one unified manager

- **WHEN** the commandlog prototyping menu is rendered
- **THEN** it offers the unified command-manager action
- **AND** the action opens the manager with page limit 320
- **AND** the legacy export-manager and replay-manager launchers are hidden

#### Scenario: Legacy export bookmark migrates safely

- **WHEN** an existing `CommandExportManager` bookmark is opened and `openCommandManager` is invoked
- **THEN** a `CommandManager` is returned with the legacy manager baseline and page limit 100
- **AND** no command entry is modified

#### Scenario: Legacy replay bookmark remains loadable

- **WHEN** an existing timestamp-only `CommandReplayManager` bookmark is resolved
- **THEN** the legacy manager retains its previous behavior
- **AND** it offers safe navigation to the unified manager
