## 1. YAML Import Parsing

- [ ] 1.1 Add a `CommandDtoUtils` import result type that exposes each parsed `CommandDto` with its optional returned object bookmark.
- [ ] 1.2 Add a command replay import parser that first reads multi-document `CommandExportDto` YAML.
- [ ] 1.3 Add fallback parsing for multi-document `CommandDto` YAML when `CommandExportDto` parsing fails.
- [ ] 1.4 Remove list-of-`CommandDto` parsing from the command replay import path.
- [ ] 1.5 Preserve useful failure details when neither supported multi-document format can be parsed.

## 2. Replay Import Persistence

- [ ] 2.1 Update `CommandReplayManager_importCommands` to use the new import result model rather than only `List<CommandDto>`.
- [ ] 2.2 Persist each imported command for replay through `CommandLogEntryRepository#saveForReplay`.
- [ ] 2.3 Set the created `CommandLogEntry` result bookmark from `CommandExportDto.returnedObject` when present.
- [ ] 2.4 Keep baseline movement based on the embedded command DTO timestamps.

## 3. Tests

- [ ] 3.1 Add or update tests for importing multi-document `CommandExportDto` YAML with returned object metadata.
- [ ] 3.2 Add or update tests for importing multi-document `CommandExportDto` YAML without returned object metadata.
- [ ] 3.3 Add or update tests for legacy multi-document `CommandDto` YAML fallback.
- [ ] 3.4 Add or update tests that a YAML list of `CommandDto` values is rejected by command replay import.
- [ ] 3.5 Run the focused command DTO and command replay import test suite.
