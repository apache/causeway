## Why

The synthetic selector action’s first parameter represents the collection owner and should be fixed to the current action target.
If a user or recorder can change that parameter, the action no longer behaves like contextual navigation from the displayed parent collection.

## What Changes

- Ensure the first synthetic selector action parameter defaults to the current action target.
- Disable the first synthetic selector action parameter using the same facet contract as a handcrafted `disableParam...` support method.
- Preserve the existing mandatory parent parameter type, selector action id, layout association, display name, scalar filter parameters, and invocation behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `parented-collection-selector-actions`: The mandatory parent parameter gains a target default and is disabled so it cannot be changed by the user.

## Impact

- Affects synthetic selector action parameter facet installation in `core/metamodel`.
- Adds or updates metamodel tests for parent parameter defaults and disabled usability.
- Does not change command logging, export, replay, action ids, layout metadata, or selector filtering semantics.
