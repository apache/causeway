## 1. Synthetic Action Naming

- [ ] 1.1 Rename the synthetic action id prefix from `__causeway_select_from_` to `__causeway_navigate_to_` in the parented collection synthetic action factory.
- [ ] 1.2 Rename the synthetic action display name from `Select` to `Navigate To` in the member-name facet installation.
- [ ] 1.3 Rename implementation symbols, constants, facet classes, and helper names from selector terminology to navigation terminology where they describe the synthetic action contract.
- [ ] 1.4 Preserve existing creation gating, collection association, styling, command publishing, parameter generation, validation, invocation, and empty-collection disabling behavior.

## 2. Command Recording Compatibility Notes

- [ ] 2.1 Identify tests or fixtures that assert command DTO action ids for synthetic collection navigation and update them to the new `__causeway_navigate_to_` prefix.
- [ ] 2.2 Add or update migration documentation or release-note text if the project has an appropriate location for command DTO action id breaking changes.

## 3. Test Coverage

- [ ] 3.1 Update metamodel tests to assert that synthetic parented collection actions use the `__causeway_navigate_to_` reserved prefix.
- [ ] 3.2 Update metamodel tests to assert that synthetic parented collection actions expose display name `Navigate To`.
- [ ] 3.3 Update any renamed class or method test references so focused tests still cover generated actions for normal, mixed-in, entity-owned, and view-model-owned parented collections.
- [ ] 3.4 Run the focused `ParentedCollectionSelectorActionUtilTest` Maven test set or its renamed equivalent.
- [ ] 3.5 Run `openspec validate rename-select-synthetic-action-to-navigate --strict` and fix any proposal, spec, design, or task validation issues.
