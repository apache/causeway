## ADDED Requirements

### Requirement: Agent tracing is available without application bridge code

Causeway SHALL supply a missing Micrometer tracer wrapping GlobalOpenTelemetry and a missing observation registry using that tracer when observation and agent profiles are active with the OTel bridge dependency. It SHALL NOT construct an SDK or exporter. A missing bridge dependency SHALL produce an actionable startup error. Agent attachment SHALL remain the application's responsibility.

#### Scenario: Attached agent observes framework work
- **WHEN** an application attaches the agent and activates observation,agent without custom bridge code
- **THEN** framework spans, identity/correlation attributes and entry classification join automatic HTTP and JDBC spans in one trace without duplicate framework export

#### Scenario: Optional dependency is absent in Boot mode
- **WHEN** the agent profile is inactive and the bridge dependency is absent
- **THEN** ordinary Causeway startup remains supported

#### Scenario: Required dependency is absent in agent mode
- **WHEN** both profiles are active without the bridge dependency
- **THEN** startup explains which dependency to add

### Requirement: Agent profile selects one trace owner

The agent profile SHALL automatically suppress competing Boot SDK, tracing exporter, tracing handler and WebMVC observation configuration. It SHALL preserve unrelated user exclusions and keep Micrometer metrics independent of agent-only OTEL environment settings. Boot-managed configuration SHALL remain unchanged without that profile.

#### Scenario: Agent tracing with Micrometer metrics
- **WHEN** the agent profile is active with OTEL_METRICS_EXPORTER=none
- **THEN** Boot does not create a competing trace pipeline and Micrometer metrics remain enabled by their application configuration

#### Scenario: Causeway observations are inactive
- **WHEN** only the agent profile is active
- **THEN** automatic agent HTTP/JDBC tracing remains available without Causeway framework spans

#### Scenario: Existing application exclusions
- **WHEN** an application already excludes another auto-configuration
- **THEN** that exclusion remains alongside the agent ownership exclusions

### Requirement: Application telemetry customization remains supported

The bridge SHALL back off from application-provided registry or tracer beans. The framework SHALL use single or primary candidates and reject ambiguous active candidates. Custom registry owners SHALL remain responsible for their observation handlers.

#### Scenario: Custom tracer
- **WHEN** an application supplies one tracer with both profiles active
- **THEN** the default agent registry and Causeway entry classifier share that tracer

#### Scenario: Custom registry
- **WHEN** an application supplies a registry with both profiles active
- **THEN** the bridge does not replace it or install additional handlers into it
