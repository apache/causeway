## Context

C1 established the immutable `causeway.extensions.command-log.recording-support` switch and authoritative `CommandRecordingSuppressed` marker.
C2 made ordinary safe actions and property edits recording-eligible through normal command-publishing facets.
C4a and D1 established bookmarkable single-result capture and result-bearing transfer DTOs.
Causeway 4 still exposes collection and reference traversal only as association navigation, so these user transitions have no invokable member identity that can enter the command lifecycle.

Maintenance solved this by adding synthetic `ObjectAction` instances during metamodel post-processing, with facets that model identity, association, parameters, usability, validation, invocation, and publication.
The maintenance implementation lives in an older specification-loader hierarchy, while Causeway 4 builds `ObjectSpecificationDefault` members and then invokes ordered `MetaModelPostProcessor` instances.
Causeway 4 already provides an `A0_BEFORE_BUILTIN` post-processing order and invokes `postProcessObject(...)` before later processors visit runtime actions, making that the appropriate adaptation seam.

## Goals / Non-Goals

**Goals:**

- Represent eligible parented-collection and scalar-reference traversal as deterministic safe `ObjectAction` members only when recording support is enabled.
- Preserve maintenance-visible identity, layout, parameter, matching, usability, validation, and invocation behavior.
- Support entity and view-model owners while respecting type-level command-recording suppression.
- Feed synthetic actions through the existing C2 command-publishing and C4a result-capture paths without duplicate publication or special result formats.
- Keep disabled recording support behavior identical to current Causeway 4 association navigation.

**Non-Goals:**

- Replay mapping SPI, imported-command adaptation, or `InteractionAdvisorPolicy` work from D2/M1/M2.
- Synthetic previous/next actions on `ReplayableCommand` from P1.
- Unified command management, reference data, reachability validation, export/import workflows, or background-completion gates.
- A separate feature flag for synthetic navigation actions.
- Changing developer-authored action ids or ordinary collection/reference rendering.

## Decisions

### Install synthetic actions with an early Causeway 4 metamodel post-processor

Add a built-in `SynthesizeNavigationActionsPostProcessor` at `PostProcessingOrder.A0_BEFORE_BUILTIN` in `ProgrammingModelDefault`.
Its object-level phase will inspect the completed associations and append synthetic actions before later built-in post-processors iterate runtime actions.
This resolves the ledger question about the Causeway 4 installation seam while preserving normal downstream naming, translation, authorization, and other action processing.

An extension-provided `MetaModelRefiner` was rejected because navigation actions are core metamodel representations whose availability is controlled by core configuration already visible through `MetaModelContext`.
Adding actions in an association callback was rejected because `PostProcessor` visits the object before member callbacks and later processors need a stable complete action list.

### Gate creation once, from existing recording configuration and owner type

The synthesis processor will do nothing unless `recordingSupport` is `ENABLED`.
It will synthesize members only for entity or view-model owner specifications that do not implement `CommandRecordingSuppressed`.
Parented collections are eligible when their element specification is an entity, view model, or abstract domain type; scalar properties are eligible only when their return specification represents a domain-object reference rather than a value.

Creation-time gating was chosen over hidden facets because disabled recording support must leave the ordinary action list unchanged and suppression must prevent a command-capable member from existing at all.
No additional boolean setting will be introduced.

### Use deterministic synthetic faceted methods and dedicated marker facets

Each generated action will be represented by a synthetic `FacetedMethod` and the existing `ObjectActionDefault` runtime abstraction, rather than a parallel action implementation.
A controlled Causeway 4 specification mutation seam will append generated actions and rebuild action indexes without exposing general mutable metamodel APIs.

Collection and reference actions will carry dedicated marker facets so framework code and tests can distinguish them from developer-authored actions without parsing ids.
Both use a reserved `__causeway_navigate_to_` id namespace derived from the associated member id and must avoid collisions with authored actions.
They will carry safe semantics, `Navigate To` naming, associated-member layout metadata, secondary-button CSS, and the select-navigation icon.

A custom external action type was rejected because normal authorization, publication, invocation, and result capture already operate on `ObjectAction` and facets.

### Derive selector parameters from rendered collection columns

A parented-collection selector uses the current action target as the collection owner and does not expose that owner as a parameter.
Optional filter parameters are generated only for eligible child properties represented by collection columns, in explicit collection-column order.
Eligible filters include scalar values other than blobs, clobs, and known technical metadata properties, plus references with bounded, choices, or autocomplete semantics.
Child collections and unconstrained references are excluded.
Boolean filters are represented as optional tri-valued predicates so omitted, explicit `false`, and explicit `true` remain distinct.

Using every child property was rejected because it diverges from the visible collection and creates unstable or unusable replay prompts.
Property declaration order is only a fallback when no explicit collection-column order exists.

### Centralize selector matching for validation and invocation

One matching utility will read the associated collection from the current owner and apply supplied filters.
String filters use containment; other scalar values and references use exact equality.
Validation succeeds only for exactly one match and reports distinct no-match and ambiguous-match reasons.
Invocation repeats the same cardinality check and fails clearly if callers bypass validation, preventing validation and execution from drifting.
The invocation facet returns the single managed child object through the normal result path.

### Model dynamic availability as ordinary usability facets

Collection selectors remain present but are disabled when the associated collection is empty.
Scalar-reference actions remain present but are disabled when the associated reference is null.
Reference invocation reads and returns the associated property value and fails clearly if called directly while null.

Hiding or removing actions dynamically was rejected because stable metamodel identity is required for command DTOs and tooling.

### Reuse normal publication and result behavior

Synthetic actions are safe actions and therefore use the recording-aware publishing policy already implemented by C2.
No subscriber fallback and no second publication path will be added.
Their single domain-object results flow through C4a result capture and D1 transfer metadata unchanged.
Where command DTO execution needs to reconstruct synthetic selector arguments, runtime adaptation will be limited to recognizing the marker facet and mapping recorded filter values to the generated parameter model; broader replay policy remains deferred.

## Risks / Trade-offs

- **Risk: Mutating an object specification during post-processing can invalidate action caches or indexes.** → Provide one narrow append/rebuild operation and test all action lookup and stream variants.
- **Risk: Post-processor ordering could leave generated actions without normal facets.** → Register synthesis at `A0_BEFORE_BUILTIN` and add tests proving later action processors observe the synthetic members.
- **Risk: Abstract element specifications or recursively loading association types can trigger introspection cycles.** → Reuse specification-loader deferred lookup patterns and test abstract element types and view-model owners.
- **Risk: Column-order metadata can differ across grid sources.** → Use the resolved parented collection metadata visible in the final metamodel and preserve eligible relative order.
- **Risk: Partial string matching can produce ambiguous recordings.** → Require exactly one match during validation and invocation.
- **Risk: Reserved ids can collide with authored members.** → Check existing action ids and fail metamodel validation clearly rather than silently replacing an action.
- **Trade-off: Synthetic action count grows with eligible associations.** → Generate only under explicit recording-support opt-in and suppress entire marked owner types.

## Migration Plan

The feature remains off by default because recording support defaults to `DISABLED`.
Applications that already enable recording support will gain synthetic navigation actions after upgrading, without adding configuration.
Disabling recording support removes the generated members on the next metamodel build and restores existing Causeway 4 navigation behavior.
No persisted schema or public DTO migration is required because deterministic action ids and ordinary command DTO/result contracts are used.

## Open Questions

None for this change.
The Causeway 4 post-processing seam and the maintenance view-model result policy are resolved by the decisions above; replay-time mapping and advisor behavior remain explicit downstream work.
