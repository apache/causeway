## Context

Current rich wrappers expose datatype, value, hidden, disabled, choices, autocomplete, validation, and invocation behavior.
Their GraphQL field descriptions may carry a Causeway description or friendly label, but a client cannot reliably distinguish the two.

The executable effective-grid probe found names, descriptions, action positions, property attributes, icon classes, field sets, columns, and unreferenced placement in the XML resource.
Adding those structural fields to rich wrappers would recreate part of the grid metamodel and enlarge an already large generated schema.
Resource `fileAccept` metadata is already exposed for Blob and Clob property reads and resource action parameters, so this change must preserve rather than duplicate that contract.

## Goals / Non-Goals

**Goals:**

- Expose canonical friendly names and descriptions independently on property, collection, action, and action-parameter wrappers.
- Expose a bounded set of local text-editor constraints on property and action-parameter wrappers.
- Support standalone components and fallback rendering when no grid is available.
- Preserve translated canonical metadata without invoking imperative domain-object naming or description methods.
- Preserve dynamic hidden, disabled, validation, and invocation behavior as authoritative.
- Remain discoverable by targeted GraphQL introspection.

**Non-Goals:**

- Returning annotation instances or metamodel objects.
- Adding an aggregate member-list endpoint.
- Duplicating rows, columns, tabs, field sets, menu sections, ordering, or unreferenced placement.
- Duplicating action positions, prompt style, redirect policy, icons, CSS, table decorators, page-size presentation, sorting hints, or sequence from layout resources.
- Adding a requiredness flag that conflicts with GraphQL input nullability.
- Reproducing Wicket rendering behavior or changing web-component toolkit selection.

## Decisions

### Extend only existing known wrappers

One non-null `metadata` field is added to rich property, collection, action, and action-parameter wrapper types.
The shared metadata object contains `friendlyName`, `description`, `maxLength`, `pattern`, `patternFlags`, `multiLine`, and `typicalLength`.
Constraint fields are populated only for property and action-parameter wrappers and are null for collection and action wrappers.
Clients discover semantic wrapper and member IDs through standard targeted introspection and request only metadata they need.
No aggregate metadata catalogue or member-list field is added.

### Use exactly one shared descriptor type

An initial direct-scalar prototype added no GraphQL object type but increased the representative generated SDL by 114,954 bytes, or 40.4 percent.
The accepted design instead adds exactly one `RichMemberMetadata` object type and one metadata field per known wrapper.
The accepted prototype increases the representative SDL by 23,784 bytes, or 8.4 percent, while keeping all seven scalar definitions in one reusable type.
Metadata resolvers return request-local maps so the shared fields use GraphQL's default property fetcher and do not add a resolver layer per scalar.

### Separate names and descriptions

`friendlyName` is a non-null `String` supplied by `getCanonicalFriendlyName()`.
`description` is a nullable `String` supplied by `getCanonicalDescription()` and never copies the friendly name when absent.
Canonical access excludes imperative per-object naming and description methods, avoids fetching domain values, and retains the metamodel translation mechanism active for the request locale.
Existing GraphQL schema descriptions remain compatible documentation and are not the independent runtime contract.

### Normalize constraint absence

`maxLength` is a nullable `Int` and ignores the unlimited fallback facet.
`pattern` is a nullable `String` containing the authoritative Java regular-expression text.
`patternFlags` is a nullable `Int` containing Java `Pattern` flags and is absent when no non-fallback regular-expression facet exists.
`multiLine` is a nullable `Int` containing the positive requested line count and is absent for the fallback single-line behavior.
`typicalLength` is a nullable `Int` containing the positive effective local sizing hint when the feature has that facet.
Malformed, non-positive, unlimited, or absent facet values become GraphQL null rather than fabricated defaults.

### Preserve established resource metadata

Resource `fileAccept` remains where existing clients already find it: under Blob or Clob property-get wrappers and directly on resource action-parameter wrappers.
The new scalar helper does not create another `fileAccept` field.
Resource input validation and deployment policy remain authoritative.

### Preserve structural authority

Effective grid XML owns member placement, action positions, page grouping, layout order, icons, CSS, prompt-related presentation, collection presentation, and fallback placement.
Effective menu XML owns bars, menus, sections, entries, labels, and structural ordering.
Wicket decorators, redirect behavior, repainting, and CSS implementation details remain excluded.

### Preserve authorization boundaries

New metadata resolvers read only static canonical facets and never fetch a property value, parameter default, disabled reason, or authorization rule.
A hidden known wrapper reveals no more static schema identity than targeted introspection already reveals.
Dynamic `hidden`, `disabled`, validation, and invocation results remain authoritative for the current object and user.

## Risks / Trade-offs

- [Wrapper metadata fields enlarge generated SDL] → Measure type count, SDL bytes, schema startup, and enforce that exactly one shared GraphQL object type is added.
- [Java regular expressions are not JavaScript patterns] → Label `patternFlags` explicitly as Java flags and keep server validation authoritative.
- [Runtime localization can affect caching] → Resolve suppliers during field execution and test two request locales without caching one locale globally.
- [Static metadata can conflict with dynamic state] → Keep hidden, disabled, validation, and invocation outcomes authoritative.
- [Local hints may encourage layout duplication] → Keep the accepted field list closed and require new framework-neutral evidence for expansion.

## Migration Plan

All accepted fields are additive.
Existing schema descriptions, generated names, resource fields, and operation documents remain valid.
Clients may adopt independent labels and constraints incrementally and must tolerate absent nullable fields.
No persisted data, route, application markup, or generated input type migration is required.

## Open Questions

None for the initial scope.
New metadata fields require a separate evidence-backed change.
