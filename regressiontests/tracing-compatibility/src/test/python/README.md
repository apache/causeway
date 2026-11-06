# Span-duration filtering investigation

These are isolated CAUSEWAY-4080 research tools, not a Python runtime dependency of Causeway or a deployable filter.
They do not alter the production observation/export pipeline.

## Reproduce from the repository root

Use the repository's supported Java toolchain; the recorded experiment used Java 25.0.4-tem and Python 3.14.7.
Python uses the standard library only.

```bash
set -e
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
OUT="$PWD/regressiontests/tracing-compatibility/target/span-duration-evaluation"

python3 -m unittest discover \
  -s regressiontests/tracing-compatibility/src/test/python -v

# Clear only this experiment's generated corpus so repeated runs are not combined.
rm -rf "$OUT/corpus"
mvn -B -f regressiontests/tracing-compatibility/pom.xml \
  -Dcauseway.tracing.evaluation=true \
  -Dcauseway.tracing.evaluation.capture-dir="$OUT/corpus" test

python3 regressiontests/tracing-compatibility/src/test/python/run_span_duration_evaluation.py \
  --captures "$OUT/corpus" --output "$OUT/results.json"
```

For a quick real-export smoke test, add `-Dtest=MicrometerTracingCompatibilityTest#durationEvaluationCapturesLateChildAndExternalParentInBothOwners` to Maven and use a separate capture directory.
The full run includes the existing Boot/agent compatibility and JPA policy tests, not just the new scenario.
Without the investigation system properties, the new scenario is disabled and no capture files are written.
These test-only properties are not supported Causeway configuration keys.

## Meaning of the results

- `span_duration_evaluation.py` contains the closed-graph oracle and bounded replay model.
- `test_span_duration_evaluation.py` checks retention, identity/metadata, uncertainty, bounded state and deliberately unsafe negative controls, using given/when/then sections.
- `fixtures/closed-reference.json` is a sanitized deterministic synthetic manifest with independent expected retention; it is not a real production trace.
- `run_span_duration_evaluation.py` checks the exported corpus and writes counts, byte measurements, workload timings and resource/control results to `results.json`.

The experimental threshold is 10 ms; it is not a production default.
The default stress preset allows eight pending traces, 256 spans and 1 MiB of serialized model data, with decision age/cache lifetime 100 replay-clock units and eight cached decisions.
The declared corpus preset allows 128 pending traces, 4,096 spans and 16 MiB, with 128 cached decisions and the same age/lifetime.
Units of the replay clock are logical experiment units, not exporter timing guarantees.

Real captures contain generated OTLP Span-message bytes in Base64, preserving retained span metadata without interpreting every instrumentation attribute.
They do not contain or re-export complete resource/scope/request envelopes.
Their observed manifests close the offline captured dataset, not a real distributed trace; baseline JUnit assertions and the explicit three-span async scenario provide independent fixture expectations.
The remote-parent scenario uses an actual tracer context with a controlled external parent, not a second remote service.
Out-of-order, late-arrival, eviction and exporter/backpressure limits are further exercised by deterministic replay models, not live Collector fault injection.

The byte comparison sums regenerated Span-message lengths at the receiver, excluding RPC/resource/scope overhead.
It is not a measurement of reduced application traffic, actual Collector export, backend storage or UI performance.
Boot captures do not automatically gain JDBC agent instrumentation: the corpus separately reports manual `fixture.jdbc` spans and agent-generated SQL-named spans.
UI projection and whole-trace sampling controls are models only, not integrations with a particular backend or Collector.

A timeout or pressure event in the conservative replay retains pending data and latches the session into pass-through.
A synchronous exporter/backpressure failure is counted as delivery failure, with no hidden/unbounded retry queue.
The sink is a test callback: these experiments do not prove a live blocked network exporter can be interrupted within a deadline.
Bounded pending/cache state does not prove bounded total process RSS, which also includes the caller's materialized graphs and serialization.

## Recommendation

**No-go for unrestricted live individual-span duration pruning under the existing contracts.**
A late useful descendant can arrive after its original parent has been pruned; neither a waiting window nor finite decision-cache state repairs that topology.
The closed-dataset oracle is conditionally feasible only when completion is genuinely established outside this model.
See the [evaluation report](../../../../../openspec/changes/general-span-duration-filtering/evaluation.adoc) for measurements, supported scope and production follow-up prerequisites.
Any production follow-up requires separate approval and a concrete completeness/eligibility contract or a separately scoped display-only/sampling goal.
