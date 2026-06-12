## Context

Safe action command publishing records finder/navigation actions so replay/export can use their single returned object as a dotted-path participant.
A safe action with no result bookmark is different: replaying it cannot produce a mapping for later commands, and exporting it does not help establish a target.
A safe action that returns a list with more than one element has no unique returned bookmark, so replay cannot safely choose which result should participate in the dotted path.

## Goals / Non-Goals

**Goals:**

- Keep persisting command log entries for safe action invocations when recording support is enabled.
- Suppress `ReplayableCommand` view models for safe action command log entries that do not have a single returned bookmark.
- Keep safe actions with one bookmarkable result replayable/exportable.
- Keep state-changing actions governed by existing replayable command behavior.

**Non-Goals:**

- Stop command log persistence for safe actions.
- Change command DTO creation or stored command log entry result metadata.
- Add replay semantics for multi-result safe actions.
- Change command replay import YAML shape.

## Decisions

### Centralize replayable eligibility

Add a small eligibility predicate or service used anywhere command log entries are wrapped as `ReplayableCommand` instances.
The predicate should treat non-safe commands as eligible and safe commands as eligible only when they have a single stored result bookmark.
This avoids each collection independently re-implementing the safe-action suppression rule.

Alternative considered: filter only in the export manager commands collection.
This was rejected because command replay and navigation flows can also construct `ReplayableCommand` wrappers from command log entries.

### Preserve command log entries

Do not change command publishing or command-log persistence for safe action invocations.
The filtering happens after persistence, at the point where command log entries are exposed for replay/export.

Alternative considered: avoid persisting void and multi-result safe actions.
This was rejected because safe action recording remains useful as an audit trail and the user explicitly requested that command log entries still be persisted.

### Classify by command semantics and stored result

Use command metadata to identify safe action command log entries, and use the stored result bookmark to determine whether they have exactly one replay-useful result.
A missing stored result bookmark means the safe action is not replayable, whether the original action returned void, a scalar, or a list with zero or multiple elements.

Alternative considered: inspect the raw action return value later.
This was rejected because replay/export list construction works from persisted command log entries, not live invocation results.

## Risks / Trade-offs

- Missing result bookmark for a single-object safe action → The entry is suppressed; tests should verify result capture remains intact for single bookmarkable results.
- Existing UI expectations change → Users will see fewer replay/export rows, but command log entries remain available through command-log views.
- Safe action metadata detection is incomplete → Centralize the predicate and cover safe action and state-changing action cases in tests.
