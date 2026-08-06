## Context

C1 established immutable recording-support configuration and application-context-wide pause/resume behavior. P1/P2 established replay-state eligibility, `ReplayableCommand`, and the unified `CommandManager`; W1 completed its sequence-repair workflows. The remaining B1/B2 gap is sequencing around asynchronous commands: a recorded or replayed foreground command can schedule `ExecuteIn.BACKGROUND` entries whose `startedAt` remains null until `RunBackgroundCommandsJob` starts them, yet another foreground command can currently advance the sequence immediately.

The persistence-neutral `CommandLogEntryRepository.findBackgroundAndNotYetStarted()` contract and its Causeway 4 JPA named query already define and retrieve that pending work. `CommandSubscriberForCommandLog.onReady` can distinguish a new foreground command from a background command already persisted by `BackgroundService`. Replay entry points share `ReplayContext`, which already carries the repository required to make the same check without persistence-module coupling. Causeway 4 retains `CommandReplayManager` as a compatibility surface while the unified manager is the forward UI, so both paths must obey B2.

## Goals / Non-Goals

**Goals:**

- Prevent recording support from accepting a later foreground command while any earlier background work has not started.
- Preserve successful completion of the foreground command that schedules background work.
- Apply one consistent pending-background definition and wait message to row, unified-manager, and legacy-manager replay entry points.
- Add next and bounded-multiple replay to the unified manager, stopping after failure or newly pending background work and allowing continuation after it commits.
- Preserve replay states already produced before a bounded replay pauses.
- Verify behavior through focused unit tests and Causeway 4 JPA-backed integration tests.

**Non-Goals:**

- Do not run, poll, join, cancel, retry, or otherwise change background execution.
- Do not associate the gate only with a parent interaction, user, tenant, baseline, or manager selection; any globally pending background entry remains conservative blocking evidence.
- Do not change replay transaction boundaries, result mapping, export/import YAML, known-participant calculation, manager state, mementos, or W1 mutation behavior.
- Do not add configuration, a count query, schema changes, new replay state, JDO sources, or a commandlog JDO adapter.
- Do not remove or rename retained legacy managers or actions.

## Decisions

### Guard only creation of a new recorded foreground entry

In `CommandSubscriberForCommandLog.onReady`, perform the B1 check only after normal commandlog enablement and pause checks, only when recording support is `ENABLED`, and only on the path that would create a new `ExecuteIn.FOREGROUND` entry. Query pending background entries before persisting that foreground entry and throw a runtime exception containing their count and the common wait instruction when the query is non-empty.

The scheduling foreground command already has its entry when it creates background children, and a background command's later lifecycle finds the entry persisted by `BackgroundService`; neither takes the new-foreground path. Checking `onCompleted` was rejected because it would fail the command that legitimately scheduled the work. Applying the guard whenever commandlog persistence is enabled was rejected because B1 is specifically a replay-sequence recording policy.

### Reuse the established repository query as the global source of truth

Both B1 and B2 use `findBackgroundAndNotYetStarted()`, whose existing named query selects `ExecuteIn.BACKGROUND` entries with null `startedAt`. Introduce no new repository or persistence API. The query is global rather than baseline-, parent-, or user-scoped, matching maintenance behavior and providing the conservative guarantee that replay never advances while durable asynchronous effects may still be missing.

A count query was rejected because the existing cross-adapter contract already expresses the required state and expected pending sets are small. A correlation-specific query was rejected because the maintenance contract deliberately treats any pending background command as a blocker.

### Centralize replay gating beside the replay domain model

Add a reusable commandlog-applib helper that reads the repository from `ReplayContext`, reports whether pending background commands exist, and supplies one wait message. `ReplayableCommand.disableReplayOrRetry()` checks the established replay-state predicate first and then the background gate; `tryReplayOrRetry()` continues to call that disablement so direct invocation cannot bypass either rule.

Centralizing the repository access and message prevents row, manager, and compatibility actions from drifting. Moving this concern into core runtime services was rejected because it is commandlog replay policy, not a general command-execution rule.

### Add next and bounded replay to the unified manager

Contribute prototyping, command- and execution-publishing-disabled `replayOrRetryNext` and `replayOrRetryMultiple` actions to `pendingOrFailed`. The next action uses the oldest pending-or-failed row, requires it to appear in `commandsInSequence` with known participants, and directly rechecks pending background work before executing. The multiple action orders the current pending-or-failed rows, applies a caller-selected limit of 5, 10, 20, 40, 80, 160, 320, or all with 10 as the default, and deliberately leaves known-participant review to the user as maintenance does.

Bounded replay checks before starting and after every replay transaction. It returns immediately after a replay failure or once the repository reports pending background work, leaving earlier successful states intact. It does not wait. On reconstruction after the background job commits, remaining pending/failed rows can be replayed normally.

Reusing W1 selection validation was rejected because bounded replay operates on manager-ordered pending work rather than an arbitrary selected block. An unbounded-only action was rejected because maintenance provides explicit operational limits for replay batches.

### Gate every retained replay-manager compatibility path

Apply the same helper to the legacy manager's next and selected replay actions, including pre-loop and post-item checks, while retaining their identifiers, parameters, mementos, ordering, and stop-on-failure behavior. This adapts maintenance's final unified UI to Causeway 4's P2 promise that legacy bookmarks and direct construction remain compatible; compatibility must not provide a route around B2.

The shared row-level guard remains the final defense for direct calls, but manager actions also expose immediate disablement and avoid entering a loop that cannot proceed.

### Complete the deferred fallback presentation

Place unified next and bounded-multiple replay actions with `pendingOrFailed`, retaining import on the same collection and all E1/W1 controls elsewhere. Presentation tests replace the prior assertion that B1/B2 replay controls are absent with assertions for their association, ordering, styling, prototyping restriction, and disabled publishing metadata. B1 has no separate manager button; its visible behavior is the recording-time rejection message.

## Risks / Trade-offs

- [Unrelated pending background work can block recording or replay globally] → Preserve the maintenance safety boundary and document the global definition; narrowing requires a later explicit contract change.
- [A repository query is performed for replay disablement and after each bounded item] → Reuse the indexed existing query and favor correctness at transaction boundaries; optimize only with evidence.
- [Bounded replay can stop after successful partial progress] → Preserve each completed replay state, use one explicit wait message, and make continuation operate on the remaining pending/failed collection.
- [UI disablement can become stale before invocation] → Recheck inside row and manager action execution, not only in `disableAct`.
- [Legacy actions and unified actions could diverge] → Share the pending-background helper and add parity-focused tests while leaving their established parameter shapes intact.

## Migration Plan

No datastore migration is required. Deploy the updated commandlog applib against the existing Causeway 4 JPA adapter and repository query. Existing entries, background jobs, replay states, manager mementos, YAML, and legacy bookmarks remain compatible. Rollback removes the gates and unified replay controls without rewriting stored data; replay-state changes completed before a paused batch remain ordinary compatible audit state.

## Open Questions

None blocking. The maintenance specifications and current repository contract establish the global pending definition, and Causeway 4's removed commandlog JDO adapter remains deliberately out of scope.
