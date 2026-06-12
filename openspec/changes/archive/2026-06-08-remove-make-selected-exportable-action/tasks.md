## 1. Remove Obsolete Action

- [x] 1.1 Review references to `CommandExportManager_makeSelectedExportable` and confirm whether `ReplayableCommand_makeExportable` is still independently used.
- [x] 1.2 Remove `CommandExportManager_makeSelectedExportable` from `CausewayModuleExtCommandLogApplib` registration.
- [x] 1.3 Delete the `CommandExportManager_makeSelectedExportable` action class.
- [x] 1.4 Remove layout, generated metadata, or documentation references that expose the manager-level action.

## 2. Tests

- [x] 2.1 Update existing command-log applib tests or generated metadata expectations that reference the manager-level action.
- [x] 2.2 Add or update focused coverage showing the command export manager no longer registers or exposes `makeSelectedExportable`.
- [x] 2.3 Verify active `commands` collection behavior and row-level exportability coverage remains intact.

## 3. Verification

- [x] 3.1 Run focused command-log applib tests affected by command export manager action registration or metadata.
- [x] 3.2 Run OpenSpec validation for `remove-make-selected-exportable-action`.
