## Context

The semantic domain components currently reflect a custom `member` attribute through a custom `member` JavaScript property.
That identifier drives context requirements for properties, actions, and collections, and column projection configuration for collection columns.
Generated object layouts, authored fragments, tests, browser selectors, and documentation all depend on that public contract.

HTML already provides a reflected `id` property for the `id` attribute.
Adopting it removes a Causeway-specific naming layer but makes normal HTML identifier semantics, including document-level uniqueness expectations, visible to application authors.

## Goals / Non-Goals

**Goals:**

- Make `id` the only public attribute that identifies the represented domain member on member-bearing elements.
- Use the native reflected `element.id` property rather than a custom `element.member` property.
- Preserve internal domain terminology and semantic payload fields where `member` describes a domain concept rather than a DOM API.
- Migrate generated and authored markup, selectors, samples, tests, and documentation together.
- Detect stale use of the removed attribute and property APIs.

**Non-Goals:**

- Rename GraphQL member descriptors, context requirement payloads, internal collection-column configuration fields, or semantic event details.
- Rename `data-causeway-associated-member` or unrelated uses of `member` in domain terminology.
- Generate scoped or synthetic host identifiers to avoid duplicate authored IDs.
- Provide a compatibility alias for the former attribute or JavaScript property.

## Decisions

### Use the native reflected identifier directly

`<cw-property>`, `<cw-action>`, `<cw-collection>`, and `<cw-collection-column>` will read their domain member identifier from `this.id`.
Their observed-attribute lists and connected mutation behavior will observe `id`, so changing an identifier reconnects the context requirement or refreshes column configuration as appropriate.

Retaining a custom `member` getter backed by `id` was rejected because it would leave two public names for one contract and obscure the requested migration to standard DOM APIs.

### Keep domain-member vocabulary below the DOM boundary

Context requirements will continue to use records such as `{kind: 'property', member: this.id}`.
GraphQL descriptors, layout plans, semantic event details, and internal column configuration will continue to call the domain concept `member`.

Renaming those internal contracts to `id` was rejected because they describe Causeway members rather than HTML identifiers and would unnecessarily widen the breaking change.

### Perform an alias-free markup migration

Generated fallback and grid layouts, HTMX fragments, custom pages, examples, selectors, and browser tests will use `id` exclusively.
The former `member` attribute will not be read, observed, mirrored, or translated.

A transitional alias was rejected because silent dual support would make stale application markup difficult to detect and would weaken the single public contract.

### Audit only DOM-facing member APIs

The source audit will reject `member=` on `cw-property`, `cw-action`, `cw-collection`, and `cw-collection-column`, corresponding `[member]` selectors and `getAttribute` or `setAttribute` calls, and custom `.member` access on those elements where it can be identified reliably.
The audit will explicitly permit domain-model fields, local variables, requirement payloads, and `data-causeway-associated-member`.

## Risks / Trade-offs

- **Risk: Repeated domain member names can produce duplicate HTML IDs when applications render multiple equivalent components in one document.** → Document standard HTML uniqueness responsibility and avoid inventing automatic scoping that would make `id` differ from the requested member name.
- **Risk: Broad replacement could rename internal GraphQL or semantic contracts.** → Use DOM-context-aware migration and retain tests proving internal member payloads remain unchanged.
- **Risk: Stale selectors can make acceptance tests time out rather than fail clearly.** → Add focused contract tests and source auditing before running full browser journeys.
- **Risk: Changing `id` while connected can leave a component bound to its former descriptor.** → Observe `id` and exercise the existing reconnect lifecycle in component tests.

## Migration Plan

1. Update member-bearing element implementations and generated markup to use native `id` reflection.
2. Migrate authored pages, selectors, tests, samples, and documentation from `member` to `id`.
3. Add focused tests and a source audit for the new public contract and removed API.
4. Run foundation, HTMX, sample, secured, and Reference Application validation.
5. Applications migrate one-to-one from `member="name"` to `id="name"` and from `element.member` or `[member='name']` to `element.id` or `#name`/`[id='name']`.

Rollback requires reverting implementation and authored markup together because no compatibility alias exists.

## Open Questions

None.
