## Why

Causeway 4's `ReplayableCommand` still exposes only a thin command wrapper, so users cannot inspect recorded versus actual target, reference-parameter, and result participants or navigate the replay sequence reliably. With portable result metadata and both in-memory and persistent bookmark remapping now reconciled, P1 can add that read model without coupling it to the later command-manager redesign.

## What Changes

- Add a non-persisted result-presence projection and reusable eligibility rules for wrapping replay-useful command log entries, while preserving imported pending-or-failed work.
- Add `ReplayableCommandParticipant` view models for recorded target, reference-parameter, and result bookmarks, their mapped actual bookmarks, and best-effort local object links.
- Derive participant data from the owning command and replay-mapping SPI, including stable readable participant mementos that rehydrate rather than store bookmark state.
- Add previous/next navigation across eligible foreground commands using the existing commandlog ordering and the current replay context.
- Update module registration, fallback layouts, table column ordering, documentation, and focused tests for the richer projection.
- Keep the existing export and replay managers and replay/retry action eligibility intact; defer unified manager state/mementos, known-participant reachability, export/import validation, workflow mutations, and background-completion gates to P2 and later slices.

## Capabilities

### New Capabilities

- `replayable-command-projection`: Defines replayable-command eligibility, result presence, participant projection and remapping, participant object availability and mementos, layouts, and adjacent navigation.

### Modified Capabilities

None.

## Impact

- `extensions/core/commandlog/applib`: enriches `ReplayableCommand`, adds the participant view model and navigation mixins, extends `ReplayContext` compatibly, extends persistence-neutral command-entry lookup where needed, adapts existing manager wrapping without replacing either manager, and adds tests and documentation.
- Existing `CommandReplayMappingListener`, `ResultRemappingService`, `CommandLogEntry`, and `CommandLogEntryRepository` contracts are reused; no replay-result mapping persistence schema changes are required.
- Existing `CommandExportManager` and `CommandReplayManager` logical types and mementos remain compatible pending P2.
- No replay/retry state-policy change, reference-data SPI, reachability validator, export/import rewrite, command workflow replacement, or background gate is introduced.
