## Context

Synthetic parented collection navigate-to actions are currently created in `ObjectSpecificationAbstract.ParentedCollectionNavigationActionUtil` when command-log recording support is enabled.
Those generated actions expose a stable reserved action id, static `Navigate To` naming, layout association metadata, secondary button styling, Font Awesome icon metadata, safe action semantics, command publishing metadata, validation, and invocation behavior.
The requested reference navigation action should reuse the same viewer-facing and recording-facing contract, but the source association is a scalar reference property rather than a parented collection.

## Goals / Non-Goals

**Goals:**

- Generate one synthetic safe navigate-to action for each eligible scalar reference association when recording support is enabled.
- Give reference navigation actions the same `__causeway_navigate_to_` prefix, `Navigate To` name, `btn-outline-secondary` CSS class, `hand-point-left` icon, and `associateWith` layout semantics as collection navigation actions.
- Disable the generated action for a current object when the associated reference property evaluates to `null`.
- Return the referenced object directly when the associated reference property is non-null.
- Cover the behavior with metamodel-focused tests that do not depend on a specific viewer.

**Non-Goals:**

- Do not change existing parented collection navigation action behavior.
- Do not add filter parameters or matching logic to scalar reference navigation actions.
- Do not change ordinary reference property rendering or direct link traversal.
- Do not make synthetic navigate-to action styling configurable.
- Do not synthesize actions for value properties, collections, or references whose referenced type is not navigable as a domain object.

## Decisions

- Add a reference-specific synthetic action factory beside the parented collection factory rather than overloading collection matching code.
  Scalar reference navigation has no filter parameters and returns the property value directly, so sharing the collection matcher would add unnecessary branching and index semantics.
  Alternative considered: generalize the current collection factory into one large navigation factory.
  That would centralize constants, but it would also couple two different invocation models and make collection tests more fragile.

- Share or mirror the static layout facet contract used by collection navigation actions.
  The generated reference action should expose ordinary metamodel facets equivalent to `@ActionLayout(named="Navigate To", associateWith="<propertyId>", cssClass="btn-outline-secondary", cssClassFa="hand-point-left")` so viewers and exporters can consume existing metadata paths.
  Alternative considered: rely on viewer conventions for synthetic action ids.
  That would not satisfy layout/export consistency and would leave command recording surfaces without reliable UI metadata.

- Use the same reserved id prefix with the scalar reference property id as the suffix.
  The requested prefix is already the canonical synthetic navigate-to namespace, and using the association id keeps the action id deterministic and easy to associate with the source property.
  Alternative considered: add a reference-specific prefix such as `__causeway_navigate_to_reference_`.
  That would reduce theoretical collisions with collection ids but would diverge from the requested contract and from existing command recording naming.

- Disable rather than hide generated reference navigation actions when the current reference value is `null`.
  This preserves a stable metamodel action list while preventing invocation that cannot produce a target object.
  Alternative considered: validate null during invocation only.
  That would give later and less useful feedback to viewers and recorders.

- Install a marker facet for scalar reference navigation actions, separate from the parented collection marker facet.
  Tooling can then distinguish synthetic reference traversal from synthetic collection traversal even though both use the same action id prefix and layout presentation.
  Alternative considered: reuse the existing `ParentedCollectionNavigationFacet` for references.
  That would confuse consumers that expect the facet to carry a collection association.

## Risks / Trade-offs

- [Risk] A type could have a collection and scalar reference with the same association id, producing the same synthetic action id.
  → Mitigation: rely on existing member id clash reporting and add tests or guard logic to avoid creating duplicate synthetic actions when an action id already exists.

- [Risk] Property access during usability or invocation could trigger lazy loading or domain exceptions.
  → Mitigation: follow existing association access APIs and surface a clear disabled or invocation failure reason consistent with other metamodel facets.

- [Risk] Synthesizing additional actions could expose more commands to recording surfaces than expected.
  → Mitigation: keep the same recording-support and suppression gating used by collection navigation actions.

- [Risk] The shared `__causeway_navigate_to_` prefix may make action ids ambiguous without association metadata.
  → Mitigation: always install marker and layout association facets so consumers can identify the action kind and source association.
