## 1. Add the Trace Classifier

- [ ] 1.1 Add `CausewayTraceClassifier` to the core-config observation package with the `causeway.execution.mode` key and enum-backed `foreground` and `background` values.
- [ ] 1.2 Implement current-span classification through the OpenTelemetry API without constructing an SDK, exporter, span, or scope.
- [ ] 1.3 Add focused tests for foreground and background attribute mutation, repeated classification, and safe no-agent behavior.

## 2. Classify Foreground HTTP Traces

- [ ] 2.1 Add a core-webapp servlet filter that marks the current Java-agent HTTP span as foreground before continuing the filter chain.
- [ ] 2.2 Register the filter for shared webapp paths through a `WebModuleAbstract` implementation and import it from `CausewayModuleCoreWebapp` without viewer-specific duplication.
- [ ] 2.3 Add filter and registration tests covering successful delegation, propagated failure, foreground attribute mutation, and no-agent behavior.

## 3. Classify Background Command Traces

- [ ] 3.1 Invoke the classifier at the start of the command-log extension's `RunBackgroundCommandsJob.execute(...)` before pause checks and polling.
- [ ] 3.2 Add focused job coverage proving background classification for paused execution and preserving existing job-control behavior.
- [ ] 3.3 Confirm that polling, command execution, listener callbacks, transaction behavior, and scheduling configuration remain unchanged.

## 4. Verify Agent Integration

- [ ] 4.1 Extend the Java 11 tracing compatibility fixture to register the foreground filter and retain the production HTTP-to-action-to-JDBC path.
- [ ] 4.2 Extend exported-span parsing and assertions to verify `causeway.execution.mode=foreground` on the agent-created HTTP entry span.
- [ ] 4.3 Verify that classifier usage remains safe with active observation but no Java agent and introduces no SDK or exporter dependency.

## 5. Document Filtering and Publication Behavior

- [ ] 5.1 Update the repository-local tracing operations guide with Jaeger logfmt searches for foreground and background execution modes and the distinction between discovery and volume reduction.
- [ ] 5.2 Document application-defined background-job usage and the transformed `IsisTraceClassifier` and `isis.execution.mode` identifiers consumed from maintenance artifacts.
- [ ] 5.3 Verify the Causeway-to-Isis publication transformation for the helper and attribute namespace without introducing hard-coded Isis identifiers into Causeway production source.
- [ ] 5.4 Run focused module tests, Java 11 real-agent regression coverage, dependency convergence, strict OpenSpec validation, and confirm no application-owned telemetry runtime was introduced.
