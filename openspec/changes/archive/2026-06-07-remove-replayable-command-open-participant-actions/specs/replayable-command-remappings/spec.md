## MODIFIED Requirements

### Requirement: Replayable participant layouts expose participant data
The system SHALL provide fallback layout metadata for `ReplayableCommandParticipant`.
The participant layout SHALL expose role, parameter name, recorded bookmark, target, argument, result, and actual bookmark in that order.
The `ReplayableCommand` fallback layout SHALL include the participants collection as a table named `Participants` before the control field set.
The `ReplayableCommand` object view MUST NOT expose separate target summary properties for target type or target identifier.
The `ReplayableCommand` object view MUST NOT expose command-level `openTarget`, `openArgument`, or `openResult` actions associated with the participants collection.
Target, argument, and result inspection and navigation SHALL be available through object-valued links in the participants collection rows.

#### Scenario: User views participants table on a replayable command
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout includes the participants collection as a table named `Participants`
- **AND** the participants table appears before the control field set

#### Scenario: User views participant fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the layout displays the participant role, parameter name, recorded bookmark, target, argument, result, and actual bookmark in order

#### Scenario: User inspects target through participants
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout does not display separate `targetType` or `targetId` properties
- **AND** target information is available through the participants table

#### Scenario: User navigates using participant object links
- **WHEN** a user views a replayable command participant with an available target, argument, or result object
- **THEN** the user can navigate to that object through the corresponding object-valued participant row link

#### Scenario: User does not see redundant open actions
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the replayable command does not expose command-level `openTarget`, `openArgument`, or `openResult` actions associated with the participants collection
