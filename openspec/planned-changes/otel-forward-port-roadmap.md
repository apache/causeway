# OpenTelemetry and related forward ports to main

## Baseline and purpose

Prepared 2026-09-27 from maintenance merge `7e799dfd360` (PR #3813, CAUSEWAY-4066 branch) and recorded `origin/main` at `351eae721d9`. The planning checkout was maintenance at `b028fca1132`; local `main` was 513 commits behind `origin/main`. No remote freshness verification or runtime testing was performed during exploration. Refresh these references before implementation.

Main already contains CAUSEWAY-3975, including instrumentation absent from the smaller maintenance backport. The task is to reconcile and extend main, not replay the merge or replace its observation implementation. Use feature-sized PRs with the originating ticket as provenance. Numeric ticket order is not a dependency order, and no 4064 implementation was identified in this merge.

## Roadmap

| Sequence | Ticket / feature | Status and proposed scope | Implementation source anchors |
|---|---|---|---|
| 1 | 3975: foundation reconciliation | **Archived:** [completed change](../changes/archive/2026-09-27-reconcile-main-observation-foundation/proposal.md). Lifecycle cleanup, activation/registry selection, Boot/agent compatibility evidence, threshold behavior, preservation of main instrumentation. | `9b8b66d5b7b`, `e6752f882b5`; compare main's original 3975 implementation |
| 1b | CAUSEWAY-4096: standardize observation names, metadata and duration-filtering policy | **Archived (2026-10-05):** [standardize observation policy](../changes/archive/2026-10-05-standardize-observation-policy/proposal.md) on branch `CAUSEWAY-4096`. Follow-up to 3975: stable operation names vs contextual names; review bookmark/query-description names, identity attributes, hard-coded duration threshold and missing exported parents. Set policy before adding new semantic names. | Main `JpaEntityFacet`, runtime `ia/_Observation`, `CausewayObservationIntegration` |
| 2 | CAUSEWAY-4068 (3975/4068 provenance): classification and correlation | **Archived (2026-10-07):** [classify and correlate observations](../changes/archive/2026-10-07-classify-and-correlate-observations/proposal.md). Foreground/background classification, root interaction ID, fixed execution-mode attribute key. Depends on foundation; adapt to main's interaction layers. | `024bd33128d`, `0325232a3c7`, `3083a51eabd` |
| 3 | CAUSEWAY-4058: current action context | **Archived (2026-10-09):** [port current action context](../changes/archive/2026-10-09-port-current-action-context/proposal.md). Exact-current-action queries and rule-checking state; nested restoration and physical mixin receiver semantics. Can proceed independently of most telemetry work. | `6d037db6609`; account for `f66b8f17a89` |
| 4 | 4062: semantic member names | Planned. Bounded operation categories with domain-facing contextual names; mixed-in property/collection access distinguished from actions. Depends on foundation and naming policy. | `a2ae5877c99`, `d5ecd49a1f3`, `45e01ff6f42` |
| 5 | 4059 + relevant 4062: Wicket regions/preparation | Planned. Entity-page preparation, region rendering and action prompts; Ajax ancestry, serialization and failure cleanup. The 4062 source touches Wicket as well as core: extract the relevant portions here rather than blindly applying step 4 commits. | `a8264fd1f9b`, relevant parts of `a2ae5877c99` / `45e01ff6f42` |
| 6 | 4062 + 4067: collection detail and volume | Planned. Table phases, rows, cells, row actions; deliver detail levels, request budgets and collection aggregates with the new detail. Depends on Wicket regions. | `8e5eaab6145`, `036a28a23c2`, `5a1dc2dd17b` |
| 7 | 4063: semantic trace display names | Planned. Foreground entry-span naming from prioritized action/prompt/view nominations. Depends on classification, semantic naming and Wicket nominations for Wicket support. | `e17c3088ac2` |
| 8a | 4059: audit-trail writing | Planned. Observe audit subscriber work, preserving transaction semantics and failures. Depends on foundation only. | `7961910a089` |
| 8b | 4059: entity-change evaluation | Planned. Observe evaluation in persistence commons, preserving existing main publishing/transaction spans. Depends on foundation only. | `02786054574` |
| 9 | 4065: application-defined spans | Planned. Public service, nested spans, suffix/name contract, no-op implementation and action identity correction. Depends on foundation, action context and naming policy. | `ea32b739d0f`, `5da64c34b57`, `f66b8f17a89` |
| 10a | 4066: application priming hooks | Planned. Registrar, startup validation, immutable argument view, action callbacks and once-per-request entity-view callbacks. Useful independently of tracing; adapt to main's metamodel/action/Wicket lifecycles. | `19d2a36d2f9` |
| 10b | 4066: primer observations | Planned. One span per matching callback, natural parentage, failure propagation. Depends on priming hooks and semantic naming. | `473454f2447` |
| Separate A | 4060: introspection diagnostics | Planned. Retained introspection chain, overflow diagnostics and stable mixin traversal; assess overhead and preserve behavior. No telemetry dependency. | `fb28fa67e06` |
| Separate B | 4061: mixin prefilter | Planned. Check applicability using type metadata before requesting full member introspection; preserve applicable contributions and ordering. Follow diagnostics for evidence, but the fix is not conceptually dependent on tracing. | `680ac40ef52` |

## Dependency sketch

```text
3975 foundation ──┬── classification/correlation ──────────────┐
                 ├── audit and entity-change observations     │
                 └── metadata policy → semantic names ────────┤
                                          │                  │
                                          ├→ Wicket regions ─┼→ trace display names
                                          │       └→ collection detail + budgets
4058 current-action context ────────────────┴→ application spans
priming hooks ── + foundation/semantic names ─→ primer observations

metamodel diagnostics → mixin applicability prefilter (separate track)
```

## Foundation findings and decisions to retain

- Main's closure can stop twice and skips stop if scope closing throws; maintenance clears state and uses finally for stop. Port behavior into main's existing helper, not its maintenance package location.
- Main's threshold wrapper returns its delegate from fluent methods/start. Potential bypass needs a targeted reproduction; no runtime defect was claimed during exploration.
- Main uses Boot-managed configuration; maintenance constructs a dedicated registry/tracer against agent-owned global context. The first proposal retains main's default and requires evidence for an explicitly supported agent configuration. Do not assume a Spring exporting predicate controls the agent exporter.
- Main has property editing, transaction, publishing and JPA spans. Preserve these while adapting the maintenance features to main's interaction carrier/layers.
- Main includes bookmarks/query descriptions in some JPA observation names and username/tenancy attributes on interactions. Maintenance's newer features deliberately use bounded domain-level metadata. Resolve the policy in step 1b, documenting any telemetry naming compatibility changes.
- 4058 distinguishes the physical action receiver/identifier from the domain-facing identity needed for spans. The later `f66b8f17a89` correction must inform both 4058 and 4065; do not rewrite invoked identifiers to solve display naming.
- Maintenance's 4067 defaults are `MEMBERS` detail and unlimited budget (`0`) for compatibility. Main can consider different defaults, but that is an explicit feature decision backed by representative traces, not an incidental port change.
- Priming is an application hook for bulk loading into the current persistence context, not an ORM-specific fetch implementation. Action and view hooks must remain useful with observation disabled.
- Wicket-only features do not imply equivalent coverage for main's web-component viewers. Extending those viewers requires a separately scoped proposal.

## Tests, tooling and documentation

Carry focused tests and documentation with every feature. Telemetry gates include profile off, missing exporter/agent, success, failure, cleanup and real exported ancestry. Wicket gates additionally include full-page/Ajax, serialization, reduced-detail ancestry and hard budget limits. Public invocation APIs require exact identity, checked/skipped/unknown rule status, nested restoration and mixin coverage. Metamodel fixes require applicable/inapplicable mixins and repeated deterministic initialization.

Adapt the maintenance process fixture early. Sources include `8d3771bb852`, `regressiontests/tracing-compatibility`, and follow-ups `90512b829c1`, `769f13af9a5`, `523117e7de4`. Retain output-draining and bounded process waits; replace the Java 11 constraint and old telemetry pins with target-main-compatible versions. Do not import Boot 2.7's observation/bridge dependency management.

Local tooling/docs sources include `acc65829bbd`, `8628d26fdc3`, `0a02d0b59d1`, `f5a499d50ef`, `d141c96edfb`, `scripts/jaeger-local-*`, `scripts/otel-local-env.sh`, its shell tests, and `adoc/micrometer-tracing-operations.adoc`. Port a main-appropriate runbook and comparison tooling as an early companion to foundation validation; do not copy the Boot 2.7 operating guide verbatim. Integrate the final guidance into main's documentation structure.

## Recovering design evidence

OpenSpec changes/specs were deleted during the maintenance sequence, but remain in Git:

- `473d2c218c4^:openspec/changes/archive/` contains the early Boot 2.7 substrate, tracing and current-action-context designs.
- `1a9910c3e77^:openspec/changes/archive/` contains Wicket rendering, naming, audit/evaluation, priming, application spans, volume controls and metamodel designs.
- `1ac42dc5408^:openspec/changes/archive/` contains the later 4068 artifacts.

Inspect with `git ls-tree -r --name-only <ref> openspec` and `git show <ref>:<path>`. Earlier proposals can be superseded by later implementation/fix commits; use the final maintenance code and tests as the behavior reference, with archived designs explaining intent.

## Execution rules

1. Refresh target main and inspect its instructions, specs, toolchain and integration changes before implementing each feature.
2. Create a main-based branch/worktree and transfer the relevant proposal artifacts. Keep this maintenance tree free of forward-port implementation.
3. Use source commits as provenance; split mixed-feature changes and include their fixes. Exclude OpenSpec pruning, `.mcp.json`, and unrelated local setup from feature PRs.
4. Commit uncommitted proposal artifacts before `/opsx-apply`. Prefix implementation/proposal commits with the appropriate branch/Jira key.
5. Record actual tests, dependency versions, remaining limitations and resulting PR/commit links here. Do not mark planned features complete solely because the maintenance implementation exists.

No new tickets or PRs have been created by this planning work. Sequence numbers indicate a suggested delivery order; independent tracks can be developed separately.

## Foundation implementation update (2026-09-27)

Planning artifacts were committed as `13f98006d08` on `CAUSEWAY-3975-observation-foundation`. Implementation was committed as `299dcdc2234` and delivered in [PR #3814](https://github.com/apache/causeway/pull/3814), targeting `main` from remote branch `CAUSEWAY-3975-observation-foundation`. The OpenSpec change is archived and PR #3814 was merged into main as `74d44c060ce`. [Validation evidence](../changes/archive/2026-09-27-reconcile-main-observation-foundation/validation.md) records the inventory, tested versions and configurations.

Boot remains the default owner. The agent configuration supplies an explicit registry bridged to GlobalOpenTelemetry, excludes competing Boot SDK/tracing auto-configuration, and disables JPA duration filtering through `causeway.observation.duration-filtering-enabled=false`. Real exported traces verify parentage and no duplicate framework spans; Boot mode verifies discard behavior. This required no new production dependency.

Main's current tracing handler preserves existing name casing; revisit whether any custom handler is needed for 4062 instead of copying maintenance's workaround. Step 1b is assigned to CAUSEWAY-4096 (observation names, metadata and duration-filtering policy) and remains a separate change. The user guide now contains the main-specific observability runbook. Jaeger/comparison shell tooling is still a planned early companion and was not included in this foundation implementation.

## Observation policy proposal (CAUSEWAY-4096)

Step 1b is now [archived](../changes/archive/2026-10-05-standardize-observation-policy/proposal.md), with design, delta specs and implementation tasks. It defines stable operation names for existing dynamic JPA/member/publishing observations, bounded contextual model metadata, independent username/tenancy opt-ins (default off), and duration filtering default off with a configurable 2 ms JPA threshold when explicitly enabled. These are telemetry compatibility changes requiring migration guidance and exported-trace evidence; implementation, validation and manual review are complete; the change is archived.

The proposal preserves existing instrumentation and registry ownership. It does not include later mixin semantics, trace-root naming, classification/correlation or collection budgets. Before creating it, the completed viewer-documentation proposal was archived on main in `da8c9a98bb7`, and branch `CAUSEWAY-4096` was rebased onto that commit.

Implementation checkpoint: planning committed as `5390171e070`; all 11 implementation tasks completed. [Validation evidence](../changes/archive/2026-10-05-standardize-observation-policy/validation.md) records 43 focused tests, 8 JPA/export tests, 22 existing regression tests and the documentation build. Explicit Boot filtering was also verified to leave exported children of discarded JPA parents; the default retains those parents. The core implementation was committed as `5b92e345321`. Local telemetry scripts, the M3 guide and a Wicket scope-ordering fix were subsequently validated and manually reviewed. The change was archived on 2026-10-05 with specs synced; follow-up implementation and archive files were committed as `6f10ac34cac`.

## Classification and correlation proposal (2026-10-06)

Step 2 is captured in [classify-and-correlate-observations](../changes/archive/2026-10-07-classify-and-correlate-observations/proposal.md), based on local `6f10ac34cac` after CAUSEWAY-4096. CAUSEWAY-3975/4068 identify maintenance provenance. The user selected `CAUSEWAY-4068-v4` for the main-side implementation under CAUSEWAY-4068.

The design adapts current-span classification to main's shared Micrometer tracer for both Boot and agent ownership, uses the fixed `causeway.execution.mode` attribute, and correlates only the existing root interaction observation. It explicitly handles main's replay-time replacement of command identity, avoids introducing job spans when instrumentation supplies none, and keeps semantic naming and rendering detail for later steps.

Before creating the proposal, completed `port-domain-object-locking` was validated and [archived](../changes/archive/2026-10-06-port-domain-object-locking/proposal.md), with its requirements synced. The focused reactor verification passed, including all eight pessimistic-locking regressions. The archive checkpoint was committed as `e65505dbe0c` after session permissions allowed Git writes; the new planning artifacts follow in a separate proposal commit.

Implementation checkpoint (2026-10-06): all 12 tasks completed on `CAUSEWAY-4068-v4`. [Validation evidence](../changes/archive/2026-10-07-classify-and-correlate-observations/validation.md) records 31 focused tests, 9 tracing/JPA compatibility tests, Petclinic packaging, generated configuration metadata and documentation checks. The M3 how-to and observability guide now cover foreground/background entry attributes, root interaction UUIDs, separate Jaeger searches and the background fixture. Agent setup shares its tracer and excludes duplicate Boot HTTP observation instrumentation, with the meter implications documented. Implementation is recorded in the commit containing this checkpoint; archival remains pending.

Review follow-up: removed the execution-mode key override at the user’s request. Both entry points always use `causeway.execution.mode`; configuration, tests and operator guidance have been simplified accordingly.

Review follow-up (2026-10-07): username and tenancy are now automatic nonempty interaction attributes alongside the root UUID and fixed entry execution mode. Removed identity opt-in properties and launcher scenarios; duration-filtering policy remains available.

Filtering simplification: `causeway.observation.jpa-duration-threshold` is the only filtering setting, with `0ms` disabling filtering. The launcher no longer takes `--scenario`; set `CAUSEWAY_OBSERVATION_JPA_DURATION_THRESHOLD` for Boot-managed runs.

Classification/correlation archived on 2026-10-07 after fresh focused and tracing validation passed (45 tests), with all delta specs synced. General duration filtering is tracked in [the exploration backlog](general-span-duration-filtering.md); the narrow JPA-only threshold is being removed in a separate change.

The [remove-jpa-duration-filtering](../changes/archive/2026-10-07-remove-jpa-duration-filtering/proposal.md) change removes the narrow JPA-only policy while preserving explicit transaction discard behavior. Replacement filtering is deferred to [general span duration filtering](general-span-duration-filtering.md).

Removal completed and archived on 2026-10-07. Implementation checkpoint `39faf40ded6`; 34 Java tests passed before and after the checkpoint, Petclinic packaged successfully, and metadata/docs/launcher checks passed. Specs were synced; all completed changes are archived. General span filtering remains backlog exploration.

## Built-in agent bridge follow-up (2026-10-07)

[provide-agent-observation-bridge](../changes/archive/2026-10-07-provide-agent-observation-bridge/proposal.md) moves the sample-owned bridge and manual exclusions into core configuration. Consumers add the optional BOM-managed OTel bridge dependency, attach the Java agent and activate `observation,agent`; Micrometer metrics remain independent. Exported-trace fixtures exercise the production configuration. This is an adoption simplification on CAUSEWAY-4068, with general duration filtering still deferred.

Agent bridge completed and archived on 2026-10-07: planning `a7c3d3584e6`, implementation `906a897df37`. All 28 focused/exported-trace tests passed before and after the checkpoint, including Micrometer metrics export in agent mode; Petclinic packaging and documentation rendering passed. The new agent bridge requirements are synced to the main specs.

## Current action context proposal (2026-10-09)

Step 3 is promoted into [port-current-action-context](../changes/archive/2026-10-09-port-current-action-context/proposal.md), with design, delta requirements and implementation tasks. It ports `6d037db6609` while incorporating the invoked/domain-facing identity correction from `f66b8f17a89`. The scope includes current-action queries, checked/skipped/unknown rule status, physical mixin receivers, separate contributed identifiers and nested success/failure restoration; it adds no spans. Public guidance and a concise M3 how-to update accompany implementation.

Planning baseline: `41d8db75a06` on CAUSEWAY-4068-v4. Refresh main and select a CAUSEWAY-4058 branch before implementation. CAUSEWAY-4065 application-defined spans remains a later dependent forward port; no implementation or validation is claimed by this proposal.

Current action context implemented on CAUSEWAY-4058 after planning commit `9a250065cfa`. All nine tasks completed; [validation evidence](../changes/archive/2026-10-09-port-current-action-context/validation.md) records 29 focused tests and 77 executed interact regressions (2 existing disabled tests), documentation rendering and main-specific mixin identity adaptation. Implementation is recorded in the checkpoint commit containing this update; the change was subsequently rebased and archived (see below).

Current action context archived on 2026-10-09 after rebase onto main `ce0c8352dee`: rebased planning `5c43e44b085`, implementation `be228adf4ac`. Fresh archive checks passed 29 focused tests and 77 interact regressions (2 existing disabled tests), documentation rendering and strict validation. Requirements are synced to the main specs.
