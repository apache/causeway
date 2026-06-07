## Context

Parented collection selector actions are synthesized by `ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil` when command-log recording support is enabled.
The current runtime eligibility rule permits entity-owned collections with entity, view model, or abstract element types.
It only permits view-model-owned collections in unit tests, and then only when the element type is also a view model.
This prevents selectors on useful runtime view models such as the PetClinic home page, whose collections return `PetOwner` and `Visit` entities.

## Goals / Non-Goals

**Goals:**

- Allow selector action synthesis for view-model-owned collections at runtime.
- Use the same element-type eligibility for entity and view-model owners.
- Preserve the existing command-log recording support gate.
- Preserve existing selector styling, naming, parameters, validation, invocation, and command publishing behavior.
- Add regression coverage for view-model owners with entity elements.

**Non-Goals:**

- Change selector matching semantics.
- Change which child properties become filter parameters.
- Create selectors when command-log recording support is disabled.
- Add application-specific logic for PetClinic or home page view models.
- Add viewer rendering changes.

## Decisions

- Replace the owner-specific eligibility rule with a rule based on supported owner and element domain types.
  Entity and view-model owners should both be eligible because the selector invocation already reads the collection from the supplied parent object and does not depend on persistence identity of the parent.
- Keep element eligibility constrained to entity, view model, or abstract domain types.
  This preserves the existing intent that selector actions navigate to a selected domain object rather than arbitrary scalar collection elements.
- Remove the unit-test-only view-model exception instead of adding another special case.
  The runtime behavior should be explicit and testable, and unit tests should follow the same eligibility rule as production.
- Validate with metamodel tests that a view-model owner with entity collection elements receives a selector action.
  Existing tests already cover entity owners and view-model element behavior.

## Risks / Trade-offs

- [Risk] More synthetic actions may appear in applications that have command-log recording support enabled and expose view-model collections.
  → Mitigation: this only applies after the existing opt-in property is enabled, and generated actions use the reserved synthetic marker and deterministic id.
- [Risk] Some view-model collections may be expensive to evaluate during selector invocation.
  → Mitigation: selector invocation already evaluates the collection only when validating or invoking the action, matching existing entity-owned behavior.
- [Risk] View-model-owned collections may not be semantically parented in every application.
  → Mitigation: the existing feature already synthesizes only from metamodel collection associations and remains gated by the command-log recording support opt-in.
