## 1. Synthetic Action Parameter Model

- [x] 1.1 Update synthetic navigation action parameter type and name creation to omit the parent target parameter.
- [x] 1.2 Remove installation of parent-parameter defaults, choices, mandatory, and disabled facets from generated navigation actions.
- [x] 1.3 Remove unused parent-parameter facet classes and imports if no remaining code references them.

## 2. Validation and Invocation Semantics

- [x] 2.1 Double-check the validation and invocation APIs expose the current action target reliably enough to use as the parent collection owner.
- [x] 2.2 Refactor navigation matching to use the action target as the parent collection owner.
- [x] 2.3 Re-index child filter matching so filter arguments start at parameter index zero.
- [x] 2.4 Preserve no-match, ambiguous-match, partial-string, reference equality, and command result behavior after removing the parent argument.

## 3. Tests and Verification

- [x] 3.1 Update metamodel tests to assert that synthetic navigate-to actions do not expose a parent target parameter.
- [x] 3.2 Update validation, invocation, and command publishing tests to pass only child filter arguments.
- [x] 3.3 Run focused `ParentedCollectionNavigationActionUtilTest` coverage for the synthetic navigation action changes.
- [x] 3.4 Run OpenSpec validation for `remove-navigate-synthetic-target-param`.
