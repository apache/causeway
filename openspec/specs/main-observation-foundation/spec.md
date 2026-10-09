## Purpose

Defines the observation lifecycle, opt-in activation, telemetry ownership and compatibility guarantees that support main-branch telemetry forward ports.
## Requirements
### Requirement: Observation ownership has deterministic cleanup

The lifecycle helper SHALL close each successfully opened scope and attempt to stop each owned started observation exactly once, in that order, and SHALL release references even when cleanup fails. It SHALL reject a second start while an observation is active.

#### Scenario: Repeated cleanup
- **WHEN** a started observation is closed and cleanup is called again
- **THEN** scope closure and observation stop are each invoked once and no closed observation remains current

#### Scenario: Scope cleanup fails
- **WHEN** closing the observation scope throws
- **THEN** observation stop is still attempted and subsequent cleanup does not repeat either operation

#### Scenario: Opening scope fails
- **WHEN** an observation starts successfully but opening its scope throws
- **THEN** the failure is recorded where possible, stop is attempted once, held state is cleared, and the original failure propagates

#### Scenario: Duplicate start
- **WHEN** a second observation is supplied while the helper still owns an active observation
- **THEN** the helper rejects the attempt without starting the second observation or losing the first

### Requirement: Failures remain attributable to application work

The observation lifecycle SHALL support recording any `Throwable`. Instrumented consumers SHALL preserve the original work failure if observation reporting or cleanup also fails, retaining secondary failures as suppressed where feasible. Existing tag and explicit discard operations SHALL remain available.

#### Scenario: Work throws an Error
- **WHEN** observed work throws an `Error`
- **THEN** that failure is reported to the observation and the same failure propagates after cleanup

#### Scenario: Cleanup also fails
- **WHEN** work throws and observation cleanup subsequently throws
- **THEN** the work failure remains the primary propagated failure and cleanup is not skipped

#### Scenario: Existing helper consumers
- **WHEN** an existing main consumer tags or explicitly discards an observation
- **THEN** those operations remain supported without introducing a second lifecycle helper

### Requirement: Causeway activation is independent of application observations

Causeway framework observations SHALL remain opt-in through the `observation` profile. Inactive Causeway instrumentation SHALL NOT disable an application's own observations. Active integration SHALL use the application's single or primary registry when supplied, otherwise a configured fallback. Unresolved multiple registries SHALL cause an actionable configuration error.

#### Scenario: Inactive profile with application registry
- **WHEN** the profile is inactive and an application supplies a working observation registry
- **THEN** Causeway emits no framework spans, application observations still work, and startup has no registry ambiguity introduced by Causeway

#### Scenario: Active profile with custom registry
- **WHEN** the profile is active and the application supplies one registry or designates a primary registry
- **THEN** Causeway uses that registry without creating a competing default registry

#### Scenario: No telemetry exporter
- **WHEN** the profile is active without a Java agent or configured exporter
- **THEN** application startup and observed work complete safely without requiring an exporter

#### Scenario: Ambiguous application registries
- **WHEN** the active configuration has multiple registry candidates and no primary selection
- **THEN** startup reports the ambiguous registry selection rather than choosing arbitrarily

### Requirement: Supported telemetry configurations have one trace owner

The implementation SHALL preserve a supported Boot-managed configuration and document a tested Java-agent-compatible configuration. Each configuration SHALL use a single SDK/export owner for the tested trace. The agent configuration SHALL join framework observations to automatic HTTP and JDBC context without duplicate framework export.

#### Scenario: Agent exports a framework request
- **WHEN** an HTTP request invokes a framework action that performs JDBC work under the documented agent configuration
- **THEN** collected spans show the expected HTTP-to-framework-to-JDBC ancestry in one trace and exactly one framework span for each expected invocation

#### Scenario: Boot-managed application
- **WHEN** a main application uses the documented Boot-managed tracing configuration without an agent
- **THEN** framework observations export through its configured tracing integration without requiring agent-specific wiring

#### Scenario: Failed request followed by successful request
- **WHEN** a failing observed request is followed by another request
- **THEN** the failure is recorded and the later request does not inherit stale observation state

### Requirement: Foundation reconciliation preserves main instrumentation

The change SHALL preserve main's existing interaction, action, property modification, transaction, execution-publishing and JPA instrumentation boundaries. It SHALL use main-compatible dependency and toolchain versions rather than maintenance's compatibility pins.

#### Scenario: Existing instrumented operations
- **WHEN** the foundation change is validated against the recorded main observation inventory
- **THEN** each existing instrumentation boundary is accounted for and focused regression coverage verifies affected paths

#### Scenario: Main compatibility fixture
- **WHEN** the tracing fixture runs on the target main toolchain
- **THEN** it validates production integration with bounded process waits and output draining, without imposing Boot 2.7 or Java 11 on main
