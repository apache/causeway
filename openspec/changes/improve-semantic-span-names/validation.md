## Automated Validation

Production compilation succeeded through `viewers/wicket/ui` with Java 11 release output and tests skipped.

The focused suites were run in the Maven reactor with Java 25, `maven.compiler.release=11`, and `maven.compiler.proc=full`:

- `CausewayObservationNamingTest`: 4 tests, 0 failures, 0 errors, 0 skipped.
- `MemberExecutorServiceDefaultObservationTest`: 3 tests, 0 failures, 0 errors, 0 skipped.
- `WicketRenderObservationTest`: 11 tests, 0 failures, 0 errors, 0 skipped.

The tracing compatibility fixture was compiled in the reactor and then run on its validated Java 11 and OpenTelemetry Java agent 1.31.0 baseline:

- `MicrometerTracingCompatibilityTest`: 2 tests, 0 failures, 0 errors, 0 skipped.

## Exported Trace Evidence

The Java-agent fixture exported one trace containing the automatic HTTP and JDBC spans together with the Causeway root interaction, page render, prompt render, and action invocation spans.

The observed semantic hierarchy included:

[source,text]
----
causeway.root.interaction
├── render tracing-fixture
│   └── prompt execute-jdbc on tracing-fixture
└── invoke execute-jdbc on tracing-fixture
    └── JDBC
----

The page span exported `causeway.object.type=causeway.TracingFixture`.
The prompt span exported `causeway.object.type=causeway.TracingFixture` and `causeway.action.id=causeway.TracingFixture#executeJdbc()`.
The action span exported the same canonical action identifier, and the JDBC span remained a child of the action invocation.

The Wicket focused suite additionally verified that the enclosing action prompt receives one render behavior, individual parameter models remain excluded from property spans, no-op rendering is unchanged, descriptors remain serializable without active telemetry, and prompt rendering retains page parentage.
