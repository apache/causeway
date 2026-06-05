## 1. Parent Parameter Defaults

- [x] 1.1 Identify the existing parameter default facet contract used by action parameter defaults.
- [x] 1.2 Add a synthetic default facet for selector parent parameters that returns the current action target.
- [x] 1.3 Install the parent default facet on parameter zero of synthetic selector actions.

## 2. Parent Parameter Disabled State

- [x] 2.1 Identify the existing disabled facet contract used by disabled action parameters.
- [x] 2.2 Add a synthetic disabled facet for selector parent parameters with a clear fixed-parent reason.
- [x] 2.3 Install the disabled facet on parameter zero only, leaving scalar filter parameters editable.

## 3. Tests and Validation

- [x] 3.1 Add or update metamodel tests proving the parent parameter has a default.
- [x] 3.2 Add or update metamodel tests proving the parent parameter default is the current action target.
- [x] 3.3 Add or update metamodel tests proving the parent parameter is disabled and scalar filter parameters are not disabled by this feature.
- [x] 3.4 Run focused metamodel tests for the synthetic selector action factory.
- [x] 3.5 Run `openspec validate refine-selector-parent-parameter --strict` and fix any proposal/spec/task validation issues.
