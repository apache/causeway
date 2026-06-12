## MODIFIED Requirements

### Requirement: Replayable participant layouts expose participant data
The system SHALL provide fallback layout metadata for `ReplayableCommandParticipant`.
The participant layout SHALL expose role, parameter name, recorded bookmark, target, parameter, result, and actual bookmark in that order.
The `ReplayableCommand` fallback layout SHALL include the participants collection as a table named `Participants` before the control field set.
The `ReplayableCommand` object view MUST NOT expose separate target summary properties for target type or target identifier.
The `ReplayableCommand` object view SHALL expose an `openTarget` action associated with the participants collection.
The `openTarget` action SHALL have layout sequence `1`.
The `openTarget` action SHALL open the actual target object when a target participant has an actual target available.
The `openTarget` action SHALL be disabled when no actual target object is available.
Target inspection and navigation SHALL be available through target rows in the participants collection.

#### Scenario: User views participants table on a replayable command
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout includes the participants collection as a table named `Participants`
- **AND** the participants table appears before the control field set

#### Scenario: User views participant fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the layout displays the participant role, parameter name, recorded bookmark, target, parameter, result, and actual bookmark in order

#### Scenario: User inspects target through participants
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout does not display separate `targetType` or `targetId` properties
- **AND** target information is available through the participants table

#### Scenario: User opens actual target from participants action
- **WHEN** a replayable command target participant has an actual target object available
- **THEN** the replayable command exposes an `openTarget` action associated with the participants collection
- **AND** the `openTarget` action has layout sequence `1`
- **AND** invoking the action opens the actual target object

#### Scenario: User cannot open target without actual target
- **WHEN** a replayable command has no target participant actual target object available
- **THEN** the `openTarget` action is disabled
