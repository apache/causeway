# ADR: Pursue a CSP-first constrained Vaadin reference-widget pilot

## Status

Analysis recommendation requiring a separate proposal and security review.

## Context

Bootstrap can improve visual consistency but does not replace the rich widget add-ons used by the Wicket viewer.
Vaadin free-core Web Components provide searchable single and multi-selection, Grid, date and time fields, upload, dialogs, and a broader business-control vocabulary while operating directly in the browser without Flow.

The evaluation proved GraphQL-backed adapters, semantic wrappers, custom HTML composition, headless accessibility journeys, route lifecycle cleanup, selective Maven packaging, free-core licensing, and zero external requests.
It also found three material constraints:

- The current strict `style-src 'self'` policy blocks component-originated inline style operations.
- GraphQL autocomplete has search but no server paging or result count.
- Collection windows have offset, size, count, and configured ordering but no user-requested sort or filter.

## Decision

Do not adopt the broad Vaadin suite or change the default viewer.
Pursue a separate opt-in pilot for Combo Box and Multi-Select Combo Box behind Causeway-owned reference wrappers, but make strict-CSP compatibility a stage-zero stop gate.

The pilot will:

- Use standalone Vaadin Web Components only and exclude Flow.
- Keep GraphQL authoritative for descriptions, choices, validation, mutation, interaction, and state.
- Preserve public Causeway elements, semantic events, routes, object contexts, and `--causeway-*` variables.
- Keep raw Vaadin tags unsupported as a framework contract during the first slice.
- Use only the approved free-core package closure.
- Load reference widgets lazily only on routes that require them.
- Retain the current editor as configuration fallback and rollback.
- Require security approval for nonce, Constructable StyleSheet, dynamic style, or narrowly scoped CSP handling before component adoption.
- Abort the pilot if compatibility requires a blanket undocumented `unsafe-inline` policy.
- Use current search-only autocomplete only when result bounds are explicit; require a GraphQL delta before claiming server-paged lookup.

Defer Grid adoption to a later slice.
A read-only lazy Grid is technically viable now, but enabling interactive sorting or filtering first requires public GraphQL collection requirements.

## Rationale

The constrained reference subset scored 4.05 against 3.90 for the current viewer and directly addresses the Select2 class of problem.
The broad suite scored 3.89 because eager payload, release tooling, adapter breadth, and API gaps offset widget coverage.

A 57,145-byte-gzip cold reference route is a material but defensible cost for accessible search and multi-selection when loaded only where required.
The current Wicket baseline does not truly page remote choices, so the existing GraphQL search operation can support a bounded parity pilot without a private endpoint.

Internal adapters preserve custom HTML composability while preventing Vaadin data-provider and event APIs from becoming ordinary application requirements.
A separately considered server-side Vaadin viewer remains independent and shares only potential widget familiarity and visual language.

## Consequences

### Positive

- Addresses the highest-value widget gap with one maintained Apache-2.0 component family.
- Avoids assembling Bootstrap plus another reference widget library.
- Preserves GraphQL, semantic components, and ordinary custom HTML pages.
- Establishes a reusable free-core packaging and accessibility baseline.
- Keeps rollout and rollback bounded to one editor family.

### Negative

- Adds npm, esbuild, 33 runtime package instances, and approximately 57 KB gzip to affected cold routes.
- Requires a security-reviewed CSP change or build strategy.
- Adds shadow-DOM-aware adapter and testing responsibilities.
- Does not provide server-paged autocomplete through the current GraphQL API.
- Does not make raw Vaadin tags a stable extension API.

## Rejected alternatives

- Broad Vaadin adoption was rejected because its 127 KB gzip bundle and adapter surface do not outperform the baseline as a whole.
- Bootstrap-first theming was deferred because it does not answer the rich-widget requirement and risks recreating an add-on ecosystem.
- Immediate Grid adoption was rejected because visible sort and filter controls would exceed the current GraphQL contract.
- A supported raw Vaadin tier was deferred because it creates version, theme, lifecycle, and CSP compatibility obligations before the internal integration is proven.
- Flow integration was rejected because the browser viewer already has GraphQL state and deliberately supports ordinary HTML composition.

## Review triggers

Stop or reconsider the pilot when:

- Strict CSP cannot be satisfied without an unacceptable policy relaxation.
- The cold reference closure exceeds 65 KB gzip.
- Current autocomplete cannot be bounded safely.
- Semantic events, validation, focus, cancellation, or custom page lifecycle cannot remain Causeway-owned.
- Required behavior crosses into a commercial Vaadin package.
