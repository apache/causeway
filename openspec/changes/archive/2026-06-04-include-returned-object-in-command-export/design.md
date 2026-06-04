## Context

`CommandExportManager_exportSelected` currently builds an export file by mapping selected `ReplayableCommand` instances back to their `CommandLogEntry` entities and serializing only `CommandLogEntry#getCommandDto()` through `CommandDtoUtils.toYaml(...)`.
`CommandLogEntry` already persists the returned object as a `Bookmark` via `getResult()`, while the command DTO includes target OIDs in a `logicalTypeName` plus `id` form.
The requested change is additive: export consumers need the returned object alongside the command information, but replay import should continue to rely on the command DTO payload.

## Goals / Non-Goals

**Goals:**

- Add returned object metadata to each exported command when the command log entry has a result bookmark.
- Use a returned object representation that mirrors target identity fields: `logicalTypeName` and `id`.
- Keep the exported YAML readable and deterministic for tests and users.
- Preserve import compatibility for existing command replay workflows.

**Non-Goals:**

- Do not change how command execution captures or persists `CommandLogEntry#getResult()`.
- Do not change replay semantics to use the returned object during command execution.
- Do not introduce a new external YAML dependency.
- Do not backfill returned object metadata into historical export files.

## Decisions

- Introduce an export-specific wrapper record rather than mutating `CommandDto`.
  This keeps replay DTO semantics stable and avoids adding result metadata to the schema-backed command DTO model.
  Alternative considered: storing result metadata in `CommandDto` user data, but that would blur replay input with export diagnostics and could affect import behavior.

- Serialize target and returned object bookmarks using the same two fields, `logicalTypeName` and `id`.
  This matches the user's requested shape and follows the existing command target identity naming convention.
  Alternative considered: serializing `Bookmark#toString()`, but that is less structured and harder to assert in YAML tests.

- Omit the returned object block when `CommandLogEntry#getResult()` is null.
  This keeps void command exports concise and follows existing non-null YAML serialization conventions.
  Alternative considered: always emit `returnedObject: null`, but that adds noise without improving replay behavior.

- Keep import tolerant by extracting command DTOs from either the new wrapper form or the legacy bare command DTO list if import support needs to read freshly exported files.
  This protects replay workflows while allowing the export file shape to evolve.
  Alternative considered: changing export only and leaving import untouched, but that could surprise users who export and immediately import the same YAML.

## Risks / Trade-offs

- [Risk] Existing consumers may expect a top-level YAML list of command DTOs only.
  Mitigation: Keep command DTO content intact and, where import is in scope, support both the new wrapper and legacy list shapes.

- [Risk] The returned object bookmark may refer to an object that no longer exists on an importing system.
  Mitigation: Treat returned object metadata as informational export data, not replay input.

- [Risk] YAML field naming could diverge from existing command target naming.
  Mitigation: Use explicit `logicalTypeName` and `id` fields and add assertions for exact YAML output.
