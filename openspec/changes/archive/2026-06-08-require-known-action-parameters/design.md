## Context

Command export now validates that each selected action target is reachable from a menu service root or from an earlier recorded result within the export manager baseline.
Replay also needs reference action parameters to be reachable, because a replayed action can fail when its target is known but one of its domain-object parameters has no recorded path.
The current command DTO already carries reference parameters as bookmark-like OID metadata, so parameter validation can use the same baseline-bounded known-bookmark model as target validation.

## Goals / Non-Goals

**Goals:**

- Validate selected action reference parameters before export YAML is emitted.
- Use the same export ordering and baseline boundary as known action target validation.
- Treat menu domain service bookmarks and earlier command results as known parameter bookmarks.
- Report the failing command, parameter name, and unknown parameter bookmark.
- Keep command recording permissive and validate only at export time.

**Non-Goals:**

- Do not add a new YAML field or persistent dotted-path metadata.
- Do not validate scalar, value, or non-reference parameters as export path participants.
- Do not change replay result remapping behavior.
- Do not block command recording when a reference parameter is not yet known.

## Decisions

### Reuse the export known-target validator

The existing export known-target validator should be extended to inspect action DTO reference parameters in addition to action targets.
This keeps target and parameter validation in one pass over the sorted selected command log entries.

Alternative considered: add a separate parameter validator.
That would duplicate ordering, baseline, root, and result-bookmark logic and could produce inconsistent decisions.

### Use action DTO reference parameter metadata

The validator should inspect `ActionDto#getParameters()` and include only parameters whose value type is `REFERENCE` and whose reference OID is present.
Those OIDs can be converted to `Bookmark` values using the same bookmark conversion already used by replay participant display and remapping.

Alternative considered: use local object resolution for parameter values.
That would prove only that the object exists locally, not that replay can reach it from the exported sequence.

### Results establish both target and parameter knownness

A non-null command result bookmark should make that bookmark known for any later selected action target or reference parameter.
The validator should add results only after validating the current command, preserving export-order semantics.

Alternative considered: pre-scan all selected results before validation.
That would incorrectly allow later results to validate earlier parameter references.

### Message includes parameter identity

For parameter failures, the validation message should identify the command, the unknown bookmark, and the parameter name.
This gives users enough information to include the missing finder or navigation command earlier in the exportable sequence.

Alternative considered: reuse the existing target-only message.
That would be ambiguous when multiple parameters are present.

## Risks / Trade-offs

- [Risk] Some action DTOs might omit parameter names for reference parameters.
  → Mitigate by reporting a fallback such as the parameter index or an unnamed parameter label.
- [Risk] Bulk actions or multiple targets could combine target and parameter failures.
  → Mitigate by failing fast with the first unknown participant while preserving deterministic export order.
- [Risk] Validation becomes stricter and may reject previously exportable YAML selections.
  → Mitigate by explaining that an earlier navigation or finder action returning the parameter object must be included.
- [Risk] Parameter validation could add overhead for large selections.
  → Mitigate by scanning each selected command once and tracking known bookmarks in a set.

## Migration Plan

No schema migration is expected.
Existing command log entries remain unchanged.
After deployment, export will reject selections whose reference parameters are not reachable from the baseline-bounded command sequence.
Rollback by reverting the validator extension if the stricter export behavior causes unexpected operational issues.

## Open Questions

- Should validation continue to fail fast on the first unknown participant, or should it report all unknown targets and parameters in a single message?
- Should non-action command results continue to establish known bookmarks for later action parameters, matching the current target behavior?
