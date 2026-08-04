## Context

C1 introduced immutable `causeway.extensions.command-log.recording-support` configuration, the `CommandRecordingSuppressed` marker, and runtime suppression at `MemberExecutorServiceDefault.prepareCommandForPublishing(...)`.
C4a/D1 then added portable result metadata and broader bookmark capture without changing which interactions become commands.
Causeway 4 still applies its normal action and property command-publishing policies when constructing `CommandPublishingFacet` instances, so enabled recording support does not yet include ordinary safe actions or property edits.

Maintenance implements this capability in the metamodel facet factories rather than in the commandlog subscriber or runtime executor.
The Causeway 4 implementation must preserve that semantic boundary while adapting to current facet-holder APIs, immutable configuration, and existing C1 suppression.

## Goals / Non-Goals

**Goals:**

- Make enabled recording support sufficient to publish unannotated safe actions through the normal action command-publishing facet model.
- Make enabled recording support sufficient to publish ordinary property edits, including properties explicitly configured with disabled command publishing, so recorded mutation sequences remain complete.
- Preserve explicit `@Action(commandPublishing = DISABLED)` as a safe-action opt-out.
- Preserve existing action and property policies when recording support is disabled.
- Keep C1 target and owner suppression authoritative after facet eligibility is established.
- Ensure already-published actions and properties still produce one normal command publication rather than a second recording-specific publication.

**Non-Goals:**

- Do not synthesize parented-collection selector or scalar-reference navigation actions; those belong to C3/C4b.
- Do not change command result capture, DTOs, YAML, replay mapping, export/import, manager workflows, or background-completion policy.
- Do not introduce a safe-action-specific configuration property or alter the C1 recording-support configuration shape.
- Do not make result shape, bookmark availability, or later export reachability part of publishing eligibility.
- Do not change commandlog persistence schemas or add a subscriber-side fallback publisher.

## Decisions

### Use command-publishing facet selection as the only eligibility seam

Action and property facet creation will consult `CausewayConfiguration.Extensions.CommandLog.recordingSupport()` and install an appropriate `CommandPublishingFacet` when recording support is enabled.
The runtime executor and commandlog subscriber will continue to consume `CommandPublishingFacet.isPublishingEnabled()` through their existing publication flow.

This follows maintenance semantics and keeps command preparation, lifecycle notifications, DTO creation, and persistence on one path.
A runtime-executor override or commandlog-subscriber fallback was rejected because either would duplicate metamodel policy and risk incomplete or duplicate lifecycle publication.

### Add a recording-backed safe-action facet without changing state-changing action policy

An unannotated or `AS_CONFIGURED` action with safe semantics will receive a configuration-backed facet that reports enabled only while recording support is `ENABLED`, even when the ordinary global action policy is `NONE` or ignores safe actions.
An explicitly disabled safe action will retain its disabled annotation facet.
An explicitly enabled action or an action with a command DTO processor will retain the existing enabled facet.
Idempotent and non-idempotent actions will continue to follow existing annotation and global action publishing policies.

This preserves maintenance’s safe-action opt-out and avoids treating all action semantics as recording candidates.
Changing the global action command-publishing policy was rejected because that would broaden unrelated policy consumers and state-changing action behavior.

### Install one recording-support property facet before ordinary property policy selection

When recording support is enabled, property facet creation will select one property-specific recording facet before evaluating property annotation or global property publishing policy.
That facet reports publishing enabled for unannotated, `AS_CONFIGURED`, explicitly enabled, and explicitly disabled properties.
When recording support is disabled, existing annotation and global property publishing policy remain unchanged.

The explicit-disabled override is intentionally stronger for properties than for safe actions because replay recording must not silently omit state mutations.
Installing one facet rather than layering a fallback preserves one publication for properties already configured as published.
Changing the global property policy to `ALL` was rejected because it would blur configuration semantics and affect consumers beyond recording support.

### Keep C1 suppression downstream of metamodel eligibility

Recording-aware facets determine whether an interaction is eligible for normal command publication, but `MemberExecutorServiceDefault.prepareCommandForPublishing(...)` will remain the authoritative suppression boundary for marked targets and owners.
The facet factories will not attempt to inspect domain target types during metamodel construction.

This preserves contributed and mixin member behavior and avoids duplicating suppression rules across metamodel paths.
Focused tests will verify that enabled recording support does not bypass C1 suppression.

### Port observable maintenance scenarios onto Causeway 4 test seams

Metamodel tests will cover facet selection for action semantics, action annotations, property annotations, global policies, and enabled or disabled recording support.
Runtime or commandlog tests will cover suppressed interactions and the absence of duplicate publication where the existing test infrastructure can verify those boundaries without introducing persistence-adapter-specific behavior.

JPA and JDO persistence-matrix tests are not required unless implementation reveals adapter-specific commandlog behavior, because this change does not alter persistence and both interaction types remain on the existing subscriber path.

## Risks / Trade-offs

- [Risk] Recording support can substantially increase command volume by including safe queries and property edits.
  → Mitigation: keep recording support disabled by default and preserve explicit safe-action opt-outs plus C1 suppression.
- [Risk] Overriding explicit property publishing disablement is surprising.
  → Mitigation: make the replay-completeness rule normative, document the asymmetry with safe actions, and test it directly.
- [Risk] A fallback facet could coexist with an annotation facet and publish twice.
  → Mitigation: select exactly one facet in each action or property creation branch and add explicit non-duplication coverage.
- [Risk] Maintenance code differs structurally from Causeway 4 facet APIs.
  → Mitigation: port the policy into current `FacetHolder`, semantic-facet, and immutable-configuration conventions rather than copying source mechanically.
- [Risk] Suppression tests might pass at the metamodel level while runtime command preparation still publishes.
  → Mitigation: retain and extend runtime coverage around `prepareCommandForPublishing(...)` with recording-aware eligible members.

## Migration Plan

The change is opt-in through the existing recording-support setting, so applications that leave it disabled require no migration.
Applications enabling recording support will see additional safe-action and property-edit commands and can use explicit safe-action disablement or `CommandRecordingSuppressed` for intentional exclusions.
Rollback consists of disabling recording support or reverting the facet-selection changes; no stored schema migration is required.

## Open Questions

None.
Maintenance semantics resolve the deliberate difference between safe-action explicit opt-out and authoritative property-edit recording.
