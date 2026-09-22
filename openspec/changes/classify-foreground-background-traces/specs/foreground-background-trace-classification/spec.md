## ADDED Requirements

### Requirement: Use a stable execution-mode attribute
The framework SHALL classify trace entry spans with the low-cardinality `causeway.execution.mode` attribute.
The only framework-defined values SHALL be `foreground` and `background`.
Classification MUST NOT add user, command, tenant, target, argument, result, or other instance-specific data.

#### Scenario: Inspect a classified source trace
- **WHEN** a supported foreground or background trace is exported from an unrenamed Causeway application
- **THEN** one entry span carries `causeway.execution.mode` with the corresponding bounded value

#### Scenario: Run without the Java agent
- **WHEN** classification executes without a valid current OpenTelemetry span
- **THEN** application behavior completes unchanged without requiring an SDK or exporter

### Requirement: Classify HTTP entry spans as foreground
The core webapp SHALL mark the current Java-agent-created HTTP server span with `causeway.execution.mode=foreground` before continuing request processing.
Classification SHALL apply from one shared servlet boundary across framework viewers and SHALL preserve filter-chain behavior on success and failure.

#### Scenario: Handle a Wicket request
- **WHEN** an agent-instrumented Wicket request enters the framework webapp
- **THEN** the HTTP entry span is classified as foreground and the complete request trace remains discoverable through that attribute

#### Scenario: Handle a non-Wicket HTTP request
- **WHEN** an agent-instrumented RESTful Objects, GraphQL, health, or other web request enters the framework webapp
- **THEN** the HTTP entry span is classified as foreground through the same shared filter

#### Scenario: Request processing fails
- **WHEN** downstream request processing throws after foreground classification
- **THEN** the original failure behavior is preserved and classification does not replace or hide the failure

### Requirement: Classify framework background-command entry spans
The command-log extension's `RunBackgroundCommandsJob` SHALL mark the current Java-agent-created Quartz span with `causeway.execution.mode=background` at job entry.
Classification SHALL occur before pause checks, polling, listener callbacks, or command execution so the complete job trace has a background-classified entry span.

#### Scenario: Poll with no pending commands
- **WHEN** the background-command Quartz job polls and finds no pending commands
- **THEN** its entry span is classified as background and can be excluded from foreground Jaeger searches

#### Scenario: Execute pending commands
- **WHEN** the background-command Quartz job executes one or more pending commands
- **THEN** its Quartz entry span is classified as background while semantic action and automatic JDBC descendants retain their existing parentage

#### Scenario: Execute while paused
- **WHEN** Quartz invokes the background-command job while its job control is paused
- **THEN** the Quartz entry span is still classified as background and the existing early return remains unchanged

### Requirement: Support application-defined background jobs
The framework SHALL provide a reusable core-config classification helper that marks the current span without exposing or constructing an OpenTelemetry SDK.
Applications with custom or copied background job implementations SHALL be able to invoke that helper at their own job entry point.

#### Scenario: Classify a custom Quartz job
- **WHEN** an application-owned Quartz job invokes the helper with background mode while the Java agent's Quartz span is current
- **THEN** the exported entry span carries the same bounded background classification as the framework command-log job

#### Scenario: Call the helper repeatedly
- **WHEN** the same current span is classified repeatedly with the same execution mode
- **THEN** classification remains idempotent and does not create additional spans or scopes

### Requirement: Preserve maintenance publication naming
Framework source SHALL use the Causeway-owned classifier and `causeway.execution.mode` namespace so the maintenance publication transform can rename them consistently to Isis identifiers.
The operational guidance SHALL identify the transformed `isis.execution.mode` key and helper usage for applications consuming renamed artifacts.

#### Scenario: Use a renamed maintenance artifact
- **WHEN** an application consumes the maintenance build after Causeway-to-Isis source transformation
- **THEN** foreground and background entry spans use `isis.execution.mode` with the same bounded values

### Requirement: Explain filtering and volume limitations
The operational guidance SHALL provide Jaeger logfmt searches for foreground and background traces using the execution-mode attribute.
It SHALL state that attribute filtering affects trace discovery but does not reduce collection, export, storage, or Jaeger memory consumption.

#### Scenario: Search foreground traces in Jaeger
- **WHEN** an operator searches with `causeway.execution.mode=foreground`, or `isis.execution.mode=foreground` for renamed artifacts
- **THEN** Jaeger returns traces containing the corresponding foreground-classified entry span

#### Scenario: Reduce telemetry volume
- **WHEN** an operator needs to reduce background trace ingestion or retention rather than only hide it from a search
- **THEN** the guidance directs the operator to sampling or collector-side processing instead of relying on the classification attribute alone
