## 1. Layout Facets

- [x] 1.1 Identify the existing facet contract consumed for `@ActionLayout(associateWith=...)` and add a synthetic selector facet implementation if needed.
- [x] 1.2 Install collection association layout metadata on synthetic selector actions using the parented collection id.
- [x] 1.3 Replace the synthetic selector action display name with `Select` using the member-name facet contract.

## 2. Tests and Validation

- [x] 2.1 Add or update metamodel tests proving selector actions are associated with their parented collection id.
- [x] 2.2 Add or update metamodel tests proving selector actions have display name `Select` while retaining the deterministic action id.
- [x] 2.3 Run focused metamodel tests for the synthetic selector action factory.
- [x] 2.4 Run `openspec validate refine-selector-action-layout --strict` and fix any proposal/spec/task validation issues.
