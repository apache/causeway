# Fourth-opinion triage — batch 2

Independent clean-room reconciliation. MAINTENANCE (v2) = `.../ecp@maintenance-branch`; MAIN (v4) = `.../main@main`; merge-base `65d64cd85b7`.

## 1. Summary table

| Ticket | Classification | One-line intent | Verdict |
|---|---|---|---|
| CAUSEWAY-3969 | BACKPORT-FROM-MAIN | JPA Weaving Safeguard (originated v4, backported to v2) | PRESENT |
| CAUSEWAY-3972 | BEHAVIOURAL | Fix `_Oneshot` deadlock potential (AtomicInteger CAS) | PRESENT |
| CAUSEWAY-3973 | BACKPORT-FROM-MAIN | Simplified grid loading / layout switching / qualified facets | PRESENT |
| CAUSEWAY-3979 | BEHAVIOURAL (obsolete in v4) | Deprecate `RoutingServiceDefault` | PRESENT (class removed in v4) |
| CAUSEWAY-3976 | BACKPORT-FROM-MAIN | Client URL rewrite only if same origin (JS redirect) | PRESENT |
| CAUSEWAY-3983 | BACKPORT-FROM-MAIN | Remove tab content when always-hidden | PRESENT |
| CAUSEWAY-3985 | BACKPORT-FROM-MAIN | Table column sorting only if Comparable/ordered | PRESENT |
| CAUSEWAY-3995 | BEHAVIOURAL | Clear all layout caches immediately after MM init | PRESENT |
| CAUSEWAY-3989 | BACKPORT-FROM-MAIN | Command replay feature + multidoc YAML | PRESENT |
| CAUSEWAY-3996 | BACKPORT-FROM-MAIN | In-memory layout patching (`Object_patchLayout`) | PRESENT |
| CAUSEWAY-3998 | BACKPORT-FROM-MAIN | Replay manager improvements (replayOrRetryNext, openTarget) | PRESENT |
| CAUSEWAY-4002 | V2-INFRA + BEHAVIOURAL (NPE guard) | v2 CI tuning; NPE guard in ServiceRegistryDefault.select | PRESENT |
| CAUSEWAY-3997 | BACKPORT-FROM-MAIN | Column-order patching + wrong-Where-context fix | PRESENT |

All verdicts PRESENT. No PARTIAL/MISSING/UNSURE.

## 2. Per BEHAVIOURAL / mixed ticket detail

### CAUSEWAY-3972 — BEHAVIOURAL, PRESENT
- Intent: eliminate deadlock potential in `_Oneshot` (`trigger(Runnable)` ran the runnable while holding a `synchronized` lock).
- Key commit: `90b381d10d2` (impl), `6469afd`/`cc3ac8a` (tests).
- Essential behaviour: replace `synchronized($lock)` + `volatile int triggerCount` with `AtomicInteger counter` + `compareAndSet(0,1)`; runnable no longer runs under lock.
- Searched main: `commons/.../internal/base/_Oneshot.java`.
- Evidence: main lines 22/38/44/54/65 — `AtomicInteger counter`, `counter.compareAndSet(0,1)` in both `trigger()` and `trigger(Runnable)`, `counter.set(0)` in `reset()`. Identical fix.

### CAUSEWAY-3979 — BEHAVIOURAL (obsolete), PRESENT
- Intent: add `@Deprecated` to `RoutingServiceDefault` ("no longer in effect since v2").
- Key commit: `773fd356e` (2-line annotation add only).
- Essential behaviour: deprecation marker.
- Searched main: `find RoutingService*.java` → only the applib interface `RoutingService.java` remains; `RoutingServiceDefault` does not exist in main (removed in v4). Grep `RoutingService` in `*.java` returns only the interface.
- Verdict: PRESENT — v4 goes further than deprecation (full removal). Nothing to forward-port.

### CAUSEWAY-3995 — BEHAVIOURAL, PRESENT
- Intent: after metamodel init (and on dispose) clear all layout caches, so stale grids are not served.
- Key commit: `339ce1088d8`.
- Essential behaviour: new `GridService.clearCache()`, `GridFacet.clearCache()`, `GridCache.clear()`, `GridServiceDefault.clearCache()`; `SpecificationLoaderDefault` gains private `clearLayoutCaches()` invoked in `createMetaModel()` and `disposeMetaModel()`.
- Searched main: applib `GridService.java`, metamodel `GridFacet.java`, `spec/impl/SpecificationLoaderDefault.java`.
- Evidence: `GridService.java:122 void clearCache()`; `GridFacet.java:46 void clearCache()`; `spec/impl/SpecificationLoaderDefault.java:294` (`clearLayoutCaches()` after MM init), `:320` (in `disposeMetaModel`), `:670` private impl iterating specs' `GridFacet::clearCache` + `GridService::clearCache`. (File relocated `specloader/` → `spec/impl/`.) Faithful.

### CAUSEWAY-4002 — V2-INFRA (mostly) + BEHAVIOURAL NPE guard, PRESENT
- Intent: v2 CI experiments (Java/Maven/Lombok versions, single-thread build) + a null-guard bug fix.
- Commits: `e7bbbe5`, `90950bd` = `.github/workflows/ci-v2-verify.yml` only → V2-INFRA, skip. `4716791` = NPE guard in `ServiceRegistryDefault.select()`.
- Essential behaviour (behavioural part): guard `iocContainer!=null ? ... : Can.empty()` instead of unconditional deref (prevents NPE during `disposeMetaModel`).
- Searched main: `core/metamodel/.../services/registry/ServiceRegistryDefault.java`.
- Evidence: main `select()` lines 84-89 — `springContextHolder!=null ? springContextHolder.select(...) : Can.empty()`. Same null-guard semantics (v4 renamed `iocContainer`→`springContextHolder`). PRESENT.

## 3. Skipped tickets — justification

- **CAUSEWAY-3969** BACKPORT-FROM-MAIN: commit messages "backports JPA Weaving Safeguard from v4". Verified present anyway: `JpaWeavingSafeguard.java`, `JpaWeavingSafeguardService.java` under `persistence/jpa/integration/.../services/`; config enum `SafeguardMode` incl. `REQUIRE_WEAVED_WHEN_ANY_SUB_IS_WEAVED` default in `CausewayConfiguration.java:2113/2126`. Extra v2 commits are adoc (CI-friendly versioning) + `static` nested-class cleanup — non-behavioural.
- **CAUSEWAY-3973** BACKPORT-FROM-MAIN: "backport of simplified grid loading / layout switching / all Qualified Facets / new FacetRanking" — feature originated in v4. Verified: `QualifiedFacet.java`, `FacetRanking.java`, `FacetRank.java`, applib `LayoutResourceLoader.java`, `LayoutSwitchingTest.java`, `GridService.LayoutKey` record all present in main. v2-only commits ("make backport non-breaking", "FacetRank thread-safe") are v2-consumer-compat adaptations, N/A to v4.
- **CAUSEWAY-3976** BACKPORT-FROM-MAIN: "backport ... client URL rewrite ... same origin". Verified: `viewers/wicket/ui/.../exec/JavaScriptRedirect.java`, `UrlBasedRedirectContext.java`, `OriginRewrite` referenced in `Mediator.java` — all present.
- **CAUSEWAY-3983** BACKPORT-FROM-MAIN: "removes tab content when always hidden (backport)". Verified: `Where.isObjectForms()` (`Where.java:216`) and `BSGridTransformer.isAlwaysHidden(...)` (lines 88-91) present in applib.
- **CAUSEWAY-3985** BACKPORT-FROM-MAIN: "Backport Table Column Sorting to be only enabled if Comparable". Verified: `ObjectSpecification.isComparableOrOrdered()` (`ObjectSpecification.java:449`) and use in `CollectionContentsAsAjaxTablePanel.java:217` (`sortability`) present. (Path moved to `collection/present/ajaxtable/`.)
- **CAUSEWAY-3989** BACKPORT-FROM-MAIN: "backport of command replay feature". Verified: `CommandExportManager`, `CommandReplayManager`, `ReplayableCommand`, `ReplayContext` under `extensions/core/commandlog/applib/.../dom/replay/`; multidoc YAML in `CommandDtoUtils.java` (`fromYamlForReplay`, `tryReadMultiDocument`). "flipped logic in export filter" fix (negated `!ReplayState.isExported`) confirmed present in main `CommandExportManager.java:184`. Some ecp commits (fresh YAML multidoc refactor/tests) are genuine work but their behaviour is in main.
- **CAUSEWAY-3996** BACKPORT-FROM-MAIN: "backports in-memory layout patching". Verified: `Object_patchLayout.java`, `LayoutPatchesMap.java`, `GridService.addPatchedLayout(...)` (applib :99, impl in `GridServiceDefault.java:107`, `LayoutResourceLookup.java:110`) present.
- **CAUSEWAY-3998** BACKPORT-FROM-MAIN: replay manager polishing built on 3989. Verified: `ReplayableCommand_openTarget.java`, `ReplayableCommand_openTargetTR.java`, `ReplayableCommand_replayOrRetry.java` present; `replayOrRetryNext` present as `CommandManager_replayOrRetryNext.java` (renamed manager mixin in v4).
- **CAUSEWAY-3997** BACKPORT-FROM-MAIN: "backports column order patching" + "fix for potentially wrong Where context". Verified: `ColumnOrderPatchingFacet.java`, `Object_patchColumnOrder.java`, applib `Listing.java`, `Can.join(...)` (`Can.java:932/944`); Where-context fix — `streamActionsForColumnRendering(Where)` signature present (`ObjectActionContainer.java:148`, `ObjectSpecificationDefault.java:404`), with `collectionVariant.whereContext()` used at `ActionColumn.java:51`.
