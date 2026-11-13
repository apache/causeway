## Context

`CommandManager_exportSequence` is the bulk exporter and the reference implementation for a per-command export:

```
// extensions/core/commandlog/applib/.../dom/replay/CommandManager_exportSequence.java:68-79
var exports = exportable.stream()
    .map(ReplayableCommand::commandLogEntry).flatMap(Optional::stream)
    .map(entry -> CommandDtoUtils.CommandExportDto.of(entry.getCommandDto(), entry.getResult()))
    .map(export -> remapResults
        ? commandManager.replayContext().resultRemappingService().remapped(export)
        : export)
    .toList();
final String yaml = CommandDtoUtils.toYamlExport(exports);
return Clob.of(filename, CommonMimeType.YAML, yaml);
```

`ResultRemappingService.remapped(CommandExportDto)` remaps the command targets/parameters and the result
bookmark. Neither `ReplayableCommand_export` nor `ReplayableCommand_exportTR` exists on `main`; the registered
`ReplayableCommand_*` mixins are `makeExportable`, `openCommandLogEntry`, `previous`, `next`, `replayOrRetry`,
`excludeFromReplay`, `delete`.

Maintenance's per-command mixins emit the same result-bearing `CommandExportDto` for a single command, with file
naming controls, and do not mutate replay state.

## Goals / Non-Goals

**Goals:**

- Export a single replayable command to a result-bearing YAML `Clob` from its object form and its table row.
- Reuse the existing export-DTO, YAML, and result-remapping infrastructure.

**Non-Goals:**

- No change to the bulk `CommandManager_exportSequence`.
- No replay-state mutation (this is not the legacy `makeExportable`/`EXPORTED` workflow, which is retained
  separately under MA-15).
- No new persistence, schema, or configuration.

## Decisions

### Reuse the bulk exporter's building blocks for one command

Each per-command mixin builds `CommandExportDto.of(entry.getCommandDto(), entry.getResult())` for its single
`ReplayableCommand`, optionally applies `ResultRemappingService.remapped(...)`, serialises with
`CommandDtoUtils.toYamlExport(List.of(export))`, and returns `Clob.of(filename, CommonMimeType.YAML, yaml)`. This
keeps per-command and bulk export byte-compatible.

### Two mixins for the two surfaces

`ReplayableCommand_export` targets the object form; `ReplayableCommand_exportTR` is the table-row variant (hidden
on object forms, `Where.OBJECT_FORMS`), mirroring the existing `_openTarget` / `_openTargetTR` pairing. Both are
`@Action(semantics = SAFE)` (non-mutating) with command/execution publishing disabled.

### File naming

Derive the download file name from the command (interaction id and/or timestamp), consistent with
`exportSequence`'s `filenamePrefix + timestampSuffix` scheme.

## Acceptance evidence

- Object-form export of one replayable command yields a YAML document containing that command and, when present,
  its recorded result bookmark; with remapping enabled, the remapped result is emitted.
- The table-row variant is available on the manager's command collections and produces the same document.
- Neither action mutates replay state.
