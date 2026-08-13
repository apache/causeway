## 1. Re-verify on current HEAD

- [x] 1.1 Confirm `ReplayableCommand_export` / `_exportTR` are absent, and confirm the `CommandExportDto.of` +
      `toYamlExport` + `ResultRemappingService.remapped` building blocks used by `CommandManager_exportSequence`.

## 2. Per-command export mixins

- [x] 2.1 Add `ReplayableCommand_export` (object form): build `CommandExportDto.of(commandDto, result)` for the
      single command, optional `ResultRemappingService.remapped(...)`, serialise with `toYamlExport`, return a
      YAML `Clob`; `SAFE` semantics, command/execution publishing disabled; derive the file name from the
      command.
- [x] 2.2 Add `ReplayableCommand_exportTR` (table-row variant, hidden on object forms) with identical behaviour.
- [x] 2.3 Register both mixins in `CausewayModuleExtCommandLogApplib`.

## 3. Tests

- [x] 3.1 Object-form export yields a YAML document with the command and its recorded result bookmark; remapping
      enabled emits the remapped result.
- [x] 3.2 Table-row variant is available on the manager collections and produces the same document.
- [x] 3.3 Neither action mutates replay state.

## 4. Verification

- [x] 4.1 Run focused commandlog applib export tests plus the affected reactor under JDK 21, and strict OpenSpec
      validation.
- [x] 4.2 Confirm the bulk `CommandManager_exportSequence` and replay state are unchanged.
