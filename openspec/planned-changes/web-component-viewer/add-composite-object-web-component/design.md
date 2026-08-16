## Context

The current public vocabulary provides semantic components for an object header, properties, actions, collections, values, links, editors, and interaction prompts.
All components share one nearest object context, which owns identity, schema descriptions, projections, commands, and authoritative object state.

Causeway exposes an effective grid resource through object metadata.
The built-in `GridFallbackLayout.xml` uses Bootstrap-style rows and twelve-column spans with a domain-object header, unreferenced actions, Identity, Other, Metadata, and Details field sets, and a collection tab group.
Applications may provide richer grid files with explicit member references, tabs, field sets, and unreferenced-member markers.

The deferred generic viewer currently plans to implement layout parsing and object composition itself.
Moving that responsibility into `<causeway-object>` makes layout-aware composition reusable by vanilla HTML and any framework and leaves HTMX responsible only for shell and navigation concerns.

## Goals / Non-Goals

**Goals:**

- Offer one semantic element for a complete object projection.
- Reuse the nearest authoritative object context.
- Interpret a documented framework-neutral subset of Causeway grid XML.
- Reproduce the semantic intent of `GridFallbackLayout.xml` without requiring Bootstrap CSS.
- Generate existing lower-level semantic components in light DOM.
- Place every discovered visible member at most once.
- Provide responsive and accessible groups, tabs, headings, and error states.
- Allow applications to style, diagnose, or replace automatic composition.

**Non-Goals:**

- Owning object identity, GraphQL transport, mutations, or action invocation independently from the context and child components.
- Reproducing every Bootstrap or Wicket layout behavior.
- Adding HTMX, routes, menus, or home-page policy.
- Replacing explicit low-level composition.
- Treating layout XML as an authorization source.

## Decisions

### Consume the nearest object context

`<causeway-object>` requires a nearest `<causeway-object-context>` and contributes member and metadata requirements to it.
It does not create a second object state owner or accept raw GraphQL documents.
Applications wanting a one-line route fragment compose the context and object together; explicit low-level pages remain valid.

### Separate layout planning from member runtime semantics

The component builds an immutable layout plan from the effective grid and targeted schema description.
The plan identifies regions and semantic member IDs.
Generated member elements then own hidden, disabled, loading, validation, editing, invocation, collection, and error behavior exactly as they do in authored HTML.

### Support a documented grid subset

The initial parser recognizes rows, columns and twelve-column spans, tab groups, tabs, field sets, domain-object placement, action/property/collection references, and unreferenced actions, properties, and collections.
Unknown nodes or attributes produce bounded diagnostics and local fallback where possible.
XML parsing disables external entities and never executes markup from the resource.

### Model fallback on Causeway's canonical fallback grid

When no usable effective grid exists, the component uses a bundled semantic plan corresponding to `GridFallbackLayout.xml`: header and actions first, property field sets in the leading region, and collections in the larger trailing tab region.
The implementation uses CSS Grid and semantic HTML rather than depending on Bootstrap 3.
Missing conventional field sets collapse without leaving empty inaccessible regions.

### Allocate members once

Explicit layout references claim matching introspected members.
Unreferenced markers claim remaining members of the corresponding kind in deterministic schema or metadata sequence.
Duplicate, stale, wrong-kind, and unknown references produce diagnostics and never duplicate visible components.
Members omitted by the layout are placed only when the applicable unreferenced marker or fallback policy permits it.

### Keep generated structure observable and customizable

Generated child components and layout containers remain in light DOM.
Stable element names, region attributes, CSS parts or classes, custom properties, and lifecycle events allow themes and host diagnostics without exposing internal GraphQL documents.
Applications needing completely custom structure use explicit composition or a registered layout-plan override rather than mutating generated children during render.

### Implement accessible responsive tabs and groups

Field sets use labelled groups or sections.
Tab lists, tabs, and panels use correct roles, relationships, keyboard movement, selected state, and focus behavior.
At narrow widths, columns stack in document order and tabs remain keyboard operable.
Loading and layout errors use appropriate status announcements without replacing successful child regions.

## Risks / Trade-offs

- [Causeway grid XML is extensive] → Support and test an explicit subset, diagnose unknown instructions, and fall back locally.
- [Generated children may be mistaken for authored stable DOM] → Document semantic hooks and reserve implementation-only wrapper details.
- [Hidden generated members can leave empty groups] → Collapse empty groups after child state settles without rebuilding unrelated interactions.
- [Layout refresh could destroy active editors] → Cache plans by logical type and defer structural replacement while an interaction owns focus unless the object generation changes.
- [Bootstrap spans may imply Bootstrap behavior] → Preserve twelve-column proportions using component-owned CSS Grid, not Bootstrap classes.

## Migration Plan

The component is additive.
Existing explicit compositions remain unchanged.
The sample adds a separate `<causeway-object>` demonstration before the generic viewer adopts it.

## Open Questions

- Should `<causeway-object>` optionally create its own internal context when identity attributes are supplied, or remain strictly context-consuming?
- Which grid attributes beyond span, names, IDs, and unreferenced markers are essential initially?
- Should application overrides register layout-plan factories by logical type or provide alternative layout resources?
- How should active interaction preservation behave if a translated or dynamic layout resource changes?
