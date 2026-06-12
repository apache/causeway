## Why

Synthetic parented collection selector actions currently use supplied scalar filter parameter values to identify one matching child object.
For string filters, exact matching is too strict for command recording and replay scenarios where a partial human-readable value can still identify the intended child clearly.

## What Changes

- Change selector action matching so provided string filter parameter values use `contains` matching rather than exact equality.
- Keep non-string scalar filter parameters using their existing exact equality matching behavior.
- Preserve the rule that selector validation and invocation succeed only when the complete set of supplied filters identifies exactly one matching child object.
- Preserve no-match and ambiguous-match validation behavior when partial string matching identifies zero or multiple child objects.

## Capabilities

### New Capabilities

### Modified Capabilities
- `parented-collection-selector-actions`: string-valued selector filter parameters match collection element string properties by containment instead of exact equality when a value is provided.

## Impact

- Affected code is expected around selector action validation and invocation filtering in the parented collection selector action facets.
- Affected tests are expected around selector action validation and invocation for string child property filters.
- Existing command recordings that used full string values should continue to match because a full value contains itself.
- Existing recordings that relied on exact-string rejection may now become valid if the partial value uniquely identifies one child.
