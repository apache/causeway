## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: Replay or retry uses the P2 replay-state boundary
The replay-or-retry action SHALL be enabled for a replayable command in state `PENDING`, `OK`, or `FAILED`.
It SHALL be disabled for state `UNDEFINED`, legacy `EXPORTED`, or `EXCLUDED`.
The action guard SHALL use a replay-specific predicate and MUST NOT broaden exclusion-action eligibility.
P2 SHALL NOT add a background-completion condition to this guard.

#### Scenario: Successful command can be replayed again
- **WHEN** a replayable command is in state `OK`
- **THEN** replay-or-retry is enabled

#### Scenario: Historical exported command cannot be replayed
- **WHEN** a replayable command is in state `EXPORTED`
- **THEN** replay-or-retry is disabled

#### Scenario: Excluded command cannot be replayed
- **WHEN** a replayable command is in state `EXCLUDED`
- **THEN** replay-or-retry is disabled

#### Scenario: Background gating remains deferred
- **WHEN** a replayable command is in `PENDING`, `OK`, or `FAILED` and background metadata is absent or incomplete
- **THEN** P2 state eligibility alone does not disable replay-or-retry
