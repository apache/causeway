## 1. Configuration and tracing bridge

- [x] 1.1 Use the shared fixed key `causeway.execution.mode`; remove the configuration property and its metadata/binding tests following review.
- [x] 1.2 Implement current-span classification using the single/primary Micrometer tracer, gated by Causeway observation activation; test foreground/background values, missing tracer/span, inactive profile, ambiguity and the fixed key for both values.
- [x] 1.3 Expose and reuse the existing tracer in the sample/fixture agent registry bridge; verify no second SDK, exporter or tracing handler is created.

## 2. Entry-point instrumentation

- [x] 2.1 Register a Jakarta foreground filter after HTTP tracing and before security/viewer observations; test Wicket, GraphQL/HTMX, REST and static paths, request failure, async/error dispatch and context restoration.
- [x] 2.2 Classify the existing span at command-log job entry before its paused check; test paused, successful and failed jobs, fixed key and missing entry span without changing retries or transaction behavior.

## 3. Root interaction correlation

- [x] 3.1 Bind high-cardinality `causeway.interaction.id` to the root interaction instance and refresh its effective UUID before observation stop; preserve existing root/nested names, identity policies and cleanup guarantees.
- [x] 3.2 Test deterministic ordinary UUIDs, nested/reused layers, real command DTO identifier replacement, failed work, cleanup failure and subsequent interactions on the same thread; verify no ID metric label or HTTP/descendant copy is introduced.

## 4. Exported compatibility evidence

- [x] 4.1 Extend the export fixture with actual servlet entry-span classification in Boot and agent modes, the fixed execution-mode key, root interaction correlation, failure followed by success and unchanged ancestry/no duplicate framework spans.
- [x] 4.2 Exercise command-log job classification with a supplied Boot entry span and agent Quartz instrumentation; assert effective replay UUID correlation and safe execution with no span.
- [x] 4.3 Run focused config/webapp/interaction/command-log tests, existing Wicket lifecycle regressions and the tracing fixture on main's supported toolchain; record exact commands, versions, results and remaining limitations in validation.md.

## 5. Operator guidance

- [x] 5.1 Extend the existing observability guidance and M3 how-to with entry-span classification, root-span UUID lookup, separate Jaeger searches; preserve default identity/filtering explanations and the current launcher structure.
- [x] 5.2 Provide a reproducible background-job verification example or fixture command with its tracing prerequisites; distinguish it from Petclinic's foreground walkthrough, document retention/sampling limits, and validate documentation rendering and configuration examples.

## 6. Automatic identity attributes (review follow-up)

- [x] 6.1 Remove username/tenancy configuration properties and always export nonempty user values as interaction span attributes; retain UUID/execution-mode placement and simplify filtering to one threshold (default `0ms`).
- [x] 6.2 Remove launcher scenarios and document the threshold environment override; verify unit/export tests, metadata removal and documentation rendering.
