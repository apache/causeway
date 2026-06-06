## 1. Metamodel Facets

- [ ] 1.1 Add a synthetic action CSS class facet that exposes `btn-secondary` using the existing `CssClassFacet` contract.
- [ ] 1.2 Add a synthetic action Font Awesome facet that exposes static quick notation `hand-point-left` using the existing `FaFacet` contract.
- [ ] 1.3 Install both styling facets when `ParentedCollectionSelectorActionUtil` creates a synthetic selector action.

## 2. Tests

- [ ] 2.1 Add metamodel test coverage that a normal selector action exposes CSS class `btn-secondary`.
- [ ] 2.2 Add metamodel test coverage that a normal selector action exposes Font Awesome quick notation `hand-point-left`.
- [ ] 2.3 Add or adjust coverage to ensure mixed-in collection selector actions receive the same styling facets if needed.

## 3. Validation

- [ ] 3.1 Run the focused `ParentedCollectionSelectorActionUtilTest` Maven test set.
- [ ] 3.2 Run `openspec validate add-selector-action-styling --strict`.
