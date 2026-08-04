# Capability Reconciliation Ledger

## Core inventory

| Capability or issue | Maintenance intent | Main evidence | Classification | Follow-up |
|---|---|---|---|---|
| CAUSEWAY-3899 | Guard `DomainChangeRecord` handling | Matching main history | Equivalent | Regression verification only |
| CAUSEWAY-3942 | Remove persistence infrastructure methods from metamodel | Matching main history | Equivalent | Regression verification only |
| CAUSEWAY-3950 | Disable selected preview MIME types | Explicit Causeway 4 backport | Equivalent | None unless tests diverge |
| CAUSEWAY-3952 | Causeway 2 version and CI revision changes | Build-line-specific | Not applicable | None |
| CAUSEWAY-3955 | Deprecate legacy `Identifier` and `LogicalType` APIs | Causeway 4 has moved beyond the migration API | Not applicable | Verify only if an extension still uses a removed form |
| CAUSEWAY-3957 | Table actions, stable values, action links, form execution, and collection mementos | Predominantly explicit Causeway 4 backports | Equivalent | Selective test comparison |
| CAUSEWAY-3968 | Simplified method filtering | Explicit backport | Equivalent | None unless tests diverge |
| CAUSEWAY-3969 | JPA weaving safeguard | Matching main history | Equivalent | Regression verification only |
| CAUSEWAY-3972 | `_Oneshot` deadlock correction | Present on main | Equivalent | None |
| CAUSEWAY-3973 | Qualified facets and layout variants | Main has concurrent `FacetRank` and corresponding features | Equivalent | Selective layout tests |
| CAUSEWAY-3979 | Deprecate `RoutingServiceDefault` | Matching main history | Equivalent | None |
| CAUSEWAY-3983 | Remove always-hidden tab content | Matching main history | Equivalent | None |
| CAUSEWAY-3985 | Restrict table sorting to comparable values | Exact `isComparableOrOrdered()` capability exists | Equivalent | None |
| CAUSEWAY-3989 | Initial command replay and YAML baseline | Main retains the earlier baseline | Adapt | Reconcile later maintenance evolution through D1 and P2 |
| CAUSEWAY-3995 | Clear layout caches after metamodel initialization | Matching main history | Equivalent | None |
| CAUSEWAY-3996 | In-memory layout patching | Matching main history | Equivalent | None |
| CAUSEWAY-3997 | Listing and column-order fixes | Matching main history | Equivalent | Selective viewer regression tests |
| CAUSEWAY-3998 | Import commands from the oldest baseline | Matching main history | Equivalent | Reassess under unified manager |
| CAUSEWAY-4002 | Avoid unavailable IoC container during metamodel disposal | Main uses a different Spring context abstraction | Unresolved | Verify Causeway 4 disposal lifecycle separately |
| Unnumbered update-only publishing | Publish entity changes only for updates | `Publishing.ENABLED_FOR_UPDATES_ONLY` exists on main | Equivalent | None |

## Command recording and replay inventory

| Issues | Capability nodes | Current main state | Classification | Planned change |
|---|---|---|---|---|
| CAUSEWAY-4012, 4015, 4018 | C1 | Recording-support configuration, suppression marker, pause/resume events, fixture suppression, and helper suppression reconciled | Adapt | Completed by `reconcile-command-recording-core-policy` |
| CAUSEWAY-4012, 4033 | C2 | Recording-aware safe-action and authoritative property-edit command-publishing policy reconciled | Adapt | Completed by `reconcile-recording-aware-publishing` |
| CAUSEWAY-4019, 4020, 4021, 4034, 4038, 4039 | C3, C4b | Synthetic parented-collection selectors, scalar-reference navigation, and runtime return behavior reconciled | Adapt | Completed by `reconcile-synthetic-command-navigation` |
| CAUSEWAY-4030, 4034, 4039 | C4a | Scalar, bookmarkable view-model, and singleton-container result capture reconciled | Adapt | Completed by `reconcile-command-result-metadata` |
| CAUSEWAY-4010, 4024 | D1 | Result-bearing transfer DTOs, bookmark metadata, deep copying, and multi-document YAML foundations reconciled | Adapt | Completed by `reconcile-command-result-metadata` |
| CAUSEWAY-4042 | D2 | `InteractionAdvisorPolicy` absent | Adapt | `reconcile-command-replay-mapping` |
| CAUSEWAY-4010, 4039 | M1, M2 | Mapping SPI and in-memory listener absent | Supersede | `reconcile-command-replay-mapping` |
| Later maintenance mapping work | M3 | Mapping entity, repository, and persistent listener absent | Supersede | `reconcile-persistent-replay-mapping` |
| Maintenance consolidated replayable-command specs | P1 | Basic `ReplayableCommand` exists but lacks participants, result presence, actual mappings, and adjacent navigation | Supersede | `reconcile-replayable-command-projection` |
| Maintenance consolidated manager specs | P2 | Separate export and replay managers exist | Supersede | `reconcile-unified-command-manager` |
| CAUSEWAY-4034 and later maintenance specs | R1 | `RefData` and command replay reference-data SPI absent | Adapt | `reconcile-command-reference-data` |
| Later maintenance export specs | R2 | Baseline-bounded participant reachability validator absent | Supersede | `reconcile-command-export-reachability` |
| CAUSEWAY-4010 and later export specs | E1 | Legacy YAML export/import baseline exists | Supersede | `reconcile-command-export-import` |
| Later manager specs | W1 | Older export/replay actions exist but maintenance workflow differs materially | Supersede | `reconcile-command-manager-workflows` |
| Later background-completion specs | B1, B2 | Required recording/replay sequencing guards not established | Adapt | `reconcile-command-background-gates` |

## Completed reconciliation changes

| Change | Nodes | Main specification | Archived change | Implementation evidence |
|---|---|---|---|---|
| `reconcile-command-recording-core-policy` | C1 | `openspec/specs/command-recording-control/spec.md` | `openspec/changes/archive/2026-08-04-reconcile-command-recording-core-policy/` | Planning `1b7f593e9ef`; implementation `2ddd9bc37fd`; archive `1771c2e17e9`; focused Maven tests passed |
| `reconcile-command-result-metadata` | C4a, D1 | `openspec/specs/command-result-metadata/spec.md` | `openspec/changes/archive/2026-08-04-reconcile-command-result-metadata/` | Planning `2d386a8c4db`; implementation `79b093c2cfb`; archive `4a481394175`; focused and aggregate Maven tests passed |
| `reconcile-recording-aware-publishing` | C2 | `openspec/specs/recording-aware-command-publishing/spec.md` | `openspec/changes/archive/2026-08-04-reconcile-recording-aware-publishing/` | Planning `fc74666a15e`; implementation `35f1b39b3a7`; archive `37f6db329ab`; focused and aggregate Maven tests passed |
| `reconcile-synthetic-command-navigation` | C3, C4b | `openspec/specs/synthetic-command-navigation/spec.md` | `openspec/changes/archive/2026-08-04-reconcile-synthetic-command-navigation/` | Planning `9194e8317f8`; implementation `42b9ecbb433`; archive `ea0a1b8b4c1`; focused and affected aggregate Maven tests passed |

## Resolved questions

| Decision | Affected nodes | Resolution evidence |
|---|---|---|
| Recording-support configuration remains under `causeway.extensions.command-log` and uses Causeway 4 immutable record configuration. | C1, C2, C3 | `reconcile-command-recording-core-policy/design.md` in the archived change |
| Commandlog pause/resume nesting is application-context-wide and maintained with atomic pause depth. | C1 | `reconcile-command-recording-core-policy/design.md` and `CommandLogPauseStateTest` |
| Result capture accepts every bookmarkable single result, including view models, while replay stability remains a downstream policy decision. | C4a, D1 | `reconcile-command-result-metadata/design.md` in the archived change |
| Recording support broadens publishing through the normal facet lifecycle; explicit safe-action disablement remains an opt-out, while property edits remain recording-eligible even when explicitly disabled so replay sequences stay complete. | C2 | `reconcile-recording-aware-publishing/design.md` and `RecordingAwareCommandPublishingFacetTest` |
| Causeway 4 installs synthetic navigation actions through an `A0_BEFORE_BUILTIN` metamodel post-processor so later standard action processing observes them. | C3, C4b | `reconcile-synthetic-command-navigation/design.md` in the archived change and `SyntheticNavigationActionTest` |

## Open questions

| Question | Affected nodes | Resolution point |
|---|---|---|
| Can the Causeway 4 Spring context become unavailable during `ServiceRegistryDefault.select()` in metamodel disposal? | CAUSEWAY-4002 | Separate lifecycle investigation before core work is declared complete |
| Can replay mapping SPI remain entirely in commandlog applib, or does command executor integration require a narrower core hook? | D2, M1 | Replay-mapping design |
| How should persistent mapping selection work when both JPA and JDO modules are visible? | M3 | Persistent-mapping design |
| How will migration from separate `CommandExportManager` and `CommandReplayManager` view-model mementos be handled? | P2 | Unified-manager design |
| Which services count as export roots in the Causeway 4 service registry and logical-type model? | R2 | Reachability design |
| Which persistence operations are required to retimestamp commands safely under JPA and JDO? | W1 | Manager-workflow design |

## Acceptance evidence policy

Each child change must identify the maintenance scenarios it satisfies.
Tests should be ported by observable behaviour rather than copied mechanically.
Framework-level contracts should be tested in `api/applib`, `core/metamodel`, `core/runtimeservices`, or `core/mmtest` as appropriate.
Commandlog behaviour should be tested in commandlog applib integration tests and in both persistence adapters when storage semantics are involved.
Any deviation from maintenance behaviour must be recorded as an explicit Causeway 4 adaptation in the child change design.
