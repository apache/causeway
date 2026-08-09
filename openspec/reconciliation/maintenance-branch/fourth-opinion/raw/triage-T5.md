# Triage T5 — LOOSE / non-"CAUSEWAY-####" commits (fourth-opinion, clean-room)

Scope: commits on `maintenance-branch` (merge-base `65d64cd85b7`) whose subject does NOT match `CAUSEWAY-[0-9]+`.
Authoritative = `ecp` (v2 maintenance). Target = `main` (v4).

## 1. Summary table

| Commit | Classification | One-line intent | Verdict |
|---|---|---|---|
| `8a7673a9b77` JDOJPA-300 | BEHAVIOURAL | Run each replayable command in its own `REQUIRES_NEW` transaction | PRESENT |
| `e1169095990` (TURNDP-184, PR #3465) | BEHAVIOURAL | Add `Publishing.ENABLED_FOR_UPDATES_ONLY` + create/update/delete-granular entity-change publishing | PRESENT |
| `5f91fe27e5d` collection summary env var | BEHAVIOURAL | Disable Wicket collection summary view via env var `causeway.viewer.wicket.summary-view-disabled` | MISSING |
| `703432fa76c` refine collection summary | BEHAVIOURAL | Refine above: require value == "true" (case-insensitive) + changelog | MISSING |
| `bbab91cdb7b` [Causeway-3998] disable failing parser test | DOCS/STYLE (test-only, v2) | @Disabled a trailing-empty-doc YAML parser test + drop 2 unused imports | N/A (test absent in main) |
| `86d43d03786` decommission replay incubator | V2-INFRA | Delete commandreplay incubator modules from v2 build | N/A (v2-only; do NOT port) |
| `0cdf3aed320` 3998-v2: cosmetics | DOCS/STYLE | Whitespace/cosmetic reformat of CommandReplayManager.java | skip |
| `142903de754` updates agent instructions | DOCS/STYLE | Edits to AGENTS.md | skip |
| `0632e37…`,`7ad268d…`,`593494c…`,`f43a035…`,`4f89606…`,`34a7dfd…` `${revision}` rename churn | V2-INFRA | Temporary `${revision}` -> 2.2.0-SNAPSHOT rename + reverts/reapplies | skip |
| `d27066bee0`,`fb240e090a`,`14f7f1c793`,`9f4042124f`,`44b9173982` [v2] docs | DOCS/STYLE | v2 changelog / README / CI-deployment docs | skip |

Counts: BEHAVIOURAL = 4 (2 distinct features). V2-INFRA = 7. DOCS/STYLE = 8.

## 2. BEHAVIOURAL commits — detail + presence check

### `8a7673a9b77` JDOJPA-300 — replay in own transaction — VERDICT: PRESENT
- Intent: each replayable command executes inside its own `REQUIRES_NEW` transaction.
- Essential behaviour: inject `TransactionService` into `ReplayContext`; wrap `commandExecutorService().executeCommand(...)` in `replayContext.transactionService().callTransactional(Propagation.REQUIRES_NEW, ...)` inside `ReplayableCommand.tryReplayOrRetry()`.
- Searched main (`extensions/core/commandlog/applib/.../dom/replay/`):
  - `ReplayContext.java:34,47,61,74` — `TransactionService transactionService` field/params present.
  - `ReplayableCommand.java:34` imports `org.springframework.transaction.annotation.Propagation`; `:510-513` `transactionService().callTransactional(Propagation.REQUIRES_NEW, () -> ... executeCommand(SWITCH_USER_AND_TIME, commandDto)...)`; `:521-522` error path also wrapped in `runTransactional(REQUIRES_NEW, ...)`.
- Evidence supports PRESENT. Main is in fact slightly more elaborate (error path also transactional), so behaviour is at least as strong.

### `e1169095990` TURNDP-184 — "update only" publishing — VERDICT: PRESENT
- Intent: new `Publishing.ENABLED_FOR_UPDATES_ONLY` enum constant; granular create/update/delete flags on `EntityChangePublishingFacet`; change tracker suppresses create/delete enlistment when only updates enabled.
- Essential behaviour tokens: `ENABLED_FOR_UPDATES_ONLY`; `isEnabledForCreate/Update/Delete`; `isPublishingEnabledForCreate/Update/Delete`; `EntityChangePublishingFacetForDomainObjectAnnotation(holder, true, false, true, false)`; listener guards in `EntityChangeTrackerDefault.enlistCreated/Updating/Deleting`.
- Searched main:
  - `api/applib/.../annotation/Publishing.java:73` — `ENABLED_FOR_UPDATES_ONLY` present.
  - `EntityChangePublishingFacet.java:41-62` — all three `isPublishingEnabledForCreate/Update/Delete` static helpers + `isEnabledForCreate/Update/Delete()` interface methods.
  - `EntityChangePublishingFacetForDomainObjectAnnotation.java:55` — `ENABLED_FOR_UPDATES_ONLY -> new ...(holder, true, false, true, false)` (identical create/update/delete tuple).
  - `Command/ExecutionPublishingFacetFor{Action,Property}Annotation` — all include `ENABLED_FOR_UPDATES_ONLY` in the ENABLED case (main uses switch-arrow form).
  - `EntityPropertyChangePublishingPolicyFacet(.ForPropertyAnnotation).java` — `ENABLED_FOR_UPDATES_ONLY` in isPublishingAllowed / create-filter.
  - `EntityChangeTrackerDefault.java:538,560,598` — the three `|| !EntityChangePublishingFacet.isPublishingEnabledFor{Create,Update,Delete}(entity.objSpec())` guards present.
  - Test `core/mmtest/.../DomainObjectAnnotationFacetFactoryTest.java:129,234-236` — `ENABLED_FOR_UPDATES_ONLY` scenario asserting create=false/update=true/delete=false present.
- Evidence supports PRESENT (full feature, incl. listener + tests).

### `5f91fe27e5d` + `703432fa76c` collection summary disable via env var — VERDICT: MISSING
- Intent: allow disabling Wicket "Collection Contents As Summary" view via env var `causeway.viewer.wicket.summary-view-disabled` (value must equal "true", case-insensitive after refine commit). In v2 `CollectionContentsAsSummaryFactory` gains a `disabled` field set from `System.getenv(...)`, and `appliesTo(...)` returns `ApplicationAdvice.DOES_NOT_APPLY` early when disabled.
- Essential behaviour tokens: env-var name `causeway.viewer.wicket.summary-view-disabled`; `System.getenv`; `disabled` field; early `DOES_NOT_APPLY` in `appliesTo`.
- Searched main:
  - Factory lives at `viewers/wicket/ui/.../components/collection/present/summary/CollectionContentsAsSummaryFactory.java` (relocated from `collectioncontents/summary` in v2; adaptation only).
  - Read full file: NO `disabled` field, NO `System.getenv`, NO early-return in `appliesTo` (lines 56-69 only compute `hasAnyBigDecProperty`).
  - `grep -rn "summary-view-disabled|summaryViewDisabled|summary.view.disabled"` over main source (excluding `openspec/`): ZERO hits. (Only hit anywhere is a prior-opinion doc under `openspec/`, which is off-limits and not code.)
- Adversarial check: no config-key equivalent (`CausewayConfiguration` / `application.properties` schema) named summary-view either. Feature is genuinely absent.
- Verdict: MISSING. This is a real, small runtime/behavioural gap (Wicket viewer). Note the mechanism in v2 is an env var, not a config property; a faithful forward-port may prefer a proper `causeway.viewer.wicket.*` config property in v4, but the disabling capability itself is not present at all.

## 3. SKIPPED / non-behavioural — justification

- `bbab91cdb7b` [Causeway-3998] disable failing parser test — TEST-ONLY, v2-only. Adds a new `...-trailing-empty.yaml` fixture and `@Disabled`s a new `scalarValuesAsMultiDocumentWithTrailingEmptyDocument` test. Main's copy of `CommandDtoUtils_fromYaml_Test.java` (now under `core/mmtest`) does NOT contain that test method or fixture at all (only `scalarValues`, `scalarValuesAsMultiDocument`, `collectionValues`). Nothing to port; no behavioural change (the two dropped imports in CommandReplayManager/ReplayableCommand are unused-import cleanup). No analogue needed in main.
- `86d43d03786` decommission replay incubator — V2-INFRA. Removes the entire `commandreplay` incubator (primary+secondary) from the v2 bom/incubator/root poms and deletes its sources. In main the replay incubator is STILL PRESENT and STILL WIRED into the build (`incubator/extensions/core/commandreplay`, referenced from `incubator/pom.xml:202-230`), and main continues to touch it in normal development. This removal is a v2-lifecycle decision (decommissioning in the maintenance line); it has NO analogue that should be applied to main. Do NOT port.
- `0cdf3aed320` 3998-v2: cosmetics — DOCS/STYLE. Whitespace-only reformat of `CommandReplayManager.java`; no logic change.
- `142903de754` updates agent instructions — DOCS/STYLE. Edits `AGENTS.md` only (v2 tooling/process docs).
- `0632e377b20`, `7ad268db198`, `593494c2c44`, `f43a035eab8`, `4f896068cc1`, `34a7dfdf3a9` — V2-INFRA. The `temporarily renames ${revision} -> 2.2.0-SNAPSHOT` change plus its chain of revert/reapply/re-revert. Pure v2 release-versioning plumbing; explicitly out of scope per instructions.
- `d27066bee0`, `fb240e090a`, `14f7f1c793`, `9f4042124f`, `44b91739828` — DOCS/STYLE. `[v2]` changelog / README table-of-contents / CI-deployment instruction docs; no runtime effect.
