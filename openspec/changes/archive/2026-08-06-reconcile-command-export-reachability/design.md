## Context

P1 projects command targets, reference-valued parameters, and recorded results without loading them. P2 supplies a unified manager whose `commandsInSequence` is ordered, bounded by a baseline and limit, and excludes ineligible or explicitly excluded entries. R1 supplies an OR-composed bookmark SPI for stable reference data. R2 must combine those contracts into contextual reachability while preserving Causeway 4's immutable configuration, Jakarta APIs, current metamodel, and view-model construction.

The maintenance implementation used a request scratchpad to let replayable commands discover their enclosing manager and used metamodel lookup to recognize domain-service bookmarks. Causeway 4's `ReplayContext` currently has neither reachability dependencies nor scratchpad state, and a `ReplayableCommand` can also be reconstructed independently from its identity memento.

## Goals / Non-Goals

**Goals:**

- Evaluate target and reference-parameter reachability using domain-service roots, R1 reference-data classification, and earlier results in manager order.
- Expose a derived, non-persisted `knownParticipants` value on replayable commands created by the unified manager.
- Resolve the R2 export-root question using Causeway 4 logical-type metamodel classification without loading bookmarked objects.
- Keep the validator independently testable and leave a single contract for E1 export enforcement.
- Preserve manager state, repository ordering, replay states, and command DTOs.

**Non-Goals:**

- Generate, validate, select, or import YAML; E1 owns those behaviors.
- Change legacy `CommandExportManager` or `CommandReplayManager` behavior.
- Add exclusion, restoration, deletion, movement, retimestamping, replay gating, or background-completion workflows.
- Persist reachability state, add database columns, or restore commandlog JDO.
- Resolve participants as local domain objects or interpret scalar parameters as reachability edges.

## Decisions

### Validate bookmark participants with a small ordered validator

Add a package-local `CommandKnownParticipantsValidator` that derives target bookmarks from `CommandDto.targets` (falling back to the command entry target for legacy DTO shapes) and reference bookmarks only from action parameters whose schema type is `REFERENCE`. It reports the first unknown participant with command and parameter identity, while scalar parameters and recorded results are not inputs requiring validation.

The validator accepts an export-root predicate. A bookmark is known when that predicate accepts it or it is contained in the set of results accumulated before the evaluated command. Results are added only after their owning command has been visited, so later results cannot make earlier commands reachable. Keeping parsing and ordering separate from framework services makes the rules directly unit-testable and reusable by E1.

Reusing `ReplayableCommandParticipant` objects was rejected because their `actualBookmark` semantics concern replay mappings and replay state, whereas export reachability deliberately uses recorded bookmarks.

### Treat domain-service logical types and reference data as roots

Extend `ReplayContext` with `CausewayConfiguration`, `SpecificationLoader`, and the injected list of `CommandReplayReferenceDataService` implementations. Root classification is the OR of:

- `SpecificationLoader.specForLogicalTypeName(bookmark.logicalTypeName()).map(ObjectSpecification::isDomainService)`; and
- `CommandReplayReferenceDataService.isReferenceData(services, bookmark)`.

This resolves the open question: the domain-service metamodel classification is the Causeway 4 representation of an application service declared with `@DomainService`. Merely finding a registered bean or resolving an entity bookmark is insufficient. Missing metamodel information yields non-root, and no bookmarked object is loaded.

Using `ServiceRegistry.lookupRegisteredBeanById` alone was rejected because the registry contains framework and non-contributing beans and does not express the export-root domain semantics as precisely as `ObjectSpecification.isDomainService()`.

### Pass manager context explicitly to replayable commands

Introduce a small `ReplayableCommandParticipantTracker` contract and give manager-created `ReplayableCommand` instances an explicit tracker reference. The normal injected/memento constructor supplies no tracker, making `knownParticipants` deterministically false outside manager context. The unified manager implements the tracker and wraps entries with itself; no request-global scratchpad mutation is required.

This is a Causeway 4 adaptation from maintenance's scratchpad hand-off. Explicit construction avoids stale or cross-manager context when more than one manager is rendered in a request and keeps the lifetime visible in tests.

### Derive prior knowledge from the manager's current sequence

For an evaluated interaction id, the manager reuses the same repository query, eligibility filter, exclusion filter, baseline, limit, and timestamp order as `commandsInSequence`. It accumulates each earlier entry's non-null recorded result and stops before the evaluated entry. Replay state does not otherwise remove an eligible, non-excluded entry from this context. A command earlier in the sequence can be unknown itself without poisoning later independent commands; its recorded result still becomes available according to maintenance behavior.

When recording support is disabled, a null entry is supplied, the command is absent from the current bounded sequence, or no tracker is present, `knownParticipants` is false. Computation has no persistence side effects.

Precomputing and persisting flags was rejected because movement and replay/workflow changes can alter ordering and the flag is inherently manager-contextual. With the manager's default page limit of 100, direct derivation is bounded; implementation may use per-manager in-memory memoization only if it preserves current ordering semantics.

### Present the value as review feedback only

Add the Boolean property after `hasResult` in the replayable-command and unified-manager fallback column orders. It remains visible as contextual review metadata but is not wired into existing export or replay actions in R2.

## Risks / Trade-offs

- [Repeated property reads can rescan a bounded sequence] → Keep work bounded by the manager limit and cover ordering behavior with focused tests before considering request-local memoization.
- [A logical type unavailable to the metamodel is classified as non-root] → Fail closed and permit applications to opt stable entities in through the R1 SPI.
- [Explicit tracker construction adds another replayable-command construction path] → Keep memento construction unchanged and test both manager-context and standalone behavior.
- [Maintenance used scratchpad context] → Record explicit tracker injection as the Causeway 4 adaptation and verify identical observable results without shared mutable request state.
- [R2 exposes feedback before export consumes it] → Keep the E1 boundary explicit in specs and scope tests so existing YAML and replay actions remain unchanged.

## Migration Plan

No datastore or bookmark migration is required. Deploy the commandlog applib change normally; rollback removes only derived UI metadata and reachability code. Existing manager and replayable-command mementos retain their formats.

## Open Questions

None blocking. E1 will decide the action-level message and selection workflow that consumes the R2 validator failure.
