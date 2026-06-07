## Why

Synthetic parented collection selector actions are useful only when the associated collection has at least one row to select from.
When the collection is empty, offering an enabled `Select` action creates a poor user experience because invocation can only fail with a no-match validation result.

## What Changes

- Disable synthetic parented collection selector actions when their associated collection is empty for the current parent object.
- Preserve selector action synthesis, visibility, styling, parameters, validation, invocation, and command publishing behavior for non-empty collections.
- Keep the disabled behavior dynamic so an action becomes enabled again when the associated collection has rows.
- No breaking changes.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: selector action usability now reflects whether the associated collection has any selectable rows for the current parent object.

## Impact

- Affects synthetic selector action usability evaluation in the metamodel.
- Affects viewers and command recording surfaces that already respect action disabled facets.
- Adds or updates metamodel tests for empty and non-empty selector collection usability.
- Does not change selector creation, matching, validation messages, invocation behavior, public APIs, or configuration properties.
