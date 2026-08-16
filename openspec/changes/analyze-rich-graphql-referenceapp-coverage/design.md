## Context

A preliminary source audit used `apache/causeway-app-referenceapp` commit `04cff95802de44d0ff5ac0802857f3bb2ffe8c3a` and found 1,271 Java files covering domain services, entities, view models, mixins, bulk actions, custom values, standard datatypes, layouts, menus, validation, choices, defaults, autocomplete, vetoes, and authorization-sensitive behavior.
The current rich GraphQL implementation already supports many of these semantics, including domain services, bookmark-addressable objects, mixins, dynamic hidden and disabled state, property interaction, action parameter negotiation, collection-valued parameters, and safe or mutating invocation placement.

The preliminary audit also identified plausible gaps: non-reversible generic string fallback for some value types, sparse structured presentation metadata, unbounded collection reads, incomplete resource-value interaction, and no explicit menu or home-page application-entry contract.
Those findings must be verified against a running generated schema and representative operations before implementation scope is fixed.

## Goals / Non-Goals

**Goals:**

- Produce a reproducible, evidence-backed coverage matrix.
- Separate GraphQL protocol gaps from component gaps and Wicket-only behavior.
- Rank gaps by correctness, security, scalability, composition value, and compatibility risk.
- Define bounded implementation proposals with explicit dependencies and exclusions.
- Identify which capabilities are prerequisites for `<causeway-object>`, menu-bar components, and the later HTMX viewer.

**Non-Goals:**

- Modifying production GraphQL schema generation or execution.
- Adding scalar marshallers, metadata fields, collection arguments, or resource transfer.
- Implementing any web component.
- Claiming complete Wicket parity.
- Adding the external reference application as a build dependency.

## Decisions

### Use both source evidence and executable GraphQL evidence

Source inspection identifies intended semantics and representative examples.
Generated-schema inspection and targeted operations determine what the current GraphQL protocol actually exposes and whether values round-trip.
A gap is confirmed only when both the intended semantic requirement and the current observable limitation are recorded.

### Classify by ownership

Each case will be assigned to one of: already supported, GraphQL value/resource semantics, GraphQL member metadata, GraphQL collection access, GraphQL application entry points, web-component composition, viewer policy, Wicket-specific behavior, or explicit non-goal.
This prevents presentation preferences from being mislabeled as protocol defects.

### Retain a machine-readable matrix and a narrative roadmap

The matrix supports comparison and future reference-app revision audits.
The narrative explains priorities, dependencies, compatibility concerns, and recommended proposal boundaries.
Both identify the source example, current GraphQL evidence, expected contract, and disposition.

### Keep analysis free of product changes

The change may add analysis documents and disposable or non-production probe instructions.
It will not alter Java, JavaScript, generated schema behavior, Maven runtime modules, or shipped browser assets.
Pending implementation drafts may be revised, merged, split, or rejected as an output of the analysis.

## Risks / Trade-offs

- [The reference application is very broad] → Audit representative equivalence classes rather than every near-duplicate example, while retaining traceability to source locations.
- [A generated schema may fail before individual probes run] → Capture startup and schema-construction failures as first-class evidence and isolate reduced fixtures where needed.
- [Wicket layout behavior can be mistaken for domain semantics] → Record whether each behavior comes from the metamodel, grid or menu layout resource, viewer policy, or Wicket implementation.
- [Analysis can become open-ended] → Use explicit completion criteria and produce a ranked roadmap even when some cases remain intentionally unsupported.

## Migration Plan

This change is documentation-only and requires no migration.
Follow-on proposals are not promoted until the analysis records their prerequisites and compatibility impact.

## Open Questions

- Can the reference application run its GraphQL variant directly from the audited revision in the available build environment?
- Which generated schema forms provide the clearest stable evidence without committing an impractically large snapshot?
- Which presentation hints are framework-neutral enough to expose outside grid and menu resources?
- Which custom values should be supported by default rather than through application extensions?
