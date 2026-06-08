## Context

Command-log recording support persists foreground user interactions so they can be exported and replayed later through `CommandExportManager` and `CommandReplayManager`.
The same command-log extension also supports `BackgroundService`, which persists `ExecuteIn.BACKGROUND` entries with `startedAt == null` until `RunBackgroundCommandsJob` executes them.
If a recorded foreground action schedules a background command, that action itself should be allowed to complete normally.
The replay-safety problem arises only if the end-user tries to record a later foreground command before the pending background work has executed and committed.
If the end-user waits long enough for the background commands to finish and then executes another command, recording should continue normally.

The command-log applib already exposes `CommandLogEntryRepository#findBackgroundAndNotYetStarted()` and uses it from the background scheduler path.
That query is a suitable persistence-neutral way to detect pending background work from the command subscriber before a new recorded command proceeds.

## Goals / Non-Goals

**Goals:**

- Allow a foreground command that schedules background commands to complete normally during command-log recording support.
- Fail fast during command-log recording support only when a later foreground command is attempted while earlier background commands remain not yet started.
- Produce a clear exception that tells the recording end-user to wait for background commands to complete before continuing the recording.
- Reuse existing command-log repository abstractions so the behavior applies consistently to JDO and JPA persistence.
- Cover enabled recording support, waited-long-enough recording support, and disabled recording-support modes with regression tests.

**Non-Goals:**

- Do not change how background commands are scheduled, started, completed, or retried.
- Do not block, poll, or automatically run background commands from the foreground command subscriber.
- Do not reject the original foreground command solely because it schedules background commands.
- Do not change command export YAML structure or replay execution semantics.
- Do not introduce a new configuration option unless implementation discovers a compatibility need.

## Decisions

- Add the guard at the start of the next foreground command lifecycle, before that subsequent command is persisted as a new recorded foreground command.
  This makes the exception correspond to the user action that advances recording too early, not to the earlier action that legitimately scheduled background work.
  Alternative considered: check in `onCompleted`, but that would incorrectly fail the command that created the background commands and would prevent the user from simply waiting and continuing.

- Gate the guard on `causeway.extensions.command-log.recording-support=ENABLED`, not merely command-log persistence being enabled.
  Recording support is the mode in which users are building replayable command streams and need the stricter sequencing rule.
  Alternative considered: always reject pending background commands before foreground commands, but that would change ordinary command-log deployments that intentionally use asynchronous background work.

- Use `CommandLogEntryRepository#findBackgroundAndNotYetStarted()` as the pending-work source of truth.
  It already captures the repository-level definition of a pending background command and avoids duplicating JDO/JPA query details in the subscriber.
  Alternative considered: add a count query for efficiency, but the current method should be sufficient for the expected small pending set and can be optimized later if profiling requires it.

- Throw a runtime exception from the subscriber rather than logging a warning.
  The subsequent foreground command must fail so the end-user is forced to wait and retry after background commands are committed.
  Alternative considered: warning-only behavior, but that would preserve the current risk of unusable replay exports.

## Risks / Trade-offs

- [Risk] The guard may see unrelated pending background commands from another user or session and block recording too broadly.
  Mitigation: start with the existing global pending query to guarantee replay safety, and narrow by parent interaction, username, or timestamp only if tests or product feedback prove it necessary.

- [Risk] Throwing while the subsequent command is starting may have different behavior depending on which command lifecycle callback performs the check.
  Mitigation: regression tests should document that the original scheduling action succeeds, the premature subsequent action fails, and retrying after background completion succeeds.

- [Risk] Applications with recording support enabled and intentional background workflows may observe a stricter runtime failure when users continue too quickly.
  Mitigation: the change is scoped to recording support mode and allows the workflow once the background commands have completed.
