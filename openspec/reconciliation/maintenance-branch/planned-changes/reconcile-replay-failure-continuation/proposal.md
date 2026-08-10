## Why

Child change 3 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancies **MA-3** and **MA-13**;
second-opinion G3, third-opinion F4, fourth-opinion raw `verify-4042` — HIGH, unanimous *in substance*). The
fourth opinion's final ledger wrongly deemed MA-3 present; both meta-analyses resolved it against code (the
`REQUIRES_NEW` error handling is present, but the failure→success mapping that drives continuation is not).

On maintenance, a handled replay failure (e.g. an advisor veto, a hidden/disabled target, or a replay-result
mapping conflict) is recorded as `FAILED` in a `REQUIRES_NEW` transaction and then **mapped to success**, so a
bounded/multiple replay **continues to the next command**. On `main` the failure is recorded but the original
failed `Try` is returned, so the identical batch loop **halts the entire batch at the first failed command**, and
the recorded failure is missing detail:

- `ReplayableCommand.java:520-528` — `tryResultBookmark.accept(ex -> …REQUIRES_NEW onReplayError(ex), __ -> {})`
  then `return tryResultBookmark`; `Failure.accept(...)` returns the Failure unchanged (contrast maintenance
  `ReplayableCommand.tryReplay:679-703`, which calls `mapFailureToSuccess(...)`).
- `CommandManager_replayOrRetryMultiple.java:85-87` — `for (…) { result = replayable.tryReplayOrRetry(); if
  (result.isFailure()) …break }` — stops on the first failure.
- `ReplayableCommand.onReplayError:561-563` — `saveAnalysis(ex.toString())`; no typed classification prefix
  (`Hidden:` / `Disabled:` / `Invalid:`) — MA-13.
- `CommandLogEntry.saveAnalysis` records the `FAILED` state and reason but does **not** also call `setException`
  (which exists at `CommandLogEntry.java:544-547`), so the recorded failure loses the exception detail that
  maintenance stores (`CommandLogEntry.java:741-748` on maintenance).

The current v4 spec `command-replay-background-completion` explicitly encodes the divergent behaviour in its
scenario **"Replay failure still stops bounded replay"** — that scenario is exactly what maintenance reversed and
must be updated here. (`ReplayableCommand` and `CommandLogEntry` are byte-identical to the audited head, so these
anchors hold on HEAD.)

## What Changes

- Record a handled replay failure as `FAILED` in a `REQUIRES_NEW` transaction and then **map the failure to a
  successful outcome**, so a caller / bounded batch treats it as handled and continues to the next command.
- Populate the recorded failure fully: set the `FAILED` state, the failure reason, **and** the exception
  (`setException`), and classify the reason with a typed prefix (`Hidden:` / `Disabled:` / `Invalid:`) as
  maintenance does (MA-13).
- Update bounded/multiple replay so a **failed command no longer halts the batch**; the batch stops only when a
  replay creates pending background work (the B2 gate, unchanged) or the requested bound / collection is reached.
- Do not change replay eligibility, the replay-state boundary, or the background-pending gate; no schema,
  persistence, or configuration change.

## Capabilities

### Modified Capabilities

- `command-replay-background-completion`: a handled replay failure no longer stops bounded replay; the batch
  continues past recorded failures and still pauses on newly-pending background work.
- `replayable-command-projection`: a handled replay failure is recorded (state, reason, exception, typed prefix)
  in a new transaction and mapped to a successful outcome.

## Impact

- Affects commandlog applib: `ReplayableCommand` (failure mapping + typed prefix), `CommandLogEntry.saveAnalysis`
  (exception population), and the bounded/next replay managers' continuation semantics.
- **Interaction with B2 (background gates):** the B2 stop-on-newly-pending-background behaviour is retained; only
  the stop-on-failure behaviour changes. The existing scenario "Replay failure still stops bounded replay" is
  replaced by "Replay failure is recorded and bounded replay continues".
- Requires coverage proving: a batch continues past a recorded failure to the next command; the failed entry
  records state + reason + exception + typed prefix; and the failure record is committed even though the outer
  replay outcome is success.
