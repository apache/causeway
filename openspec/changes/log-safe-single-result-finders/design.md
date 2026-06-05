## Context

Command export and replay are used for regression testing by recording commands on a test instance and replaying them against both the current system version and a newer system version.
Replay result mapping already lets applications map recorded returned object bookmarks to actual replay result bookmarks, and later replay steps can use that mapping when targets or reference parameters no longer have the same identifiers.
The missing link is that safe finder actions are normally not command logged, so a recording can show a later command against an object without also recording the safe action that found that object.

The change crosses command publishing, command logging, export, and replay because the framework must capture the finder invocation as a command log entry and then allow the existing export and replay mapping flows to process it.
The behavior must be opt-in because safe actions can be frequent.

## Goals / Non-Goals

**Goals:**

- Provide a configuration property that enables command publishing for safe actions.
- Preserve the default behavior where safe actions are not command logged unless explicitly configured.
- Reuse the existing `CommandPublishingFacet`, `CommandDto`, `CommandLogEntry#getResult()`, command export YAML, and replay result mapping SPI concepts.
- Ensure replay can execute the logged safe action command and notify the mapping SPI when recorded and actual result bookmarks are available.

**Non-Goals:**

- Do not log safe actions by default.
- Do not inspect or constrain the safe action result shape as part of deciding whether to command publish it.
- Do not add support for replay mapping list-valued results in this change.
- Do not change command export file format beyond carrying safe action entries through the existing command export shape.
- Do not require applications to rewrite existing replay mapping listener implementations.

## Decisions

### Decision: Use an opt-in configuration property

The new behavior will be guarded by `causeway.extensions.command-log.safe-action-command-publishing`.
The default value will be `false` to avoid increasing command log volume or changing production behavior unexpectedly.

Alternative considered: enable safe action command publishing automatically whenever command logging is enabled.
That was rejected because many applications use safe actions heavily and may not want finder calls in the audit log outside regression recording contexts.

### Decision: Implement through command publishing facets

The implementation will adjust the action command publishing facet factory so safe actions receive a command publishing facet whose enabled state is controlled by the new command log property.
This keeps command logging on the normal path: action execution prepares the command for publishing when its facet is enabled, and `CommandSubscriberForCommandLog` persists the command on the standard ready/start/completed notifications.
Explicit `@Action(commandPublishing = DISABLED)` remains a local opt-out, and explicitly enabled actions are not duplicated because they already use the normal enabled facet.

Alternative considered: create command log entries during `CommandSubscriberForCommandLog#onCompleted` for safe actions that were not published earlier.
That was rejected because it still required special command-log subscriber logic instead of using the existing command publishing facet model.

Alternative considered: force safe finder commands through ready/start publishing from `MemberExecutorServiceDefault` after the action result was known.
That was rejected because it was a bolt-on around the normal logging boundary and duplicated command publishing concerns outside the facet and subscriber pipeline.

### Decision: Do not use result shape for eligibility

The framework will consider safe action semantics and configuration only when deciding whether to command publish through this feature.
Returned object bookmarks will still be captured when the existing command result handling can capture them, but null, list, scalar, or otherwise non-bookmarkable results do not prevent the command entry from being recorded.

Alternative considered: log only safe actions that return a single domain object.
That was rejected for this change because result shape may not be available at facet installation time and the command publishing decision belongs before invocation.

### Decision: Keep replay mapping listener contracts unchanged

Replay of safe action entries will use the existing result mapping notification when the recorded entry and replay result both have bookmarks.
No new listener method is required.

Alternative considered: add a finder-specific replay mapping callback.
That was rejected because the existing callback already expresses the required mapping from recorded bookmark to actual bookmark.

## Risks / Trade-offs

- Increased command log volume when enabled → Keep the property disabled by default and document that it is intended for regression recording environments.
- Safe action replay might have side effects in poorly behaved applications → Limit the feature to opt-in regression recording and rely on the existing semantic contract that safe actions do not mutate state.
- Safe actions without returned object bookmarks will be recorded but cannot contribute result mappings → Preserve them as replayable dotted-line steps and only notify mapping listeners when bookmark data is available.
- Applications may want selected safe actions excluded → Continue to respect explicit command publishing annotations such as `DISABLED`.

## Migration Plan

No data migration is required.
Existing applications keep current behavior because the new property defaults to disabled.
Regression test applications can enable the property in their test configuration before recording command exports.
Rollback consists of disabling the property and ignoring any previously recorded safe action entries in newly generated recordings.

## Open Questions

- Confirm whether the final property name `causeway.extensions.command-log.safe-action-command-publishing` matches existing Causeway configuration conventions.
