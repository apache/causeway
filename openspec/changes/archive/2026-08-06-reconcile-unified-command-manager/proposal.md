## Why

With the P1 replayable-command projection complete, command review is still split between `CommandExportManager` and `CommandReplayManager`, each with different collections, controls, and mementos. P2 establishes the maintenance branch's single baseline-bounded sequence surface so later reachability, export/import, workflow, and background-gate slices can share one manager context.

## What Changes

- Add a public, command-recording-suppressed `CommandManager` view model with baseline and page-limit state, a stable baseline/limit memento, and safe shared controls for changing that state.
- Add unified `commandsInSequence`, `excluded`, `pendingOrFailed`, and `recordedOrReplayed` collections using P1 eligibility, foreground ordering, replay-state boundaries, and the pending-work exception.
- Preserve Causeway 4's legacy `EXPORTED` state as visible non-excluded historical work during the transition rather than hiding persisted commands.
- Add the persistence-neutral and Jakarta Persistence repository queries needed for excluded and recorded-or-replayed collections without changing the datastore schema.
- Make the unified manager the primary commandlog menu entry point and add fallback layout and collection column metadata.
- Retain the legacy export/replay manager logical types and memento constructors as compatibility shims, add safe navigation from each to the unified manager, and accept a legacy timestamp-only value when constructing the new manager (using the default page limit).
- Update replay-or-retry eligibility so `PENDING`, `OK`, and `FAILED` commands are replayable while `UNDEFINED`, `EXPORTED`, and `EXCLUDED` commands remain disabled; background-work gating remains deferred to B2.
- Keep known-participant tracking and reachability, sequence YAML export/import redesign, exclusion/restoration/deletion/movement/retimestamping workflows, and background-completion gates outside this change.

## Capabilities

### New Capabilities

- `unified-command-manager`: Defines unified manager identity and state, compatibility migration, baseline/limit controls, ordered command collections, menu and fallback presentation, and repository-neutral collection semantics.

### Modified Capabilities

- `replayable-command-projection`: Extends the shared eligibility contract to unified-manager collections and defines P2 replay-or-retry state eligibility without adding the later background gate.

## Impact

- `extensions/core/commandlog/applib`: adds the unified manager, state-control and compatibility mixins, menu/module wiring, collection filtering, layout metadata, replay-action policy, and focused tests while retaining the two legacy manager types.
- `extensions/core/commandlog/persistence-jpa`: adds named-query wiring for the new replay-state collection reads; no entity or schema migration is required.
- Existing bookmarks for `CommandExportManager` and `CommandReplayManager` remain loadable. New navigation uses `causeway.ext.commandLog.CommandManager` mementos containing baseline and limit.
- P1 eligibility and replay context are reused; no dependency on reference-data, reachability, YAML redesign, persistent participant state, command mutation workflows, background gating, or commandlog JDO is introduced.
