## Context

Command export and replay are used for regression testing by recording commands on a test instance and replaying them against both the current system version and a newer system version.
Replay result mapping already lets applications map recorded returned object bookmarks to actual replay result bookmarks, and later replay steps can use that mapping when targets or reference parameters no longer have the same identifiers.
The missing link is that safe finder actions are normally not command logged, so a recording can show a later command against an object without also recording the safe action that found that object.

The change crosses command publishing, command logging, export, and replay because the framework must capture the finder invocation as a command log entry and then allow the existing export and replay mapping flows to process it.
The behavior must be opt-in because safe actions can be frequent and many safe actions return lists, scalar values, or other results that are not useful for replay result remapping.

## Goals / Non-Goals

**Goals:**

- Provide a configuration property that enables command logging for safe actions that return exactly one domain object.
- Preserve the default behavior where safe finder actions are not logged unless explicitly configured.
- Reuse the existing `CommandDto`, `CommandLogEntry#getResult()`, command export YAML, and replay result mapping SPI concepts.
- Limit the added logging to safe action invocations whose result can be represented as a single returned object bookmark.
- Ensure replay can execute the logged safe finder command and notify the mapping SPI with recorded and actual finder result bookmarks.

**Non-Goals:**

- Do not log all safe actions by default.
- Do not add support for replay mapping list-valued finder results in this change.
- Do not change command export file format beyond carrying safe finder entries through the existing command export shape.
- Do not require applications to rewrite existing replay mapping listener implementations.

## Decisions

### Decision: Use an opt-in configuration property

The new behavior will be guarded by a boolean configuration property in the Causeway command logging or command publishing configuration area.
The default value will be `false` to avoid increasing command log volume or changing production behavior unexpectedly.

Alternative considered: enable safe single-result finder logging automatically whenever command logging is enabled.
That was rejected because many applications use safe actions heavily and may not want finder calls in the audit log outside regression recording contexts.

### Decision: Classify eligibility by action semantics and bookmarkable result shape

The framework will consider an invocation eligible only when the action has safe semantics and the completed result resolves to a single bookmarkable domain object.
List, collection, array, optional-empty, scalar, void, and other non-single-object results will not be logged by this feature.

Alternative considered: use naming conventions such as `find*` to identify finders.
That was rejected because Causeway already models safe semantics and result cardinality more reliably than action names.

### Decision: Reuse command log entries and returned object bookmarks

Eligible safe finder invocations will be represented as normal command log entries with a command DTO and a returned object bookmark.
This allows command export YAML and replay mapping notifications to use the existing returned object metadata pathway.

Alternative considered: create a separate finder recording artifact outside command log entries.
That was rejected because regression replay already consumes command log entries and the replay mapping SPI already understands recorded and actual returned object bookmarks.

### Decision: Keep replay mapping listener contracts unchanged

Replay of safe finder entries will use the existing result mapping notification when the recorded finder entry and replay result both have bookmarks.
No new listener method is required.

Alternative considered: add a finder-specific replay mapping callback.
That was rejected because the existing callback already expresses the required mapping from recorded bookmark to actual bookmark.

## Risks / Trade-offs

- Increased command log volume when enabled → Keep the property disabled by default and document that it is intended for regression recording environments.
- Ambiguous result shapes such as `Optional<T>` or framework wrappers → Define tests for accepted and rejected shapes, and only log when the framework can produce one returned object bookmark unambiguously.
- Safe action replay might have side effects in poorly behaved applications → Limit the feature to opt-in regression recording and rely on the existing semantic contract that safe actions do not mutate state.
- Duplicate logging if a safe action is explicitly command-published already → Prefer existing explicit command publishing behavior and avoid creating an additional entry for the same interaction.

## Migration Plan

No data migration is required.
Existing applications keep current behavior because the new property defaults to disabled.
Regression test applications can enable the property in their test configuration before recording command exports.
Rollback consists of disabling the property and ignoring any previously recorded safe finder entries in newly generated recordings.

## Open Questions

- Confirm the final property namespace and name during implementation to match existing Causeway configuration conventions.
- Confirm whether `Optional<T>` with a present bookmarkable object should be treated as a single-result finder or excluded from the first implementation for simplicity.
