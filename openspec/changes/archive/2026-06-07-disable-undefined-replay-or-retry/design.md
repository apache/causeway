## Context

`ReplayableCommand_replayOrRetry` is the action users invoke from the replayable command UI to replay or retry an imported command.
The action currently delegates to `ReplayableCommand#tryReplayOrRetry`, which already has a guard path through `disableReplayOrRetry()`, but the mixin action currently returns `null` from `disableAct()` and is therefore always enabled.
A command that has just been recorded on the primary system has replay state `UNDEFINED` and is not yet in the imported replay queue.

## Goals / Non-Goals

**Goals:**

- Disable `ReplayableCommand#replayOrRetry` for `UNDEFINED` replay state.
- Keep `ReplayableCommand#replayOrRetry` enabled for `PENDING`, `OK`, and `FAILED` replay states.
- Keep the action disabled for states that are not replayable from the command view, including `EXPORTED` and `EXCLUDED`.
- Keep direct invocation guarded even if UI disablement is bypassed.

**Non-Goals:**

- Do not change how commands transition into `UNDEFINED`, `EXPORTED`, `PENDING`, `OK`, `FAILED`, or `EXCLUDED`.
- Do not change export/import workflows.
- Do not remove the ability to retry an `OK` command when a user intentionally invokes replay/retry.

## Decisions

- Restore `ReplayableCommand_replayOrRetry#disableAct()` to delegate to `ReplayableCommand#disableReplayOrRetry()`.
  This makes the UI action use the same guard as direct invocation.
  Alternative considered: duplicate replay-state checks in the mixin action.
  That would be simpler locally but risks drift from the invocation guard.

- Define replay/retry action availability as `PENDING`, `OK`, or `FAILED` only.
  `PENDING` is the normal imported state, `FAILED` supports retry, and `OK` remains allowed because existing comments indicate legitimate retry use cases after success.
  `UNDEFINED` is excluded because it represents a newly recorded command, not an imported replay candidate.

- Keep the guard as a replay-state predicate close to `ReplayState` or `ReplayableCommand.CommandRecord`.
  A named predicate makes tests explicit and avoids relying on older `isPendingOrFailed` terminology that excludes `OK`.

## Risks / Trade-offs

- [Risk] Existing users may have relied on clicking replay/retry for newly recorded `UNDEFINED` commands.
  → Mitigation: this state is not a replay queue state; users should export/import commands before replaying them.

- [Risk] The disable reason may mention only some non-replayable states.
  → Mitigation: update the message to describe the allowed states rather than a partial list of disallowed states.
