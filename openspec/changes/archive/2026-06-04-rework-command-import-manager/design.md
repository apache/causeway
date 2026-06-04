## Context

`CommandExportManager_exportSelected` now exports selected commands as multi-document YAML entries whose root type is `CommandExportDto`.
Each export entry contains the replayable `CommandDto` plus optional `returnedObject` metadata derived from `CommandLogEntry#getResult()`.

`CommandReplayManager_importCommands` currently delegates YAML decoding to `CommandDtoUtils.fromYaml(...)` and persists only the returned `CommandDto` values through `CommandLogEntryRepository#saveForReplay`.
The current utility also accepts a YAML list of `CommandDto` values and does not expose returned object metadata to the importer.

## Goals / Non-Goals

**Goals:**

- Prefer importing multi-document `CommandExportDto` YAML as the canonical export/import format.
- Preserve compatibility with legacy multi-document `CommandDto` YAML.
- Store `CommandExportDto.returnedObject` as the persisted `CommandLogEntry` result bookmark when importing exported commands.
- Remove support for importing a single YAML list of `CommandDto` values.

**Non-Goals:**

- Do not change how command export selects commands or serializes `CommandDto` content.
- Do not require the returned object bookmark to resolve to a live domain object during import.
- Do not change command replay execution semantics beyond the imported metadata stored on `CommandLogEntry`.

## Decisions

- Add an import result model that carries each `CommandDto` together with the optional returned object bookmark.
  This keeps parsing concerns in `CommandDtoUtils` while giving `CommandReplayManager_importCommands` enough information to persist the result.
  Alternative considered: encode returned object back into `CommandDto.userData`, but that would blur the new export envelope with legacy command DTO data and make the import path harder to reason about.

- Attempt `CommandExportDto` multi-document parsing before `CommandDto` multi-document parsing.
  This matches the new export shape and prevents a wrapped export document from being accidentally treated as an empty or malformed command DTO.
  Alternative considered: retain the old order and add post-processing, but that would make error handling dependent on Jackson's partial binding behavior.

- Remove list parsing from the command replay import path.
  The supported formats become explicit multi-document streams, which matches the export manager output and avoids ambiguity between list and document-stream formats.
  Alternative considered: keep list support as an undocumented fallback, but the requested behavior explicitly removes that compatibility surface.

- Persist returned object metadata after creating the replay `CommandLogEntry`.
  `CommandLogEntryRepository#saveForReplay` already owns replay entry creation, and the importer can set `CommandLogEntry#setResult(...)` on the returned entity when the parsed export entry includes a returned object.
  Alternative considered: add an overload to the repository that accepts both values; that is reasonable if implementations need stricter encapsulation, but the import action can remain the orchestration point.

## Risks / Trade-offs

- YAML documents that were previously accepted only because they were lists will fail import → document the supported multi-document alternatives and update tests to assert rejection.
- Parsing order changes can alter error messages for malformed YAML → keep suppressed failures or otherwise surface both attempted formats when neither import path succeeds.
- Returned object bookmarks might refer to objects absent on the importing system → store the bookmark without resolving it, matching the export metadata contract.
