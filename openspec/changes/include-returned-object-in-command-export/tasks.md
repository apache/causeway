## 1. Export Data Shape

- [ ] 1.1 Add an export-specific DTO or record that wraps the command DTO and optional returned object metadata.
- [ ] 1.2 Add a bookmark metadata type with `logicalTypeName` and `id` fields derived from a `Bookmark`.
- [ ] 1.3 Ensure returned object metadata is absent when `CommandLogEntry#getResult()` is null.

## 2. YAML Export Integration

- [ ] 2.1 Update `CommandExportManager_exportSelected` to serialize wrapped export entries instead of bare command DTOs.
- [ ] 2.2 Preserve existing ordering, filename generation, YAML mime type, and replay state updates.
- [ ] 2.3 Verify exported YAML includes returned object metadata for commands with results and omits it for void commands.

## 3. Import Compatibility

- [ ] 3.1 Update YAML import utilities or import action code to accept the new wrapped export shape.
- [ ] 3.2 Keep legacy command DTO-only YAML import behavior unchanged.
- [ ] 3.3 Ensure returned object metadata is treated as informational and is not required for replay execution.

## 4. Tests

- [ ] 4.1 Add unit tests for bookmark metadata mapping from returned object bookmarks.
- [ ] 4.2 Add export tests covering commands with and without returned object bookmarks.
- [ ] 4.3 Add import compatibility tests for legacy command DTO YAML and new wrapped YAML.
- [ ] 4.4 Run the relevant commandlog applib test suite or targeted Maven tests.
