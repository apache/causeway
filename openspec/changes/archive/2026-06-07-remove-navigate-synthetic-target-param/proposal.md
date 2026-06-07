## Why

Synthetic parented collection navigation actions currently expose the current target as a first, mandatory, disabled parameter.
For command recording support, that target is already implicit in the recorded action target, so serializing it again creates unnecessary noise and a less natural replay contract.

## What Changes

- Remove the synthetic navigation action's explicit target parameter from the generated action parameter model.
- Keep the action target as the source used to access the associated parented collection during validation and invocation.
- Keep child filter parameters unchanged, including column-based selection, ordering, and matching semantics.
- **BREAKING**: Existing recordings or tests that expect the synthetic navigation action to include the disabled parent parameter will need to omit that parameter.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: The synthetic navigation action parameter contract no longer includes the current target as a mandatory disabled first parameter.

## Impact

- Affects synthetic parented collection action construction in `core/metamodel`.
- Affects command recording and replay payloads for synthetic navigate-to actions because the parameter list becomes shorter.
- Affects focused metamodel and command recording tests that assert synthetic action parameters.
