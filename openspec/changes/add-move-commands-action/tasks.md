## 1. Locate and Prepare Command Movement Points

- [x] 1.1 Confirm how `CommandExportManager` obtains the baseline-bounded exportable command list for `notYetExported`.
- [x] 1.2 Confirm how `ReplayableCommand` resolves its backing `CommandLogEntry` and exposes interaction id and timestamp data.
- [x] 1.3 Confirm whether existing repository queries and persistence contexts flush `CommandLogEntry#setTimestamp(...)` changes without additional save calls.
- [x] 1.4 Identify where `CausewayModuleExtCommandLogApplib` registers `CommandExportManager` action contributions.

## 2. Implement Move Action

- [x] 2.1 Add a `CommandExportManager_moveCommands` action associated with the `notYetExported` collection.
- [x] 2.2 Accept `List<ReplayableCommand> selected` and a `ReplayableCommand target` parameter.
- [x] 2.3 Provide target choices from the baseline-bounded exportable command list, excluding selected commands.
- [x] 2.4 Disable or validate the action when the exportable command list is empty.
- [x] 2.5 Validate that the selection is non-empty and that the target is present.
- [x] 2.6 Validate that selected commands and the target command are all within the baseline-bounded exportable command set.
- [x] 2.7 Validate that the target command is not one of the selected commands.
- [x] 2.8 Register the new action contribution in the command-log applib module.

## 3. Retimestamp Selected Commands

- [x] 3.1 Resolve selected commands and the target command to `CommandLogEntry` instances.
- [x] 3.2 Sort selected command log entries by their original timestamps before mutating any timestamps.
- [x] 3.3 Retimestamp the first selected command to target timestamp plus 10ms.
- [x] 3.4 Retimestamp subsequent selected commands by preserving original positive gaps from the selected block.
- [x] 3.5 Use a deterministic minimum 10ms gap when original gaps are zero, negative, or unavailable.
- [x] 3.6 Ensure unselected commands and the target command keep their original timestamps.
- [x] 3.7 Return the `CommandExportManager` after movement so the UI can refresh the same manager context.

## 4. Verify Export Validation After Movement

- [x] 4.1 Add or update tests showing a moved finder result can validate a later selected action target.
- [x] 4.2 Add or update tests showing a moved navigation or finder result can validate a later selected reference parameter.
- [x] 4.3 Ensure `CommandExportManager_exportSelected` continues to sort by current command timestamps and needs no separate order override.
- [x] 4.4 Ensure existing unknown-target and unknown-reference-parameter rejection tests still pass when commands have not been moved.

## 5. Test Move Action Behavior

- [x] 5.1 Add action tests showing target choices include baseline-bounded exportable commands and exclude selected commands.
- [x] 5.2 Add validation tests for empty selection, missing target, target included in selected commands, and commands outside the baseline-bounded set.
- [x] 5.3 Add retimestamping tests for a single moved command using target plus 10ms.
- [x] 5.4 Add retimestamping tests for multiple moved commands preserving positive original internal gaps.
- [x] 5.5 Add retimestamping tests for zero or negative original gaps using the deterministic minimum 10ms increment.
- [x] 5.6 Add retimestamping tests showing unselected commands and the target command are unchanged.

## 6. Build and Review

- [x] 6.1 Run focused command-log applib tests for export selection, known-target validation, and the new move action.
- [x] 6.2 Run the relevant module build or broader test target used by this repository for command-log changes.
- [x] 6.3 Review action labels, descriptions, and collection association so the UI clearly explains that timestamps are changed.
- [x] 6.4 Run `openspec validate add-move-commands-action --strict` and fix any proposal, spec, or task issues.
