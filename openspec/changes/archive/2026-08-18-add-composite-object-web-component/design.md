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

`<causeway-object>` requires a nearest `<causeway-object-context>` and contributes a layout metadata requirement to it.
It does not accept identity attributes, create an implicit context, own GraphQL transport, or accept raw GraphQL documents.
Applications wanting a one-line route fragment compose the context and object together; explicit low-level pages remain valid.

The public attributes are `editable`, which enables editing on generated property children, and `layout-mode`, whose supported values are `auto` and `fallback`.
The default `auto` mode requests the authorized effective grid; `fallback` deliberately skips resource retrieval.
The component exposes `refreshLayout()` for an explicit locale, authorization, or layout-context transition without rebuilding itself on every ordinary object refresh.

### Keep structural-resource retrieval context-owned

The context exposes one bounded same-origin structural-resource operation used by the component after its metadata requirement returns the opaque grid path.
The operation accepts only an origin-relative path beginning with exactly one slash, sends credentials under normal same-origin fetch policy, requests XML, uses no-store semantics, supports cancellation, and reports bounded status failures without response bodies.
The component does not derive resource URLs from domain identity and does not cache layout data across object contexts.

### Separate layout planning from member runtime semantics

The component builds an immutable layout plan from the effective grid and targeted schema description.
The plan identifies regions and semantic member IDs.
Generated member elements then own hidden, disabled, loading, validation, editing, invocation, collection, and error behavior exactly as they do in authored HTML.

### Support a documented grid subset

The initial parser recognizes Bootstrap-grid rows and columns, spans from one through twelve, tab groups, tabs, field sets, domain-object placement, action/property/collection references, nested action ordering, `named` labels, and unreferenced actions, properties, and collections.
It recognizes component elements by namespace URI or the established `c` and `cpt` prefixes and grid elements by namespace URI or the established `bs` prefix.
Known non-semantic presentation hints such as size, label position, typical length, date adjustment, paging, and default view are ignored without changing semantic behavior.
Unknown nodes, unsupported placement hints, or other unknown attributes produce bounded diagnostics and preserve recognized descendants where safe.
XML declarations and comments are accepted, while document types, entity declarations, processing instructions beyond the XML declaration, scripts, styles, malformed markup, input beyond one mebibyte, nesting beyond 64 elements, more than 4,096 elements, and unknown entity references are rejected before planning.
The parser never inserts resource markup into the document and emits only escaped semantic component markup.

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
Stable element names, `data-causeway-region` attributes, CSS classes and custom properties, `data-layout-state`, and layout state and diagnostic events allow themes and host diagnostics without exposing internal GraphQL documents.
Diagnostics are redacted, deduplicated, and capped at twenty entries per plan.
Applications needing completely custom structure use explicit composition rather than mutating generated children during render; a layout-plan override registry is not introduced.

### Implement accessible responsive tabs and groups

Field sets use labelled groups or sections.
Tab lists, tabs, and panels use correct roles, relationships, keyboard movement, selected state, and focus behavior.
At narrow widths, columns stack in document order and tabs remain keyboard operable.
Loading and layout errors use appropriate status announcements without replacing successful child regions.
Generated regions react to child hidden state through stable host attributes and collapse only when they contain no visible semantic child.
The component memoizes a successfully rendered plan by context and grid reference so ordinary authoritative object refreshes do not destroy active child interactions.

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

## Resolved Questions

- `<causeway-object>` remains strictly context-consuming and never creates an internal context from identity attributes.
- The initial grid contract supports semantic structure, spans, IDs, `named` labels, unreferenced markers, and nested member order; known non-semantic hints are safely ignored and unknown hints are diagnosed.
- No framework-neutral layout-plan registry is introduced; applications use effective grid resources or explicit lower-level composition.
- Ordinary context refresh retains the rendered plan, while an explicit `refreshLayout()` or changed grid reference cancels retrieval and replaces structure after the new plan is available.
