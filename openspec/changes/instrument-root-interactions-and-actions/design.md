## Context

Phase 2 introduced a qualified Causeway observation registry, profile-gated OpenTelemetry bridge wiring, consistent observation providers, and Java 11-compatible lifecycle support in `core/config`.
The real-agent regression test proved that observations created through this substrate share the Java agent's context and parent agent-generated JDBC spans.
No runtime service currently creates semantic Causeway observations, so traces still lack the framework-level relationship between an incoming request, its root interaction, and an invoked action.

Causeway 4 instruments interaction and member-execution services, but that implementation also reflects substantial interaction-model refactoring and broader coverage than is appropriate for this maintenance branch.
This change therefore backports the semantics into the existing Boot 2.7 service lifecycles without restructuring the interaction model.

## Goals / Non-Goals

**Goals:**

- Create exactly one semantic observation for each top-level Causeway interaction.
- Create one child observation around each action invocation.
- Preserve parentage from an agent-created HTTP span through the Causeway observations to agent-created JDBC work.
- Use stable observation names and bounded metadata that operators can understand and aggregate.
- Record failures and guarantee cleanup without changing application-visible exception behavior.
- Preserve default no-op behavior and Java 11 compatibility.

**Non-Goals:**

- Instrument nested interaction layers, property edits, transaction boundaries, Wicket request cycles, execution publishing, or persistence operations with additional semantic observations.
- Capture action arguments, return values, target identifiers, user names, tenancy tokens, or other application data.
- Refactor `InteractionLayer`, `CausewayInteraction`, or the public interaction APIs to carry observation state.
- Change Java-agent, SDK, exporter, sampling, or profile ownership established by the observation substrate.

## Decisions

### Observe only the existing top-level interaction boundary

`InteractionServiceDefault` will obtain an observation provider from `CausewayObservationIntegration` and start `causeway.root.interaction` only when opening the first interaction layer on the thread.
Nested layers and reused layers will participate in the current observation without creating another semantic span.

The root observation lifecycle will be retained beside the existing thread-local interaction stack and closed when that stack is reduced to zero.
This avoids changing the maintenance branch's interaction model solely to carry telemetry state.
The retained lifecycle state will be removed in the same defensive cleanup path as the interaction stack.

**Alternative considered:** create an observation for every interaction layer as Causeway 4 currently does.
This is rejected because the roadmap deliberately limits the initial semantic slice to root interactions and because authentication-layer nesting would create noisy spans whose semantics are harder to explain.

### Wrap the complete action invocation boundary

`MemberExecutorServiceDefault.invokeAction(...)` will create `causeway.action.invocation` before choosing pass-through or transactional execution and will execute the existing invocation logic through that observation.
This placement includes transaction selection, action execution, result handling, and propagated failure while preserving the existing `Try` and transaction behavior.

The action observation will naturally become a child of the current root-interaction observation through Micrometer's current trace context.
No explicit parent identifiers will be managed by Causeway.

**Alternative considered:** observe only the reflective domain method call.
This is rejected because it would omit meaningful framework work and failures performed by the action invocation boundary.

### Use stable names and explicit bounded tags

The observation names will be exactly `causeway.root.interaction` and `causeway.action.invocation`.
The action observation will include `causeway.action.id` containing the metamodel feature identifier and `causeway.execution.initiatedBy` containing the bounded `InteractionInitiatedBy` enum name.
Both services will retain the existing `causeway.bean` and normalized `causeway.module` metadata supplied by `CausewayObservationIntegration`.

The action identifier is modeled as an attribute instead of being interpolated into the span name so dashboards can aggregate all action spans by one stable operation name.
Arguments, return values, object identifiers, users, tenancy values, and locale context will not be captured.

**Alternative considered:** follow Causeway 4's action name format containing the feature identifier.
This is rejected for the backport because it creates one operation name per action and conflicts with the roadmap's stable low-cardinality naming requirement.

### Record failures without changing exception semantics

The root-interaction lifecycle will receive failures observed by the existing `callInternal(...)`, `runInternal(...)`, and root-close paths before those failures continue through the current rollback and rethrow behavior.
The action observation's `observe(...)` wrapper will record failures propagated through the existing `Try` or transaction call.
Cleanup will remain in `finally` paths, close scope before stopping, and tolerate defensive repeated cleanup.

Instrumentation MUST NOT swallow, replace, or newly throw application failures.

### Verify behavior at service and real-agent levels

Focused runtime-service tests will use a recording observation handler to verify names, tags, ordering, root-only behavior, action parentage, failures, and cleanup with the active registry.
Inactive-registry tests will verify that the same code paths execute without retained observation state.

The tracing compatibility harness will exercise the semantic names and nesting through the production observation substrate while attached to the real Java agent and will include an agent-instrumented HTTP entry and JDBC descendant.
The exported trace assertion will verify the ordered ancestry `HTTP → causeway.root.interaction → causeway.action.invocation → JDBC` without depending on generated trace or span identifiers and while allowing additional agent-created spans between those semantic boundaries.

## Risks / Trade-offs

- **[Risk] Thread-local observation state can leak when interaction cleanup fails.** → Clear and close it from the same unconditional stack-to-zero cleanup path that removes the interaction thread-local.
- **[Risk] A failure can be reported at both action and root boundaries.** → Accept recording on both semantic spans because each boundary failed, while ensuring each observation receives only its own lifecycle callbacks once.
- **[Risk] Action identifiers can reveal application model names.** → Treat metamodel feature identifiers as bounded operational metadata, document the choice, and exclude instance data and arguments.
- **[Risk] Constructor changes affect focused tests and manual service construction.** → Update all call sites and keep observation optionality inside the injected integration rather than adding optional lookups.
- **[Trade-off] Root-only instrumentation does not expose nested authentication layers.** → Prefer a concise initial trace and defer nested semantics until operational experience justifies them.
- **[Trade-off] Stable span names differ from Causeway 4's current action-name formatting.** → Favor low-cardinality operation names for this maintenance backport while retaining compatible module and initiation metadata.

## Migration Plan

1. Add focused observation behavior tests around the two runtime services.
2. Inject the existing observation integration and instrument root interaction lifecycle paths.
3. Instrument action invocation with stable names and bounded tags.
4. Extend the real-agent regression test to assert the complete semantic ancestry.
5. Run runtime-service, core configuration, dependency-convergence, and Java 11 real-agent verification.

Observation remains inactive unless the existing `observation` profile is enabled.
Rollback removes the two runtime-service integrations and their tests without changing application data, public APIs, or deployment configuration.

## Open Questions

None.
