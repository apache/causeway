# Validation: metamodel introspection diagnostics forward port

## Provenance and environment

CAUSEWAY-4060 ports maintenance `fb28fa67e06` and the archived `diagnose-metamodel-introspection-recursion` design recoverable at `1a9910c3e77^`.
Planning commit: `218eca83517` on `CAUSEWAY-4060-v4`, stacked on primer-observation PR #3913 at `7c6f8015198`.
V4's relocated `spec.impl` loader, REGISTER/TYPE_ONLY/FULL requests, legacy state handler, experimental facade and existing phased/eager mixin bootstrap are retained.
No public state accessor, dependency, configuration or telemetry API was added, and 4061 prefiltering is excluded.

Validation on 2026-11-06 uses Java 25.0.4-tem, Maven 3.9.13 and the inherited Boot 4.2.0-RC1 baseline.
IntelliJ compilation was unavailable because this worktree is not an open IDE project; Maven compiled the affected modules.

## Commands

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -pl core/config,core/metamodel -am \
  -Dtest=CausewayBeanTypeRegistryMixinOrderTest,_IntrospectionDiagnosticsTest,SpecificationLoaderDiagnosticsTest,PrimingRegistryDefaultTest,PrimingRegistryDefaultObservationTest,ActionExecutorPrimingTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
mvn -B -f regressiontests/pom.xml -pl domainmodel -am \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true test
openspec validate port-metamodel-introspection-diagnostics --strict
git diff --check
```

The domain-model reactor command is required because the regression base module is not installed as a standalone artifact by this repository's build.
An initial direct domainmodel-POM invocation failed dependency resolution; rerunning with the base module in the reactor resolved it without code or dependency changes.

## Focused tests

The focused build/install passed thirty-seven tests without failures or skips: two registry-order tests, seven diagnostic helper tests, six loader tests and twenty-two existing priming/registry/executor regressions.

Helper coverage includes immutable ordered snapshots, requested/effective/substituted metadata, available/unavailable state, compact callers, same-error deduplication before unwind, distinct errors and subsequent independent chains, failing reporters/capture sinks, idempotent cleanup and concurrent per-thread isolation.
No real JVM stack exhaustion is manufactured; controlled StackOverflowError instances test identity and cleanup safely.

Loader tests invoke the actual common nullable-load method with controlled cached specs/state handlers.
They verify nested retained chains, identical original overflow/non-overflow failures, clean subsequent work, unchanged cache identity, mixin-phase request downgrade, substitutions, filtered/null paths, original TYPE_ONLY alias registration and passive legacy/facade state access without forced introspection.
Reporter preparation and cleanup calls are additionally guarded at the loader boundary because they can fail before entering a helper when stack space is scarce.

Registry tests vary insertion order across thirty seeded permutations, compare fully qualified class-name sequences, retain membership and check empty behavior.
No applicability prefilter or state-handler lock/transition code was changed.

## Real domain-model regression

The complete domain-model reactor suite passed all 120 tests without failures or skips.
The new representative test bootstraps two independent real Spring contexts over cyclic mixed-in collections and an action whose element type is the other view-model type, with command recording enabled.
Both bootstraps retain the synthetic collection-navigation actions, correct action element type, deterministic registry order and explicit zulu-before-alpha action sequence despite opposing class-name traversal order.
The existing good/bad-domain, editing, serialization, primitive lookup, annotation synthesis and general metamodel regressions remain passing.
Successful complete-suite logs contain no overflow-chain reports.
V4 is not required to reproduce maintenance's original overflow; controlled nested-loader tests establish failure reporting separately from the real graph's successful bootstrap behavior.

## Non-gating overhead sample

The helper test warms 2,000 scopes and times 20,000 enter/close operations without a capture sink or report.
Local samples were 39.7 ms (1,983 ns per scope) in the initial focused run and 91.9 ms (4,594 ns per scope) in the broader concurrent test run.
These measurements include caller/frame retention and cleanup but exclude actual introspection, mixin sorting and report formatting.
They are not a JMH/production benchmark, have no pass/fail threshold and do not establish a production latency guarantee.
Real bootstrap timings also vary with cold/warm state and concurrent contexts and are not used as performance assertions.

## Documentation and specification

The new navigable user guide page explains fields, passive state snapshots, best-effort original-error handling, thread isolation, deterministic traversal, resource limitations and investigation guidance.
It rendered with Asciidoctor 2.0.26 core/data from cached AsciidoctorJ 3.0.1 using safe-mode output `target/metamodel-introspection-diagnostics.html`.
Strict change validation and whitespace checks passed.
Four additive requirements preserve existing loading and ordering contracts.

## Limits

Real bootstrap tests exercise v4's current eager/phased metamodel path, not the original maintenance implementation or a production application's overflowing graph.
State snapshots are passive entry/transition-request snapshots and may be unavailable or stale relative to concurrent progress; they never force synchronization or introspection.
Very scarce stack/memory can prevent best-effort reporting, and same-named classes from different classloaders do not receive a new tie-breaking policy.
No full repository reactor, browser session or complete Antora site build was performed.
