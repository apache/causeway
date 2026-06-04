## Why

Command replay import currently needs to align with the export format that can carry returned object metadata.
The import manager should prefer the richer `CommandExportDto` multi-document YAML shape while retaining replay compatibility for the legacy multi-document `CommandDto` shape.

## What Changes

- Import documents containing multiple `CommandExportDto` YAML documents and extract the embedded command DTOs for replay.
- When importing `CommandExportDto` YAML, persist each export entry's `returnedObject` value as the result bookmark of the corresponding `CommandLogEntry`.
- If parsing as multiple `CommandExportDto` documents fails, fall back to parsing the same input as multiple `CommandDto` YAML documents.
- **BREAKING** Remove support for importing a single YAML list of `CommandDto` values.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `command-export-yaml-result`: Command replay import changes to consume `CommandExportDto` multi-document YAML, retain multi-document `CommandDto` fallback, persist returned object metadata on import, and drop list-of-`CommandDto` import support.

## Impact

- Affects command replay import logic in the command import manager.
- Affects YAML parsing behavior for command import inputs.
- Affects `CommandLogEntry` creation or update behavior by setting the result bookmark when returned object metadata is present.
- Affects tests and documentation for supported command replay import formats.
