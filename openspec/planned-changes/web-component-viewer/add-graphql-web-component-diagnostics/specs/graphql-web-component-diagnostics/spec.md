## ADDED Requirements

### Requirement: Opt-in GraphQL diagnostic observation
The web-component foundation SHALL provide an opt-in observer contract for GraphQL operations executed through its shared executor without changing operation behavior or semantic results.

#### Scenario: Diagnostics are disabled
- **WHEN** a GraphQL client uses the default configuration
- **THEN** no diagnostic payload snapshots or records are produced
- **AND** existing executor, context, and component behavior remains unchanged

#### Scenario: Diagnostics are enabled
- **WHEN** an application configures a diagnostic observer
- **THEN** every executor operation publishes its diagnostic lifecycle through that observer
- **AND** observer failures do not fail or delay the GraphQL operation

### Requirement: Correlated operation lifecycle
Each observed GraphQL operation SHALL have one local correlation identifier and structured start and terminal lifecycle records.

#### Scenario: Operation succeeds
- **WHEN** an operation starts and returns a successful GraphQL response
- **THEN** the observer receives correlated start and success information with timestamps and elapsed duration

#### Scenario: Concurrent operations complete out of order
- **WHEN** multiple operations overlap and complete in a different order from submission
- **THEN** each completion remains associated with its own request through the correlation identifier
- **AND** the diagnostic store preserves deterministic start ordering

#### Scenario: Operation is cancelled or obsolete
- **WHEN** transport cancellation or a known higher-level generation makes an operation obsolete
- **THEN** its diagnostic outcome distinguishes cancellation or obsolescence from transport and GraphQL failure

### Requirement: GraphQL request diagnostics
A diagnostic request record SHALL expose the endpoint, operation name and kind, exact submitted GraphQL document, and detached redacted variables.

#### Scenario: Mutating action is invoked
- **WHEN** the context submits a top-level GraphQL mutation
- **THEN** the diagnostic record identifies the operation as a mutation
- **AND** shows the exact mutation document and structured target and argument variables after redaction

#### Scenario: Targeted introspection runs
- **WHEN** the client submits a targeted `__type` operation
- **THEN** the diagnostic record identifies the operation and document independently from object reads and interactions

### Requirement: GraphQL response and error diagnostics
A terminal diagnostic record SHALL expose detached redacted response data, GraphQL errors, HTTP outcome, transport error, and duration applicable to that operation.

#### Scenario: GraphQL returns partial data
- **WHEN** a response contains successful data and GraphQL errors
- **THEN** the diagnostic outcome is partial success
- **AND** retains both redacted data and structured error paths

#### Scenario: Transport fails
- **WHEN** the endpoint cannot return a GraphQL response
- **THEN** the diagnostic outcome records the transport failure without fabricating GraphQL data

### Requirement: Secure diagnostic snapshots
Diagnostic payloads SHALL be detached from live execution objects and SHALL apply redaction and truncation before publication, storage, rendering, copying, or export.

#### Scenario: Rich member is hidden
- **WHEN** a response wrapper reports `hidden: true` alongside a member value
- **THEN** the standard policy masks that value in every diagnostic representation

#### Scenario: Payload contains sensitive-looking keys
- **WHEN** variables or results contain keys covered by the configured sensitive-key policy
- **THEN** their values are replaced by the configured mask

#### Scenario: Payload exceeds configured limits
- **WHEN** a string, collection, subtree, or complete record exceeds its diagnostic limit
- **THEN** the detached diagnostic representation is truncated with an explicit marker
- **AND** the live GraphQL result remains complete

### Requirement: Bounded in-memory diagnostic store
The standard diagnostic store SHALL combine correlated lifecycle records, retain a configurable bounded number of operations, support subscriptions and filtering, and permit completed records to be cleared or exported.

#### Scenario: Retention bound is exceeded
- **WHEN** completed operation count exceeds the configured maximum
- **THEN** the oldest completed records are evicted first
- **AND** active operations remain represented until they reach a terminal outcome

#### Scenario: User clears diagnostics
- **WHEN** completed records are cleared while operations remain active
- **THEN** active GraphQL execution is not cancelled
- **AND** subsequent lifecycle updates remain valid

#### Scenario: Diagnostics are exported
- **WHEN** an application exports the current store
- **THEN** the export contains only the bounded redacted diagnostic representation

### Requirement: Framework-neutral diagnostic presentation
The library SHALL provide an optional `<causeway-graphql-diagnostics>` light-DOM element that consumes a compatible diagnostic source without requiring HTMX or another frontend framework.

#### Scenario: Operations are observed
- **WHEN** diagnostic records enter the configured source
- **THEN** the element renders operation name, kind, outcome, and duration summaries
- **AND** permits request, variables, response, and error details to be expanded

#### Scenario: User filters records
- **WHEN** the user selects an operation-kind or outcome filter
- **THEN** the element shows only matching records without discarding stored operations

#### Scenario: User copies or clears records
- **WHEN** the user copies, exports, or clears diagnostics
- **THEN** only redacted content is copied or exported
- **AND** the controls remain keyboard operable and accessibly labelled

### Requirement: Executable diagnostic sample
The vanilla-HTML sample SHALL demonstrate explicitly enabled GraphQL diagnostics against the packaged foundation and real same-origin endpoint while preserving its existing page, identity, readiness, and hidden-state contracts.

#### Scenario: Sample reaches ready state
- **WHEN** `/sample-html/index.html` reaches `data-state="ready"`
- **THEN** its diagnostic region can show targeted introspection, coordinated object reads, and secondary collection operations through stable hooks

#### Scenario: User performs interactions
- **WHEN** the user validates or updates a property or invokes a safe or mutating action
- **THEN** the diagnostic region shows the corresponding query or mutation and its terminal result
- **AND** semantic interaction diagnostics continue to operate independently

#### Scenario: Hidden sample member is returned internally
- **WHEN** a GraphQL response contains the sample wrapper whose semantic hidden state is true
- **THEN** neither diagnostic markup nor exported records contain the hidden value

### Requirement: Automated diagnostic verification
The build SHALL verify diagnostic lifecycle, security, retention, presentation, and real-endpoint correspondence with deterministic tests.

#### Scenario: Executor contract is tested
- **WHEN** fixture tests execute success, partial error, transport failure, cancellation, and concurrent operations
- **THEN** emitted records have deterministic correlation, classification, redaction, and ordering

#### Scenario: Real endpoint is tested
- **WHEN** random-port integration tests execute representative reads, safe actions, and mutations
- **THEN** diagnostic documents and outcomes correspond to the actual GraphQL operations and responses
- **AND** hidden and configured sensitive values remain absent

#### Scenario: Browser diagnostics are audited
- **WHEN** the sample diagnostic element is exercised at narrow and wide viewports with keyboard input
- **THEN** filtering, expansion, copy, clear, focus, and live announcements remain usable
- **AND** configured accessibility auditing reports no failures
