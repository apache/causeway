## 1. Browser Module and Contract Fixtures

- [x] 1.1 Establish the browser package/module layout, ECMAScript-module output, test runner, and Maven integration without requiring a host framework runtime.
- [x] 1.2 Capture representative rich-schema introspection and object-response fixtures for scalar properties, hidden and disabled state, partial errors, and object metadata.
- [x] 1.3 Define and document the public TypeScript or JavaScript types for GraphQL execution, semantic schema descriptions, object snapshots, requirement registrations, and observable context states.

## 2. Rich-Schema Discovery

- [x] 2.1 Implement and unit-test deterministic mapping between Causeway logical identifiers and generated rich-schema object and member type names.
- [x] 2.2 Implement targeted object-type introspection and reachable member-wrapper discovery using standard GraphQL introspection.
- [x] 2.3 Implement semantic classification of introspected properties, collections, actions, parameters, metadata fields, arguments, and output types.
- [x] 2.4 Implement diagnostics for missing object types, unsupported schema shapes, and unrecognized generated names.

## 3. GraphQL Client Provider

- [x] 3.1 Implement the replaceable GraphQL executor contract and default browser HTTP executor with cancellation support.
- [x] 3.2 Implement the client-scoped schema-description cache and deduplicate concurrent descriptions of the same generated type.
- [x] 3.3 Implement `<causeway-graphql-client>` with declarative endpoint configuration and structured executor injection.
- [x] 3.4 Test that several object contexts under one client share execution and schema-cache services while retaining independent object state.

## 4. Semantic Object Context

- [x] 4.1 Implement `<causeway-object-context>` identity validation and resolution of the nearest GraphQL client provider.
- [x] 4.2 Implement the bubbling and composed context-request protocol, direct context injection, and nearest-provider behavior for nested contexts.
- [x] 4.3 Implement semantic requirement registration, release, and translation into an internal structural GraphQL selection representation.
- [x] 4.4 Implement active-projection merge, deduplication, rendering-turn coalescing, and GraphQL object lookup document generation.
- [x] 4.5 Implement missing-selection delta loading, snapshot merge, requirement removal, invalidation, and complete active-projection refresh.
- [x] 4.6 Test that a header and several properties mounted in one rendering turn cause one coordinated initial object operation.

## 5. Observable State and Error Handling

- [x] 5.1 Implement immutable schema-loading, object-loading, ready, partial-error, and terminal-error context states.
- [x] 5.2 Implement request generations and cancellation so superseded responses cannot replace newer state.
- [x] 5.3 Implement GraphQL partial-data preservation and response-path mapping from member errors to semantic requirements.
- [x] 5.4 Test successful reads, missing objects, partial property failures, executor failures, cancellation, and out-of-order responses.

## 6. Minimal Semantic Components

- [x] 6.1 Implement `<causeway-object-header>` using semantic object metadata obtained from the nearest object context.
- [x] 6.2 Implement scalar read-only `<causeway-property member="…">` behavior for loading, visible, hidden, disabled, ready, and error states.
- [x] 6.3 Define and test the components' standard attributes, structured JavaScript properties, semantic custom events, and accessible rendered markup.
- [x] 6.4 Ensure component connect, disconnect, reconnect, and member-change lifecycles register and release context requirements correctly.

## 7. Demonstration and Verification

- [x] 7.1 Add a plain-HTML demonstrator that loads the ECMAScript modules and renders an object header and multiple properties against the rich GraphQL endpoint.
- [x] 7.2 Add integration coverage proving targeted introspection, schema-cache reuse, coordinated projection execution, and state distribution against representative GraphQL responses.
- [x] 7.3 Document package consumption, client and object context composition, executor injection, lifecycle behavior, and the deliberate exclusions of the foundation change.
- [x] 7.4 Run the browser package tests, relevant Maven module tests, formatting checks, and strict OpenSpec validation, and resolve all failures.

## 8. Executable Vanilla-HTML Sample Application

- [x] 8.1 Add `viewers/webcomponents/sample-html` to the Maven reactor as a non-deployed executable Causeway Spring Boot application with GraphQL, JPA/H2, and the foundation artifact.
- [x] 8.2 Implement a minimal deterministic JPA sample domain and startup data with a stable string identifier, title, version, visible property, hidden property, and disabled property semantics.
- [x] 8.3 Add `/sample-html/index.html` using the packaged ECMAScript modules, same-origin `/graphql`, semantic `data-testid` hooks, and an observable context-state readiness marker.
- [x] 8.4 Add random-port integration tests proving application startup and delivery of the sample HTML and packaged ECMAScript modules.
- [x] 8.5 Add integration tests proving targeted introspection and rich object lookup against the running endpoint and deterministic sample object.
- [x] 8.6 Document how to run the sample manually and the stable URL, data, selector, and readiness contracts reserved for future Playwright tests.
- [x] 8.7 Run the browser tests, sample integration tests, relevant Maven verification, formatting checks, and strict OpenSpec validation.
