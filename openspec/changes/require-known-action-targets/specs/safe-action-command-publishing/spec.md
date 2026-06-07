## ADDED Requirements

### Requirement: Logged navigation actions establish known export targets
When command-log recording support is `ENABLED`, logged safe actions that return bookmarkable domain objects SHALL make those result bookmarks available as known targets for later command export validation.
Synthetic parented collection navigate-to actions SHALL make their returned child bookmark available as a known export target when their command log entry stores that bookmark as the result.
Synthetic scalar reference navigate-to actions SHALL make their returned referenced-object bookmark available as a known export target when their command log entry stores that bookmark as the result.
Safe actions that do not store a result bookmark MUST NOT establish a known target for later exported actions.

#### Scenario: Finder safe action establishes a later export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a logged safe finder action returns bookmark `demoCustomer:1`
- **WHEN** a later selected export action targets bookmark `demoCustomer:1`
- **THEN** the later target is known from the safe action result if the safe action is within the export manager baseline

#### Scenario: Parented collection navigate-to action establishes a child export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a synthetic parented collection navigate-to command returns bookmark `demoLine:1`
- **WHEN** a later selected export action targets bookmark `demoLine:1`
- **THEN** the later target is known from the synthetic navigate-to result if the navigate-to command is within the export manager baseline

#### Scenario: Scalar reference navigate-to action establishes a referenced export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a synthetic scalar reference navigate-to command returns bookmark `demoLease:1`
- **WHEN** a later selected export action targets bookmark `demoLease:1`
- **THEN** the later target is known from the synthetic navigate-to result if the navigate-to command is within the export manager baseline

#### Scenario: Safe action without result does not establish a later export target
- **GIVEN** command-log recording support is `ENABLED`
- **AND** a logged safe action has no result bookmark
- **WHEN** a later selected export action targets bookmark `demoCustomer:1`
- **THEN** the safe action without a result does not make bookmark `demoCustomer:1` known
