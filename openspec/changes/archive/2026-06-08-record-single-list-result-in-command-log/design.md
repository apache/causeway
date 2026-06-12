## Context

Command result capture currently stores a `CommandLogEntry` result when command execution produces a scalar bookmarkable domain object.
The capture path is upstream of command export and replay, so export YAML, known-target validation, and replay result mapping all depend on this single stored bookmark.
List-returning finder actions are common in UI workflows, and the viewer automatically opens the object when the returned list has exactly one instance.
Command recording does not currently mirror that viewer behavior, so a finder that returns a singleton list creates no recorded result and cannot establish the dotted path needed by later replay commands.

## Goals / Non-Goals

**Goals:**

- Capture a command result bookmark when the action result is a one-element container whose element is a bookmarkable domain object.
- Keep scalar bookmarkable object result capture unchanged.
- Keep ambiguous multi-object results and non-bookmarkable results unrecorded.
- Reuse existing `CommandLogEntry#getResult()`, command export YAML, export validation, and replay mapping behavior once the bookmark is captured.

**Non-Goals:**

- Do not add support for recording multiple result bookmarks from a multi-object result.
- Do not change the exported YAML `result` field shape.
- Do not change replay mapping SPI signatures.
- Do not change viewer navigation behavior for list results.

## Decisions

### Decision: Normalize singleton containers before bookmark extraction

Result capture will treat a scalar managed object and a one-element result container as two ways to identify one candidate result object.
When the result is a packed or iterable managed object with exactly one element, command result extraction will inspect that element and apply the same persistable-object and bookmark checks used for scalar results.
When the result has zero elements, more than one element, or a single non-bookmarkable value, the command result remains absent.

Alternative considered: store the first bookmarkable object from any list.
That was rejected because multi-object results are ambiguous and would produce misleading replay paths.

Alternative considered: add a separate collection-result metadata model.
That was rejected because command replay and export currently require a single dotted-path participant, and the requested behavior only needs singleton-list parity with the viewer.

### Decision: Keep the existing command log result field as the integration point

The implementation will populate the existing command result bookmark before `CommandSubscriberForCommandLog` synchronizes the command to `CommandLogEntry`.
Downstream export, replay import, known-target validation, and replay result mapping will continue to read `CommandLogEntry#getResult()` exactly as they do today.

Alternative considered: teach export validation to inspect historical action return payloads independently of `CommandLogEntry#getResult()`.
That was rejected because export validation should rely on recorded command metadata rather than reconstructing results after the fact.

### Decision: Do not make singleton-list capture specific to safe actions

The extraction rule belongs to command result capture rather than to safe action command publishing.
That allows the same unambiguous result behavior for any command-published action that returns a singleton container while preserving the safe-action opt-in boundary for whether safe finder invocations are logged at all.

Alternative considered: handle only safe finder actions.
That was rejected because the result-shape rule is independent from action semantics once an invocation is already command-published.

## Risks / Trade-offs

- [Risk] Container handling could accidentally initialize or iterate large lazy collections.
  Mitigation: detect only enough elements to distinguish zero, one, and many, and stop after the second element.
- [Risk] Some singleton containers may contain scalar values or view models without persistent bookmarks.
  Mitigation: reuse the existing bookmarkability checks and leave the result unset when no bookmark can be obtained.
- [Risk] Existing tests may assume list results never produce command result metadata.
  Mitigation: update tests to distinguish singleton bookmarkable lists from empty, multi-object, and non-bookmarkable lists.

## Migration Plan

No data migration is required.
Previously recorded commands without result bookmarks remain unchanged.
New recordings will capture singleton-list result bookmarks when command logging is enabled for the invocation.
Rollback consists of reverting the extraction rule; exported YAML and replay data formats remain unchanged.

## Open Questions

- Confirm the exact managed-object APIs that best expose singleton elements without forcing full collection materialization.
