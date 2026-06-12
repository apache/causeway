## MODIFIED Requirements

### Requirement: Replayable command displays command participants
The system SHALL expose a participants collection on each replayable command.
The participants collection SHALL contain rows represented by `ReplayableCommandParticipant` view models.
Each participant SHALL expose the role, parameter name, recorded bookmark, target object, parameter object, result object, and actual bookmark in that order.
The system SHALL include target participants when the command DTO has recorded target bookmarks.
The system SHALL include reference parameter participants when the command DTO has recorded reference parameter bookmarks.
The system MUST NOT include non-reference parameters as bookmark participants.
The system SHALL include a result participant when the recorded command has a recorded result bookmark.
Each included participant SHALL expose its recorded bookmark.
Target participants SHALL expose an actual bookmark whenever replay mapping lookup provides a mapped bookmark.
Reference parameter participants SHALL expose an actual bookmark whenever replay mapping lookup provides a mapped bookmark.
Target and reference parameter participants SHALL leave actual bookmarks empty before successful replay when replay mapping lookup provides no mapped bookmark.
Target and reference parameter participants SHALL use their recorded bookmark as the actual bookmark after successful replay when replay mapping lookup provides no mapped bookmark.
Result participants SHALL expose actual bookmarks only after the replayable command has successfully replayed.
When a successful replay has no explicit replacement bookmark for a result participant, the participant SHALL use its recorded bookmark as the actual bookmark.

#### Scenario: Target participant appears in replayable command details
- **WHEN** a replayable command has recorded target bookmark `demoCustomer:1`
- **THEN** the replayable command participants collection contains a participant with role `TARGET`
- **AND** the participant has recorded bookmark `demoCustomer:1`

#### Scenario: Reference parameter participant appears in replayable command details
- **WHEN** a replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **THEN** the replayable command participants collection contains a participant with role `PARAMETER`
- **AND** the participant has parameter name `customer`
- **AND** the participant has recorded bookmark `demoCustomer:1`

#### Scenario: Non-reference parameter is omitted
- **WHEN** a replayable command has parameter `name` recorded as a string value
- **THEN** the replayable command participants collection does not contain a participant for parameter `name`

#### Scenario: Pending command populates mapped target actual bookmark
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the target participant has actual bookmark `demoCustomer:2`

#### Scenario: Pending command populates mapped parameter actual bookmark
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the parameter participant has actual bookmark `demoCustomer:2`

#### Scenario: Pending command leaves unmapped target actual bookmark empty
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the target participant has no actual bookmark

#### Scenario: Pending command leaves unmapped parameter actual bookmark empty
- **WHEN** a replayable command has replay state `PENDING`
- **AND** the replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the parameter participant has no actual bookmark

#### Scenario: Successful replay populates mapped target actual bookmark
- **WHEN** a replayable command has replay state `OK`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides actual bookmark `demoCustomer:2`
- **THEN** the target participant has actual bookmark `demoCustomer:2`

#### Scenario: Successful replay keeps unchanged target actual bookmark populated
- **WHEN** a replayable command has replay state `OK`
- **AND** the replayable command has recorded target bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the target participant has actual bookmark `demoCustomer:1`

#### Scenario: Successful replay keeps unchanged parameter actual bookmark populated
- **WHEN** a replayable command has replay state `OK`
- **AND** the replayable command has reference parameter `customer` recorded as bookmark `demoCustomer:1`
- **AND** replay mapping lookup provides no actual bookmark
- **THEN** the parameter participant has actual bookmark `demoCustomer:1`
