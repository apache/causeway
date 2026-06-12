## Why

Command recording can already model navigation from a parent object to a selected element of a parented collection, but it cannot model the simpler navigation step from an object to one of its scalar referenced objects through a generated action.
Adding a synthetic reference navigation action gives recording and replay a stable action id and metamodel contract for traversing ordinary object references while preserving direct UI navigation behavior.

## What Changes

- Synthesize a safe `Navigate To` action for each eligible scalar reference association when command-log recording support is enabled.
- Use the deterministic reserved action id prefix `__causeway_navigate_to_` followed by the reference association id.
- Associate each synthetic action with its reference property through layout metadata equivalent to `@ActionLayout(associateWith=...)`.
- Expose action layout metadata equivalent to `@ActionLayout(named="Navigate To", cssClass="btn-outline-secondary", cssClassFa="hand-point-left")`.
- Disable the synthetic action for a current object when the associated reference value is `null`.
- Return the referenced object when the associated reference value is non-null.
- Preserve existing scalar reference rendering and direct link traversal behavior.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: Extend the synthetic navigate-to action contract from parented collections to scalar reference associations with equivalent naming, styling, association, enablement, and invocation semantics.

## Impact

- Affects synthetic action creation in `core/metamodel`, likely alongside `ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil` and related navigation synthetic action facets.
- Affects metamodel tests that cover generated navigate-to actions, marker metadata, layout association metadata, styling facets, usability, and invocation behavior.
- Affects command recording and replay surfaces that enumerate actions for reference navigation when recording support is enabled.
- Does not add dependencies and does not change ordinary reference rendering or direct link navigation when the synthetic action is unavailable.
