## Context

This is the first forward-port change associated with CAUSEWAY-3975. Analysis used maintenance merge `7e799dfd360` (#3813) and recorded `origin/main` at `351eae721d9`. Local `main` was 513 commits behind that remote-tracking ref. These are inspected snapshots, not a claim that the remote was freshly fetched.

Main uses Boot `4.2.0-M1` in the inspected BOM, a commons-level `ObservationClosure`, Boot observation auto-configuration, and its own interaction carrier/layer lifecycle. Maintenance uses Boot 2.7, a core-config closure, and a dedicated observation registry bridged to the Java agent. Main additionally observes property modification, transactions, execution publishing and JPA operations. A replacement with maintenance classes would lose behavior and undo main's architecture.

Confirmed source findings:

- Main's closure does not clear its observation on close; a second close invokes stop again. A throwing scope close prevents stop.
- Main's error method accepts `Exception`; maintenance's accepts `Throwable`.
- Main's threshold wrapper returns the delegate from fluent methods and start. This permits callers to escape the wrapper; the effect on existing call sites needs characterization.
- Main's inactive configuration supplies an unqualified no-op registry while the integration injects an optional registry. Application-registry coexistence needs explicit tests rather than assuming it is safe.

## Goals / Non-Goals

**Goals:** deterministic lifecycle cleanup, explicit opt-in semantics, compatibility with main's existing Boot-managed setup, proven agent context integration, and a reusable main-compatible regression fixture.

**Non-Goals:** wholesale migration to maintenance's registry architecture; new semantic spans or applib APIs; new naming/privacy defaults; Wicket span budgets; asynchronous scope transfer; copying Boot 2.7 or Java 11 constraints.

## Decisions

### 1. Implement against main, with an initial baseline checkpoint

Before implementation, refresh the target refs, inspect target instructions, and record the actual base SHA and BOM/toolchain versions. Use a main-based branch/worktree. Transfer these proposal artifacts there; do not apply production tasks in this maintenance tree. Reconcile any intervening changes in the affected classes and existing specs first.

A fresh branch is preferred to replaying the maintenance merge, whose contents include history pruning and unrelated workspace files. Commit proposal artifacts before `/opsx-apply`, as required by repository instructions.

### 2. Keep one lifecycle helper in main's existing location

Adapt main's `commons/internal/observation/ObservationClosure`, retaining tagging/discard APIs and consumers. Do not introduce a second core-config helper. Clear held state before cleanup, close the scope before stopping the observation, and attempt stop even when scope cleanup fails. Repeated cleanup must not repeat either operation. Reject starting another observation while one is owned; clean up an observation already started if opening its scope fails.

Accept `Throwable` and preserve the original work failure when reporting or cleanup also fails, attaching secondary failures as suppressed where feasible. The helper alone cannot preserve an application failure unless the caller supplies it, so inspect and adapt affected catch/finally consumers too. Preserve an existing `Exception` overload if required for binary linkage rather than silently removing it.

Maintenance provides useful behavior and tests, but its synchronized methods do not make thread-local observation scopes transferable between threads. Cross-thread closing is not introduced by this change.

### 3. Retain Boot-managed wiring by default and prove an agent-compatible configuration

The `observation` profile gates Causeway instrumentation independently of whether an application has its own observation registry. The inactive path must use a no-op Causeway integration without disabling application observations or introducing an ambiguous registry injection. With the profile active, honor a single application-supplied registry or its Spring primary selection; use a fallback only when none exists. Multiple unqualified candidates must produce an actionable configuration failure rather than arbitrary selection.

Keep main's existing Boot configuration as the starting point. Establish two documented configurations in the fixture: the normal Boot-managed tracing setup and a Java-agent setup where framework observations join the agent-owned context. Each configuration must have one exporter/SDK owner for the trace under test. Do not activate independent Boot and agent pipelines and accept disconnected or duplicate traces.

First test the actual target dependency set. If agent compatibility requires a bridge or auto-configuration exclusions, isolate that adaptation behind an explicit configuration and document it; do not make a dedicated agent registry the default as a side effect. If compatibility requires a broader ownership redesign, record the failed evidence and revise this design before expanding implementation scope. Successful agent compatibility remains an exit criterion, not an assumed property of Boot.

### 4. Characterize and repair threshold composition

Exercise the wrapper directly and through the production JPA observation path, including fluent customization and use with `ObservationClosure`. Ensure methods representing mutation/start retain wrapper identity so its stop policy is applied. Successful spans below the existing threshold remain discardable; failures must not be discarded solely because they completed quickly. Preserve the current threshold value in this change.

Test that existing discard/export integration actually enforces the marker for each supported configuration. An exporter predicate registered only in Spring must not be assumed to control an agent-owned exporter. If the agent configuration cannot honor duration discarding without a broader pipeline change, explicitly disable that optimization for that configuration and document that spans are retained; do not claim a marker guarantees suppression.

Duration filtering can leave exported descendants whose parent is absent. Record that limitation and defer a different sampling/volume policy to the metadata/policy follow-up. Wicket's later admission budget is a different mechanism and must not be implemented here.

### 5. Carry evidence, not maintenance dependency pins

Adapt `regressiontests/tracing-compatibility` as a child-process fixture using main's supported Java version and BOM. Preserve bounded startup/shutdown, asynchronous output draining, and useful failure diagnostics from maintenance follow-ups `90512b829c1`, `769f13af9a5`, and `523117e7de4`, without carrying the Java 11 pin.

Use a local OTLP receiver and deterministic sampling for assertions. Prove an inbound HTTP span, framework interaction/action descendants, and JDBC descendants share the expected trace and parent IDs, with one framework span per invocation. Also test profile off, no agent/no exporter, and failure cleanup. Unit tests cover lifecycle fault injection that would be difficult to force reliably through a process fixture.

Inventory existing observation sites before edits and account for them afterward. Attach focused regression evidence for interaction nesting, property changes, transactions and publishing so maintenance's smaller instrumentation surface cannot become an accidental replacement.

## Risks / Trade-offs

- [Target drift] → Refresh and record the main baseline before implementation; regenerate specs if a matching capability now exists.
- [Boot and agent have different ownership contracts] → Prove both documented configurations and keep any agent adaptation explicit.
- [Discard predicates may not control agent export] → Assert exported output, and document retained spans where filtering is unsupported.
- [Cleanup exceptions can mask application failures] → Test primary/suppressed exception precedence at helper and consumer boundaries.
- [More error spans are exported] → Document the deliberate exception to duration suppression.
- [Scope expands into privacy or naming redesign] → Track those changes separately in the roadmap.

## Migration Plan

No data migration is required. Deliver the change as a main-targeted PR with focused tests, fixture evidence and configuration guidance. Preserve existing opt-in activation and normal Boot-managed application setup. Document any explicit agent-mode settings separately. Rollback is a code/configuration revert; no persisted schema or application API migration is intended.

## Open Questions

- Which exact Boot/agent versions will be present on the refreshed target, and what wiring joins their context? Resolve in the first compatibility tasks using the target dependencies.
- Does the current threshold wrapper fail through existing call sites, or only through permitted fluent composition? Characterize before modifying it.
- Does agent-mode export support the existing discard contract? If not, document the retained-span behavior specified above.

These are bounded implementation investigations. They do not authorize switching the default telemetry owner without updating this proposal.

## Implementation decisions confirmed

The refreshed main baseline remained `351eae721d9`; implementation is on `CAUSEWAY-3975-observation-foundation`. The framework continues to use Boot-managed tracing by default. Agent mode is an explicit application registry using the BOM-managed OtelTracer bridge and documented Boot SDK/tracing exclusions; no production agent-specific registry or dependency was necessary.

`causeway.observation.duration-filtering-enabled` defaults to true and is false in the validated agent configuration. The integration retains its existing registry constructor and exposes a threshold factory used by JPA. This controls framework duration filtering without attempting to control the agent's exporter with Spring predicates.

Consumer inspection identified a shared transaction closure reused across transaction-manager iterations. Each iteration now owns a closure, and cleanup closes them in reverse order. This is observation ownership correction, not a change to main's documented limitations on multiple transaction managers. Interaction layers retain their closure as an interaction attribute to report Throwable failures before closing; work exceptions remain primary when cleanup also fails.

The task list's branch/commit item was converted into a workflow checkpoint rather than counted as implementation work, following the apply skill. See validation.md for the instrumentation inventory and test evidence.
