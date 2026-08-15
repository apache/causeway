## Why

The rich GraphQL schema was designed to expose enough Causeway domain semantics for a generic user interface, but there is currently no framework-neutral browser foundation that turns that schema into reusable semantic web components.
A thin vertical slice centered on a shared object context will validate schema introspection, context propagation, and coordinated querying before the project commits to the broader component library and generic viewer.

## What Changes

- Introduce a framework-neutral GraphQL executor and targeted introspection model for the rich schema.
- Treat the existing rich-schema type and field naming grammar as the protocol for discovering domain objects, properties, collections, actions, parameters, and their supported semantics.
- Introduce a shared GraphQL client provider that owns transport and schema-description caching.
- Introduce `<causeway-object-context>` to represent one domain object and coordinate the semantic data requirements of descendant components.
- Coalesce active requirements into an evolving object read projection, while permitting lazy secondary operations for interaction-specific data.
- Introduce minimal object-header and read-only property components to demonstrate context discovery, state distribution, and one coordinated object read.
- Define framework-neutral loading, data, partial-error, invalidation, and context-request contracts for the vertical slice.
- Add a runnable same-origin `sample-html` Causeway application with a deterministic sample domain, the real rich GraphQL endpoint, packaged browser modules, and a plain-HTML component page.
- Add automated integration verification that the sample application starts and serves the page, ECMAScript modules, targeted introspection, and object reads.
- Give the sample page stable URLs and semantic selectors so later Playwright coverage can exercise the same application without redesigning the fixture.

## Capabilities

### New Capabilities

- `graphql-web-component-context`: Provides rich-schema introspection, shared GraphQL execution, semantic object contexts, coordinated read projections, and the minimal components needed to validate the context contract.

### Modified Capabilities

None.

## Impact

- Adds a new browser-facing web-component foundation and its build, packaging, and test infrastructure.
- Adds a non-deployed `sample-html` application module as the executable integration fixture for vanilla HTML and future browser automation.
- Consumes the existing GraphQL viewer through its public schema and endpoint rather than Causeway metamodel internals.
- Establishes public custom-element attributes, JavaScript properties, context propagation, state, and error contracts that later domain-member components and viewers will depend upon.
- Does not change the rich GraphQL schema in this change; any missing semantics discovered by the vertical slice will be proposed separately with concrete evidence.
