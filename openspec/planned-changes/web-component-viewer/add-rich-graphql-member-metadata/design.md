## Context

Current wrappers expose datatype, value, hidden, disabled, choices, autocomplete, validation, and invocation behavior.
GraphQL field descriptions may carry a Causeway description or friendly label, but a client cannot reliably distinguish the two.

The executable effective-grid probe found names, descriptions, action positions, property attributes, icon classes, field sets, columns, and unreferenced placement in the XML resource.
Adding those same fields to every rich wrapper would increase an already 21,142-type schema and recreate part of the grid metamodel.

## Goals / Non-Goals

**Goals:**

- Expose canonical friendly names and descriptions independently.
- Expose a small confirmed set of editor-neutral local constraints on known property and parameter wrappers.
- Support standalone components and fallback rendering when no grid is available.
- Preserve dynamic hidden, disabled, validation, and invocation behavior as authoritative.
- Remain discoverable by targeted GraphQL introspection.

**Non-Goals:**

- Returning annotation instances or metamodel objects.
- Adding a second member-list endpoint.
- Duplicating rows, columns, tabs, field sets, menu sections, ordering, or unreferenced placement.
- Duplicating action positions, prompt style, redirect policy, icons, CSS, table decorators, page-size presentation, sorting hints, or sequence from layouts.
- Reproducing Wicket rendering behavior.

## Decisions

### Extend only known wrappers

Local metadata fields live beneath existing property, action, parameter, collection, object-meta, or service shapes where evidence justifies them.
Clients first discover semantic member IDs through standard targeted introspection and request metadata only for wrappers they use.
No aggregate metadata catalogue or member-list field is added.

### Separate names and descriptions

A canonical friendly name and a distinct description are independent nullable values.
A missing description does not automatically duplicate the friendly name.
Existing GraphQL field descriptions remain compatible and may continue to provide concise schema documentation.

Localized runtime values use request-context locale and documented cache behavior.
Schema-level static descriptions are not treated as the sole localized contract.

### Admit only local editor-neutral constraints

The accepted initial set is limited to constraints or simple text-editing semantics not already expressed by GraphQL input types and needed by standalone property or parameter editors.
Candidate fields include maximum length, regular-expression intent, accepted-file values, multiline, and typical length when confirmed by metamodel facets.

GraphQL nullability remains the canonical structural requiredness signal.
Server validation remains authoritative even when a client uses constraints proactively.

### Keep structural and viewer hints in resources

Effective grid XML owns member placement, action positions, page grouping, layout order, icons, CSS, prompt-related presentation, collection presentation, and fallback placement.
Effective menu XML owns bars, menus, sections, entries, labels, and structural ordering.
Wicket decorators, redirect behavior, repainting, and CSS implementation details remain excluded.

### Preserve authorization boundaries

New local metadata does not expose values, authorization rules, or disabled-reason internals.
A hidden wrapper reveals no more static identity than established schema introspection already reveals and returns no sensitive runtime metadata beyond the documented hidden contract.

## Risks / Trade-offs

- [Small metadata may omit useful hints] → Add fields only after another concrete framework-neutral client requirement and matrix evidence.
- [Runtime localization can affect caching] → Scope wrapper metadata reads by locale and avoid treating schema descriptions as mutable localized state.
- [Static metadata can conflict with dynamic state] → Keep hidden, disabled, validation, and invocation outcomes authoritative.
- [Descriptor types can enlarge the schema] → Reuse narrow shapes and measure schema-size and startup deltas.

## Migration Plan

All accepted fields are additive.
Existing descriptions, generated names, and operations remain valid.
Clients may adopt independent labels and constraints incrementally.

## Open Questions

- Whether multiline and typical length belong in the first local constraint shape or remain grid-only until a standalone editor requires them.
- Whether common name and description fields should use one reusable metadata type or direct fields on each wrapper to minimize generated type count.
