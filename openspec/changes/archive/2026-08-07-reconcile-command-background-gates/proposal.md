## Why

Causeway 4 can record or replay a later foreground command while asynchronous work created by an earlier command is still pending. That can advance a replayable sequence before the earlier command's durable effects exist, so B1/B2 complete the commandlog reconciliation with explicit recording and replay sequencing gates.

## What Changes

- Reject creation of a subsequent recorded foreground entry while recording support is enabled and any persisted background command has not yet started, without rejecting the foreground command that originally scheduled that work.
- Add one shared replay-side pending-background check and wait message, using the existing persistence-neutral command-log repository query.
- Disable and directly guard replay-or-retry while background commands are pending, while preserving the established replay-state boundary.
- Add unified-manager actions for replaying the next eligible command or a bounded number of pending/failed commands; stop bounded replay after the first failure or after any replay creates pending background work.
- Apply the same replay sequencing gate to retained legacy replay-manager entry points so compatibility surfaces cannot bypass it.
- Expose the unified replay controls in the manager fallback layout and add focused applib and Causeway 4 JPA integration coverage.
- Do not poll for, execute, or wait synchronously on background work; add no configuration, schema, query, persistence-adapter, YAML, mapping, or manager-memento change, and do not restore commandlog JDO.

## Capabilities

### New Capabilities

- `command-recording-background-completion`: Defines when recording support rejects a later foreground command because previously scheduled background work has not started.
- `command-replay-background-completion`: Defines shared replay gating, bounded replay pausing, continuation, user feedback, and compatibility coverage while background work is pending.

### Modified Capabilities

- `replayable-command-projection`: Adds pending-background completion to replay-or-retry eligibility and direct invocation guarding.
- `unified-command-manager`: Adds next and bounded replay actions to the forward manager and exposes them in its fallback presentation.

## Impact

- Affects the commandlog applib subscriber, replay support, replayable-command action, unified and legacy replay-manager actions, module registration, fallback layout, and focused tests.
- Reuses `CommandLogEntryRepository.findBackgroundAndNotYetStarted()` and the existing Causeway 4 JPA named query; no persistence contract or datastore migration is expected.
- Requires integration evidence that the scheduling foreground command succeeds, a subsequent recorded command is rejected only in recording-support mode, and replay can resume after background execution commits.
