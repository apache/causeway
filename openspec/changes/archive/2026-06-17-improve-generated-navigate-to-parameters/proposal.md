## Why

Synthetic parented collection navigate-to actions currently expose mandatory boolean filter parameters for checkbox columns, which forces a predicate even when the caller did not intend to filter by that boolean.
The generated parameter order should also mirror the parented collection column order exactly so prompts are predictable and match the rendered collection.

## What Changes

- Treat mandatory boolean child columns as optional tri-valued selector parameters for generated parented collection navigate-to actions.
- Apply a boolean predicate only when the caller explicitly supplies `true` or `false`.
- Leave the boolean predicate unapplied when the tri-valued parameter is not selected.
- Ensure generated selector action parameters follow the same order as the associated parented collection columns.
- Add regression coverage for mandatory checkbox columns and mixed column ordering.

## Capabilities

### New Capabilities

### Modified Capabilities
- `parented-collection-selector-actions`: Synthetic selector action filter parameter semantics and ordering change for boolean collection columns and parented collection column order.

## Impact

- Affects synthetic parented collection selector action metamodel generation in command-log recording support.
- Affects action parameter optionality, validation, and invocation filtering for mandatory boolean child properties.
- Affects tests/specs around parameter ordering for generated navigate-to-one actions.
- No new dependencies or public configuration properties are expected.
