## 1. Browser Module and Contract Fixtures

- [ ] 1.1 Establish the browser package/module layout, ECMAScript-module output, test runner, and Maven integration without requiring a host framework runtime.
- [ ] 1.2 Capture representative rich-schema introspection and object-response fixtures for scalar properties, hidden and disabled state, partial errors, and object metadata.
- [ ] 1.3 Define and document the public TypeScript or JavaScript types for GraphQL execution, semantic schema descriptions, object snapshots, requirement registrations, and observable context states.

## 2. Rich-Schema Discovery

- [ ] 2.1 Implement and unit-test deterministic mapping between Causeway logical identifiers and generated rich-schema object and member type names.
- [ ] 2.2 Implement targeted object-type introspection and reachable member-wrapper discovery using standard GraphQL introspection.
- [ ] 2.3 Implement semantic classification of introspected properties, collections, actions, parameters, metadata fields, arguments, and output types.
- [ ] 2.4 Implement diagnostics for missing object types, unsupported schema shapes, and unrecognized generated names.

## 3. GraphQL Client Provider

- [ ] 3.1 Implement the replaceable GraphQL executor contract and default browser HTTP executor with cancellation support.
- [ ] 3.2 Implement the client-scoped schema-description cache and deduplicate concurrent descriptions of the same generated type.
- [ ] 3.3 Implement `<causeway-graphql-client>` with declarative endpoint configuration and structured executor injection.
- [ ] 3.4 Test that several object contexts under one client share execution and schema-cache services while retaining independent object state.

## 4. Semantic Object Context

- [ ] 4.1 Implement `<causeway-object-context>` identity validation and resolution of the nearest GraphQL client provider.
- [ ] 4.2 Implement the bubbling and composed context-request protocol, direct context injection, and nearest-provider behavior for nested contexts.
- [ ] 4.3 Implement semantic requirement registration, release, and translation into an internal structural GraphQL selection representation.
- [ ] 4.4 Implement active-projection merge, deduplication, rendering-turn coalescing, and GraphQL object lookup document generation.
- [ ] 4.5 Implement missing-selection delta loading, snapshot merge, requirement removal, invalidation, and complete active-projection refresh.
- [ ] 4.6 Test that a header and several properties mounted in one rendering turn cause one coordinated initial object operation.

## 5. Observable State and Error Handling

- [ ] 5.1 Implement immutable schema-loading, object-loading, ready, partial-error, and terminal-error context states.
- [ ] 5.2 Implement request generations and cancellation so superseded responses cannot replace newer state.
- [ ] 5.3 Implement GraphQL partial-data preservation and response-path mapping from member errors to semantic requirements.
- [ ] 5.4 Test successful reads, missing objects, partial property failures, executor failures, cancellation, and out-of-order responses.

## 6. Minimal Semantic Components

- [ ] 6.1 Implement `<causeway-object-header>` using semantic object metadata obtained from the nearest object context.
- [ ] 6.2 Implement scalar read-only `<causeway-property member="…">` behavior for loading, visible, hidden, disabled, ready, and error states.
- [ ] 6.3 Define and test the components' standard attributes, structured JavaScript properties, semantic custom events, and accessible rendered markup.
- [ ] 6.4 Ensure component connect, disconnect, reconnect, and member-change lifecycles register and release context requirements correctly.

## 7. Demonstration and Verification

- [ ] 7.1 Add a plain-HTML demonstrator that loads the ECMAScript modules and renders an object header and multiple properties against the rich GraphQL endpoint.
- [ ] 7.2 Add integration coverage proving targeted introspection, schema-cache reuse, coordinated projection execution, and state distribution against representative GraphQL responses.
- [ ] 7.3 Document package consumption, client and object context composition, executor injection, lifecycle behavior, and the deliberate exclusions of the foundation change.
- [ ] 7.4 Run the browser package tests, relevant Maven module tests, formatting checks, and strict OpenSpec validation, and resolve all failures.
