## Context

The completed D2/M1/M2 reconciliation added bookmark-only replay input lookup, replay-result observation inside the replay transaction, and a built-in in-memory listener. It also introduced `causeway.extensions.command-log.replay-result-mapping.storage-strategy=PERSISTENT`, but deliberately provides no listener for that selection.

Maintenance now has a persistent mapping model, repository abstraction, JPA and JDO adapters, query/menu actions, interaction-id capture, and deletion. Causeway 4 differs materially: CAUSEWAY-3866 removed the commandlog JDO persistence module, and the current commandlog reactor contains only applib and JPA persistence modules. This change therefore ports the observable persistent-mapping behavior onto Jakarta Persistence and Spring Boot 4 without restoring the removed JDO adapter.

The persistent listener is called by the existing `ResultRemappingService` in the replay transaction. Its stored mappings must remain portable bookmarks and must preserve the first observed mapping and interaction id across retries and restarts.

## Goals / Non-Goals

**Goals:**

- Complete M3 with a durable built-in replay-mapping listener selected by the existing `PERSISTENT` strategy.
- Persist identity and changed recorded-to-actual bookmark mappings and their optional originating interaction id.
- Preserve the M2 first-observation, idempotency, conflict-policy, lookup, custom-listener back-off, and transactional semantics.
- Provide common applib domain/repository/UI contracts and a concrete Causeway 4 JPA implementation.
- Make persisted mappings inspectable and removable through commandlog UI actions.
- Resolve the programme's JPA/JDO selection question explicitly for Causeway 4.

**Non-Goals:**

- Do not change `CommandReplayMappingListener`, `ResultRemappingService`, or the replay transaction boundary.
- Do not restore the removed commandlog JDO persistence module or add a new persistence selector beyond the existing mapping storage strategy.
- Do not add replayable-command participant projections, unified manager behavior, export validation/import, reference data, reachability, manager workflows, or background gates.
- Do not migrate transient mappings already held by an in-memory listener into the datastore.
- Do not add paging or an external/distributed cache.

## Decisions

### Implement the Causeway 4 adapter for JPA only

Add the concrete mapping entity and repository to `extensions/core/commandlog/persistence-jpa` using Jakarta Persistence, the existing Causeway bookmark converter, entity listener, module import, and entity-scan conventions. The former commandlog JDO module was deliberately removed from Causeway 4 and is not a viable runtime choice, so recreating it solely for M3 would reverse an architectural decision and expand scope substantially.

The alternative of porting both maintenance adapters was rejected because no Causeway 4 commandlog JDO entry repository or reactor module remains. Consequently, there is no dual-adapter ambiguity to resolve: the active commandlog persistence module supplies the single repository, which on Causeway 4 is JPA.

### Keep persistence-neutral contracts and behavior in commandlog applib

Define an abstract `CommandReplayResultMapping` domain type, a repository interface, and a reusable repository base in commandlog applib. The abstract mapping exposes recorded and actual `Bookmark` values plus an optional command interaction id. The repository supports lookup by recorded bookmark, lookup by actual bookmark, listing all and changed mappings, creation, and removal.

The persistent listener also lives in applib and depends only on this repository contract. This mirrors the existing commandlog persistence split, keeps bookmark logic independent of JPA, and leaves applications free to provide their own repository implementation without changing the replay SPI.

Putting the listener directly in the JPA module was considered, but rejected because conflict and lookup behavior is commandlog policy rather than datastore-specific behavior.

### Preserve the first mapping with datastore enforcement

Persist every first observation, including identity mappings. Make recorded bookmark unique and indexed, and index actual bookmark for reverse lookup. The listener first queries by recorded bookmark:

- no row: create and flush a row containing recorded bookmark, actual bookmark, and the supplied interaction id;
- same actual bookmark: treat the observation as idempotent and retain the original row and interaction id;
- different actual bookmark: retain the original row and either throw or log according to the existing conflict policy.

The database uniqueness constraint is the final guard against concurrent first observations. A concurrent uniqueness failure propagates from the replay transaction rather than overwriting an established mapping. An upsert was rejected because it could make the winning mapping datastore-dependent and violate first-observation semantics.

### Reuse the existing conditional built-in-listener contract

Register the persistent listener only when storage strategy is `PERSISTENT`, a `CommandReplayResultMappingRepository` bean is available, and no application-defined `CommandReplayMappingListener` bean exists. The current in-memory listener remains selected only for `IN_MEMORY`; the two built-ins are therefore mutually exclusive. A custom listener remains authoritative in either mode.

The JPA commandlog module registers its concrete repository and imports the persistent-listener bean factory. If an application selects `PERSISTENT` without the JPA module or another repository implementation, no built-in listener is created, matching the existing conditional contract and avoiding a broken bean graph.

### Expose persistent data through optional commandlog UI integration

Inject the repository optionally into `CommandLogMenu`. Add safe prototyping actions to list all mappings, list changed mappings, find by recorded bookmark, and find by actual bookmark. Add an idempotent are-you-sure action to delete all mappings, reporting the number deleted without touching command log entries. Hide all mapping menu actions when no repository exists.

Provide fallback layout metadata showing recorded bookmark, actual bookmark, and command interaction id. Contribute an idempotent are-you-sure delete mixin for removing one mapping. Place export/replay actions before mapping finders and bulk deletion after the finders to keep the maintenance workflow ordering.

Keeping these actions in JPA was rejected because the UI contract is persistence-neutral and should disappear naturally when no repository implementation is present.

### Treat the mapping table as an additive schema change

Use the commandlog schema and a dedicated replay-result-mapping table with a generated surrogate key, non-null recorded and actual bookmark columns, and a nullable UUID interaction-id column. Registration and teardown are extended in the JPA module. Existing commandlog rows and replay DTOs are unchanged.

## Risks / Trade-offs

- [Risk] Selecting `PERSISTENT` without a repository silently yields no built-in listener. → Document the JPA module requirement and cover present/absent repository contexts explicitly.
- [Risk] Two concurrent replays can both observe no mapping before insertion. → Enforce recorded-bookmark uniqueness, flush creation inside the replay transaction, and propagate the losing transaction's persistence failure rather than overwrite data.
- [Risk] Bookmark column length or conversion could diverge from existing commandlog storage. → Reuse Causeway's `Bookmark` converter and established commandlog length constants/conventions.
- [Risk] Listing all mappings can be large. → Match maintenance behavior for this reconciliation slice; retain focused finders and defer paging until usage demonstrates a need.
- [Risk] Menu deletion is operationally destructive. → Use are-you-sure semantics, restrict the menu actions consistently with existing prototyping commandlog tools, and ensure deletion targets only mapping rows.
- [Trade-off] JPA-only behavior differs from maintenance's JPA/JDO matrix. → Record this as an explicit Causeway 4 adaptation justified by removal of the commandlog JDO module.

## Migration Plan

Applications using the default `IN_MEMORY` strategy require no migration. Applications opting into `PERSISTENT` add or upgrade the commandlog JPA schema so the mapping table, unique constraint, indexes, and optional interaction-id column exist, include the commandlog JPA module, and then select the existing `PERSISTENT` property value.

Deploy the schema addition before enabling persistent storage. Rollback consists of returning the storage strategy to `IN_MEMORY` and reverting the listener/model/module registrations; the additive mapping table may remain unused or be removed separately according to the application's schema-management policy. No command log entry or imported command data is rewritten.

## Open Questions

None blocking. The ledger's JPA/JDO selection question is resolved by the Causeway 4 module inventory: M3 implements JPA only and does not restore the removed commandlog JDO adapter.
