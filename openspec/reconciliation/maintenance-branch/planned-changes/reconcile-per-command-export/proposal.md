> **Execution note:** two mixins reusing the existing bulk-export building blocks; **`medium` reasoning effort**
> is sufficient.

## Why

Child change 7 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancy **MA-10**;
third-opinion F6 — MED, single-source but code-confirmed by both meta-analyses).

Maintenance provides **per-command** YAML export actions on `ReplayableCommand` — `ReplayableCommand_export`
(object form, row → `Clob`) and `ReplayableCommand_exportTR` (table row) — that emit a result-bearing
`CommandExportDto` for one command. `main` has **neither source file**, and neither is registered; only the bulk
`CommandManager_exportSequence` exists. Bulk sequence export does not replace the observable ability to export a
single `ReplayableCommand` directly from its object form or table row.

All the infrastructure the mixins need already exists on `main`:
`CommandManager_exportSequence.java:68-79` builds each entry as
`CommandDtoUtils.CommandExportDto.of(entry.getCommandDto(), entry.getResult())`, optionally remaps via
`ResultRemappingService.remapped(export)`, serialises with `CommandDtoUtils.toYamlExport(...)`, and returns
`Clob.of(filename, CommonMimeType.YAML, yaml)`.

## What Changes

- Add `ReplayableCommand_export` (object-form action) that exports the single command as a result-bearing
  `CommandExportDto` YAML `Clob`, non-state-mutating, reusing `CommandExportDto.of` + `toYamlExport` and the
  optional `ResultRemappingService` remapping, with a file name derived from the command (interaction id /
  timestamp).
- Add `ReplayableCommand_exportTR` (table-row variant, hidden on object forms) with the same behaviour.
- Register both mixins in `CausewayModuleExtCommandLogApplib` alongside the existing `ReplayableCommand_*` and
  `CommandManager_exportSequence` entries.
- Do not change or replace the bulk `CommandManager_exportSequence`, replay state, or persistence.

## Capabilities

### Modified Capabilities

- `replayable-command-projection`: a single replayable command can be exported to a result-bearing YAML document
  from its object form and its table row, without mutating replay state.

## Impact

- Adds commandlog applib `ReplayableCommand_export` and `ReplayableCommand_exportTR`, and their module
  registration; reuses `CommandDtoUtils.CommandExportDto`, `toYamlExport`, and `ResultRemappingService`.
- Non-state-mutating (no `EXPORTED` transition) — this is a read/export surface, distinct from the retained
  legacy `makeExportable` workflow (MA-15, kept).
- Requires coverage: exporting one command yields a YAML document containing that command and its recorded
  result; the table-row variant is available in the manager's collections; neither mutates replay state.
