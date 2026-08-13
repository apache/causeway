## Context

There are two YAML decoders in `CommandDtoUtils`:

- `fromYaml(DataSource)` (`:178-182`) — general-purpose, lenient: `tryReadAsList(...).getValue().orElseGet(emptyList)`.
  A parse failure yields an empty list.
- `fromYamlForReplay(DataSource)` (`:195-233`) — strict: tries the wrapped `CommandExportDto` multi-document form,
  falls back to the plain `CommandDto` multi-document form, rejects a YAML list root, and calls `ifFailureFail()`
  so unparseable input throws.

The unified importer already uses the strict one:

```
// CommandManager_importCommands.java:64-67
final var imported = CommandDtoUtils.fromYamlForReplay(commandsYaml.asDataSource());
imported.forEach(value -> …commandLogEntryRepository().saveForReplay(value.getCommand()));
```

The legacy importer uses the lenient one:

```
// CommandReplayManager.java:159-160
final List<CommandDto> commandDtos = CommandDtoUtils.fromYaml(yamlDs);
commandDtos.forEach(commandLogEntryRepository()::saveForReplay);
```

The existing `command-result-metadata` spec already specifies the strict decoder ("Replay import decodes canonical
and legacy multi-document YAML") and explicitly keeps the plain public `fromYaml` API unchanged ("existing plain
`CommandDto` YAML APIs … MUST remain unchanged"). The gap (MA-11) is only that the legacy **importer** does not
*use* the strict decoder.

## Goals / Non-Goals

**Goals:**

- The legacy replay-manager import fails on malformed YAML instead of silently importing nothing, and accepts the
  same canonical wrapped and legacy multi-document forms as the unified importer.

**Non-Goals:**

- No change to the general-purpose public `CommandDtoUtils.fromYaml(...)` contract (non-replay callers keep the
  lenient list behaviour, per the existing spec).
- No removal of the legacy `CommandReplayManager` (retained as documented v4 compatibility — MA-15 / D-D).
- No new decoder — reuse the existing `fromYamlForReplay`.

## Decisions

### Route the legacy importer through `fromYamlForReplay`

Replace the legacy importer's `fromYaml` call with `fromYamlForReplay`, iterate the returned carriers, and
`saveForReplay(carrier.getCommand())` — identical to the unified importer. Derive the `moveBaselineToOldest`
timestamp from the decoded carriers' commands.

Rejected — hardening the public `fromYaml` API to fail-not-empty: the existing spec deliberately keeps that
general API's list behaviour for non-replay callers; the reachable defect is the importer, so fix it there.

### Keep the legacy manager (D-D)

This change only swaps the decoder inside the retained legacy importer. The legacy managers, `EXPORTED` state,
and `makeSelectedExportable` remain as documented v4 compatibility (MA-15 decision).

## Acceptance evidence

- A malformed-YAML upload through the legacy `CommandReplayManager.importCommands` **fails** with a clear error
  (no entries imported, failure reported) — contrast today's silent empty success. A runtime-style probe (as in
  third-opinion F9) confirming the legacy path now throws is acceptable.
- A valid canonical (wrapped, result-bearing) stream and a valid legacy multi-document stream both import
  correctly through the legacy path, storing result bookmarks where present.
- The general-purpose `CommandDtoUtils.fromYaml` API behaviour is unchanged for non-replay callers.
