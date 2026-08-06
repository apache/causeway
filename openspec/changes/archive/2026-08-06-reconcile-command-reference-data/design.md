## Context

P2 established the unified, baseline-bounded command sequence, while R2 will decide whether every target and reference parameter is reachable from export roots, earlier results, or stable reference data. Causeway 4 currently has no reference-data classification contract. The maintenance branch added a bookmark-based SPI, a marker-backed default implementation, and marker declarations on four SecMan domain abstractions.

R1 must establish those reusable contracts without pulling R2 validation into the manager. The public marker belongs in `api/applib` so application domain types and unrelated extensions can opt in without depending on commandlog. The classifier belongs in commandlog applib, which already depends on Causeway's metamodel and can register its default implementation through the existing module import list.

## Goals / Non-Goals

**Goals:**

- Provide a dependency-neutral marker for domain types whose bookmarked identities are stable across replay environments.
- Provide a public commandlog SPI that classifies bookmarks and composes any number of implementations using OR semantics.
- Supply and register a conservative default classifier based only on metamodel type information.
- Preserve the maintenance branch's built-in SecMan marker declarations.
- Define focused API, classification, module-registration, and non-loading tests and documentation.

**Non-Goals:**

- Do not calculate known participants, export roots, or baseline-bounded reachability; those belong to R2.
- Do not change `ReplayableCommand` exportability, command-manager collections, YAML export/import, replay remapping, or workflow actions.
- Do not infer reference data from persistence annotations, table names, packages, local object resolvability, or fixture conventions.
- Do not introduce persistent state, a schema migration, or a commandlog JDO adapter.

## Decisions

### Put `RefData` in the public applib domain package

Add the empty marker as `org.apache.causeway.applib.domain.RefData`. Implementing it asserts that a domain type's logical type and bookmark identifiers are well-known and expected to exist in every replay environment. It has no methods and does not itself depend on commandlog.

Keeping the marker in commandlog was rejected because application domain classes and SecMan would then acquire a reverse dependency on an optional extension. An annotation was rejected because the maintenance contract uses assignability and needs no annotation metadata.

### Classify bookmarks through a small commandlog SPI

Add `CommandReplayReferenceDataService` under the commandlog applib SPI package with `boolean isReferenceData(Bookmark)`. Provide a null-safe static composition helper over a list of implementations. A bookmark is reference data when any non-null implementation accepts it; null input, a null list, an empty list, or no accepting implementation produces `false`.

Passing resolved objects was rejected because classification must not require the entity to exist in the source datastore. Selecting one privileged implementation was rejected because applications and extensions can own independent reference-data domains.

### Implement the default classifier with `SpecificationLoader`

Register `CommandReplayReferenceDataServiceForRefData` through `CausewayModuleExtCommandLogApplib`. It asks `SpecificationLoader.specForBookmark` for the bookmark's `ObjectSpecification`, obtains the corresponding Java class, and returns whether `RefData` is assignable from that class. Null bookmarks and unresolved types return `false`; the classifier never resolves or instantiates the bookmarked object.

Using repositories or object loading was rejected because local row existence is not evidence of cross-environment stability. Duplicating logical-type lookup outside the metamodel was rejected because `SpecificationLoader` already owns Causeway 4 bookmark-to-specification resolution.

### Preserve the maintenance SecMan opt-ins

Make `ApplicationUser`, `ApplicationRole`, `ApplicationTenancy`, and `ApplicationPermission` extend `RefData` in their Causeway 4 interface-based forms. This is a source-compatible declaration and adds no fields or persistence behavior. No other framework or application type is inferred to be reference data.

Omitting these declarations was rejected because they are explicit maintenance behavior rather than an R2 reachability choice. Broadening the marker to all SecMan types was rejected because the marker is an explicit stability assertion.

### Keep R1 classification disconnected from export decisions

R1 registers the classifier and tests composition, but does not inject it into `CommandManager`, legacy managers, or `ReplayableCommand`. R2 will combine classification with its export-root and earlier-result predicates once the open export-root decision is resolved.

Integrating the SPI immediately into legacy export validation was rejected because that would partially implement R2 before its reachability model and Causeway 4 service-root adaptation are specified.

## Risks / Trade-offs

- [An application marks a mutable or environment-local type as `RefData`] → Document the stability assertion prominently; R2 can only trust the classification contract and cannot prove cross-environment existence.
- [A custom classifier throws] → Do not silently convert failures into acceptance or rejection; preserve normal service failure semantics so configuration errors remain visible.
- [A logical type is unknown during metamodel lookup] → Return `false`, retaining the conservative classification default.
- [SecMan identities differ between replay environments] → The maintenance behavior intentionally treats their identities as reference data; document that deployments using replay must provision matching identities.
- [R1 adds API before it affects exportability] → Keep the slice independently tested and explicitly document that R2 is the first consumer of the classification result.

## Migration Plan

Deploy applib, commandlog applib, and SecMan applib changes together. Existing applications remain unclassified unless their domain classes implement `RefData` or they register a custom SPI; no data conversion or bookmark rewrite is required. Rollback removes the marker declarations, classifier, and SPI without datastore repair.

## Open Questions

None blocking for R1. Which services count as Causeway 4 export roots remains an R2 design decision.
