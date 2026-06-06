## Context

Command export YAML wraps the replayable command DTO together with metadata derived from `CommandLogEntry#getResult()`.
The current YAML field for that metadata is `returnedObject`, while the command log entry property is `result`.
The requested change intentionally removes the old field name rather than preserving backward compatibility.

## Goals / Non-Goals

**Goals:**

- Rename the exported YAML field from `returnedObject` to `result`.
- Rename the corresponding DTO property and accessor methods where appropriate.
- Update replay import to read only the `result` field for command export DTOs.
- Update tests, fixtures, and documentation/specs to use the new field name.

**Non-Goals:**

- Do not support deserializing the old `returnedObject` YAML field.
- Do not change the nested bookmark metadata shape; it still contains `logicalTypeName` and `id`.
- Do not change `CommandLogEntry#getResult()` semantics or replay result mapping behavior.
- Do not change legacy multi-document `CommandDto` import support.

## Decisions

- Rename the DTO property to `result` instead of using a serialization alias on a `returnedObject` Java property.
  This aligns the Java model, YAML shape, and `CommandLogEntry` terminology.
  The alternative was adding a Jackson property annotation while leaving Java code unchanged, but that would preserve inconsistent internal naming.
- Remove old-field compatibility.
  This satisfies the explicit requirement that backwards compatibility is unnecessary and avoids dual-property ambiguity during import.
  The alternative was to accept both names, but that would complicate validation and make exported examples less definitive.
- Keep the nested result object structure unchanged.
  Only the field name changes, so replay result bookmark import/export behavior remains focused and testable.
  The alternative was to redesign the bookmark payload, but that is outside the requested rename.

## Risks / Trade-offs

- [Risk] Existing exported YAML files containing `returnedObject` will no longer import as result-bearing command export DTOs.
  Mitigation: document this as a breaking change and update tests/fixtures to use `result`.
- [Risk] Some code or tests may still use old Java accessor names.
  Mitigation: search for `returnedObject` across source, tests, fixtures, docs, and OpenSpec artifacts during implementation.
- [Risk] Replay import fallback to legacy `CommandDto` YAML could mask a malformed command export DTO.
  Mitigation: keep existing replay import behavior unchanged except for the renamed field, and validate with focused export/import tests.
