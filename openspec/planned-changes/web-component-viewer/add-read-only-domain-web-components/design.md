## Context

The foundation change introduces the GraphQL client provider, semantic object context, coordinated read projection, object header, and a narrow scalar-property probe.
This change turns that probe into a coherent read-only component library suitable for declarative custom pages and later generic composition.
The components must preserve Causeway semantics while remaining consumable from plain HTML and frontend frameworks.

## Goals / Non-Goals

**Goals:**

- Define a stable semantic custom-element vocabulary for read-only domain pages.
- Render members according to rich-schema capabilities and instance state supplied by object contexts.
- Support pluggable value presentation without exposing GraphQL operation construction.
- Support object navigation and lazy read-only collections.
- Reuse hydrated GraphQL results across nested object contexts.
- Provide accessible and styleable framework-neutral markup.

**Non-Goals:**

- Property editing or mutation.
- Action prompts, parameter negotiation, validation, or invocation.
- Collection paging, filtering, sorting, selection, or bulk actions.
- Generic page generation, routing, or HTMX integration.
- A replacement for the GraphQL schema or object context.
- Reproduction of every Wicket viewer presentation hint.

## Decisions

### Mirror the rich grammar with semantic elements

The library will expose elements corresponding to domain object, property, value, action, collection, collection column, and object-link concepts.
Each element will identify members by Causeway semantic member ID and will obtain generated GraphQL paths and state through the nearest object context.

This keeps custom pages readable and independent of GraphQL document syntax while retaining a direct correspondence with the rich grammar.

### Keep data ownership in object contexts

Read-only components will register semantic requirements and render observable context state.
They will not create independent GraphQL clients or issue uncoordinated object requests.
Collection components will use a context-owned secondary collection operation because collection content can be large and should not permanently expand the primary object projection.

### Select value renderers through a registry

A shared renderer registry will resolve a renderer using the introspected GraphQL output shape and semantic member descriptor.
Resolution will prefer an application registration, then a more specific Causeway or GraphQL type renderer, and finally an explicit unsupported-value renderer.

Standard renderers will cover supported scalar and enum values, null values, object references, and existing rich-schema LOB representations that are practical in read-only mode.
The renderer contract will receive semantic value state and will not receive raw responsibility for object lookup or GraphQL execution.

### Use light-DOM semantic hosts

Public custom elements will render accessible light-DOM content so application styles, design systems, HTMX composition, and framework tooling can operate across component boundaries.
Stable host classes, parts of the semantic DOM contract, and documented slots will permit styling and local composition.
The implementation may use internal helpers, but it will not require consumers to enter a component-specific Shadow DOM styling system.

### Publish semantic navigation and action requests

Object links will publish a bubbling, composed navigation event carrying a semantic target bookmark and source context.
Read-only action affordances will publish a bubbling, composed action-request event when enabled and activated.
They will not decide routing, open prompts, or invoke GraphQL operations in this change.

This allows plain HTML, the future HTMX viewer, or another host framework to choose navigation and interaction presentation.

### Treat collections as lazy secondary projections

A collection will initially register visibility and usability with its object context but will defer `get` until activated.
Without declared columns, object-valued rows will request identity and title metadata and render object links.
Declarative collection-column children will contribute row-member requirements to the collection query.

The collection result will hydrate row object contexts with the returned bookmark, metadata, and selected fields.
Those row contexts will reuse the parent GraphQL client and will query only requirements not present in their hydrated snapshots.

The current rich grammar returns a complete collection list, so paging and large-result policy remain explicit limitations and potential evidence for a later schema proposal.

### Make visibility and errors local

A hidden member will render no interactive or value content.
A disabled member will remain representable where useful and will expose its reason accessibly.
A member-path GraphQL error will be rendered by that member without replacing successful siblings.
Unsupported types will produce an explicit diagnostic placeholder in development behavior rather than silently disappearing.

## Risks / Trade-offs

- [Light DOM provides weaker style isolation] → Document stable host classes and keep generated markup narrow and semantic.
- [Renderer selection can become application-specific] → Specify deterministic precedence and expose registration rather than hard-coded application checks.
- [Collection reads can be large] → Load them only when activated, document the limitation, and defer paging until the rich grammar supplies a concrete contract.
- [Hydrated row contexts can become stale] → Retain object version metadata and let any row context perform a complete active-projection refresh.
- [Action affordances without invocation may feel incomplete] → Keep their event contract stable so the interaction change can add the standard prompt controller without changing page composition.
- [Frontend frameworks differ in custom-event handling] → Use bubbling and composed standard events and provide interoperability examples rather than framework-specific public wrappers.

## Migration Plan

This is additive to the foundation package.
The minimal foundation property element will evolve into the richer read-only contract while retaining its semantic `member` configuration.
No existing Causeway viewer is replaced or enabled by default.

## Open Questions

- Which initial Causeway value types beyond GraphQL scalars, enums, object references, Blob, and Clob are required for the first useful release?
- Which collection activation conventions should be standardized for tabs and collapsible regions without depending on a host framework?
- Which styling tokens should be public library contract rather than concerns of the later generic viewer theme?
