## 1. Export Manager State and Collections

- [x] 1.1 Remove `CommandExportManager.Mode`, `mode`, `withMode`, and mode parsing/serialization from `CommandExportManager` state.
- [x] 1.2 Replace `getNotYetExported` and `getExported` with a single `getCommands` collection that returns all foreground commands at or after the baseline regardless of replay state.
- [x] 1.3 Replace previous-page support with unified command paging that is not replay-state filtered.
- [x] 1.4 Remove `CommandExportManager_toggleMode` and its references from layouts, tests, and generated metadata expectations.

## 2. Actions and UI Metadata

- [x] 2.1 Update `CommandExportManager_exportSelected` to associate with and choose from `commands`.
- [x] 2.2 Update export selection handling so selected commands from `commands` are exported and then marked `EXPORTED` without disappearing because of replay state.
- [x] 2.3 Update `CommandExportManager_moveCommands` to associate with and choose from `commands`, including commands whose replay state is `EXPORTED`.
- [x] 2.4 Rename or replace fallback column-order resources so the unified `commands` collection displays replay state.
- [x] 2.5 Update fallback layout resources to remove mode/toggle UI and show the unified `commands` collection.

## 3. Repository Queries and Validation

- [x] 3.1 Add or reuse repository methods for foreground commands since and before the baseline without replay-state filtering.
- [x] 3.2 Ensure move-command validation rejects only commands outside the baseline-bounded command set, not commands with replay state `EXPORTED`.
- [x] 3.3 Ensure known-target validation continues to use selected command order and can use selected earlier result commands regardless of replay state.

## 4. Tests and Verification

- [x] 4.1 Update export-manager state tests for baseline-and-limit mementos with no mode.
- [x] 4.2 Update collection tests to assert `commands` includes mixed `UNDEFINED` and `EXPORTED` replay states and excludes commands before the baseline.
- [x] 4.3 Update export-selected tests for the `commands` association and replay-state update behavior.
- [x] 4.4 Update move-command tests for unified command choices and exported target commands.
- [x] 4.5 Run the commandlog applib test suite or the nearest affected Maven module tests.
