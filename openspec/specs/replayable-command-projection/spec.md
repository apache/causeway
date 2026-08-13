# Replayable Command Projection Specification

## Purpose

Define replay-useful command eligibility, result presence, bookmark participant projection and remapping, participant object availability and mementos, fallback presentation, and adjacent foreground navigation.
## Requirements
### Requirement: Replay-useful entries are eligible for command projections
The system SHALL apply a reusable replayable-command eligibility policy before wrapping command log entries in general command-sequence, review, and adjacent-navigation projections.
State-changing command entries SHALL be eligible subject to the collection's existing replay-state, baseline, and limit filters.
Safe action entries SHALL be eligible for those projections only when they store a non-null result bookmark.
When action semantics cannot be resolved, the entry SHALL remain eligible.
Ineligible entries MUST remain persisted and MUST NOT be mutated.
The unified manager's `commandsInSequence`, `excluded`, and `recordedOrReplayed` collections and the legacy export and completed-or-excluded manager collections SHALL apply this policy without changing the legacy manager logical types, mementos, baselines, or actions.

#### Scenario: State-changing command remains eligible
- **WHEN** a general command projection considers a state-changing command entry
- **THEN** the entry remains eligible for `ReplayableCommand` wrapping

#### Scenario: Safe action with result is eligible
- **WHEN** a general command projection considers a safe action entry with a stored result bookmark
- **THEN** the entry is eligible for `ReplayableCommand` wrapping

#### Scenario: Safe action without result is omitted without mutation
- **WHEN** a general command projection considers a safe action entry with no stored result bookmark
- **THEN** the entry is not wrapped as a `ReplayableCommand`
- **AND** the command log entry remains persisted and unchanged

#### Scenario: Unknown action semantics are retained
- **WHEN** the metamodel cannot resolve the semantics of a command entry's action
- **THEN** the entry remains eligible for `ReplayableCommand` wrapping

### Requirement: Pending replay work bypasses general eligibility
The unified manager's and legacy replay manager's pending-or-failed collections SHALL wrap every command entry returned by their repository query.
A pending-or-failed safe action entry with no stored result bookmark SHALL remain visible for replay or retry review.
This exception MUST NOT broaden eligibility in other projections.

#### Scenario: Resultless safe action remains visible as imported work
- **WHEN** the pending-or-failed repository query returns a safe action entry with no result bookmark
- **THEN** each pending-or-failed collection wraps it as a `ReplayableCommand`

#### Scenario: Pending exception does not affect general collections
- **WHEN** the same resultless safe action is considered by `commandsInSequence`
- **THEN** the general collection does not wrap it

### Requirement: Replay or retry uses the P2 replay-state boundary
The replay-or-retry action SHALL be enabled for a replayable command in state `PENDING`, `OK`, or `FAILED` when no background command is pending execution. It SHALL be disabled for state `UNDEFINED`, legacy `EXPORTED`, or `EXCLUDED`. It SHALL also be disabled while at least one persisted `ExecuteIn.BACKGROUND` command has not yet started, with a message instructing the replay user to wait until pending background commands have executed and committed. The action guard SHALL use a replay-specific predicate and MUST NOT broaden exclusion-action eligibility. Direct invocation MUST NOT replay a command when either the replay-state or background-completion guard disables it.

#### Scenario: Successful command can be replayed again
- **GIVEN** no background command is pending
- **WHEN** a replayable command is in state `OK`
- **THEN** replay-or-retry is enabled

#### Scenario: Historical exported command cannot be replayed
- **WHEN** a replayable command is in state `EXPORTED`
- **THEN** replay-or-retry is disabled

#### Scenario: Excluded command cannot be replayed
- **WHEN** a replayable command is in state `EXCLUDED`
- **THEN** replay-or-retry is disabled

#### Scenario: Pending background work disables replay
- **GIVEN** a replayable command is in `PENDING`, `OK`, or `FAILED`
- **AND** at least one background command is pending execution
- **WHEN** replay-or-retry disablement is evaluated
- **THEN** replay-or-retry is disabled with the pending-background wait message

#### Scenario: Direct invocation cannot bypass background completion
- **GIVEN** a replayable command is otherwise replayable
- **AND** at least one background command is pending execution
- **WHEN** replay-or-retry is invoked directly
- **THEN** the command is not replayed

### Requirement: Replayable command reports result presence
A replayable command SHALL expose a non-persisted Boolean property named `hasResult`.
The property SHALL be `true` exactly when the wrapped command log entry stores a non-null result bookmark and SHALL otherwise be `false`.
Reading the property MUST NOT resolve the bookmark and MUST NOT change replay state.

#### Scenario: Stored result reports true without lookup
- **WHEN** a replayable command wraps an entry with recorded result bookmark `demo.Customer:1`
- **THEN** `hasResult` is `true`
- **AND** no bookmark lookup is required

#### Scenario: Missing result reports false
- **WHEN** a replayable command wraps an entry with no recorded result bookmark
- **THEN** `hasResult` is `false`

### Requirement: Replayable command derives bookmark participants
Each replayable command SHALL expose a collection of `ReplayableCommandParticipant` view models.
The collection SHALL include a `TARGET` participant for every recorded target bookmark, a `PARAMETER` participant for every reference-valued action parameter, and a `RESULT` participant when the command stores a recorded result bookmark.
Parameter participants SHALL expose their parameter name.
Non-reference parameters MUST NOT create participants.
Every participant SHALL expose its role and recorded bookmark without requiring that bookmark to resolve locally.
Participant derivation MUST NOT persist participant state or mutate the command entry.

#### Scenario: Target, reference parameter, and result become participants
- **WHEN** a command records target `demo.Order:1`, reference parameter `customer=demo.Customer:1`, scalar parameter `quantity=2`, and result `demo.Invoice:1`
- **THEN** its participants contain `TARGET`, named `PARAMETER`, and `RESULT` rows for the three bookmarks
- **AND** no participant is created for `quantity`

#### Scenario: Unresolvable recorded bookmark remains visible
- **WHEN** a participant's recorded bookmark does not resolve on the local system
- **THEN** the participant still exposes its role and recorded bookmark

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

### Requirement: Participants expose role-specific local objects best-effort
A target participant SHALL expose a target object only when its actual bookmark resolves locally.
A parameter participant SHALL expose an argument object only when its actual bookmark resolves locally.
A result participant SHALL expose a result object only when its actual bookmark resolves locally.
All object-valued properties SHALL be optional, role-specific, and independent of replay-state mutation.
Failure to resolve an actual bookmark MUST NOT hide the recorded or actual bookmark values.

#### Scenario: Mapped target resolves locally
- **WHEN** a target participant has actual bookmark `demo.Customer:2` and that bookmark resolves locally
- **THEN** its target property links to the local object
- **AND** its recorded and actual bookmarks remain visible

#### Scenario: Unresolvable parameter retains audit bookmarks
- **WHEN** a parameter participant's actual bookmark cannot be resolved locally
- **THEN** its argument property is empty
- **AND** its recorded and actual bookmark values remain visible

#### Scenario: Properties are role-specific
- **WHEN** a result participant is displayed
- **THEN** target and argument properties are hidden
- **AND** only its optional result-object property is applicable

### Requirement: Participant mementos encode identity rather than bookmark state
A participant memento SHALL contain its owning command interaction id and participant identity only.
A target memento SHALL use `[interactionId]--target`, a parameter memento SHALL use `[interactionId]--parameter--[parameterName]`, and a result memento SHALL use `[interactionId]--result`.
Mementos MUST NOT contain recorded or actual bookmarks.
When reconstructed from a memento, a participant SHALL derive its bookmarks and object links from the current owning command and mapping state.
The framework constructor SHALL accept the memento string before injected services.

#### Scenario: Parameter memento is readable and portable
- **WHEN** parameter `customer` belongs to command `11111111-1111-1111-1111-111111111111`
- **THEN** its memento is `11111111-1111-1111-1111-111111111111--parameter--customer`
- **AND** neither recorded nor actual bookmark is encoded

#### Scenario: Rehydration reflects current mapping
- **WHEN** a participant is reconstructed from its identity memento after replay mapping state changes
- **THEN** it exposes bookmarks and object links derived from the current owning command and mapping lookup

### Requirement: Replayable commands navigate between adjacent eligible commands
A replayable command SHALL expose safe `previous` and `next` actions using foreground commandlog ordering.
Navigation SHALL skip entries that are ineligible under the general replayable-command policy.
The returned adjacent command SHALL retain the current replay context.
The previous action SHALL be disabled when no earlier eligible command exists, and the next action SHALL be disabled when no later eligible command exists.
Navigation MUST NOT mutate command entries.

#### Scenario: Navigate across an ineligible entry
- **WHEN** eligible command `A` precedes a resultless safe action and eligible command `B` follows it
- **AND** the user invokes `next` on `A`
- **THEN** navigation returns replayable command `B` with the same replay context

#### Scenario: First eligible command has no previous action
- **WHEN** a replayable command has no earlier eligible foreground command
- **THEN** its previous action is disabled

#### Scenario: Last eligible command has no next action
- **WHEN** a replayable command has no later eligible foreground command
- **THEN** its next action is disabled

### Requirement: Fallback layouts expose the richer read model
The replayable-command fallback layout SHALL display `hasResult`, place its Participants table before replay controls, and order `hasResult` before any future known-participants column.
The participant table SHALL order role, parameter name, recorded bookmark, target, argument, result, and actual bookmark.
The participant object layout SHALL use three width-four columns.
Its first column SHALL expose the owning replayable command, role, and parameter name in a general fieldset and logical type name in metadata.
Its second column SHALL expose recorded bookmark, target only for target participants, and argument only for parameter participants.
Its third column SHALL expose actual bookmark and result only for result participants.
The participant SHALL provide a human-readable title.
The participant object form MUST NOT expose its owning interaction id.
The replayable-command object form SHALL use participants rather than separate target-type, target-id, or command-level open-target presentation for target inspection.

#### Scenario: Replayable command shows participants before controls
- **WHEN** a replayable command uses fallback layout metadata
- **THEN** `hasResult` is visible
- **AND** the Participants table appears before replay controls

#### Scenario: Participant layout separates recorded and actual data
- **WHEN** a participant uses fallback layout metadata
- **THEN** three width-four columns separate identity, recorded-side data, and actual-side data
- **AND** its owning replayable command, role, parameter name, and logical type metadata are displayed in the first column
- **AND** its recorded bookmark and role-specific target or argument link are displayed in the second column
- **AND** its actual bookmark and role-specific result link are displayed in the third column
- **AND** its owning interaction id is not displayed on the object form

#### Scenario: Target inspection uses participant rows
- **WHEN** a replayable command uses fallback object layout metadata
- **THEN** separate target-type and target-id properties are not displayed
- **AND** target navigation is available through the target participant's object link

### Requirement: Handled replay failures are recorded and mapped to a successful outcome

The system SHALL record a handled replay failure durably and then present the replay outcome as successful, so that callers and bounded, multiple, or next replay batches treat the failure as handled and continue to the next command. A handled replay failure is an advisor veto, a hidden or disabled target, or a replay-result-mapping conflict.

The recorded failure SHALL set the command's replay state to `FAILED`, SHALL store a failure reason, and SHALL store the underlying exception. The failure reason SHALL be classified with a typed prefix that distinguishes a hidden or disabled target (`Disabled:`) from an invalid input (`Invalid:`). Recording the failure MUST be committed in a `REQUIRES_NEW` transaction, independently of the outer replay transaction, so the failure record survives even though the outer outcome is success.

#### Scenario: A handled failure is recorded but the outcome is success

- **GIVEN** a command whose replay fails a pre-requisite / advisor check
- **WHEN** the command is replayed
- **THEN** its replay state is recorded as `FAILED` with a reason and the exception
- **AND** the replay outcome returned to the caller is a success

#### Scenario: The failure record is committed in its own transaction

- **GIVEN** a command replay that fails in a handled way
- **WHEN** the failure is recorded
- **THEN** the `FAILED` state, reason, and exception are committed in a separate transaction
- **AND** are visible even though the outer replay outcome is success

#### Scenario: The failure reason carries a typed classification prefix

- **GIVEN** a replay failure caused by a hidden or disabled target
- **WHEN** the failure reason is recorded
- **THEN** the reason is prefixed `Disabled:`

#### Scenario: An invalid-input failure is classified distinctly

- **GIVEN** a replay failure caused by invalid input
- **WHEN** the failure reason is recorded
- **THEN** the reason is prefixed `Invalid:`

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

### Requirement: A single replayable command can be exported to YAML

The system SHALL provide an action that exports one replayable command to a result-bearing YAML document, both from the command's object form and from its table row. The exported document SHALL contain the recorded command and, when present, its recorded result envelope and result bookmark, using the same export-DTO form as the bulk sequence export. The export action SHALL support optional result remapping and SHALL NOT mutate the command's replay state. The exported content SHALL be delivered as a downloadable YAML value with a file name derived from the command.

#### Scenario: Export one command from its object form

- **GIVEN** a replayable command whose recorded result bookmark is `demo.Invoice:1`
- **WHEN** the per-command export action is invoked from the object form
- **THEN** a YAML document is produced containing that command and the recorded result bookmark
- **AND** the command's replay state is unchanged

#### Scenario: Export one command from its table row

- **WHEN** the per-command export action is invoked from a command's table row
- **THEN** the same result-bearing YAML document is produced for that command

#### Scenario: Export with result remapping

- **GIVEN** a replayable command whose recorded result maps to an actual result bookmark
- **WHEN** the per-command export action is invoked with result remapping enabled
- **THEN** the exported document carries the remapped result bookmark

#### Scenario: Export without a recorded result

- **GIVEN** a replayable command with no recorded result
- **WHEN** the per-command export action is invoked
- **THEN** the exported document contains the command and no result envelope

