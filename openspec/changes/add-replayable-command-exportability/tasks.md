## 1. Validation Support

- [x] 1.1 Add shared exportability validation support so `CommandExportManager_exportSelected` and `ReplayableCommand` can use the same baseline, export-root, recording-support, target, and reference-parameter rules.
- [x] 1.2 Add a helper that provides known participants as-of a specific command and validates only that command's own participants.
- [x] 1.3 Preserve the existing export action validation messages and behavior.

## 2. Scratchpad Context

- [x] 2.1 Define a namespaced well-known scratchpad key for the current `CommandExportManager`.
- [x] 2.2 Inject or otherwise obtain `Scratchpad` where `CommandExportManager#getCommands()` can populate the key before constructing replayable commands.
- [x] 2.3 Ensure replayable commands constructed outside the export-manager commands collection do not require scratchpad context.

## 3. Replayable Command Property

- [x] 3.1 Add a nullable Boolean exportability property to `ReplayableCommand` with suitable Causeway property annotations and table-visible placement.
- [x] 3.2 Compute `true` when the current command passes export validation in its export-manager sequence prefix.
- [x] 3.3 Compute `false` when the current command fails export validation in its export-manager sequence prefix.
- [x] 3.4 Return `null` when no export-manager context is available or exportability cannot be evaluated.
- [x] 3.5 Ensure exportability computation does not mutate replay state or command log entries.

## 4. Tests

- [x] 4.1 Add tests showing a command with known target or reference parameter reports exportable `true` in export-manager context.
- [x] 4.2 Add tests showing a command with unknown target or reference parameter reports exportable `false` in export-manager context.
- [x] 4.3 Add tests showing a later result does not make an earlier command exportable.
- [x] 4.4 Add tests showing a replayable command constructed outside export-manager context reports unknown exportability.
- [x] 4.5 Add regression coverage that export action validation still rejects invalid selected sequences and accepts valid selected sequences.

## 5. Verification

- [x] 5.1 Run the commandlog applib replay tests relevant to command export and replayable command mapping.
- [x] 5.2 Run OpenSpec validation for `add-replayable-command-exportability`.
