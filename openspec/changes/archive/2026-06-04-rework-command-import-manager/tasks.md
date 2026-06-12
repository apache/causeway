## 1. YAML Import Parsing

- [x] 1.1 Add a `CommandDtoUtils` import result type that exposes each parsed `CommandDto` with its optional returned object bookmark.
- [x] 1.2 Add a command replay import parser that first reads multi-document `CommandExportDto` YAML.
- [x] 1.3 Add fallback parsing for multi-document `CommandDto` YAML when `CommandExportDto` parsing fails.
- [x] 1.4 Remove list-of-`CommandDto` parsing from the command replay import path.
- [x] 1.5 Preserve useful failure details when neither supported multi-document format can be parsed.

## 2. Replay Import Persistence

- [x] 2.1 Update `CommandReplayManager_importCommands` to use the new import result model rather than only `List<CommandDto>`.
- [x] 2.2 Persist each imported command for replay through `CommandLogEntryRepository#saveForReplay`.
- [x] 2.3 Set the created `CommandLogEntry` result bookmark from `CommandExportDto.returnedObject` when present.
- [x] 2.4 Keep baseline movement based on the embedded command DTO timestamps.

## 3. Tests

- [x] 3.1 Add or update tests for importing multi-document `CommandExportDto` YAML with returned object metadata.
- [x] 3.2 Add or update tests for importing multi-document `CommandExportDto` YAML without returned object metadata.
- [x] 3.3 Add or update tests for legacy multi-document `CommandDto` YAML fallback.
- [x] 3.4 Add or update tests that a YAML list of `CommandDto` values is rejected by command replay import.
- [x] 3.5 Run the focused command DTO and command replay import test suite.
