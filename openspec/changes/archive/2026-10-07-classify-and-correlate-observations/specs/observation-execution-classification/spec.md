## ADDED Requirements

### Requirement: Foreground HTTP entry classification

With the observation profile active and a supported tracing bridge, Causeway SHALL annotate the existing HTTP entry span with execution mode `foreground`. It SHALL NOT create or rename spans, alter parentage, or tag a nested security/viewer span in place of the entry span. Classification SHALL cover framework servlet routes including static resources and SHALL NOT mean that an authenticated human initiated the request.

#### Scenario: Framework HTTP request
- **WHEN** a Wicket, GraphQL/HTMX, REST or static-resource request enters the filter under Boot-managed or agent-managed tracing
- **THEN** its exported HTTP entry span carries `foreground` under `causeway.execution.mode` and existing names and parentage remain intact

#### Scenario: Failure and redispatch
- **WHEN** request processing fails or undergoes async/error redispatch
- **THEN** classification preserves the failure and existing context lifecycle without creating spans or classifying an unrelated child

### Requirement: Background command job classification

Causeway SHALL annotate an existing current entry span at the beginning of `RunBackgroundCommandsJob.execute` with execution mode `background`, including paused jobs. It SHALL preserve scheduling, retries, callback and transaction semantics and SHALL NOT manufacture an entry span when none exists.

#### Scenario: Instrumented job invocation
- **WHEN** the command-log job starts with a recording entry span, whether paused or executing commands
- **THEN** that span carries `background` and job results and failures retain their existing behavior

#### Scenario: Job without tracing instrumentation
- **WHEN** the command-log job starts without a current span
- **THEN** classification is a no-op and execution proceeds without requiring an SDK or exporter

### Requirement: Fixed shared execution-mode key

Both classifiers SHALL use the fixed attribute key `causeway.execution.mode` and the bounded values `foreground` and `background`. The key SHALL NOT be configurable.

#### Scenario: Either entry point
- **WHEN** a servlet request or command-log job classifies a current span
- **THEN** it writes `foreground` or `background` respectively under `causeway.execution.mode`

### Requirement: Classification respects tracing ownership and activation

Classification SHALL use the configured single/primary tracing bridge and SHALL NOT create another SDK, exporter or scope. Inactive Causeway observation SHALL NOT mutate application spans. An absent tracer or current span SHALL be safe; ambiguous active tracer candidates SHALL require explicit resolution.

#### Scenario: Inactive profile
- **WHEN** application tracing is active but Causeway's observation profile is inactive
- **THEN** Causeway adds no execution-mode attribute and application tracing continues independently

#### Scenario: Agent bridge
- **WHEN** the documented agent setup exposes the same tracer used by its observation handler
- **THEN** classification reaches the agent entry span without duplicate framework export or a competing trace owner

#### Scenario: Missing or ambiguous tracer
- **WHEN** no tracer is available, or multiple active tracers have no primary
- **THEN** absence results in safe no-op classification, while ambiguity produces an actionable configuration error
