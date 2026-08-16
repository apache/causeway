## Context

Current wrappers expose datatype, value, hidden, disabled, choices, autocomplete, validation, and invocation behavior.
GraphQL field descriptions may carry a Causeway description or friendly label, but a client cannot reliably distinguish the two.
Property layout constraints, action prompt and association hints, and collection presentation semantics are not available as local structured metadata.

Grid and menu layout XML already describe complete page and application structure.
The new metadata must complement those resources, support components used outside a full layout, and avoid a duplicate API that enumerates every member.

## Goals / Non-Goals

**Goals:**

- Expose canonical friendly names and descriptions independently.
- Expose confirmed framework-neutral constraints and hints on known wrappers.
- Support fallback composition when a complete layout is unavailable.
- Preserve dynamic hidden and disabled behavior as authoritative.
- Remain discoverable by targeted GraphQL introspection.

**Non-Goals:**

- Returning annotation instances or metamodel objects.
- Adding a second member-list endpoint.
- Duplicating the complete grid or menu layout beneath object reads.
- Requiring clients to obey CSS, prompt, or redirect hints.
- Reproducing Wicket-only rendering behavior.

## Decisions

### Extend known-member wrappers

Metadata fields live beneath the existing property, action, parameter, collection, object-meta, and service shapes.
Clients first discover semantic member IDs through standard introspection and request metadata only for wrappers they use.
This keeps metadata additive and targeted.

### Separate semantic constraints from optional presentation hints

Validation-related information such as optionality, maximum length, regular-expression intent, accepted files, and datatype constraints is distinguished from optional hints such as multiline, typical length, label position, prompt style, icon, CSS class, and redirect preference.
Server validation remains authoritative in all cases.

### Keep structural layout in resources

Grid resources remain responsible for rows, columns, tabs, field sets, explicit member references, and unreferenced-member placement.
Menu resources remain responsible for primary, secondary, tertiary, menu, section, and service-action structure.
Wrapper metadata supplies local labels, descriptions, constraints, icons, and fallback ordering.

### Publish stable semantic values

Enum-like metadata uses stable GraphQL enum or documented string values rather than Java enum names leaking accidentally.
Missing facets return null or documented defaults.
Authorization-sensitive metadata is omitted with the member when hidden and never reveals policy rules.

## Risks / Trade-offs

- [Metadata breadth can recreate the metamodel] → Admit only fields justified by the coverage analysis and generic client requirements.
- [Hints may be viewer-specific] → Mark them optional and preserve client freedom to ignore them.
- [Static metadata can conflict with dynamic state] → Keep hidden, disabled, validation, and invocation outcomes authoritative at runtime.
- [Schema size can grow] → Use reusable descriptor types and targeted introspection rather than aggregate metadata payloads.

## Migration Plan

All fields are additive.
Existing descriptions and operations remain valid.
Clients may adopt canonical names and descriptors incrementally.

## Open Questions

- Which CSS and redirect hints are sufficiently framework-neutral to expose?
- Should sequence be numeric, lexical, or a normalized sortable key?
- Which metadata belongs at generated GraphQL field level versus runtime wrapper level?
- How should translated friendly names and descriptions interact with schema caching?
