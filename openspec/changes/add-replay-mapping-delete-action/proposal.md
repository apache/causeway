## Why

Operators can inspect persisted replay result mappings but cannot remove an obsolete or incorrect mapping directly from the mapping page.
Providing an explicit delete action makes replay mapping maintenance safer and more discoverable.

## What Changes

- Add a delete action for `CommandReplayResultMapping` as an applib mixin.
- Make the delete action `IDEMPOTENT_ARE_YOU_SURE` so users must confirm the destructive operation.
- Delete the selected persistent replay result mapping using the repository service.
- Register the mixin with the command log applib module.
- Update replay result mapping fallback layout XML to include the delete action.

## Capabilities

### New Capabilities

### Modified Capabilities
- `persistent-command-replay-mapping-listener`: Persisted replay result mappings can be deleted from the mapping entity UI using a confirmation-required action.

## Impact

- Affects the command log applib module by adding a `CommandReplayResultMapping` delete mixin.
- Affects `CommandReplayResultMapping.layout.fallback.xml` to place the new action.
- Requires tests for action semantics, repository-service deletion, module registration, and layout metadata.
