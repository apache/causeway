## 1. Typed Configuration

- [x] 1.1 Add the `CausewayConfiguration.Execution.Mode` hierarchy with `causeway.execution.mode` as the default key.
- [x] 1.2 Add configuration binding tests that verify the default and an application-provided `causeway.execution.mode.key` override.

## 2. Trace Classification

- [x] 2.1 Update `CausewayTraceClassifier` and its unit tests to write execution mode under a caller-provided attribute key while preserving bounded mode values and no-agent behavior.
- [x] 2.2 Inject the typed configuration into `CausewayForegroundTraceFilter`, use its execution-mode key, and update filter tests to verify key propagation.
- [x] 2.3 Update `RunBackgroundCommandsJob` to classify with the typed configuration key and extend its focused tests to verify key propagation.

## 3. Documentation and Verification

- [x] 3.1 Document `causeway.execution.mode.key`, its default, application reuse, and the operational effect of overriding it in the tracing guidance.
- [x] 3.2 Run the focused config, webapp, command-log, and tracing compatibility tests and resolve any regressions.
