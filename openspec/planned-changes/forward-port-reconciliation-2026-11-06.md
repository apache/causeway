# Maintenance-to-v4 forward-port reconciliation

## Scope and cutoffs

Audit performed on 2026-11-06 without implementing further ports.
The maintenance worktree `../ecp` and refreshed `origin/CAUSEWAY-4078` both resolve to `2bc5478983b`; their ahead/behind count is zero.
The destination checkout is `CAUSEWAY-4061-v4` at `a5365dad4fc`, including the still-open PR #3916 and its readability follow-up.
Refreshed `origin/main` resolves to `a221b2c00b4`; the verification branch is therefore distinguished from already-merged main.

The source interval is `9b8b66d5b7b^..2bc5478983b`, starting immediately before the maintenance observation foundation.
It contains 84 commits, including three merges whose remerge diffs contain no additional resolution changes.
All non-merge commit subjects and changed-file inventories were classified, including later work outside the original observability roadmap.
This is a bounded audit of that source lineage, not a claim about every remote branch or future maintenance commit.

## Findings

1. No additional production gap was identified in the original observability/priming/metamodel roadmap.
All 23 relevant production-touching maintenance commits are named by the roadmap's source anchors, and all fourteen corresponding archived change task lists have zero unchecked tasks and recorded validation evidence.
2. Three later tracing-harness commits require explicit classification rather than being mistaken for missing production features; see below.
3. CAUSEWAY-4070 domain-object locking, outside the original roadmap, is already ported and archived in v4, including its approval corrections.
4. CAUSEWAY-4078 transparent-SSO original-destination handling is a concrete additional forward-port candidate and is not present in the inspected v4 sign-in-page path.

The roadmap has fifteen completed/covered rows because it also includes the main-specific CAUSEWAY-4096 observation-policy work.
Mapping a source commit is not proof of byte-identical implementation: v4 intentionally retains its different observation ownership, interaction model, phased loader and Wicket structure.

## Original production-source coverage

| Area | Maintenance production anchors | Destination evidence |
|---|---|---|
| Foundation | `9b8b66d5b7b`, `e6752f882b5` | `reconcile-main-observation-foundation`; Boot/agent ownership, shared integration and robust closure |
| Classification/correlation | `024bd33128d`, `0325232a3c7`, `3083a51eabd` | `classify-and-correlate-observations`; foreground filter/classifier and interaction identity |
| Current action context | `6d037db6609` | `port-current-action-context`; current action/rule-checking state |
| Semantic member identity | `a2ae5877c99`, `d5ecd49a1f3`, `45e01ff6f42` | `port-semantic-member-names`; canonical domain-facing names |
| Wicket regions/preparation | `a8264fd1f9b` and semantic-member portions | `port-wicket-region-observations`; v4 page/member observation hooks |
| Collection detail/budgets | `8e5eaab6145`, `036a28a23c2`, `5a1dc2dd17b` | `port-wicket-collection-observations`; row/cell/action detail and volume controls |
| Trace display names | `e17c3088ac2` | `port-semantic-trace-display-names`; foreground nominations and naming ownership |
| Audit writing | `7961910a089` | `port-audit-trail-observations`; subscriber callback boundary |
| Entity-change evaluation | `02786054574` | `port-entity-change-evaluation-observations`; evaluation boundary |
| Application spans | `ea32b739d0f`, `5da64c34b57`, `f66b8f17a89` | `port-application-defined-spans`; no-op service and preserved physical invoked identity |
| Priming hooks | `19d2a36d2f9` | `port-application-priming-hooks`; effective action arguments and authorized request-local view hooks |
| Primer observations | `473454f2447` | `port-application-primer-observations`; per-callback observation boundaries |
| Introspection diagnostics | `fb28fa67e06` | `port-metamodel-introspection-diagnostics`; retained chains and stable traversal |
| Mixin prefilter | `680ac40ef52` | `verify-v4-mixin-prefiltering`; existing eager index verified rather than redundantly rewritten |

Archive links and exact validation records are indexed by the [roadmap](otel-forward-port-roadmap.md).
The 23 commits touch 68 distinct production/build paths; fifty retain their source path in v4 and eighteen require explicit relocation or architectural mapping.

Important mappings checked include:

- Maintenance observation configuration/custom tracing handler → v4 `CausewayObservationAutoConfiguration`, `CausewayAgentObservationAutoConfiguration` and shared `CausewayObservationIntegration`, not a second tracing owner.
- Maintenance namespace marker → v4's existing `org.apache.causeway` agent tracer scope, without introducing a duplicate namespace class.
- `ObservationClosure` → `commons.internal.observation.ObservationClosure`.
- `session.InteractionServiceDefault` → `ia.InteractionServiceDefault`.
- Maintenance web classification module → `WebObservationConfiguration` and `CausewayForegroundTraceFilter`.
- `specloader.SpecificationLoaderDefault` and diagnostic helper → `spec.impl`.
- `ObjectSpecificationAbstract` per-target scan → existing phased bootstrap, `MixinSpecStreamerEager` and `MixedInMemberFactory`.
- `EntityPage`/view guard → `pages.obj.DomainObjectPage`/`WicketViewPrimingGuard`.
- `EntityCollectionPanel` → `ParentedCollectionPanel` observation/preparation hooks.
- Scalar-panel-specific hooks → `AttributePanel` with `WicketRenderObservationPolicy.propertyDescriptor`.
- `WicketPreparationObservation` → `WicketPagePreparationObservation`.
- Ajax table, collection presentation, action-column and property-group classes → their relocated v4 packages.

## Tracing-harness follow-ups

| Source commit | Purpose | V4 assessment |
|---|---|---|
| `90512b829c1` | Run the Boot 2.7 fixture on validated Java 11 independently of the build JDK | Version-specific, not a missing v4 runtime feature; the v4 fixture uses the current build JVM and passed on Java 25. No Java-11 fixture override should be copied blindly into Boot 4. |
| `769f13af9a5` | Avoid blocking on a child whose output pipe is full | Equivalent protection exists through a dedicated concurrent output-drainer thread writing a log file before the parent waits. |
| `523117e7de4` | Increase per-fixture timeout from 45 seconds to three minutes under CI load | Not copied literally: v4 currently allows 120 seconds per fixture. This is a remaining test-policy difference, not an identified production omission. Fresh tracing tests passed; a separate small CI-hardening follow-up could align/document the bound if desired. |

The consolidated tracing suite takes longer than one fixture timeout because it launches multiple fixtures; its total duration is not evidence that an individual fixture exceeded its bound.

## Additional maintenance work

### CAUSEWAY-4070: already covered

Maintenance implementation `746fd6393ce`, approval correction `04029032473` and archive `47f055e3cbd` have destination counterparts.
V4 history includes implementation `b2a9e8a9556`, merge PR #3838 and archive `934d4a60282`.
See [the existing locking archive](../changes/archive/2026-10-06-port-domain-object-locking/tasks.md), which explicitly records reviewing the approval correction and regenerating the five affected snapshots.
The annotation/configuration/facets, pessimistic bookmark find path and provider/concurrency tests are present.
No new locking port was identified by this audit.

### CAUSEWAY-4078: recommended next functional port

Maintenance implementation: `2bc5478983b`.
Source planning artifacts: `openspec/changes/wicket-deep-link-after-sso/` in the maintenance worktree.

In current v4 `WicketSignInPage`, an already-signed-in session goes directly to `pageNavigationService.restartAt(PageType.HOME)`.
The maintenance implementation first attempts the intercepted original destination, while skipping that attempt when reporting an exception or when original-destination clearing is configured.
The normal interactive sign-in success path already continues to the original destination; that does not cover the transparent-authentication constructor path.

A separate port should:

- Restore the original destination before falling back to Home for transparent sign-in.
- Preserve the exception-reporting guard to avoid redirect loops.
- Respect the existing clear-original-destination setting, using v4's record accessor `clearOriginalDestination()` rather than maintenance's JavaBean getter.
- Adapt the regression test to v4's Wicket 11 and separate `ui-test` module.
- Verify fallback, exception, clearing and original-destination cases without altering ordinary sign-in behavior.

No implementation or new active OpenSpec change was created during this audit.

## Consolidated validation

Current source was built with Java 25.0.4-tem, including Wicket/UI dependencies, before the consolidated tests.

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -pl viewers/wicket/ui-test,viewers/wicket/viewer -am \
  -DskipTests -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
mvn -B -pl viewers/wicket/ui-test,viewers/wicket/viewer \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true test
mvn -B -f regressiontests/tracing-compatibility/pom.xml test
```

All commands passed.
Wicket discovered 118 tests, with 113 executed successfully and five existing skips.
All 23 Boot/agent tracing compatibility tests passed without failures or skips.
Logs are `/tmp/causeway-forward-port-reconciliation-{install,wicket,tracing}.log`.
The preceding readability checkpoint `d4397e473af` also freshly passed 44 focused and 121 domain-model tests; those are prior-checkpoint evidence, not additional tests rerun by this audit.
No full repository reactor, live SSO browser flow or production deployment was tested.

## Retained idea #2

[General span duration filtering](general-span-duration-filtering.md) remains an explicit follow-up for exploration, as requested by the user.
It is not an outstanding maintenance forward port and was not implemented during reconciliation.
Retain the existing requirement to preserve meaningful trace ancestry, failures and bounded resource use rather than restoring the removed JPA-only threshold.
The next functional port and the duration-filtering exploration can be scheduled independently.
