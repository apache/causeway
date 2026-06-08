## Why

Command recording currently misses the result bookmark when an action returns a list, even when that list contains exactly one bookmarkable domain object.
That makes single-result finders that return lists unusable for command export and replay path construction, because later commands cannot prove their target or reference parameter came from an earlier recorded result.

## What Changes

- Enhance command log result capture so that a list, collection, array, or otherwise iterable action result containing exactly one bookmarkable domain object is recorded as the command result bookmark.
- Preserve existing behavior for scalar bookmarkable object results.
- Preserve existing behavior for empty results, multi-object results, non-bookmarkable results, and scalar value results by leaving the command log result unset.
- Ensure exported command YAML, replay mapping, and export known-target validation can use the recorded bookmark without changing their public formats or SPI contracts.

## Capabilities

### New Capabilities

### Modified Capabilities

- `safe-action-command-publishing`: Command logging records the single bookmarkable object contained in a one-element list result.
- `command-export-yaml-result`: Exported command YAML and downstream replay/export validation can use the command log result captured from one-element list results.

## Impact

- Affects command result bookmark extraction during command lifecycle completion, especially `CommandSubscriberForCommandLog` and the command outcome/result handling used to populate `CommandLogEntry#getResult()`.
- Affects tests for safe action command logging, command export known-target validation, and replay result mapping scenarios that depend on finder results.
- No change is expected to command export YAML shape, replay import shape, or command replay mapping SPI signatures.
