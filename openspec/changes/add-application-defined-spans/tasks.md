## 1. Public API

- [x] 1.1 Add `org.apache.causeway.applib.services.span.ApplicationSpanService` with synchronous `call(String, Callable<T>)` and `run(String, ThrowingRunnable)` operations that do not expose telemetry implementation types or declare checked exceptions.
- [x] 1.2 Document the 46-character suffix limit, static low-cardinality naming expectations, unchanged sneaky-throw exception semantics, inactive behavior, and the exclusion of asynchronous span lifecycles.
- [x] 1.3 Add the new service package to applib documentation and module exports where required by the existing module structure.

## 2. Naming and Runtime Implementation

- [x] 2.1 Correct `ActionInvocation` construction so declared and contributed mixin actions expose their domain-facing `Identifier`, while retaining the implementation identifier for invocation internals.
- [x] 2.2 Extend observation naming support with a 50-character application-span builder that formats canonical identities without action signatures, tries the full identity, then its namespace-free form, and finally truncates only the identity while preserving the complete suffix.
- [x] 2.3 Validate null, blank, and longer-than-46-character suffixes before supplied application work is invoked.
- [x] 2.4 Implement the runtime service using `InteractionService.currentInteraction()`, the current execution's domain-facing logical member identifier, and the existing `CausewayObservationIntegration` registry.
- [x] 2.5 Configure each application observation with stable name `causeway.application.span`, the suffix-preserving contextual name using one plain-space separator, `causeway.member.id` when available, and `causeway.application.span.suffix`.
- [x] 2.6 Register the implementation in runtime service composition and ensure nested spans use the current observation scope without adding an SDK, registry, sampler, or exporter.

## 3. Unit and Integration Tests

- [x] 3.1 Add naming tests for signature-free canonical identifiers, namespace compaction, suffix-preserving identifier truncation, fallback `app <suffix>` names, 46-character suffixes, exact 50-character boundaries, casing, and invalid suffixes.
- [x] 3.2 Add service tests proving callable return values, exactly-once runnable and callable execution, unchanged checked and unchecked failure behavior, error reporting, scope cleanup, and no-op registry behavior.
- [x] 3.3 Add interaction tests proving domain-facing `ActionInvocation` identity for declared and mixin actions, DTO and publishing compatibility, nested wrapper invocations, absent current executions, canonical metadata, and nested application-span parentage.
- [x] 3.4 Extend the tracing compatibility regression to assert that an exported application-defined span is a child of its action span, carries the expected stable metadata, and preserves its suffix.
- [x] 3.5 Verify the no-agent tracing regression still starts successfully and does not create an application-owned OpenTelemetry SDK or exporter.

## 4. Documentation and Verification

- [x] 4.1 Update the tracing operations guide with injection and usage examples, naming examples, profile behavior, cardinality guidance, and the prohibition on instance-specific suffixes.
- [x] 4.2 Run focused applib, core configuration, runtime services, and tracing compatibility tests on the supported Java and Maven toolchain.
- [x] 4.3 Run strict OpenSpec validation and confirm all application-defined-span scenarios are covered by automated tests or explicit documentation checks.
