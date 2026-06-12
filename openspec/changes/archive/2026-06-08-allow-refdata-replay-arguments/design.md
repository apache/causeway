## Context

Command export validation currently treats a selected command participant as known only when its bookmark is a menu-domain-service export root or appears as the result of an earlier selected command in baseline-bounded export order.
That dotted-path rule protects replay by ensuring the target graph can be reconstructed in a replay environment, but it rejects stable reference-data entities that applications expect to pre-exist in every environment.
The command-log extension already centralizes this validation in `CommandExportKnownTargetValidator`, with `CommandExportManager` providing the export-root predicate and exportability context.

## Goals / Non-Goals

**Goals:**

- Provide a public SPI that lets consuming applications classify bookmarked entities as replay reference data.
- Support multiple SPI implementations and treat an entity as reference data when any implementation accepts it.
- Reuse the same reference-data decision for export action validation and exportability calculation.
- Preserve existing dotted-path validation for ordinary mutable entities.

**Non-Goals:**

- Automatically infer reference data from persistence annotations, table names, or local resolvability.
- Change replay input remapping semantics for non-reference-data entities.
- Change YAML shape or exported command ordering.
- Guarantee that reference data exists in the replay environment beyond delegating that responsibility to the consuming application.

## Decisions

### Add a command-log SPI based on bookmarks

Introduce a public service interface in the command-log applib, for example `CommandReplayReferenceDataService`, with a method such as `boolean isReferenceData(Bookmark bookmark)`.
The SPI works from a `Bookmark` because export validation already has logical type and identifier data and should not need to instantiate the entity to classify it.

Alternative considered: resolve the entity and pass the object instance to the SPI.
This was rejected because validation should remain cheap, should not depend on local database presence, and should not accidentally make local resolvability part of the export contract.

### Compose implementations with OR semantics

Inject all registered SPI implementations into the export manager or a small classifier adapter, and consider a bookmark reference data if any implementation returns `true`.
An empty implementation list means no reference-data participants are accepted and existing behavior remains unchanged.

Alternative considered: require a single implementation bean.
This was rejected because applications and extensions may each own different reference-data domains.

### Treat reference data as an export root equivalent for validation

Extend the known-participant predicate so that a bookmark is known when it is a menu-domain-service root, an application-declared reference-data entity, or a previously produced result in the baseline-bounded selected sequence.
Use the same predicate in full export validation and in `ReplayableCommand` exportability calculations.

Alternative considered: add reference-data bookmarks to the known-target set at startup.
This was rejected because classification is predicate-based and does not require enumerating all possible reference-data rows.

## Risks / Trade-offs

- Application classifier is wrong → Replay can still fail if the replay environment lacks the classified entity; document that SPI implementations must only accept stable cross-environment data.
- Multiple classifiers disagree → OR semantics deliberately favors permissiveness; keep implementations small and test their scopes.
- SPI method throws → Validation should treat exceptions consistently with existing service failures rather than silently accepting the bookmark.
- Classification by bookmark is less expressive than object inspection → This preserves replay-environment independence and avoids loading entities during export validation.
