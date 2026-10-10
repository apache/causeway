# Semantic trace display naming validation

Validated on 2026-10-10 on `CAUSEWAY-4059-v4`, following proposal commit `a0a7c4bfb59` and collection observation archive `81047dc9830`.

## Main adaptations

- Maintenance `e17c3088ac2` uses the OpenTelemetry API's current span and maintenance servlet/runtime shapes. Main captures the entry through its existing single/primary Micrometer tracer at the Jakarta foreground filter, before security/viewer children, supporting Boot and agent ownership without another SDK, registry, tracing handler or exporter.
- Micrometer's receiver handler assigns a name at observation stop. A Boot ObservationFilter retains the selected contextual display after HTTP conventions refresh the captured entry context; stable operation names and HTTP key values stay intact. Agent entries use the captured span directly. This adaptation is recorded in design.md.
- Request-local nested scopes select action > prompt > view, first wins ties. Static identity uses existing case-preserving 50-character compaction and complete canonical action signatures/types. Closing removes state before touching the tracing bridge, preserving original failures and preventing worker reuse leaks; inert nested scopes shield an enclosing request.
- Main's InteractionHead.isCommandForMember supplies actual domain-facing command matching. Action identities retain declared parameters and contributed member names without rewriting physical mixin identifiers. Pass-through work returns before nomination; mixed-in association facets are excluded even when association metadata cannot resolve, and unmatched nested actions do not nominate.
- Wicket PAGE and ACTION_PROMPT descriptors nominate before coordinator admission, without additional model resolution. NONE detail and exhausted budgets suppress region spans independently of entry names. Arbitrary member subtrees do not fabricate a view nomination, and descriptors retain no live naming state.
- Documentation integrates expected Petclinic action/prompt/view outcomes into the existing M3 how-to and observability guide, preserving Boot-first then agent structure and existing launcher/environment overrides. No new configuration or applib API is introduced.

## Commands and evidence

Java 25.0.4-tem, Maven 3.9.13 and existing managed dependencies, including Micrometer tracing 1.8.0-M1 and OpenTelemetry Java agent 2.31.1. Maven uses the configured Java 25 JAVA_HOME and local cache.

```sh
mvn -o -pl core/config,core/webapp,core/runtimeservices,viewers/wicket/ui,viewers/wicket/ui-test,viewers/wicket/viewer \
  -Dtest=CausewaySemanticTraceNamerTest,CausewayTraceClassifierTest,CausewayForegroundTraceFilterTest,MemberObservationPolicyTest,MemberExecutorServiceDefaultTest,WicketRenderObservationTest,WicketObservationCoordinatorTest,WicketCollectionObservationTest,ActionPromptObservationTest,TelemetryRequestCycleTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install

mvn -o -pl core/runtimeservices \
  -Dtest=MemberObservationPolicyTest,MemberExecutorServiceDefaultTest \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install

mvn -o -f regressiontests/tracing-compatibility/pom.xml test

openspec validate port-semantic-trace-display-names --strict
```

The focused core/Wicket build passed 76 tests with no failures or skips: 14 configuration/classification/naming, 18 runtime member, 4 foreground filter, 37 Wicket UI and 3 viewer request lifecycle tests. The subsequent runtime build/install passed all 18 tests after explicit association-facet exclusion was added.

The complete tracing/JPA suite passed all 15 tests (13 tracing compatibility and 2 JPA policy tests). The new semantic fixture performs six real servlet requests per child JVM under Boot and agent ownership, for each of full detail, NONE detail and a one-span budget. It verifies final exported act/prompt/view displays, full canonical attributes, first-wins ties and action priority, unnamed plain/member-subtree requests, and original HTTP method/path/route/status metadata. A supplied remote trace/parent ID and distinct entry span IDs remain intact, and security children retain entry parentage without receiving entry-outcome attributes. Failed action processing is followed by plain/member-subtree requests on the same servlet worker. NONE detail produces no semantic rendering spans while names remain available.

The raw HTTP fixture client deliberately avoids Java-agent client instrumentation replacing the supplied remote parent. HTTP attribute assertions account for Boot's method/uri/status convention and the agent's OpenTelemetry semantic HTTP keys.

Both modified AsciiDoc pages rendered with the installed Asciidoctor core, including semantic naming content. Strict change validation and git diff --check passed.

## Limits

The semantic fixture uses actual servlet filters, the production runtime member service's eligibility/nomination path, real InteractionHead command matching and Wicket component callbacks. Domain dispatch, execution carrier/result and publishing collaborators are controlled test doubles; it does not establish a complete persistence-backed domain action invocation. The member-subtree case exercises the same render descriptor behavior as an Ajax-only region, while existing Wicket regressions cover actual Ajax callbacks. Contributed identity, association exclusion and cleanup/bridge failures have focused coverage. No interactive Petclinic browser verification or full repository reactor test run was performed. Naming supports synchronous work in the initial servlet dispatch and does not propagate mutable state to asynchronous continuations.

Implementation remains uncommitted after apply; archival and spec synchronization are subsequent workflow steps.
