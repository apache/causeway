## Why

Causeway 4 now has result-bearing command transfer DTOs, a unified baseline-bounded manager, and participant reachability feedback, but its operational export and import actions still use the legacy two-manager, plain-`CommandDto` YAML workflow. E1 connects those foundations so a reviewed sequence can be exported as replay-complete YAML and imported with its recorded result identities intact.

## What Changes

- Add a unified-manager `exportSequence` action that implicitly selects commands with known participants, emits result-bearing multi-document YAML in manager order, and leaves command replay state unchanged.
- Add an optional export mode that applies existing recorded-to-actual bookmark mappings to independent copies of command targets, reference parameters, and result metadata without mutating recorded DTOs or entries.
- Add a replay-import decoder that accepts result-bearing multi-document `CommandExportDto` YAML first and falls back to legacy multi-document `CommandDto` YAML, while deliberately rejecting YAML-list roots.
- Persist imported result bookmarks without resolving domain objects and optionally move the unified manager baseline to the oldest imported command while preserving its limit.
- Retain the existing plain `CommandDto` YAML APIs and legacy manager logical types/bookmarks for compatibility; make the unified manager the forward export/import surface.
- Keep command exclusion, restoration, deletion, movement, retimestamping, replay-action gating, and background-completion gating in W1 and B1/B2.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `command-result-metadata`: Adds replay-import decoding for result-bearing and legacy multi-document YAML, list-root rejection, and unresolved result-bookmark preservation.
- `command-export-known-targets`: Makes R2 known-participant status the implicit selection boundary for sequence export without changing replay state.
- `command-replay-mapping`: Adds optional export-time remapping of copied command inputs and result metadata through the existing mapping SPI.
- `unified-command-manager`: Adds the primary sequence-export and replay-import actions, including filename defaults and optional baseline movement.

## Impact

- Affects `api/applib` command DTO YAML utilities and focused YAML compatibility tests.
- Affects commandlog applib unified-manager actions, replay context integration, result remapping, fallback layout, and focused manager tests.
- Reuses D1 result-bearing envelopes, M1 mapping listeners, P2 manager ordering, and R2 reachability; adds no persisted fields or database schema changes.
- Existing stored command DTOs, manager mementos, replay states, legacy manager bookmarks, Jakarta persistence adapters, and the removed commandlog JDO module remain unchanged.
