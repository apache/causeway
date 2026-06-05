## Why

Selector commands currently rely on invocation-time failure when their filter parameters identify no child row or multiple child rows.
This allows a command action to be invoked even though the selector cannot identify a unique target, so users and replay flows receive the failure too late.

## What Changes

- Add pre-invocation validation for synthetic parented collection selector actions.
- Prevent selector action invocation when the supplied parent and scalar filter parameters match no rows.
- Prevent selector action invocation when the supplied parent and scalar filter parameters match more than one row.
- Preserve successful invocation when the selector parameters identify exactly one collection row.
- Surface clear validation messages for no-match and ambiguous-match cases.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: Selector actions validate that their parameters identify exactly one child row before invocation is allowed.

## Impact

- Affects synthetic parented collection selector action validation and invocation behavior in the metamodel.
- Affects tests for `ParentedCollectionSelectorActionFactoryTest` and any related synthetic action validity facets.
- No public API or dependency changes are expected.
