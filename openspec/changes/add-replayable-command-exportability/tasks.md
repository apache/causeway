## 1. Validation Support

- [ ] 1.1 Add shared exportability validation support so `CommandExportManager_exportSelected` and `ReplayableCommand` can use the same baseline, export-root, recording-support, target, and reference-parameter rules.
- [ ] 1.2 Add a helper that can validate the export-manager command prefix ending at a specific `CommandLogEntry`.
- [ ] 1.3 Preserve the existing export action validation messages and behavior.

## 2. Scratchpad Context

- [ ] 2.1 Define a namespaced well-known scratchpad key for the current `CommandExportManager`.
- [ ] 2.2 Inject or otherwise obtain `Scratchpad` where `CommandExportManager#getCommands()` can populate the key before constructing replayable commands.
- [ ] 2.3 Ensure replayable commands constructed outside the export-manager commands collection do not require scratchpad context.

## 3. Replayable Command Property

- [ ] 3.1 Add a nullable Boolean exportability property to `ReplayableCommand` with suitable Causeway property annotations and table-visible placement.
- [ ] 3.2 Compute `true` when the current command passes export validation in its export-manager sequence prefix.
- [ ] 3.3 Compute `false` when the current command fails export validation in its export-manager sequence prefix.
- [ ] 3.4 Return `null` when no export-manager context is available or exportability cannot be evaluated.
- [ ] 3.5 Ensure exportability computation does not mutate replay state or command log entries.

## 4. Tests

- [ ] 4.1 Add tests showing a command with known target or reference parameter reports exportable `true` in export-manager context.
- [ ] 4.2 Add tests showing a command with unknown target or reference parameter reports exportable `false` in export-manager context.
- [ ] 4.3 Add tests showing a later result does not make an earlier command exportable.
- [ ] 4.4 Add tests showing a replayable command constructed outside export-manager context reports unknown exportability.
- [ ] 4.5 Add regression coverage that export action validation still rejects invalid selected sequences and accepts valid selected sequences.

## 5. Verification

- [ ] 5.1 Run the commandlog applib replay tests relevant to command export and replayable command mapping.
- [ ] 5.2 Run OpenSpec validation for `add-replayable-command-exportability`.
