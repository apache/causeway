## Why

Synthetic parented collection selector action ids currently use the reserved prefix `__causeway_select_`.
Renaming the prefix to `__causeway_select_from_` makes the generated ids read more clearly as actions that select from a specific collection while retaining deterministic framework-owned ids.

## What Changes

- Change the reserved synthetic parented collection selector action id prefix from `__causeway_select_` to `__causeway_select_from_`.
- Preserve deterministic action id generation by continuing to append the associated collection id after the prefix.
- Update tests and OpenSpec requirements that describe the reserved selector action id format.
- **BREAKING**: Existing recordings, integrations, or assertions that reference the old synthetic action id prefix will need to use the new prefix.

## Capabilities

### New Capabilities

### Modified Capabilities
- `parented-collection-selector-actions`: the deterministic selector action identifier requirement changes to use the reserved `__causeway_select_from_` prefix.

## Impact

- Affected code is expected around synthetic selector action creation in `ObjectSpecificationAbstract`.
- Affected tests are expected around parented collection selector action id generation and metadata assertions.
- Exported command or replay data that stores synthetic selector action ids may need corresponding updates because the reserved id string changes.
