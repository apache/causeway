# application-defined-spans Specification

## Purpose

Provide telemetry-independent synchronous application tracing regions with bounded names, current domain-facing identity and unchanged work, failure and inactive behavior.

## Requirements

### Requirement: Public synchronous application span service

Causeway SHALL provide injectable `org.apache.causeway.applib.services.span.ApplicationSpanService` with `call(String, Callable<T>)` and `run(String, ThrowingRunnable)` operations without exposing telemetry types in its public contract.
Supplied work SHALL run exactly once, and its return value SHALL be preserved unchanged.
Spans SHALL be synchronous and SHALL NOT outlive the service call.

#### Scenario: Returning and non-returning work

- **WHEN** application code invokes either service operation with a valid suffix
- **THEN** work runs once within an application observation and the scope closes afterward
- **AND** a callable's result, including null, is returned unchanged

### Requirement: Current domain-facing member identity

Application spans SHALL obtain identity from the current interaction's current execution, using the separate domain-facing identifier for action invocations and the existing logical identifier for other executions.
Canonical identity SHALL be `<logical-type-name>#<member-logical-name>` without parameter signatures.
The service MUST NOT change physical invocation identity or infer identity from callable classes, target classes or outer commands.

#### Scenario: Declared or contributed action

- **WHEN** an application span starts during a declared action or contributed mixin action
- **THEN** its name and `causeway.member.id` identify the current domain-facing action
- **AND** a mixin invocation retains its existing physical invoked-method identity independently

#### Scenario: Nested execution

- **WHEN** a nested current execution starts an application region
- **THEN** the region uses that execution rather than the outer action or command identity

#### Scenario: Missing execution identity

- **WHEN** no current execution identity is available
- **THEN** the display name is `app <suffix>` and no `causeway.member.id` is added

### Requirement: Stable operation and bounded suffix-preserving names

The default service SHALL use stable operation `causeway.application.span` and contextual name `<identity> <suffix>` with a maximum length of 50 characters.
It SHALL retain the full canonical member identity in `causeway.member.id` when available and the accepted suffix in `causeway.application.span.suffix`.
Suffixes MUST be non-null, nonblank and at most 46 characters; invalid suffixes SHALL be rejected before work runs, including when the default service uses a no-op registry.

#### Scenario: Full name fits

- **WHEN** full canonical identity, one space and suffix fit within 50 characters
- **THEN** the name preserves that full identity and suffix

#### Scenario: Namespace compaction is sufficient

- **WHEN** the full name exceeds 50 characters but the namespace-free identity and suffix fit
- **THEN** only the type namespace is removed from the displayed identity
- **AND** canonical metadata remains complete

#### Scenario: Identity truncation is needed

- **WHEN** the compact identity and suffix still exceed the limit
- **THEN** only the identity is truncated and the entire suffix remains after one space
- **AND** a 46-character suffix is accepted with or without a current execution

#### Scenario: Invalid suffix

- **WHEN** the default service receives a null, blank or overlong suffix
- **THEN** it rejects the suffix before invoking application work

### Requirement: Natural parentage and nesting

An application span SHALL inherit current observation context under either Boot or agent ownership.
Nested application spans SHALL restore the enclosing scope when they finish without replacing existing framework or automatic instrumentation.

#### Scenario: Nested phases and JDBC

- **WHEN** an observed action starts an outer application region and that region starts another region containing synchronous JDBC work
- **THEN** the outer region is below the action, the inner region is below the outer region and JDBC spans are below the inner region
- **AND** completion restores the previous current observations

### Requirement: Original failures and cleanup

The service SHALL record escaping checked exceptions, runtime exceptions and errors, close its scope and stop its observation, and rethrow the original throwable unchanged and unwrapped.
Checked exceptions SHALL NOT be declared in the public signatures, matching the interaction-service convention.
Cleanup or error-reporting failures MUST NOT replace an existing application failure.

#### Scenario: Work fails

- **WHEN** application work throws a checked exception, runtime exception or error
- **THEN** the identical throwable escapes after observation error recording and cleanup
- **AND** subsequent calls cannot inherit a stale application scope

#### Scenario: Telemetry cleanup also fails

- **WHEN** observation error reporting or cleanup fails while an application failure is already propagating
- **THEN** the application failure remains primary and secondary telemetry failures are suppressed on it

### Requirement: Safe inactive and standalone no-op behavior

With an inactive observation registry or no exporting tracing pipeline, the default service SHALL preserve work invocation, results and exceptions without exporting application spans or installing a tracing pipeline.
The public `NOOP` helper SHALL execute work directly without naming validation and without requiring a service container or observation infrastructure.

#### Scenario: Inactive default service

- **WHEN** application code uses a valid suffix with the default service under inactive observation
- **THEN** work runs once and results or identical failures are preserved without application spans

#### Scenario: Standalone no-op helper

- **WHEN** a unit test uses `ApplicationSpanService.NOOP`
- **THEN** callable and runnable work execute once with unchanged result and throwable behavior

### Requirement: Static private suffix metadata

The API SHALL document suffixes as static operation/phase identifiers and MUST warn against object identifiers, titles, bookmarks, arguments, record values, user/tenant identities or other instance-specific data in suffixes.
The service SHALL add no payload or identity data beyond static canonical member, suffix and framework bean/module metadata.

#### Scenario: Repeated application phase

- **WHEN** an application instruments repeated executions of one phase
- **THEN** it uses the same bounded static suffix rather than per-invocation values
