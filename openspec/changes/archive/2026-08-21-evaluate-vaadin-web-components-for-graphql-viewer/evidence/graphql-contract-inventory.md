# Existing GraphQL and viewer contract inventory

## Scope

This inventory maps the standalone Vaadin browser APIs required by the evaluation to the current public rich GraphQL and semantic web-component contracts.
It records gaps rather than inventing candidate-only endpoints.

## Reference choices and validation

| Candidate need | Existing public behavior | Mapping | Initial gap |
|---|---|---|---|
| Fixed property choices | Rich property `choices` field and `ObjectContextController.propertyChoices()` | Load choices through `executeObjectInteraction` and normalize encoded values or object bookmarks | No server paging or count |
| Property autocomplete | Rich property `autoComplete(search)` and `ObjectContextController.autoCompleteProperty()` | Map Vaadin filter text to the GraphQL search argument and abort superseded requests | No offset, page size, count, or continuation token |
| Property validation | Rich property `validate(value)` and property mutation | Validate through the object context before mutation | No gap for one pending value |
| Action parameter choices | Rich action-parameter `choices`, `default`, and `disabled` fields | Populate parameter state through the existing interaction controller | No paging or count |
| Action parameter autocomplete | Rich parameter `autoComplete(search)` and `autoCompleteActionParameter()` | Map filter text and current argument values to the existing operation | No paging or count |
| Action validation | Rich action `validate` over current argument values | Keep Causeway validation authoritative | No candidate-specific gap |
| Action invocation | Existing rich mutation invocation and semantic outcome normalization | Keep the interaction controller authoritative | No candidate-specific gap |
| Request cancellation | `AbortSignal` is accepted by GraphQL client and transient object-context commands | Abort or ignore stale Combo Box requests by generation | Supported |
| Stable object identity | Object references are represented by logical type and opaque identifier or encoded choice value | Use canonical bookmark identity as Vaadin item identity | The adapter must not use array index or display text |
| Multi-reference selection | Rich arguments and choices can represent lists where the schema exposes a list type | Preserve ordered bookmark arrays and validate as one argument value | Editable multi-valued properties require capability-specific confirmation |

The current autocomplete operation is search-only.
Vaadin Combo Box exposes page and page-size data-provider parameters, but the existing GraphQL operation cannot express them.
The prototype can safely return one bounded autocomplete result set, but true server paging requires a later GraphQL specification change.

## Collection windows and Grid

| Candidate need | Existing public behavior | Mapping | Initial gap |
|---|---|---|---|
| Lazy row window | Rich collection `window(offset, size)` | Map Grid page and page size to zero-based offset and bounded size | Supported |
| Total count | Window returns `totalCount` | Pass total size to the Grid callback | Supported |
| Previous and next state | Window returns `hasPrevious` and `hasNext` | Record transport consistency and loading state | Supported |
| Stable ordering | Window reports `CONFIGURED` or `ENCOUNTER` ordering | Accept configured order; flag encounter order for cross-request instability | Encounter ordering is not a stable paging guarantee |
| Client-requested sort | Collection metadata may configure server ordering before the window | No equivalent argument exists on `window` | Unsupported without GraphQL change |
| Client-requested filter | Collection window has no filter argument | No honest mapping exists | Unsupported without GraphQL change |
| Row projection | Browser supplies a semantic column selection to `readCollectionWindow()` | Map Grid columns to existing row selection | Supported for known schema fields |
| Stable row identity | Selected object metadata can provide canonical logical type and identifier | Use canonical bookmark identity and never Grid index | Depends on projecting identity for every navigable row |
| Cancellation and caching | Object context keys reads by member, projection, offset, size and accepts abort signals | Invalidate superseded Grid work by route generation | Supported |
| Object navigation | Semantic object links emit Causeway navigation information | Navigate using the canonical HTMX bookmark route | Supported |

The current Grid-compatible transport is therefore lazy and count-aware but not an interactive server-side sorting or filtering API.
The prototype must disable or clearly label unsupported Grid controls rather than sorting only the loaded page and implying global correctness.

## Object context and lifecycle

`<causeway-object-context>` owns one logical type and opaque object identifier and coordinates schema discovery, object reads, transient commands, mutations, cancellation, and generation-scoped state.
The GraphQL client supports replaceable execution and `AbortSignal` propagation.
Collection and interaction components already separate loading, ready, partial-error, terminal-error, validation, and disabled states.

The candidate adapters must register and unregister beneath the existing route context rather than create a second domain state store.
Repeated route replacement must disconnect candidate listeners, abort transient work, close overlays, and prevent stale callbacks from mutating the current fragment.

## HTMX routing and custom pages

The HTMX router resolves a custom server fragment factory for the exact public logical type before falling back to the generic object page.
Every generic or custom object fragment contains one disposable route-level `<causeway-object-context>` for the canonical identity.
The browser data plane remains independent of HTMX after fragment composition.

A custom page can therefore contain semantic Causeway elements, Causeway-owned Vaadin-backed wrappers, or optional raw allowlisted Vaadin controls while sharing the one route context.
No custom page needs Flow, Java Vaadin APIs, or a new server route.

## Initial gap summary

- Autocomplete supports search but not server-side page, size, total count, or continuation state.
- Collection windows support offset, size, total count, projection, and configured ordering but not user-requested sorting or filtering.
- Encounter-ordered collections do not guarantee stable row order across requests.
- Every Grid row used for navigation must project canonical identity.
- Multi-reference action parameters are credible; editable multi-reference property coverage requires confirmation from the exposed schema.
- The existing cancellation and route-generation model is suitable for Vaadin data-provider adapters.
