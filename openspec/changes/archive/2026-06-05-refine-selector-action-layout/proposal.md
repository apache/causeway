## Why

Synthetic parented collection selector actions are currently distinguishable and invokable, but their layout metadata does not yet mirror a handcrafted action associated with the collection.
Recording and metamodel tooling should be able to treat the synthetic action like an action declared with `@ActionLayout(associateWith=..., named="Select")`.

## What Changes

- Associate each synthetic selector action with the parented collection that it selects from using the same layout facet model as `@ActionLayout(associateWith=...)`.
- Name each synthetic selector action `Select` using the same member-name facet model as `@ActionLayout(named="Select")`.
- Preserve the existing deterministic action id, marker facet, safe semantics, config-gated creation, parameter model, and invocation behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `parented-collection-selector-actions`: Synthetic selector actions gain collection association layout metadata and a fixed display name of `Select`.

## Impact

- Affects synthetic action facet installation in `core/metamodel`.
- Adds or updates metamodel tests for layout group association and display name metadata.
- Does not change command logging, export, replay, action ids, or selector invocation semantics.
