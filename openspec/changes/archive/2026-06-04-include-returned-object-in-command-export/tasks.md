## 1. Export Data Shape

- [x] 1.1 Add an export-specific DTO or record that wraps the command DTO and optional returned object metadata.
- [x] 1.2 Add a bookmark metadata type with `logicalTypeName` and `id` fields derived from a `Bookmark`.
- [x] 1.3 Ensure returned object metadata is absent when `CommandLogEntry#getResult()` is null.

## 2. YAML Export Integration

- [x] 2.1 Update `CommandExportManager_exportSelected` to serialize wrapped export entries instead of bare command DTOs.
- [x] 2.2 Preserve existing ordering, filename generation, YAML mime type, and replay state updates.
- [x] 2.3 Verify exported YAML includes returned object metadata for commands with results and omits it for void commands.

## 3. Import Compatibility

- [x] 3.1 Update YAML import utilities or import action code to accept the new wrapped export shape.
- [x] 3.2 Keep legacy command DTO-only YAML import behavior unchanged.
- [x] 3.3 Ensure returned object metadata is treated as informational and is not required for replay execution.

## 4. Tests

- [x] 4.1 Add unit tests for bookmark metadata mapping from returned object bookmarks.
- [x] 4.2 Add export tests covering commands with and without returned object bookmarks.
- [x] 4.3 Add import compatibility tests for legacy command DTO YAML and new wrapped YAML.
- [x] 4.4 Run the relevant commandlog applib test suite or targeted Maven tests.
