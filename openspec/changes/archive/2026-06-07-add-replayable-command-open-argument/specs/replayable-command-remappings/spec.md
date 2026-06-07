## MODIFIED Requirements

### Requirement: Replayable participant layouts expose participant data
The system SHALL provide fallback layout metadata for `ReplayableCommandParticipant`.
The participant layout SHALL expose role, parameter name, recorded bookmark, target, argument, result, and actual bookmark in that order.
The `ReplayableCommand` fallback layout SHALL include the participants collection as a table named `Participants` before the control field set.
The `ReplayableCommand` object view MUST NOT expose separate target summary properties for target type or target identifier.
The `ReplayableCommand` object view SHALL expose an `openTarget` action associated with the participants collection.
The `openTarget` action SHALL have layout sequence `1`.
The `openTarget` action SHALL open the actual target object when a target participant has an actual target available.
The `openTarget` action SHALL be disabled when no actual target object is available.
The `ReplayableCommand` object view SHALL expose an `openArgument` action associated with the participants collection.
The `openArgument` action SHALL have layout sequence `2`.
The `openArgument` action SHALL be disabled when no parameter participants are available.
The `openArgument` action SHALL define a `parameterName` parameter.
The `parameterName` parameter choices SHALL be the parameter names of participants whose role is `PARAMETER`.
The `parameterName` parameter SHALL default to the only parameter participant name when exactly one parameter participant is available.
The `parameterName` parameter SHALL have no default when more than one parameter participant is available.
The `openArgument` action SHALL validate the selected `parameterName` when the matching parameter participant has no actual bookmark for its argument.
The `openArgument` action SHALL open the selected actual argument object when the matching parameter participant has an actual argument object available.
Target and argument inspection and navigation SHALL be available through rows and actions associated with the participants collection.

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

#### Scenario: User opens actual target from participants action
- **WHEN** a replayable command target participant has an actual target object available
- **THEN** the replayable command exposes an `openTarget` action associated with the participants collection
- **AND** the `openTarget` action has layout sequence `1`
- **AND** invoking the action opens the actual target object

#### Scenario: User cannot open target without actual target
- **WHEN** a replayable command has no target participant actual target object available
- **THEN** the `openTarget` action is disabled

#### Scenario: User opens actual argument from participants action
- **WHEN** a replayable command parameter participant has parameter name `customer`
- **AND** the parameter participant has an actual argument object available
- **THEN** the replayable command exposes an `openArgument` action associated with the participants collection
- **AND** the `openArgument` action has layout sequence `2`
- **AND** the `parameterName` parameter choices include `customer`
- **AND** invoking the action with `customer` opens the actual argument object

#### Scenario: User cannot open argument without parameter participants
- **WHEN** a replayable command has no participants with role `PARAMETER`
- **THEN** the `openArgument` action is disabled

#### Scenario: Single parameter participant defaults parameter name
- **WHEN** a replayable command has exactly one parameter participant
- **AND** the parameter participant has parameter name `customer`
- **THEN** the `openArgument` action defaults `parameterName` to `customer`

#### Scenario: Multiple parameter participants do not default parameter name
- **WHEN** a replayable command has more than one parameter participant
- **THEN** the `openArgument` action has no default `parameterName`

#### Scenario: User cannot open argument without actual bookmark
- **WHEN** a replayable command parameter participant has parameter name `customer`
- **AND** the parameter participant has no actual bookmark
- **THEN** validating the `openArgument` action `parameterName` parameter with `customer` rejects the selection
