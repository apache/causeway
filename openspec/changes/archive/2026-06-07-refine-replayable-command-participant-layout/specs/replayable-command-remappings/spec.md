## MODIFIED Requirements

### Requirement: Replayable participant layouts expose participant data
The system SHALL provide fallback layout metadata for `ReplayableCommandParticipant`.
The `ReplayableCommandParticipant` view model SHALL provide a human-readable title.
The `ReplayableCommandParticipant` view model SHALL expose a derived `replayableCommand` property that links to its owning replayable command.
The participant layout SHALL expose replayable command, role, parameter name, recorded bookmark, target, argument, result, and actual bookmark.
The participant object view MUST NOT expose `owningInteractionId`.
The participant object view SHALL expose `logicalTypeName` only in metadata.
The participant fallback layout SHALL use a first column with width `4`.
The first column SHALL contain a `general` fieldset with replayable command, role, and parameter name.
The first column SHALL contain a `metadata` tab or fieldset in the same tab group as `general` with logical type name.
The second column SHALL contain recorded bookmark, target, and argument.
The second column SHALL hide target unless the participant role is `TARGET`.
The second column SHALL hide argument unless the participant role is `PARAMETER`.
The third column SHALL contain actual bookmark and result.
The third column SHALL hide result unless the participant role is `RESULT`.
The participant table column order SHALL expose role, parameter name, recorded bookmark, target, argument, result, and actual bookmark in that order.
The `ReplayableCommand` fallback layout SHALL include the participants collection as a table named `Participants` before the control field set.
The `ReplayableCommand` object view MUST NOT expose separate target summary properties for target type or target identifier.
The `ReplayableCommand` object view MUST NOT expose command-level `openTarget`, `openArgument`, or `openResult` actions associated with the participants collection.
Target, argument, and result inspection and navigation SHALL be available through object-valued links in the participants collection rows.

#### Scenario: User views participants table on a replayable command
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout includes the participants collection as a table named `Participants`
- **AND** the participants table appears before the control field set

#### Scenario: User views participant identity fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the first column has width `4`
- **AND** the first column general fieldset displays replayable command, role, and parameter name
- **AND** the first column metadata tab or fieldset displays logical type name
- **AND** the participant has a human-readable title
- **AND** the participant does not display owning interaction id

#### Scenario: User views participant recorded-side fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the second column displays recorded bookmark
- **AND** the second column displays target only for target participants
- **AND** the second column displays argument only for parameter participants

#### Scenario: User views participant actual-side fields
- **WHEN** a user views a replayable command participant using fallback layout metadata
- **THEN** the third column displays actual bookmark
- **AND** the third column displays result only for result participants

#### Scenario: User inspects target through participants
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the layout does not display separate `targetType` or `targetId` properties
- **AND** target information is available through the participants table

#### Scenario: User navigates using participant object links
- **WHEN** a user views a replayable command participant with an available target, argument, or result object
- **THEN** the user can navigate to that object through the corresponding object-valued participant row link

#### Scenario: User navigates to replayable command from participant
- **WHEN** a user views a replayable command participant
- **THEN** the participant exposes a replayable command property linked to the owning replayable command

#### Scenario: User does not see redundant open actions
- **WHEN** a user views a replayable command using fallback layout metadata
- **THEN** the replayable command does not expose command-level `openTarget`, `openArgument`, or `openResult` actions associated with the participants collection
