## 1. Synthetic Reference Navigation Factory

- [ ] 1.1 Add a reference-navigation synthetic action factory alongside `ParentedCollectionNavigationActionUtil` and invoke it during action creation when recording support is enabled.
- [ ] 1.2 Apply the same eligibility gates as collection navigation for owner type, recording support, command recording suppression, and navigable referenced type.
- [ ] 1.3 Generate deterministic action ids using `__causeway_navigate_to_` plus the scalar reference association id while avoiding duplicate synthetic action ids.
- [ ] 1.4 Create parameterless safe synthetic actions that return the referenced type and install command publishing metadata.

## 2. Facets and Runtime Behavior

- [ ] 2.1 Install marker metadata for scalar reference navigation actions so tooling can distinguish them from developer-authored actions and collection navigation actions.
- [ ] 2.2 Install static layout metadata equivalent to `named="Navigate To"`, `associateWith="<referenceId>"`, `cssClass="btn-outline-secondary"`, and `cssClassFa="hand-point-left"`.
- [ ] 2.3 Add a disabled facet that disables the action with a clear reason when the current reference value is `null`.
- [ ] 2.4 Add an invocation facet that reads the reference from the action target, returns the referenced object when non-null, and fails clearly for direct null-reference invocation.

## 3. Tests

- [ ] 3.1 Add metamodel tests asserting scalar reference navigation actions are absent by default and present only when recording support is enabled.
- [ ] 3.2 Add metamodel tests for entity-owned and view-model-owned eligible references, suppression-marker exclusion, value-property exclusion, deterministic id, marker metadata, and zero parameters.
- [ ] 3.3 Add metamodel tests for layout association, display name, CSS class, and Font Awesome metadata.
- [ ] 3.4 Add usability and invocation tests for null and non-null reference values.
- [ ] 3.5 Add or update tests proving existing parented collection navigation behavior remains unchanged.

## 4. Validation

- [ ] 4.1 Run the focused metamodel synthetic navigation test suite.
- [ ] 4.2 Run `openspec status --change "add-navigate-to-reference-synthetic-action"` and confirm the change is apply-ready.
