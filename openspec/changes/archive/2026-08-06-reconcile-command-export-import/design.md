## Context

D1 added `CommandExportDto`, `ImportedCommandDto`, bookmark metadata, deep command copying, and result-bearing multi-document YAML output while deliberately retaining the legacy plain-`CommandDto` methods. M1 added mapping lookup and independent command copies for replay. P2 added the unified `CommandManager`, and R2 added manager-scoped `knownParticipants` plus an ordered validator. The operational actions still live on the legacy `CommandExportManager` and `CommandReplayManager`: they export plain command DTOs, change replay state to `EXPORTED`, and import without preserving envelope result metadata.

The maintenance implementation supplies unified-manager export and import mixins and is authoritative for observable behavior. Causeway 4 must adapt that behavior to Jakarta injection, immutable replay context, the retained legacy `EXPORTED` state and manager logical types, current `CommandDtoJacksonSupport`, and the absence of commandlog JDO.

## Goals / Non-Goals

**Goals:**

- Export the manager's implicit sequence of R2-known commands as ordered result-bearing multi-document YAML without mutating replay state.
- Optionally remap exported targets, reference parameters, and results on independent envelope copies using M1 lookup semantics.
- Decode canonical result-bearing YAML and legacy multi-document command YAML through an explicit replay-import API.
- Persist imported result bookmarks without resolving them and optionally reposition the unified manager baseline.
- Keep the format, action, and orchestration contracts independently testable.

**Non-Goals:**

- Do not exclude, restore, delete, move, or retimestamp commands; W1 owns those workflows.
- Do not replay commands or change replay/retry action eligibility.
- Do not wait for recording or replay background work; B1/B2 own those gates.
- Do not change manager mementos, add replay states, add persisted fields, or restore commandlog JDO.
- Do not remove or rewrite legacy manager logical types and bookmarks.

## Decisions

### Add a strict replay-import decoder beside the legacy YAML API

Add `CommandDtoUtils.fromYamlForReplay(DataSource)` returning `ImportedCommandDto` values. It rejects a YAML list root, then attempts multi-document `CommandExportDto` decoding first. A wrapped parse counts as successful only when at least one document contains an embedded command; this prevents permissive Jackson binding from misclassifying legacy command documents as empty envelopes. If wrapped decoding fails, it falls back to multi-document `CommandDto` and supplies null result bookmarks. When neither succeeds, the final exception retains the wrapped failure as suppressed context.

The existing `fromYaml`, `toYaml`, and `toMultiDocYaml` methods remain unchanged. Replacing `fromYaml` was rejected because it would silently remove list compatibility from unrelated public callers; E1 needs a stricter operational import boundary, not a breaking API change.

### Export the manager's implicit known-participant sequence

Add an `exportSequence` contribution to `CommandManager`. It derives entries from the same bounded, eligible, non-excluded order as `commandsInSequence`, retains only replayable commands whose R2 `knownParticipants` value is true, and emits a `CommandExportDto` for each entry using its recorded command DTO and result bookmark. The action is disabled when that implicit sequence is empty.

There is no explicit row-selection parameter. This follows maintenance's final workflow and makes the visible R2 flag the selection contract. Retaining the legacy selectable export action as the primary path was rejected because it can bypass the unified context and mutates entries to `EXPORTED`. E1 leaves the legacy action loadable for compatibility but does not expose it as the forward workflow.

### Keep export observational and emit independent data

Export preserves repository order and does not write replay state, result, DTO, timestamp, or manager state. Filename prefix defaults to `commands`; an optional suffix is derived from the first exported command timestamp and sanitized for a filename. Output is a YAML `Clob` produced only after the complete envelope stream is prepared.

An optional `remapResults` parameter uses `ResultRemappingService` to copy each envelope, remap command targets and reference parameters, and remap the result bookmark through the same ordered first-match lookup. Disabled remapping emits recorded identities. Extending the service with envelope remapping was chosen over mutating an envelope in the action so copy and listener-failure behavior stays centralized and directly testable.

### Import through repository creation, then attach result identity

Add an `importCommands` contribution to `CommandManager`. For each decoded carrier it calls `CommandLogEntryRepository.saveForReplay(command)` and then sets the returned entry's result only when envelope metadata is present. Bookmark construction uses logical type and identifier only; it never asks `BookmarkService` to resolve an object. The existing repository operation retains ownership of replay-state initialization and idempotence.

When requested, the returned manager uses the oldest non-null imported command timestamp as its baseline and preserves the current limit. Empty imports or imports without usable timestamps return the current manager. Adding a repository overload for the carrier was rejected because E1 adds orchestration but no new persistence semantic or adapter-specific query.

### Preserve compatibility boundaries explicitly

The unified manager becomes the visible forward export/import surface and its fallback layout exposes both actions. Legacy manager types, mementos, hidden menu identifiers, and direct construction remain available. Plain YAML methods keep their accepted formats; only the new replay-import operation rejects list roots. No persistence-adapter verification is needed beyond the repository contract because E1 adds no schema or query operation.

## Risks / Trade-offs

- [Permissive YAML binding can accept the wrong root type] → Require at least one embedded command before accepting wrapped documents and reject list roots before type attempts.
- [A mapping listener can fail during optional export remapping] → Reuse M1 lookup behavior, logging a listener failure and continuing to later listeners or the recorded identity.
- [Imported result bookmarks may not exist locally] → Store portable bookmark identity without object resolution, as required for later replay mapping.
- [Repeated import can encounter existing interaction ids] → Delegate idempotence and replay-state initialization to `saveForReplay` and test repeated import behavior at the commandlog boundary.
- [Exporting computed metadata could accidentally alter recorded data] → Deep-copy every remapped envelope and assert DTO, result, replay state, timestamp, and manager memento remain unchanged.
- [Legacy list YAML is accepted by the general utility but rejected operationally] → Keep APIs separate, document the distinction, and add focused compatibility and rejection tests.

## Migration Plan

No datastore or bookmark migration is required. Deploy the API and commandlog applib changes together. Existing legacy managers and plain YAML callers continue to work; new unified-manager exports use the canonical envelope shape. Rollback removes the unified actions and strict decoder without changing stored commands or manager mementos.

## Open Questions

None blocking. W1 will decide how workflow mutations interact with the sequence before export, and B1/B2 will add background-completion gates around later actions.
