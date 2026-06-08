## MODIFIED Requirements

### Requirement: Replay input remapping does not mutate recorded command data
The system SHALL preserve the imported command log entry's recorded command DTO when applying replay-time target or reference parameter remapping.
Replay input remapping SHALL affect the command DTO supplied to replay execution, not the recorded command DTO retained for audit and inspection.
When target remapping supplies a replacement bookmark, the replay execution DTO SHALL use the replacement target bookmark and the recorded command DTO SHALL retain the original target bookmark.
When reference parameter remapping supplies a replacement bookmark, the replay execution DTO SHALL use the replacement parameter reference bookmark and the recorded command DTO SHALL retain the original parameter reference bookmark.
Replay retry SHALL start from the recorded command DTO and MUST NOT reuse a previously remapped execution DTO as recorded input.
Replay failure MUST NOT leave remapped target or reference parameter values in the recorded command DTO.

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
