## Context

Command replay executes `ReplayableCommand` instances through the replay manager collection actions and through the individual `replayOrRetry` action.
A replayed command can invoke domain behavior that schedules `ExecuteIn.BACKGROUND` command log entries, just as the original recorded command did.
The recording side now prevents the recorder from advancing while background commands from earlier work remain pending.
Replay needs the same sequencing guarantee so replayed commands do not advance past background work that has not yet reached durable state.

The command-log repository already provides `findBackgroundAndNotYetStarted()` for pending background command detection.
That query can be reused by replay UI action guards and by replay loops without introducing persistence-specific behavior.

## Goals / Non-Goals

**Goals:**

- Stop multi-command replay after a replayed command creates pending background commands.
- Disable single replay actions while pending background commands exist from earlier replayed work.
- Tell the replay user to wait until pending background commands have executed and committed before continuing replay.
- Reuse existing command-log repository abstractions so JDO and JPA behavior stays consistent.
- Cover selected replay, single replay, and continuation-after-wait behavior with tests.

**Non-Goals:**

- Do not change how background commands are scheduled, executed, retried, or marked complete.
- Do not automatically run, poll for, or wait on background commands inside replay actions.
- Do not change export YAML or replay result mapping semantics.
- Do not change the recording-side guard introduced by the previous change.

## Decisions

- Add a reusable replay pending-background guard close to the replay domain services/actions.
  `CommandReplayManager` collection actions and `ReplayableCommand#replayOrRetry` should use the same message and same repository query.
  Alternative considered: duplicate checks in each mixin, but that risks diverging messages and behavior.

- Check for pending background commands between iterations of selected replay.
  The selected replay action should replay a command, then stop the loop if that command leaves pending background commands.
  Alternative considered: check only before the loop starts, but that misses background commands created by an earlier command in the same selected replay run.

- Disable single-command replay when pending background commands exist.
  This applies to the replayable command action and should also be considered for replay manager single-next style actions so the replay user cannot bypass the sequencing rule through another replay entry point.
  Alternative considered: allow invocation and fail inside `act`, but disablement gives immediate UI feedback and matches the existing replay-state guard style.

- Treat any not-yet-started background command as a blocker.
  This mirrors the recording guard and is the safest interpretation for replay consistency.
  Alternative considered: only block background commands tied to the replayed command's parent interaction, but existing repository support and the user's safety requirement favor a conservative first implementation.

## Risks / Trade-offs

- [Risk] A pending background command unrelated to the replay flow may block replay.
  Mitigation: keep the behavior conservative for correctness and narrow the query later only if real-world usage shows false positives.

- [Risk] Stopping selected replay after a command succeeds may look like a partial replay failure.
  Mitigation: the command that created background work remains successful; the UI message should explain that replay paused so the user can wait and continue.

- [Risk] Tests need to simulate both replay and background execution, which can be integration-heavy.
  Mitigation: reuse existing command-log background service integration fixtures and focused command replay tests rather than adding broad end-to-end coverage.
