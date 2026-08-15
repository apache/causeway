## Context

The GraphQL viewer already exposes Causeway domain objects through a rich schema whose object, property, collection, action, parameter, and metadata types encode the semantics needed by a generic user interface.
The repository has no browser component foundation that consumes that public contract, and no existing frontend package structure that constrains the implementation.
This change establishes a thin vertical slice before the broader domain-component library and generic HTMX viewer are built.
The public contracts must remain usable from plain HTML and interoperable with HTMX, React, Vue, Svelte, and other composition technologies.

## Goals / Non-Goals

**Goals:**

- Prove that targeted GraphQL introspection can describe a rich-schema domain object without introducing a duplicate member-metadata API.
- Provide a replaceable GraphQL executor and a shared schema cache.
- Provide a semantic object context identified by logical type name and object identifier.
- Let descendant components register semantic data requirements without constructing GraphQL documents.
- Coordinate registered requirements as one evolving object read projection.
- Demonstrate the contract through a minimal object header and read-only property component.
- Define stable framework-neutral context, state, loading, and error boundaries for later components.

**Non-Goals:**

- Property editing, validation, choices, autocomplete, defaults, or mutations.
- Action prompts or invocation.
- Collection rendering, paging, sorting, or filtering.
- Generic page composition, HTMX navigation, or Causeway layout interpretation.
- Framework-specific wrappers.
- A new GraphQL member-list or metamodel endpoint.
- Changes to the existing rich GraphQL schema.
- Persistent cross-session schema or object-state caching.

## Decisions

### Consume only the public rich GraphQL contract

The browser foundation will discover and read domain objects through GraphQL schema introspection and GraphQL operations.
It will not depend on Java metamodel services, GraphQL viewer implementation classes, or an HTML fragment backend with privileged metamodel access.

This constraint makes the vertical slice an effective test of the rich schema as an application protocol.
If a required semantic cannot be obtained from that protocol, the gap will be recorded and proposed separately rather than bypassed.

### Treat the generated rich-schema naming grammar as protocol

The client will deterministically map Causeway logical type and member identifiers to the existing generated GraphQL names and will classify introspected member wrapper types by that grammar.
The mapping and classification rules will be isolated in one schema-name module and covered by contract tests.

This is preferred over adding a parallel GraphQL member descriptor because the schema already contains the member graph, field descriptions, argument definitions, output types, and supported operation fields.

### Use targeted, reachable-type introspection

The client will introspect the requested rich object type and then the member wrapper types reachable from it.
It will not fetch the complete application schema merely to render one object.
Reachable type descriptions will be cached by GraphQL client instance and generated type name.

This limits startup payload and parsing cost for applications with large Causeway metamodels while still using standard GraphQL introspection.

### Separate the GraphQL client provider from object contexts

A shared `<causeway-graphql-client>` provider will own the endpoint, executor, schema cache, request cancellation, and schema-name grammar.
A `<causeway-object-context>` will own one object identity, the active semantic requirements below it, and the current object snapshot.

The separation permits several object contexts to reuse introspection results and permits applications to inject an executor that supplies authentication, tracing, or an existing networking stack.
The default executor will use browser platform facilities and the standard GraphQL HTTP request and response shape.

### Propagate context through a semantic DOM request

A descendant component will obtain its nearest object context using a bubbling, composed context-request event.
The provider will answer the request with a JavaScript context API, and direct property injection will remain available for tests and host-framework adapters.
Nested object contexts will stop the request at the nearest provider.

This avoids coupling the public API to a particular framework dependency-injection mechanism and works across ordinary custom-element composition and open shadow boundaries.

### Represent component needs as semantic requirements

Components will register requirements such as object header state or the visible value and usability of a named property.
They will not submit GraphQL strings or generated field paths.
Each registration will return a release mechanism so disconnected or inactive components no longer contribute to the active projection.

The context will translate requirements into an internal selection representation using the cached schema description.
Keeping this representation independent of string concatenation allows selections to be merged, deduplicated, compared, and tested structurally.

### Maintain one evolving coordinated read projection per object context

The object context will maintain the union of currently registered read requirements.
Registrations made during the same rendering turn will be coalesced before execution.
The context will compile the active projection into an object lookup operation and distribute the resulting member slices to all subscribers.

When a newly active component requires fields absent from the current snapshot, the context may fetch only the missing selection and merge it.
When the context is invalidated or explicitly refreshed, it will execute the complete active projection to restore a coherent snapshot.
Removing a requirement will shrink future refreshes but will not itself trigger a network request.

Interaction-specific operations such as validation, autocomplete, mutations, and large collection reads are intentionally outside the foundation and will later use secondary operations rather than continuously expanding the primary projection.

### Model asynchronous state and partial errors explicitly

The object context will publish immutable observable state with a monotonically increasing request generation.
Late responses from obsolete generations will not replace newer state.
GraphQL data and errors will both be retained, with path-addressable errors associated with the narrowest matching semantic requirement where possible.
A member-level failure will not discard unrelated successful member data.

The foundation will distinguish schema-loading, object-loading, ready, partial-error, and terminal-error states so components can render deterministic feedback.

### Validate the architecture with minimal semantic components

`<causeway-object-header>` will request and display the object's title and identity metadata.
A read-only `<causeway-property member="…">` will request visibility, usability, and the current value for a scalar property.
The demonstrator will mount a header and multiple properties below one object context and will prove that their initial requirements are represented by one coordinated object operation.

These components are deliberately narrow probes of the context contract rather than the complete component library.

### Keep public APIs framework-neutral

Public element configuration will use standard attributes for simple declarative values and JavaScript properties for structured values and injected services.
State changes and interaction outcomes will use standard `CustomEvent` instances with stable semantic names and payloads.
No public contract will expose HTMX attributes, framework component instances, or an implementation-specific reactive primitive.

The implementation may select build-time tooling, but consumers will receive standards-compliant custom elements and ECMAScript modules without a required host framework runtime.

## Risks / Trade-offs

- [Generated schema naming changes could break discovery] → Centralize the grammar, test it against representative generated schemas, and fail with a diagnostic that identifies the unrecognized type or field.
- [Targeted introspection may require several round trips] → Batch known reachable type names with aliases and cache results across object contexts.
- [Requirement registration may cause request churn during rendering] → Defer compilation until the current rendering turn has settled and cancel superseded requests.
- [Merged snapshots can contain fields read at different moments after delta loads] → Treat invalidation and explicit refresh as complete active-projection reads and expose object version metadata with the snapshot.
- [A coordinated query can become too large] → Keep interaction-specific and expensive collection operations outside the primary projection and allow inactive registrations to be released.
- [GraphQL partial errors can complicate component state] → Preserve data and errors separately and route errors using GraphQL response paths.
- [Authentication differs between host applications] → Put request execution behind an injected executor and retain no cross-user persistent object cache.
- [The initial components may over-constrain later visual design] → Keep them minimal, use semantic state contracts, and avoid committing the foundation to a design system or Shadow DOM styling policy.

## Migration Plan

This is additive and has no migration requirement for existing viewers or applications.
The foundation will be introduced as an opt-in browser package and demonstrator.
Removal or rollback consists of removing that package and its module registration without changing the existing GraphQL endpoint.

## Open Questions

- Which repository module and package publication arrangement best supports both Maven-hosted assets and direct ECMAScript-module consumption?
- Should the implementation use only browser custom-element primitives or a lightweight internal authoring library while preserving a dependency-free host contract?
- Which exact semantic custom-event names and structured state types should be frozen as public API in the first release?
