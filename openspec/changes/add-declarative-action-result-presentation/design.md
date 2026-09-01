## Context

The interaction controller currently invokes actions with a generic result selection, publishes a normalized result, and lets the HTMX shell present successful collection results through a dynamically created `<cw-standalone-collection>` in one stable shell region.
The standalone component can render declared columns only when corresponding property wrappers already exist in the supplied result, while object-valued collection action results normally contain authoritative `_meta` identity only.
Application object pages can declare action and parameter presentation, but they cannot declare result presentation or choose an in-page result destination.
The HTMX viewer already discovers trusted logical-type object pages from `META-INF/causeway/webcomponents/pages`, and rich action metadata already carries additive static confirmation and prompt-style values.

## Goals / Non-Goals

**Goals:**

- Discover one bounded default standalone collection presentation for a canonical action result element logical type.
- Let one direct-child standalone collection declaration override that default for an authored action.
- Resolve presentation before invocation and include valid declared columns in the original action-result GraphQL selection.
- Add one generic passive `<cw-action-results>` outlet that can locate non-navigating action outcomes within the active page.
- Preserve application result-policy authority, canonical object navigation, scalar and void behavior, stable-shell fallback, accessibility, focus, and route lifecycle.
- Keep normalized result values finite, authoritative, and free of follow-up hydration.

**Non-Goals:**

- Add server paging, sorting, filtering, associated actions, row mutation, or member ownership to standalone results.
- Let presentation declarations fabricate fields, bypass authorization, alter action parameters, invoke an action, or replace canonical validation.
- Infer logical type names from generated GraphQL names or from the first returned row.
- Add named presentation variants, arbitrary outlet targeting, multiple simultaneous result regions, or collection-fragment factories in the first iteration.
- Change object-valued result navigation or make `<cw-action-results>` a global semantic-event listener.

## Decisions

### Use a dedicated bounded collection-presentation resource registry

The HTMX module will discover default resources at `classpath*:/META-INF/causeway/webcomponents/collections/*.html` and key each accepted file by the canonical logical type represented by its filename.
A resource will contain exactly one `<cw-standalone-collection>` root and zero or more direct-child `<cw-collection-column>` declarations.
The loader will apply bounded count, byte-size, UTF-8, logical-name, duplicate, cached, and reload-mode protections equivalent to object-page resources, with collection-specific diagnostics.
The client will resolve a default through a reserved same-origin HTMX endpoint before invocation, cache accepted normalized presentation by logical type and resource revision policy, and treat an absent resource as the generic presentation rather than an error.
Returned HTML will be parsed inertly and reduced to supported standalone attributes and column configurations before any live element is created, so fragment content cannot introduce scripts, event handlers, arbitrary elements, or a second result owner.

Embedding every default fragment into the shell was rejected because application-wide payload grows with unused result types and reload-mode changes cannot become visible without replacing the shell.
Placing collection fragments under `pages` was rejected because the existing registry would interpret suffixes as object logical types and mix two resource contracts.

### Advertise an authoritative result element logical type

`RichMemberMetadata` will add nullable `resultElementLogicalTypeName` for actions whose declared result is a collection with a canonical element logical type.
The value will be derived from static metamodel return semantics, remain null for non-actions, non-collection results, unsupported or non-domain element types, and require no domain-object method execution.
Object and service action preparation will select the field only when schema introspection advertises it, preserving compatibility with older schemas.
The action presentation snapshot will carry the advertised value into interaction preparation, avoiding inference from generated GraphQL type names, returned rows, or non-empty results.

Looking up presentation after invocation from the first row was rejected because empty results have no row and required columns would already be absent from the completed mutation response.

### Treat a nested standalone collection as an action declaration

One direct-child `<cw-standalone-collection>` inside `<cw-action>` will be captured as inert result presentation in the same parser-safe lifecycle used for direct-child parameter declarations.
Its element and column nodes will remain hidden, connected, and identity-stable across action rerenders, but the declaration node will never receive the action result or be moved to an outlet.
The action request will carry an immutable normalized snapshot of supported standalone attributes and column declarations.
A current inline snapshot will replace the complete type-default presentation; defaults and inline columns will not merge implicitly.
Multiple direct-child standalone declarations or a declaration on a non-collection action will produce a deterministic diagnostic and fall back safely without affecting invocation.

Moving the authored node into the outlet was rejected because the action would lose its reusable declaration and repeated or concurrent lifecycle would make ownership ambiguous.
Using `<cw-property>` children was rejected because properties consume the current object context, whereas `<cw-collection-column>` already represents row projection without property ownership.

### Compose declared columns into the original invocation selection

After action preparation identifies a collection-valued object result, the interaction controller will resolve the inline snapshot or type default and pass its valid columns to `invokeAction` as presentation selection input.
Object and service action contexts will reuse or extract the established collection-row selection builder to merge authoritative `_meta` identity with schema-advertised property wrappers and the bounded fields needed by standard value renderers.
Unknown, unsupported, abstract-incompatible, hidden-at-schema, or malformed columns will be omitted from the request and represented through bounded diagnostics rather than causing a second read or fabricating a value.
The result event will carry the resolved immutable presentation snapshot additively so the host uses exactly the configuration that shaped the invocation.
The live standalone component will still issue no GraphQL operation and will render only wrappers supplied by the completed action response.

Keeping presentation entirely post-invocation was rejected because useful declared cells would remain unavailable.
Hydrating every returned identity was rejected because it changes one action into unbounded follow-up work and weakens result-generation authority.

### Make `<cw-action-results>` a passive generic outlet

`<cw-action-results>` will be a registered public element and accessible result-region boundary that is empty when no host-owned presentation is mounted.
It will not listen globally for action results, invoke policy, resolve fragments, navigate, or construct domain presentation independently.
The HTMX result policy will snapshot the single outlet in the active route page when action interaction begins and will mount successful non-navigating scalar, void-status, or collection presentation there after application override policy declines the result.
Object results will retain canonical navigation instead of being mounted.
When no unique active-page outlet exists, when duplicate outlets make placement ambiguous, or when the captured outlet disconnects before completion, the existing stable shell result region will remain the deterministic fallback.
The shell fallback will itself use the same result-region presentation contract, and preserved void results will be rehomed to the equivalent current outlet after route refresh when still current.
A newer successful result, route generation, action generation, application claim, or disconnection will retire prior live result nodes and prevent stale work from mounting.

Making the outlet itself subscribe to every `causeway-action-result` event was rejected because it would compete with HTMX application override, navigation, refresh, announcements, and route-generation policy.
Adding outlet names or action target IDs was deferred until a demonstrated need for multiple concurrent result regions exists.

### Keep default and inline presentation precedence deterministic

The effective collection presentation order will be a valid current inline action declaration, then an available valid type-default resource, then generic identity or scalar presentation.
An explicit inline declaration with zero columns will intentionally select generic row presentation while retaining its authored heading and description attributes.
Malformed inline presentation will not silently activate the type default for the same attempt unless its failure is wholly equivalent to absence; deterministic diagnostics will distinguish absent, rejected, and accepted sources.
The resolved source and logical type will be exposed through bounded diagnostic state and test hooks without reflecting structured result data to attributes.

## Risks / Trade-offs

- [Resolving an uncached default adds one same-origin request before first invocation] → Cache normalized successful and absent lookups, expose preparation state, and never invoke until the current resolution generation completes.
- [Presentation columns broaden mutation response size] → Bound column count, select only schema-advertised wrapper fields, and perform no follow-up reads.
- [A fragment typo can omit a desired column] → Validate against introspection, publish deterministic diagnostics, and keep identity-only presentation usable.
- [Route-owned outlets can disconnect during asynchronous service actions] → Snapshot the origin outlet and route generation, then use only a still-current outlet or stable shell fallback.
- [Duplicate page outlets create ambiguous placement] → Reject page-level ambiguity deterministically and use the stable shell rather than DOM-order selection.
- [Abstract result element types can have incompatible members] → Select only safely common or existing bounded inline-fragment fields and fall back to identity presentation when qualification cannot be proven.
- [The new metadata field expands a shared GraphQL type] → Keep it nullable, static, action-only, additive, and covered by schema-growth and introspection compatibility tests.

## Migration Plan

Existing pages without collection fragments, nested result declarations, or action-result outlets will retain generic standalone collection presentation in the stable shell.
Applications may add type-default resources independently, then add page outlets or action-specific overrides incrementally.
Rollback removes the new resources and declarations while leaving existing action invocation and generic shell result behavior valid.

## Open Questions

None.
