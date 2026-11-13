## Context

P1 is the next dependency-ordered reconciliation slice after D1 and M1-M3. Causeway 4 already has a `ReplayableCommand` view model, separate export and replay managers, bookmark-only replay remapping, recorded command-result bookmarks, and durable replay-result mappings. It does not yet expose the maintenance branch's participant-oriented read model, result-presence indicator, or adjacent navigation.

The maintenance specifications span later concerns as well: eligibility is consumed by the P2 unified manager, known-participant state belongs to R2, replay action background gating belongs to B2, and several workflow actions belong to W1. This change must establish the reusable P1 projection while preserving the current Causeway 4 manager types and their view-model mementos.

## Goals / Non-Goals

**Goals:**

- Enrich `ReplayableCommand` with non-persisted result presence and recorded/actual participant projections.
- Represent target, reference-parameter, and result participants using bookmark-portable view models with best-effort local object links.
- Reuse the M1 mapping SPI for actual-bookmark lookup and preserve the replay-state distinctions in the maintenance behavior.
- Define reusable wrapping eligibility and apply it to existing general sequence/review collections and adjacent navigation, while retaining every repository result in the pending-or-failed replay queue.
- Add safe previous/next navigation using existing commandlog ordering and the current replay context.
- Adapt the maintenance behavior to Causeway 4 APIs, Jakarta, and the current commandlog persistence split.

**Non-Goals:**

- Do not replace or merge `CommandExportManager` and `CommandReplayManager`, change their logical types or mementos, or introduce the P2 baseline-bounded `CommandManager` collections.
- Do not add participant tracking, `knownParticipants`, export-root/reference-data classification, or reachability validation from R1/R2.
- Do not change replay/retry action state eligibility; the maintenance action contract remains split across P2 and B2.
- Do not change YAML export/import, selection validation, exclusion/restoration/deletion/reordering workflows, or background-completion gates from E1, W1, or B1/B2.
- Do not change replay-result persistence, conflict policy, replay transaction boundaries, or datastore schema.
- Do not restore the removed commandlog JDO module.

## Decisions

### Treat participants as a derived read model

Add a `ReplayableCommandParticipant` view model with `TARGET`, `PARAMETER`, and `RESULT` roles. `ReplayableCommand` derives rows from its current `CommandLogEntry`: every recorded target, reference-valued action parameter, and recorded result becomes a participant; scalar parameters are ignored. No participant state is persisted.

This keeps the authoritative state in the command DTO, command result bookmark, and replay-mapping listener. Persisting participant rows was rejected because it would duplicate DTO state, introduce synchronization problems, and overlap neither P1 nor M3's intended ownership.

### Resolve actual bookmarks through the existing remapping service

Extend `ReplayContext` with the applib services needed by the projection, notably `BookmarkService` and `ApplicationFeatureRepository`, while continuing to use `ResultRemappingService` as the only mapping facade. Preserve the existing constructor as a compatibility overload while module wiring supplies the new services. Target and reference-parameter participants show an explicit mapping in any replay state; after replay state `OK`, an unmapped bookmark falls back to the recorded bookmark. Before successful replay an unmapped target or parameter has no actual bookmark.

A result participant always exposes its recorded bookmark, but exposes an actual bookmark only when replay state is `OK`; it then uses an explicit mapping or falls back to the recorded bookmark. This state gate follows the normative maintenance specification even if historical maintenance code could surface a pre-existing result mapping earlier. Direct dependency on the persistent mapping repository was rejected because P1 must work identically with custom, in-memory, and persistent listeners.

### Use identity-only participant mementos and derive on rehydration

Participant mementos contain only the owning interaction id and participant identity: `--target`, `--parameter--<name>`, or `--result`. Rehydration reconstructs the owning `ReplayableCommand`, finds the matching derived participant, and recalculates bookmarks and object links. Recorded or actual bookmarks are not embedded in the memento.

This produces readable, stable links without freezing mutable replay state into URLs. Encoding the full row was rejected because actual mappings can change between requests and bookmarks can contain values awkward for durable mementos.

### Resolve participant objects only from actual bookmarks

The target, argument, and result properties use `BookmarkService` to resolve the participant's actual bookmark, returning no object when the bookmark is absent or unresolved. Resolution is role-specific and does not affect replay state. Recorded bookmarks are never required to resolve for the participant row to exist.

This makes the read model portable between source and replay systems. Resolving the recorded bookmark as a fallback was rejected because it could display the wrong local object after remapping.

### Centralize conservative replayable-command eligibility

Add one reusable eligibility policy that distinguishes safe actions by resolving the entry's logical member identifier through the public `ApplicationFeatureRepository` and reading its action semantics. State-changing commands remain eligible. Safe actions are eligible in general command-sequence/review projections only when the entry stores a result bookmark. If member semantics cannot be resolved, the policy retains the entry rather than hiding potentially state-changing work. Depending directly on core metamodel internals was rejected because the commandlog applib can express this policy through its existing public applib dependency.

Existing export and completed/excluded collections apply this policy at their wrapping boundary without changing manager identity or workflow. The pending-or-failed replay collection deliberately bypasses it so imported work remains visible. This is the minimum adapter needed to make P1 behavior observable before P2; introducing the maintenance `CommandManager` or its collection names is deferred.

### Navigate by persistence-neutral foreground ordering

Extend the command-entry repository contract and reusable base with bounded foreground-before and foreground-since queries backed by existing named-query conventions. `ReplayableCommand` scans in repository order for the nearest eligible entry, excluding itself from the forward result, and constructs the adjacent row with the same `ReplayContext`. Safe previous/next mixins are disabled at boundaries and never mutate entries.

Loading a complete manager collection and navigating by list index was rejected because a replayable command can be opened directly and must not depend on a P2 manager instance. Querying belongs behind the existing repository abstraction; JPA receives only the named-query wiring needed for these reads and no schema change.

### Make participants the detailed navigation surface

Update fallback layouts so the replayable command shows `hasResult` and a Participants table before replay controls. The participant layout uses the maintenance contract's three-column recorded-versus-actual presentation: a width-four identity/metadata column, a recorded-side column, and an actual-side column. It shows owning command, role, parameter name, recorded bookmark, role-specific object link, and actual bookmark. Remove the redundant command-level target summary/open-target presentation from the fallback object view while retaining compatibility methods only if required by existing public callers.

The module registers the participant type and previous/next mixins explicitly. Known-participant columns are not added; P1 orders `hasResult` so R2 can append `knownParticipants` later without revisiting the contract.

## Risks / Trade-offs

- [Risk] Metamodel lookup cannot classify an imported or removed action. → Treat unresolved semantics conservatively as eligible and test the fallback.
- [Risk] Adjacent queries can return ineligible safe actions before the true neighbor. → Scan ordered foreground results until the nearest eligible entry is found rather than applying a limit before eligibility filtering.
- [Risk] Timestamp ties can make adjacency ambiguous. → Preserve the repository's established commandlog ordering and document/test the same ordering contract; do not invent P2 sequence state in this slice.
- [Risk] Participant object lookup can fail on a replay system. → Keep all bookmark properties visible and object-valued properties optional.
- [Risk] Adding services to `ReplayContext` affects its constructors and tests. → Retain the existing constructor as a compatibility overload, update the single module bean and focused fixtures, and keep interaction-id-only `ReplayableCommand` mementos.
- [Trade-off] Existing managers receive small collection-filter adaptations before their P2 replacement. → Limit edits to the shared eligibility boundary and preserve all manager actions, logical types, mementos, and baseline behavior.
- [Trade-off] Result participants deliberately hide an otherwise known mapping until replay state is `OK`. → Follow the normative maintenance scenario so the UI represents this command's replay outcome rather than global mapping availability.

## Migration Plan

This is an additive applib/read-query change with no datastore migration. Applications keep their existing export and replay manager bookmarks and configuration. Deploy the updated commandlog applib and applicable JPA module together so adjacent-query named queries are registered. Rollback restores the earlier view models and query wiring; persisted command entries and replay-result mappings remain compatible.

## Open Questions

None blocking. P2 retains the open decision about migrating separate manager mementos; R2 retains export-root and known-participant semantics.
