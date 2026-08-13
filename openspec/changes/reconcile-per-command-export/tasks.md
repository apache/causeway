## 1. Re-verify on current HEAD

- [ ] 1.1 Confirm `ReplayableCommand_export` / `_exportTR` are absent, and confirm the `CommandExportDto.of` +
      `toYamlExport` + `ResultRemappingService.remapped` building blocks used by `CommandManager_exportSequence`.

## 2. Per-command export mixins

- [ ] 2.1 Add `ReplayableCommand_export` (object form): build `CommandExportDto.of(commandDto, result)` for the
      single command, optional `ResultRemappingService.remapped(...)`, serialise with `toYamlExport`, return a
      YAML `Clob`; `SAFE` semantics, command/execution publishing disabled; derive the file name from the
      command.
- [ ] 2.2 Add `ReplayableCommand_exportTR` (table-row variant, hidden on object forms) with identical behaviour.
- [ ] 2.3 Register both mixins in `CausewayModuleExtCommandLogApplib`.

## 3. Tests

- [ ] 3.1 Object-form export yields a YAML document with the command and its recorded result bookmark; remapping
      enabled emits the remapped result.
- [ ] 3.2 Table-row variant is available on the manager collections and produces the same document.
- [ ] 3.3 Neither action mutates replay state.

## 4. Verification

- [ ] 4.1 Run focused commandlog applib export tests plus the affected reactor under JDK 21, and strict OpenSpec
      validation.
- [ ] 4.2 Confirm the bulk `CommandManager_exportSequence` and replay state are unchanged.
