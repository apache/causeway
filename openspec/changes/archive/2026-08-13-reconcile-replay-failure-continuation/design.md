## Context

Replay executes a command, then observes/records the result. On `main`, when execution fails, the failure is
recorded in a new transaction but the failed `Try` is propagated:

```
// extensions/core/commandlog/applib/.../dom/replay/ReplayableCommand.java (identical to audited head)
:511  …callTransactional(Propagation.REQUIRES_NEW, () -> { … })   // execute
:520  tryResultBookmark.accept(
:522        ex -> …runTransactional(Propagation.REQUIRES_NEW, () -> onReplayError(ex)),
            __ -> {});
:528  return tryResultBookmark;                                    // still a Failure
:561  private void onReplayError(Throwable ex) { … saveAnalysis(ex.toString()); }   // no typed prefix
```

`Try.Failure.accept(...)` runs the failure consumer and returns the same Failure. The bounded batch then stops:

```
// …/dom/replay/CommandManager_replayOrRetryMultiple.java
:85  for (var replayable : replayables) {
:86      var result = replayable.tryReplayOrRetry();
:87      if (result.isFailure()) …            // stop
```

`CommandLogEntry.saveAnalysis` sets the `FAILED` state and the failure reason but does not call `setException`,
even though `setException(String)` / `setException(Throwable)` exist (`CommandLogEntry.java:544-547`).

Maintenance (`ecp`, CAUSEWAY-4042 commits `3f128a7a791`, `622ed00ba0c`, `0d5ee322b17`, `9a9d4444d17`) records the
failure in `REQUIRES_NEW`, sets state + reason + exception, classifies the reason with a typed prefix, and then
calls `mapFailureToSuccess(...)` (`ReplayableCommand.tryReplay:679-703`, `CommandLogEntry:741-748`), so the
returned `Try` is a Success and the identical batch loop continues.

## Goals / Non-Goals

**Goals:**

- A handled replay failure is durably recorded (state + reason + exception + typed prefix) in its own
  transaction, and the replay outcome is a success so callers/batches continue.
- Bounded/multiple/next replay continues past a recorded failure to the next eligible command.

**Non-Goals:**

- No change to replay eligibility, the P2 replay-state boundary, or the B2 background-pending gate (batch still
  pauses when a replay creates pending background work).
- No new configuration, schema, or persistence contract.
- Not changing how *unhandled* framework errors (outside the recorded advisor/execution failure path) propagate.

## Decisions

### Map a handled failure to success after recording it

Replace `accept(...) ; return tryResultBookmark` with a `mapFailureToSuccess`-style handling: on failure, in a
`REQUIRES_NEW` transaction record the analysis and return a success outcome (e.g. an empty/absent result
bookmark). This is the single behavioural pivot — it makes `isFailure()` false for a handled failure so the
existing batch loop continues without changing the loop itself.

Rejected — change the batch loop to "continue on failure": that would also swallow genuinely unhandled failures
and diverges from maintenance, which pivots at the single-command outcome, not the loop.

### Record full failure detail with a typed prefix

In the failure branch, set the `FAILED` state, the failure reason, and the exception (`setException`), and prefix
the reason with the advisor classification — `Hidden:` / `Disabled:` / `Invalid:` — matching maintenance
(maintenance maps both hidden and disabled advisor exceptions to `Disabled:`). MA-13 is bundled here because it
touches the same `onReplayError` / `saveAnalysis` path.

### Update the background-completion spec's failure scenario

`command-replay-background-completion`'s requirement "Bounded replay pauses when background commands become
pending" currently states bounded replay stops "or a command replay fails" and has a scenario "Replay failure
still stops bounded replay". Replace those with record-and-continue semantics, preserving every
background-pending stop/pause/continue scenario unchanged.

## Acceptance evidence

- A bounded-replay integration test with a command that fails a pre-requisite/advisor check: the batch records
  that command as `FAILED` (state + reason + exception + typed prefix) and **continues** to replay the next
  eligible command; replay states committed before the failure are unchanged.
- A unit assertion that the single-command replay outcome after a handled failure is a success (`Try` is not a
  Failure) while the failure record is committed in a separate transaction.
- A test asserting the typed prefix for hidden/disabled/invalid advisor outcomes.
- The B2 background-pending pause/continue scenarios still pass unchanged.
