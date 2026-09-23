## Automated Validation

Production compilation succeeded through `viewers/wicket/ui` with Java 25 producing Java 11 release output and tests skipped.

The focused suites were run in the Maven reactor with Java 25, `maven.compiler.release=11`, and `maven.compiler.proc=full`:

- `CausewayObservationNamingTest`: 7 tests, 0 failures, 0 errors, 0 skipped.
- `CausewayTracingObservationHandlerTest`: 3 tests, 0 failures, 0 errors, 0 skipped.
- `CausewayObservationConfigurationTest`: 3 tests, 0 failures, 0 errors, 0 skipped.
- `MemberExecutorServiceDefaultObservationTest`: 7 tests, 0 failures, 0 errors, 0 skipped.
- `WicketRenderObservationTest`: 15 tests, 0 failures, 0 errors, 0 skipped.

The tracing compatibility test was compiled in the Java 25 reactor while its application fixture was launched on the validated Java 11 and OpenTelemetry Java agent 1.31.0 baseline:

- `MicrometerTracingCompatibilityTest`: 2 tests, 0 failures, 0 errors, 0 skipped.

## Exported Trace Evidence

The Java-agent fixture exported one trace containing the automatic HTTP and JDBC spans together with the Causeway root interaction, page preparation, page render, prompt render, and action invocation spans.

The observed semantic hierarchy included:

[source,text]
----
causeway.root.interaction
├── prepare causeway.TracingFixture
├── render causeway.TracingFixture
│   ├── render fieldset identity
│   │   ├── render property emailAddress
│   │   └── render action executeJdbc
│   ├── render collection roles
│   └── prompt causeway.TracingFixture#executeJdbc
└── act causeway.TracingFixture#executeJdbc
    └── JDBC
----

The preparation and page-render spans exported `causeway.object.type=causeway.TracingFixture` and were consecutive children of the root interaction.
The prompt span exported `causeway.object.type=causeway.TracingFixture` and `causeway.action.id=causeway.TracingFixture#executeJdbc()`.
The action span exported the same complete domain-facing canonical identifier, and the automatic JDBC span remained a child of the action invocation.
The automatic HTTP entry span retained the Java-agent name `GET /trace` and the Causeway foreground classification.

The runtime focused suite verified `act` naming for declared and mixed-in actions, domain-facing `prop` and `coll` observations with canonical association attributes, and action fallback when an association cannot be recovered.

The Wicket focused suite additionally verified preparation/render ordering, pre-render child parentage, failure cleanup, no-op behavior, and preparation logical-type fallback.
It retained coverage that the enclosing action prompt receives one render behavior, individual parameter models remain excluded from property spans, descriptors remain serializable without active telemetry, prompt rendering retains page parentage, and the unnamed fieldset displays as `default`.
