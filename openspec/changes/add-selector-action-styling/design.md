## Context

Parented collection selector actions are synthesized in `ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil` when command-log recording support is enabled.
The current facet installation adds the static display name, collection layout association, selector marker, safe semantics, validation, invocation, and command publishing metadata.
Viewer and layout export code already understand action `CssClassFacet` and static Font Awesome `FaFacet` metadata installed by ordinary `@ActionLayout` processing.
This change only needs to make the synthetic selector action install the same style facets that a developer-authored action would expose.

## Goals / Non-Goals

**Goals:**

- Install a static action CSS class of `btn-secondary` on every synthetic parented collection selector action.
- Install a static Font Awesome icon of `hand-point-left` on every synthetic parented collection selector action.
- Keep the existing selector action id, name, association, semantics, command publishing, parameters, validation, and invocation behavior unchanged.
- Cover the new facets with metamodel tests so viewer-independent behavior is specified.

**Non-Goals:**

- Make selector action styling configurable in this change.
- Change the generated action name or collection association metadata.
- Change selector eligibility, parameter discovery, matching, validation, invocation, or command publishing behavior.
- Add viewer-specific rendering logic.

## Decisions

- Add dedicated synthetic facet classes rather than trying to reuse annotation facet factories.
  Annotation facet classes such as `CssClassFacetForActionLayoutAnnotation` and `FaFacetForActionLayoutAnnotation` are designed around an `ActionLayout` annotation and are not directly reusable for synthetic actions.
  Dedicated classes can extend the same common facet bases, `CssClassFacetSimple` and `FaStaticFacetAbstract`, to expose equivalent metamodel semantics without fabricating an annotation.
- Install the style facets in `installActionFacets(...)` alongside the existing name and layout group facets.
  This keeps all static synthetic action layout metadata in one place and ensures mixed-in collection selectors receive the same styling path as ordinary collection selectors.
- Use literal constants for `btn-secondary` and `hand-point-left` in the synthetic facet classes or action synthesis utility.
  The values are part of the default generated selector action contract, and there is no proposal requirement for runtime configuration.
- Validate through `ParentedCollectionSelectorActionUtilTest` by asserting the selector action exposes `CssClassFacet` and static `FaFacet` values.
  This verifies metamodel behavior without coupling the change to a particular viewer implementation.

## Risks / Trade-offs

- [Risk] Viewers may interpret `btn-secondary` differently or ignore it entirely.
  → Mitigation: expose standard layout facets only, matching existing `@ActionLayout` metadata, and leave rendering decisions to viewers.
- [Risk] Font Awesome quick notation may require the same value shape as annotations.
  → Mitigation: use the annotation-equivalent quick notation `hand-point-left` and test the resulting static facet quick notation.
- [Risk] Future requests may want the styling configurable per collection.
  → Mitigation: keep this change limited to a small default facet installation that can later be overridden or parameterized if a new requirement appears.
