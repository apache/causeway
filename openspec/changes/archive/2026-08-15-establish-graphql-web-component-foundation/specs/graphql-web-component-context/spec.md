## ADDED Requirements

### Requirement: Public rich-schema consumption
The web-component foundation SHALL obtain domain type descriptions and object state exclusively through the public rich GraphQL schema and GraphQL execution contract.

#### Scenario: Foundation describes a domain object
- **WHEN** a client requests a context for a Causeway logical type
- **THEN** the foundation uses GraphQL introspection to discover that type and its members
- **AND** it does not require a separate member-list endpoint or direct access to Causeway metamodel services

### Requirement: Rich-schema naming grammar
The web-component foundation SHALL provide a deterministic mapping between Causeway logical identifiers and the generated rich-schema object, property, collection, action, parameter, and metadata names.

#### Scenario: Member wrapper classification
- **WHEN** introspection returns generated wrapper types for an object
- **THEN** the foundation classifies each wrapper according to the rich-schema naming grammar
- **AND** exposes the result to components using semantic member kinds and identifiers

#### Scenario: Unrecognized generated type
- **WHEN** an introspected type does not conform to a supported rich-schema grammar rule
- **THEN** the foundation reports a diagnostic identifying the type and failed classification

### Requirement: Targeted schema introspection
The GraphQL client SHALL discover only the requested object type and the schema types reachable from the members required to describe it, rather than requiring complete-schema introspection.

#### Scenario: First object context of a type
- **WHEN** the first object context for a logical type is connected
- **THEN** the client introspects the generated object type and its reachable member wrapper types

#### Scenario: Repeated object type
- **WHEN** another object context uses a type already described by the same GraphQL client
- **THEN** the client reuses its cached schema description without repeating equivalent introspection requests

### Requirement: Replaceable GraphQL executor
The GraphQL client SHALL execute introspection and object operations through a replaceable framework-neutral executor contract.

#### Scenario: Default browser execution
- **WHEN** no executor is injected
- **THEN** the client uses the default browser executor and the standard GraphQL HTTP request and response shape

#### Scenario: Application-provided execution
- **WHEN** an application injects an executor
- **THEN** all GraphQL operations use that executor without changing descendant component APIs

### Requirement: Shared GraphQL client provider
The `<causeway-graphql-client>` element SHALL provide endpoint, execution, cancellation, schema-name, and schema-cache services to descendant Causeway contexts.

#### Scenario: Several object contexts share a client
- **WHEN** several object contexts are descendants of one GraphQL client provider
- **THEN** they share the provider's executor and schema-description cache
- **AND** retain independent object snapshots and active read projections

### Requirement: Semantic object context
The `<causeway-object-context>` element SHALL represent one domain object using its logical type name and identifier and SHALL expose a semantic context API to descendant components.

#### Scenario: Descendant requests context
- **WHEN** a descendant dispatches the standard bubbling and composed context-request event
- **THEN** the nearest object context supplies its semantic context API

#### Scenario: Nested object contexts
- **WHEN** a component is nested beneath more than one object context
- **THEN** its context request resolves to the nearest object context

#### Scenario: Missing object identity
- **WHEN** an object context lacks a logical type name or required object identifier
- **THEN** it enters a diagnostic error state without issuing an object query

### Requirement: Semantic read registration
The object context SHALL allow descendants to register and release semantic read requirements without exposing generated GraphQL names or GraphQL document construction to those descendants.

#### Scenario: Property requirement registration
- **WHEN** a property component registers a read requirement using a semantic member identifier
- **THEN** the context resolves the member through its schema description and adds the required visibility, usability, and value selections to its active projection

#### Scenario: Component disconnects
- **WHEN** a component releases its requirement or disconnects
- **THEN** the released requirement is omitted from subsequent complete projection refreshes
- **AND** the release alone does not cause a network request

### Requirement: Coordinated object read projection
The object context SHALL maintain the union of active semantic read requirements and coordinate them as an evolving GraphQL object projection.

#### Scenario: Initial component composition
- **WHEN** an object header and multiple property components register requirements during the same rendering turn
- **THEN** the context coalesces their selections into one initial object operation

#### Scenario: Newly active component
- **WHEN** a newly active component requires an object field absent from the current snapshot
- **THEN** the context loads the missing selection and merges the returned data into the object snapshot

#### Scenario: Complete refresh
- **WHEN** the object context is invalidated or explicitly refreshed
- **THEN** it executes the complete currently active projection
- **AND** distributes the refreshed snapshot to all active subscribers

### Requirement: Observable object state
The object context SHALL expose deterministic observable schema-loading, object-loading, ready, partial-error, and terminal-error states to descendant components.

#### Scenario: Successful object read
- **WHEN** a coordinated object operation returns without errors
- **THEN** the context publishes an immutable ready snapshot containing the returned metadata and member state

#### Scenario: Superseded response
- **WHEN** an older request completes after a newer request generation has become authoritative
- **THEN** the older response does not replace the newer context state

### Requirement: GraphQL partial-error preservation
The object context SHALL preserve successful GraphQL data when the same response also contains errors and SHALL associate path-addressable errors with the narrowest corresponding semantic requirement.

#### Scenario: One property fails
- **WHEN** a response contains valid object and property data plus an error whose path identifies another property
- **THEN** components for the successful data receive their normal state
- **AND** the failing property receives an error state derived from that path

#### Scenario: Object lookup fails
- **WHEN** an error prevents the requested object from being resolved
- **THEN** the object context publishes a terminal object error state with the GraphQL diagnostic available to consumers

### Requirement: Minimal context-validation components
The foundation SHALL provide a minimal object-header component and scalar read-only property component that consume the semantic object context.

#### Scenario: Object header rendering
- **WHEN** `<causeway-object-header>` is connected beneath a ready object context
- **THEN** it renders the object title and semantic identity obtained through the context

#### Scenario: Visible scalar property
- **WHEN** `<causeway-property>` identifies a visible scalar property
- **THEN** it renders the current value supplied by the context

#### Scenario: Hidden property
- **WHEN** the rich schema reports that a requested property is hidden for the current object and user
- **THEN** the property component does not render the property value

### Requirement: Framework-neutral consumption
The public foundation SHALL use web-platform custom elements, attributes, JavaScript properties, and semantic custom events without requiring HTMX or another host framework runtime.

#### Scenario: Vanilla HTML application
- **WHEN** the `sample-html` application loads its page using plain HTML and ECMAScript modules
- **THEN** its GraphQL client, object context, object header, and property components operate without HTMX, React, Vue, Svelte, or an equivalent host framework

#### Scenario: Structured service injection
- **WHEN** a host framework supplies an executor or context through a JavaScript property
- **THEN** the supplied service is used without requiring framework-specific public component APIs

### Requirement: Executable vanilla-HTML sample
The first vertical slice SHALL include a bootable `sample-html` Causeway application that consumes the packaged foundation artifact through a real rich GraphQL endpoint.

#### Scenario: Same-origin packaged consumption
- **WHEN** the sample application is started
- **THEN** `/sample-html/index.html`, the packaged ECMAScript modules, and `/graphql` are served from the same application origin
- **AND** the page loads the modules from the foundation artifact rather than from copied source files

#### Scenario: No frontend build runtime
- **WHEN** the sample page is built and served
- **THEN** it uses vanilla HTML, native custom elements, ECMAScript modules, and plain CSS without npm build tooling or a host frontend framework

### Requirement: Deterministic sample object
The sample application SHALL provide deterministic domain data suitable for repeatable component and endpoint verification.

#### Scenario: Stable sample identity
- **WHEN** the sample application starts with its normal or test configuration
- **THEN** a sample entity with a documented stable logical type and string identifier is available through the rich GraphQL object lookup

#### Scenario: Representative read semantics
- **WHEN** the stable sample entity is queried
- **THEN** its schema and data provide predictable title, version, visible property, hidden property, and disabled property semantics

### Requirement: Browser-automation contract
The sample application SHALL expose stable browser-facing hooks so browser automation can observe component readiness and semantic output without depending on incidental markup.

#### Scenario: Stable sample route and selectors
- **WHEN** automation opens `/sample-html/index.html`
- **THEN** the page exposes documented semantic `data-testid` selectors for the sample application, object context, object header, and rendered properties

#### Scenario: Observable readiness
- **WHEN** the object context changes state
- **THEN** the sample page reflects the semantic context state on a documented readiness marker that automation can wait for

### Requirement: Automated sample integration verification
The Maven build SHALL exercise the runnable sample application against its packaged page, modules, deterministic data, and real GraphQL endpoint.

#### Scenario: Application and resource smoke test
- **WHEN** the sample integration test starts the application on a random port
- **THEN** the sample HTML page and packaged ECMAScript entry module are served successfully

#### Scenario: Real GraphQL contract test
- **WHEN** the sample integration test calls the running `/graphql` endpoint
- **THEN** targeted standard introspection resolves the sample object's generated rich-schema types
- **AND** a rich-schema object lookup returns the deterministic object's metadata and property state
