## 1. Action Defaults

- [ ] 1.1 Review `CommandExportManager_exportSelected` and `CommandExportManager_excludeCommands` default-selection patterns.
- [ ] 1.2 Add `defaultSelected()` to `CommandExportManager_exportSelected` that returns active commands with `Boolean.TRUE` exportability.
- [ ] 1.3 Keep `choicesSelected()` returning the full active `commands` collection.
- [ ] 1.4 Ensure default selection does not mutate replay state.

## 2. Tests

- [ ] 2.1 Add focused coverage showing exportable active commands are selected by default and non-exportable active commands are not.
- [ ] 2.2 Add focused coverage showing commands with `null` exportability are not selected by default, if construction outside export-manager context is relevant to the action test fixture.
- [ ] 2.3 Add or verify coverage showing export action choices still include the full active command collection.
- [ ] 2.4 Add or verify coverage showing computing defaults does not mark commands `EXPORTED`.

## 3. Verification

- [ ] 3.1 Run focused command-log applib tests for `CommandExportManagerExportSelectedTest`.
- [ ] 3.2 Run OpenSpec validation for `autoselect-exportable-commands`.
