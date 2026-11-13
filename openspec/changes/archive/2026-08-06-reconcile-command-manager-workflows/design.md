## Context

P2 established `CommandManager` as the baseline-and-limit-bounded forward surface, R2 made participant reachability visible in that context, and E1 added canonical sequence export and replay import. Causeway 4 still exposes exclusion and deletion primarily through older replay-manager or row actions and has no unified-manager command movement. The maintenance branch's final W1 behavior adds collection actions for excluding, restoring, deleting, and retimestamping commands, but its code targets older APIs and includes a commandlog JDO adapter that Causeway 4 deliberately removed.

The current applib already provides the required primitives: mutable replay state and timestamp on `CommandLogEntry`, embedded `CommandDto` access, `RepositoryService` removal, baseline-bounded manager collections, R2 `knownParticipants`, and recording-support configuration. The JPA command-log entry is a managed Jakarta Persistence entity, so its existing setters participate in the action transaction; W1 does not need an ordering column, new named query, or repository mutation API. The removed commandlog JDO source tree must remain absent.

## Goals / Non-Goals

**Goals:**

- Make the unified manager the forward UI for excluding, restoring, deleting, and moving commands.
- Validate every supplied row against a fresh current manager collection before performing any mutation, including direct programmatic invocation.
- Default exclusion to current commands whose contextual `knownParticipants` value is false.
- Move selected commands in manager order after any unselected active target, supporting earlier and later movement through one action.
- Persist replay-state changes, deletion, and matching command-log/DTO timestamps through existing Causeway 4 JPA behavior.
- Preserve existing manager state, legacy managers, bookmarks, replay states, and compatibility actions.

**Non-Goals:**

- Do not change E1 YAML formats, implicit export selection, result remapping, or replay import.
- Do not automatically infer or repair a sequence, validate the final sequence after movement, or guarantee space before the target's next unselected command.
- Do not add replay execution actions to the manager; B2 owns replay background gating and later replay workflow presentation.
- Do not add recording or replay background-completion gates; B1/B2 remain separate.
- Do not add persisted ordering metadata, schema changes, a new replay state, or a commandlog JDO adapter.
- Do not remove or rename legacy managers or their existing actions.

## Decisions

### Contribute four collection actions to the unified manager

Register `excludeCommands` and `moveCommands` against `commandsInSequence`, and `unexcludeCommands` and `deleteCommands` against `excluded`. All are prototyping, command-recording-suppressed through their manager target, and disable command and execution publishing. They return the same manager so a refresh re-queries the current collections without changing its baseline, limit, or memento.

This follows the maintenance collection workflow while adapting `excludedCommands` to Causeway 4's established `excluded` collection. Reusing only per-row actions was rejected because it cannot express multi-selection, default unknown-participant selection, or block movement. Adding the actions to a legacy manager was rejected because P2 made the unified manager the forward path.

### Validate identity membership before resolving mutations

Each action compares selected interaction ids with a fresh snapshot of its owning manager collection and rejects null or empty selections before mutating anything. Movement additionally requires a target in the same fresh active sequence and rejects a target included in the selection. The action derives the selected block's order from the fresh manager sequence rather than trusting parameter order. Choices come from the same collection contracts.

This makes UI choices and direct invocation obey the same baseline, limit, eligibility, and replay-state boundary. Comparing interaction ids rather than view-model instances avoids dependence on object identity across view-model reconstruction. Partial mutation before detecting an invalid row was rejected because a single invalid selection must leave the workflow unchanged.

### Apply recording-support gates according to workflow meaning

Exclusion and movement require recording support to be enabled in both disablement and direct validation, because they repair the recorded export sequence and can otherwise rewrite history without the R2 recording context. Restoration is disabled in the UI when recording support is disabled, matching maintenance behavior, while direct restoration remains governed by excluded membership and non-`EXCLUDED` state validation. Deletion remains available for excluded history independent of recording support.

Hiding actions was rejected because an explicit disabled reason is more useful and matches existing manager conventions. Applying the recording gate to deletion was rejected because cleanup of already excluded entries does not depend on recording new sequence information.

### Persist exclusion and restoration through the existing replay state

Exclusion sets each validated active entry to `ReplayState.EXCLUDED`. Restoration accepts choices from every replay state except `EXCLUDED` and assigns the chosen state to each validated excluded entry. No separate exclusion list is added to the manager memento or datastore.

The existing replay state is durable, already drives all four manager collections, and is compatible with legacy managers. A separate manager-only selection model was rejected because it would not survive reconstruction and would conflict with established repository queries.

### Delete only validated excluded entries

Deletion validates the entire selection against `excluded`, then removes each backing entry through the existing repository service in the action transaction. It never accepts an active, pending, failed, recorded, or replayed row merely because a caller can construct its `ReplayableCommand` view model. The action uses danger styling and a clear irreversible description.

Adding a general repository bulk-delete operation was rejected because the existing entity removal primitive is sufficient and an interaction-id bulk query would broaden the deletion surface. W1 does not delete replay-result mappings: they are an independent audit/mapping capability without a command-entry foreign-key ownership contract.

### Retimestamp a selected block with one deterministic action

`moveCommands(selected, target, squashTimings)` supports movement in either direction by placing the selected block after the target. The first selected entry receives `target timestamp + 1 second`. Subsequent entries either preserve their original positive gap when it is at least one second or use the one-second minimum; when squashing is enabled, every gap is exactly one second. Squashing defaults to false. Only selected entries change, and both `CommandLogEntry.timestamp` and the embedded `CommandDto.timestamp` are updated to the same value.

A single bidirectional action reflects the maintenance specification's final consolidation and avoids direction-specific action IDs. A new persistent order column was rejected because established repository and export ordering is timestamp-based. Retimestamping unselected neighbors to manufacture space was rejected because movement must have a narrow, predictable mutation boundary. The action does not promise collision-free placement relative to an unselected successor; it provides deterministic timestamps after the chosen target and preserves the selected block's internal order.

### Use managed JPA mutation without extending the persistence contract

Replay-state and timestamp setters on the current `CommandLogEntry` interface are the persistence-neutral mutation boundary, and the JPA implementation is managed during action execution. Updating the embedded DTO through `setCommandDto` ensures its converter sees the new timestamp as an assigned value. Focused applib tests cover computation and all-or-nothing validation; JPA integration tests reload entries to prove replay state, deletion, and both timestamp representations persist.

Restoring the maintenance JDO adapter or adding JDO-specific behavior was rejected because Causeway 4 removed that module under CAUSEWAY-3866. Adding repository methods solely to wrap setters was also rejected because it would not improve the transaction boundary and would create unnecessary adapter work.

### Extend the fallback layout without pulling forward background gates

The fallback layout exposes exclusion and movement with `commandsInSequence` and restoration and deletion with `excluded`, alongside E1 export/import. Presentation tests assert that replay-multiple controls, recording-completion gates, and replay-completion gates remain absent for B1/B2.

## Risks / Trade-offs

- [Retimestamping changes historical metadata and can affect adjacency, reachability, and export order] → Keep movement prototyping-only, explicit, recording-gated, and limited to the current sequence; test R2 and E1 observe the refreshed order.
- [A moved block can overlap an unselected successor's timestamp range] → Document that W1 does not make a fit guarantee; offer deterministic squash timing and leave unselected entries untouched.
- [View-model choices can become stale between rendering and invocation] → Re-query collection membership and validate all ids before mutation.
- [Deleting command entries is irreversible] → Restrict deletion to the current excluded collection, validate the whole selection first, and use danger presentation.
- [Mutating both entity and DTO timestamps could diverge if one update fails] → Perform both in one action transaction and verify by JPA reload; exceptions roll back the transaction.
- [Legacy actions can still express older workflows] → Keep them for bookmark/programmatic compatibility but expose W1 only on the unified manager's forward layout.

## Migration Plan

No datastore migration is required. Deploy the updated commandlog applib with the existing Causeway 4 JPA adapter. Existing entries, replay states, manager mementos, YAML, and legacy bookmarks remain valid. Rollback removes the unified workflow actions; replay-state and timestamp changes already made by users remain ordinary compatible command-log data.

## Open Questions

None blocking. The ledger's persistence question is resolved for W1 by using existing managed `CommandLogEntry` mutations and JPA reload coverage without restoring commandlog JDO. B1/B2 will decide how background-completion state disables these or later replay actions.
