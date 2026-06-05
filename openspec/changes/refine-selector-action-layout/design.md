## Context

Synthetic parented collection selector actions are built without Java annotations, so their layout metadata must be installed as facets during synthetic action construction.
Handcrafted actions use `@ActionLayout(associateWith=...)` to produce a `LayoutGroupFacet` and `@ActionLayout(named=...)` to produce a member name facet.
The synthetic selector action should expose the same effective metamodel contracts without requiring an actual annotated Java method.

## Goals / Non-Goals

**Goals:**

- Associate each synthetic selector action with the parented collection that it selects from.
- Give each synthetic selector action the display name `Select`.
- Use the existing facet contracts consumed by viewers, layout services, and metamodel tooling.
- Keep the existing synthetic action id and marker metadata unchanged.

**Non-Goals:**

- Do not change selector action invocation, parameters, safe semantics, or config-gated creation.
- Do not add a domain annotation, mixin, or generated Java method for each selector action.
- Do not change collection layout XML semantics.

## Decisions

### Install layout association metadata as facets

The implementation should install a `LayoutGroupFacet` on each synthetic selector action whose group id is the parented collection id.
This mirrors the effective metamodel produced by `@ActionLayout(associateWith="<collectionId>")`.
The group name can be inferred from the collection id or copied from collection metadata, because consumers primarily use the group id to place the action with its collection peer.

Alternative considered: synthesize an `ActionLayout` annotation instance for the virtual method.
That would reuse `ActionLayoutFacetFactory`, but it would make the virtual method facade responsible for annotation-proxy behavior that is not otherwise needed.
Direct facet installation is smaller and matches the existing synthetic action factory approach.

### Install the display name through the member-name facet contract

The implementation should install or replace the synthetic action member-name facet with the static value `Select`.
This mirrors the effective metamodel produced by `@ActionLayout(named="Select")`.
The action id remains deterministic and unchanged, so command DTO identity and metamodel lookup continue to use the reserved selector id rather than the display name.

Alternative considered: rename the synthetic action id to `select`.
That would conflate identity with presentation and could break existing lookup and command DTO behavior.

## Risks / Trade-offs

- Installing layout metadata directly could diverge from future `@ActionLayout` processing changes.
  Mitigation: use the same public facet contracts that layout consumers already read.
- A fixed `Select` name could be less descriptive than `Select <Child>` in some UIs.
  Mitigation: the collection association provides context, so the shorter name is appropriate when rendered near the collection.
