## Context

The foundation change introduces the GraphQL client provider, semantic object context, coordinated read projection, object header, and a narrow scalar-property probe.
This change turns that probe into a coherent read-only component library suitable for declarative custom pages and later generic composition.
The components must preserve Causeway semantics while remaining consumable from plain HTML and frontend frameworks.
The existing `sample-html` application provides the executable same-origin fixture in which the complete read-only vocabulary will be demonstrated against the real rich GraphQL endpoint.

## Goals / Non-Goals

**Goals:**

- Define a stable semantic custom-element vocabulary for read-only domain pages.
- Render members according to rich-schema capabilities and instance state supplied by object contexts.
- Support pluggable value presentation without exposing GraphQL operation construction.
- Support object navigation and lazy read-only collections.
- Reuse hydrated GraphQL results across nested object contexts.
- Provide accessible and styleable framework-neutral markup.
- Validate the complete read-only composition through the packaged modules, deterministic data, stable hooks, and readiness contract of `sample-html`.
- Make `sample-html` an understandable page-specific reference showcase with representative scalar breadth, visual hierarchy, visible diagnostics, and accessible responsive styling.

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
Schema discovery will retain the foundation's GraphQL Java-compatible strategy of independent cached one-type introspection operations rather than repeating `__Type.fields` in a batched introspection document.

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

### Use `sample-html` as the executable acceptance fixture

The existing `/sample-html/index.html` page will evolve from its foundation probe into a representative custom read-only domain page without introducing HTMX, a frontend framework, or an npm build.
The existing logical type, persisted identifier, bookmark `s_sample-1`, object title, route, selectors, and `data-state` readiness behavior will remain stable.

The deterministic domain will add text, numeric, boolean, enum, null, and object-valued properties, stable related objects, visible enabled and disabled action semantics, a hidden action, a populated object collection with declared columns, and an empty collection.
Ordinary properties will expose enabled semantics so the deliberately disabled members remain clear demonstrations rather than making the entire page appear unavailable.
The page will use the new semantic components for those members and will expose additional stable `data-testid` hooks for values, object links, actions, collections, columns, semantic sections, collection diagnostics, and an event-diagnostic outlet.
Navigation and action-request events will be observed by plain application JavaScript and reported without imposing routing, prompts, or invocation.

Random-port integration tests will continue to verify the packaged page and ECMAScript modules, and will exercise targeted introspection plus deterministic object and collection reads against the running `/graphql` endpoint.
A manual real-browser smoke check will verify that the page reaches `ready`, renders the representative states, hides hidden members, and publishes semantic events without console failures.
Playwright remains deferred, but the expanded stable hooks will preserve a direct path to later automated browser coverage.

### Present the executable sample as a page-specific reference showcase

The sample will wrap the semantic components in a deliberate page shell with separate object-summary, property, action, collection, and event-diagnostic sections.
The shell will use page-specific plain CSS for responsive cards, spacing, typography, tables, focus treatment, and light and dark color schemes.
The presentation will remain an application theme layered over stable light-DOM classes and will not add display policy to the component library solely for this fixture.

The domain will add deterministic `summary`, `capacity`, and `featured` properties to demonstrate text, numeric, and boolean scalar rendering alongside the existing enum, null, and reference values.
The root domain object will allow ordinary property editing at the metamodel level so those values are not all reported as disabled, while `code`, `relatedObject`, and `archive` retain deliberate disabled semantics.
No editing control is introduced because the component slice remains read-only.

A visible coverage guide will name the semantics represented on the page and explain that the secret property and hidden action are intentionally absent.
The guide will never reproduce the secret value or replace the component state as the source of truth.
A prominent diagnostics panel will retain `[data-testid="sample-event"]`, show the latest semantic navigation or action request, and report the populated collection row count from collection state events.

New section and value hooks will be additive, while all previously documented selectors and `data-state="ready"` behavior remain stable.
This page-specific structure is not metadata-driven, reusable generic composition, routing, or an alternative viewer.

## Risks / Trade-offs

- [Light DOM provides weaker style isolation] → Document stable host classes and keep generated markup narrow and semantic.
- [Renderer selection can become application-specific] → Specify deterministic precedence and expose registration rather than hard-coded application checks.
- [Collection reads can be large] → Load them only when activated, document the limitation, and defer paging until the rich grammar supplies a concrete contract.
- [Hydrated row contexts can become stale] → Retain object version metadata and let any row context perform a complete active-projection refresh.
- [Action affordances without invocation may feel incomplete] → Keep their event contract stable so the interaction change can add the standard prompt controller without changing page composition.
- [Frontend frameworks differ in custom-event handling] → Use bubbling and composed standard events and provide interoperability examples rather than framework-specific public wrappers.
- [An acceptance fixture can grow into a second generic viewer] → Keep `sample-html` deterministic and page-specific, use it only to prove public component contracts, and defer routing and generic composition.
- [A richer sample theme can obscure the component contract] → Keep all domain values and semantic state inside public components, limit page JavaScript to diagnostics, and use only ordinary light-DOM CSS overrides.
- [Light and dark application colors can regress contrast] → Require automated accessibility auditing and keyboard smoke verification as final acceptance checks.

## Migration Plan

This is additive to the foundation package.
The minimal foundation property element will evolve into the richer read-only contract while retaining its semantic `member` configuration.
The existing `sample-html` route, root bookmark, selectors, and readiness behavior will remain valid while additive domain data and hooks exercise the new components.
No existing Causeway viewer is replaced or enabled by default.

## Open Questions

- Which initial Causeway value types beyond GraphQL scalars, enums, object references, Blob, and Clob are required for the first useful release?
- Which collection activation conventions should be standardized for tabs and collapsible regions without depending on a host framework?
- Which styling tokens should be public library contract rather than concerns of the later generic viewer theme?
