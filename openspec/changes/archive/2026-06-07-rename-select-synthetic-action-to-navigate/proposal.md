## Why

The synthetic parented collection action currently uses “select” naming even though its behavior is navigation from an owner object to a child object.
Renaming the action contract to “navigate to” makes command recording terminology more generic and creates room for future recorded navigation enhancements beyond collection row selection.

## What Changes

- Rename the synthetic parented collection action display name from `Select` to `Navigate To`.
- Rename the deterministic synthetic action identifier prefix from `__causeway_select_from_` to `__causeway_navigate_to_`.
- Rename selector-focused implementation types, facet names, constants, and tests to use navigation terminology where they describe the synthetic action itself.
- Preserve the action's existing creation gating, collection association, styling, parameters, validation, invocation, command publishing, and result behavior.
- **BREAKING**: Previously recorded command DTOs that reference `__causeway_select_from_<collectionId>` will no longer match the generated synthetic action id unless they are migrated or remapped to `__causeway_navigate_to_<collectionId>`.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `parented-collection-selector-actions`: The synthetic action's user-facing name and deterministic command action id change from select-oriented terminology to navigate-oriented terminology.

## Impact

- Affects synthetic parented collection action creation in `core/metamodel`, especially `ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil` and related synthetic action facets.
- Affects metamodel tests for synthetic parented collection actions and any tests asserting the reserved action id prefix or display name.
- Affects command recording, export, and replay payloads that identify these synthetic actions by action id.
- Does not add dependencies or change ordinary collection browsing behavior.
