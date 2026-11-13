## Why

The unified command manager can show a baseline-bounded sequence, and R1 can classify stable reference data, but it cannot yet tell whether each command is replay-reachable from the sequence that precedes it. R2 adds that derived feedback now so the later export/import slice can enforce one shared participant-validity contract rather than inventing validation inside YAML actions.

## What Changes

- Add a deterministic validator for command targets and reference-valued action parameters, treating a participant as known only when it is a domain-service root, stable reference data, or the recorded result of an earlier eligible command in the manager's current baseline-bounded order.
- Define Causeway 4 export roots by logical type: a bookmark is a root when its metamodel specification is a domain service; resolving or loading an ordinary bookmarked object does not make it a root.
- Add a non-persisted `knownParticipants` property to `ReplayableCommand`, false outside an active manager context and when command-log recording support is disabled.
- Make the unified manager provide the ordered reachability context used while its command collections are rendered, including results from earlier eligible non-excluded commands regardless of replay state.
- Surface `knownParticipants` after result presence in manager tables without changing replay state, command persistence, or existing manager mementos.
- Keep YAML generation and import, export-sequence selection/enforcement, workflow mutations, replay action gates, and background-completion gates in their later slices.

## Capabilities

### New Capabilities

- `command-export-known-targets`: Defines export roots and baseline-bounded reachability rules for command targets and reference-valued parameters.
- `replayable-command-exportability`: Defines the derived `knownParticipants` state and its context-sensitive presentation on replayable commands.

### Modified Capabilities

- `unified-command-manager`: Makes the manager the provider of ordered participant-reachability context and exposes the resulting status in its review tables.

## Impact

- Affects commandlog applib replay-domain code, replay context wiring, fallback layouts, and focused tests.
- Consumes the existing command-recording configuration, `CommandReplayReferenceDataService`, metamodel logical-type lookup, P1 result/participant metadata, and P2 manager ordering.
- Adds no persisted fields or schema changes and does not restore the removed commandlog JDO adapter.
- Establishes the validation primitive consumed by the later `reconcile-command-export-import` slice without changing YAML behavior in R2.
