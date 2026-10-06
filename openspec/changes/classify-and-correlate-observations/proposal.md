## Why

The observation foundation and CAUSEWAY-4096 now provide reliable scopes and explicit metadata policy, but operators cannot consistently distinguish foreground requests from background command jobs or locate a trace using a Causeway interaction ID. Forward-port roadmap step 2 so these searches work in both supported tracing configurations before adding semantic names and viewer detail.

## What Changes

- Classify existing HTTP entry spans as `foreground` and existing command-log Quartz job entry spans as `background`, without creating extra spans or inferring execution mode from usernames or thread names.
- Expose typed configuration `causeway.execution.mode.key`, default `causeway.execution.mode`, shared by both entry points; reject blank keys.
- Add high-cardinality `causeway.interaction.id` to the existing root interaction observation, using its actual UUID, including the effective command ID after background replay replaces the initial identifier.
- Support Boot-managed and agent-managed tracing using the existing trace owner; remain inert with the `observation` profile inactive or no current span.
- Extend export/lifecycle tests and the M3 how-to with concrete attribute locations, Jaeger searches, custom-key checks and an honest background-job example.

## Capabilities

### New Capabilities

- `observation-execution-classification`: Foreground/background entry-span classification, configurable attribute key, activation and trace-owner compatibility.
- `interaction-trace-correlation`: Root interaction UUID metadata, replay identity, and retained-trace lookup guidance.

### Modified Capabilities

None. Existing foundation and observation-policy requirements remain in force, including default identity omission, duration filtering policy and deterministic scope cleanup.

## Impact

Main modules: `core/config`, `core/webapp`, `core/runtimeservices` and potentially `core/metamodel` for interaction lifecycle binding; `extensions/core/commandlog/applib` for the job entry point; tracing compatibility tests; sample agent bridge and observability documentation. No new SDK/exporter, production persistence schema, dependency-version upgrade or span-name migration is intended.

Source provenance: CAUSEWAY-3975 commit `024bd33128d`, CAUSEWAY-4068 commits `0325232a3c7` and `3083a51eabd`. This is a main-side adaptation, not a cherry-pick. The main-side implementation uses the user-created `CAUSEWAY-4068-v4` branch under CAUSEWAY-4068.

## Non-goals

Semantic trace-root names, action/mixin identity changes, Wicket rendering regions, collection budgets, new application span APIs, automatic classification of arbitrary jobs/executors, MDC/baggage propagation, metric correlation labels, and creating job spans when no tracing instrumentation supplies one.
