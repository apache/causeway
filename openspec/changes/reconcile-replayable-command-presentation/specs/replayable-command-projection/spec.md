<!--
DRAFT DELTA — re-validate on promotion.
MODIFIES "Participant actual bookmarks reflect mapping and replay state" (MA-7): the unmapped
fallback and the result-role gate use "executed ok" (replay state UNDEFINED or OK) instead of OK only.
ADDS three presentation requirements (MA-6 displayed DTO, MA-8 title, MA-9 recorded/actual target UI).
The full modified requirement is restated below.
-->

## MODIFIED Requirements

### Requirement: Participant actual bookmarks reflect mapping and replay state

Target and parameter participants SHALL expose the mapped bookmark whenever replay mapping lookup returns one. A command SHALL be considered executed successfully when its replay state is `UNDEFINED` (recorded-only, never replayed) or `OK`. Before a command is executed successfully, an unmapped target or parameter participant SHALL expose no actual bookmark. When a command is executed successfully, an unmapped target or parameter participant SHALL use its recorded bookmark as the actual bookmark. A result participant SHALL expose no actual bookmark before the command is executed successfully, even when a mapping for its recorded bookmark exists. When the command is executed successfully, a result participant SHALL use its mapped bookmark when available and SHALL otherwise use its recorded bookmark. The projection SHALL use the configured replay-mapping SPI and MUST NOT depend directly on a persistent mapping repository.

#### Scenario: Recorded-only command exposes its recorded bookmark as actual

- **WHEN** a recorded-only command with replay state `UNDEFINED` records target `demo.Customer:1` and mapping lookup returns no replacement
- **THEN** its target participant exposes actual bookmark `demo.Customer:1`

#### Scenario: Pending target displays an explicit mapping

- **WHEN** a pending command records target `demo.Customer:1` and mapping lookup returns `demo.Customer:2`
- **THEN** its target participant exposes actual bookmark `demo.Customer:2`

#### Scenario: Pending unmapped parameter has no actual bookmark

- **WHEN** a genuinely-pending (`PENDING`) command records reference parameter `customer=demo.Customer:1` and mapping lookup returns no replacement
- **THEN** its parameter participant exposes no actual bookmark

#### Scenario: Successful unmapped target falls back to recorded bookmark

- **WHEN** a successfully replayed command records target `demo.Customer:1` and mapping lookup returns no replacement
- **THEN** its target participant exposes actual bookmark `demo.Customer:1`

#### Scenario: Pending result does not expose global mapping

- **WHEN** a genuinely-pending command records result `demo.Invoice:1` and mapping lookup could return `demo.Invoice:2`
- **THEN** its result participant exposes no actual bookmark

#### Scenario: Successful result displays mapped bookmark

- **WHEN** a successfully replayed command records result `demo.Invoice:1` and mapping lookup returns `demo.Invoice:2`
- **THEN** its result participant exposes actual bookmark `demo.Invoice:2`

## ADDED Requirements

### Requirement: Replayable command DTO view includes the recorded result

The replayable command SHALL present its command DTO view as a result-bearing export DTO that embeds both the recorded command and its optional recorded result, rather than the raw command DTO. The displayed YAML MUST therefore include the recorded result envelope and result bookmark when a recorded result is present, and MUST omit them when it is absent.

#### Scenario: DTO view of a command with a recorded result

- **GIVEN** a replayable command whose recorded result bookmark is `demo.Invoice:1`
- **WHEN** its DTO view is rendered
- **THEN** the rendered YAML contains the embedded command and the recorded result bookmark

#### Scenario: DTO view of a command without a recorded result

- **GIVEN** a replayable command with no recorded result
- **WHEN** its DTO view is rendered
- **THEN** the rendered YAML contains the embedded command and no result envelope

### Requirement: Replayable command title uses the full recorded target

The replayable command title SHALL identify the command using its complete recorded target bookmark, and MUST NOT truncate the target identifier. A separate short/abbreviated target identifier MAY still be offered for table-column display.

#### Scenario: Title is not truncated

- **GIVEN** a replayable command whose recorded target id is longer than ten characters
- **WHEN** its title is built
- **THEN** the title contains the complete recorded target identifier, not an ellipsified form

### Requirement: Replayable command exposes recorded and actual target

The replayable command SHALL expose its recorded target and its actual target as top-level projections, where the actual target derives from replay mapping or, for a successfully-executed command, falls back to the recorded target. The replayable command SHALL provide an action, on both the object form and the table row, that opens either the recorded or the actual target according to a `RECORDED` / `ACTUAL` choice. That action SHALL be disabled with a user-facing message when the chosen target cannot be resolved.

#### Scenario: Recorded and actual target are exposed

- **GIVEN** a successfully-replayed command whose recorded target `demo.Customer:1` is mapped to actual `demo.Customer:2`
- **WHEN** the replayable command is displayed
- **THEN** its recorded-target projection is `demo.Customer:1`
- **AND** its actual-target projection is `demo.Customer:2`

#### Scenario: Open action offers recorded and actual

- **WHEN** the open-target action is invoked with choice `RECORDED`
- **THEN** the recorded target object is opened
- **AND** invoking it with choice `ACTUAL` opens the actual target object

#### Scenario: Open action is disabled when the target cannot be resolved

- **GIVEN** a chosen target whose bookmark does not resolve to a local object
- **WHEN** the open-target action is evaluated for that choice
- **THEN** the action is disabled with a message explaining the target cannot be resolved
