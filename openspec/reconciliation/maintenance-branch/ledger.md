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
| CAUSEWAY-3989 | Initial command replay and YAML baseline | Main retains the earlier baseline | Adapt | D1 and P2 reconciled; complete later YAML evolution through E1 |
| CAUSEWAY-3995 | Clear layout caches after metamodel initialization | Matching main history | Equivalent | None |
| CAUSEWAY-3996 | In-memory layout patching | Matching main history | Equivalent | None |
| CAUSEWAY-3997 | Listing and column-order fixes | Matching main history | Equivalent | Selective viewer regression tests |
| CAUSEWAY-3998 | Import commands from the oldest baseline | Matching main history | Equivalent | Reassess through E1 under the unified manager |
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
| CAUSEWAY-4042 | D2 | Configurable command-execution advisor policies reconciled | Adapt | Completed by `reconcile-command-replay-mapping` |
| CAUSEWAY-4010, 4039 | M1, M2 | Bookmark-only remapping SPI, execution-copy remapping, atomic result observation, and conditional in-memory listener reconciled | Supersede | Completed by `reconcile-command-replay-mapping` |
| Later maintenance mapping work | M3 | Persistent mapping contracts, listener, management UI, and Jakarta Persistence adapter reconciled; the removed commandlog JDO adapter is not applicable on Causeway 4 | Supersede | Completed by `reconcile-persistent-replay-mapping` |
| Maintenance consolidated replayable-command specs | P1 | Replay-useful eligibility, result presence, bookmark participants, actual mappings, object links, identity mementos, layouts, and adjacent navigation reconciled | Supersede | Completed by `reconcile-replayable-command-projection` |
| Maintenance consolidated manager specs | P2 | Unified baseline/limit manager, four review collections, replay-state boundary, primary menu entry, and legacy-manager compatibility shims reconciled | Supersede | Completed by `reconcile-unified-command-manager` |
| CAUSEWAY-4034 and later maintenance specs | R1 | `RefData`, the bookmark-classification SPI, marker-backed default classifier, and SecMan declarations reconciled | Adapt | Completed by `reconcile-command-reference-data` |
| Later maintenance export specs | R2 | Baseline-bounded participant reachability, Causeway 4 export-root classification, and contextual manager feedback reconciled | Supersede | Completed by `reconcile-command-export-reachability` |
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
| `reconcile-command-replay-mapping` | D2, M1, M2 | `openspec/specs/command-execution-advisor-policy/spec.md`; `openspec/specs/command-replay-mapping/spec.md` | `openspec/changes/archive/2026-08-04-reconcile-command-replay-mapping/` | Planning `b9412ee98a7`; implementation `6b4ab64d193`; archive `74ba8b40c0a`; focused and affected aggregate Maven tests passed |
| `reconcile-persistent-replay-mapping` | M3 | `openspec/specs/persistent-command-replay-mapping/spec.md` | `openspec/changes/archive/2026-08-05-reconcile-persistent-replay-mapping/` | Planning `d88eb297388`; implementation `1990363924f`; archive `d95ee405f68`; focused and affected aggregate Maven tests passed |
| `reconcile-replayable-command-projection` | P1 | `openspec/specs/replayable-command-projection/spec.md` | `openspec/changes/archive/2026-08-06-reconcile-replayable-command-projection/` | Planning `2e65bb795a7`; implementation `4bc1b204484`; archive `30b53793cf2`; focused and full commandlog reactor Maven tests plus IDE inspection passed |
| `reconcile-unified-command-manager` | P2 | `openspec/specs/unified-command-manager/spec.md`; `openspec/specs/replayable-command-projection/spec.md` | `openspec/changes/archive/2026-08-06-reconcile-unified-command-manager/` | Planning `3ee2f7875cb`; implementation `5b95dd3b4a9`; archive `6811dfc6b1f`; focused commandlog applib and JPA Maven verification plus strict OpenSpec validation passed |
| `reconcile-command-reference-data` | R1 | `openspec/specs/command-export-refdata-marker/spec.md`; `openspec/specs/command-export-reference-data-participants/spec.md` | `openspec/changes/archive/2026-08-06-reconcile-command-reference-data/` | Planning `4d9156d2c24`; implementation `c4716bce636`; archive `aa8f7c5a8b7`; focused applib, commandlog applib, and SecMan applib Maven verification plus strict OpenSpec validation passed |
| `reconcile-command-export-reachability` | R2 | `openspec/specs/command-export-known-targets/spec.md`; `openspec/specs/replayable-command-exportability/spec.md`; `openspec/specs/unified-command-manager/spec.md` | `openspec/changes/archive/2026-08-06-reconcile-command-export-reachability/` | Planning `39813b4d6f2`; implementation `1e7b3930db9`; archive `83422722e3d`; commandlog applib Maven verification under JDK 21 plus strict OpenSpec validation passed |

## Resolved questions

| Decision | Affected nodes | Resolution evidence |
|---|---|---|
| Recording-support configuration remains under `causeway.extensions.command-log` and uses Causeway 4 immutable record configuration. | C1, C2, C3 | `reconcile-command-recording-core-policy/design.md` in the archived change |
| Commandlog pause/resume nesting is application-context-wide and maintained with atomic pause depth. | C1 | `reconcile-command-recording-core-policy/design.md` and `CommandLogPauseStateTest` |
| Result capture accepts every bookmarkable single result, including view models, while replay stability remains a downstream policy decision. | C4a, D1 | `reconcile-command-result-metadata/design.md` in the archived change |
| Recording support broadens publishing through the normal facet lifecycle; explicit safe-action disablement remains an opt-out, while property edits remain recording-eligible even when explicitly disabled so replay sequences stay complete. | C2 | `reconcile-recording-aware-publishing/design.md` and `RecordingAwareCommandPublishingFacetTest` |
| Causeway 4 installs synthetic navigation actions through an `A0_BEFORE_BUILTIN` metamodel post-processor so later standard action processing observes them. | C3, C4b | `reconcile-synthetic-command-navigation/design.md` in the archived change and `SyntheticNavigationActionTest` |
| Replay mapping policy remains entirely in commandlog applib through a bookmark-only listener SPI and result-remapping service; core runtime services implement only general command-execution advisor policy. | D2, M1, M2 | `reconcile-command-replay-mapping/design.md` in the archived change, `ResultRemappingServiceTest`, and `CommandExecutorInteractionAdvisorTest` |
| Persistent replay-result mappings use the Causeway 4 commandlog JPA module only; the commandlog JDO adapter was deliberately removed and is not restored. | M3 | `reconcile-persistent-replay-mapping/design.md`, `CommandReplayResultMapping_IntegTest`, and the Causeway 4 commandlog module inventory |
| Replayable-command participants remain a derived read model; actual bookmarks come through the mapping SPI, result mappings remain gated until replay succeeds, and pending-or-failed imported work bypasses general eligibility. | P1 | `reconcile-replayable-command-projection/design.md`, `ReplayableCommandParticipantTest`, and `CommandManagerEligibilityTest` |
| The unified manager is the forward path with a baseline/limit memento; both legacy manager logical types and timestamp-only mementos remain loadable compatibility shims, their standard launchers are hidden, and stored bookmarks are not rewritten. | P2 | `reconcile-unified-command-manager/design.md`, `CommandManagerCompatibilityTest`, and `CommandLogMenuTest` |
| Reference data is an explicit stability assertion made through the dependency-neutral `RefData` marker or application bookmark classifiers; the default classifier uses metamodel type assignability without loading domain objects, and the built-in SecMan identity abstractions opt in. | R1 | `reconcile-command-reference-data/design.md`, `CommandReplayReferenceDataServiceForRefDataTest`, and `ReferenceDataContractTest` |
| Export roots are metamodel logical types classified as domain services, OR-composed with R1 reference-data classifiers; ordinary bookmarked objects are never loaded merely to establish reachability, and manager context is passed explicitly rather than through request-global scratchpad state. | R2 | `reconcile-command-export-reachability/design.md`, `CommandKnownParticipantsValidatorTest`, and `CommandManagerKnownParticipantsTest` |

## Open questions

| Question | Affected nodes | Resolution point |
|---|---|---|
| Can the Causeway 4 Spring context become unavailable during `ServiceRegistryDefault.select()` in metamodel disposal? | CAUSEWAY-4002 | Separate lifecycle investigation before core work is declared complete |
| Which persistence operations are required to retimestamp commands safely under JPA and JDO? | W1 | Manager-workflow design |

## Acceptance evidence policy

Each child change must identify the maintenance scenarios it satisfies.
Tests should be ported by observable behaviour rather than copied mechanically.
Framework-level contracts should be tested in `api/applib`, `core/metamodel`, `core/runtimeservices`, or `core/mmtest` as appropriate.
Commandlog behaviour should be tested in commandlog applib integration tests and in every applicable persistence adapter when storage semantics are involved; omitted maintenance adapters must be justified as explicit Causeway 4 adaptations.
Any deviation from maintenance behaviour must be recorded as an explicit Causeway 4 adaptation in the child change design.
