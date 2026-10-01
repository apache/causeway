## Context

An orchestration publishes one outbox event per payment line. External workers process those events in parallel and invoke a REST action accepting an `Orchestration<?,?>` reference and payload. Concurrent callbacks update shared orchestration state and can fail optimistic version checks or race completion decisions.

The RESTful Objects request filter starts a transaction around request handling. `_DomainResourceHelper.invokeAction()` parses arguments before validating and invoking the action. `ObjectActionArgHelper` delegates reference parsing to `JsonParserHelper`, which extracts a bookmark from `value.href`. `IResourceContext.getObjectAdapterForOidFromHref()` loads it through `ObjectManager`, `ObjectLoader.LoadEntity`, and `JpaEntityFacet.fetchByBookmark()`. The latter currently calls ordinary `EntityManager.find(entityClass, primaryKey)`. The bookmark resolves the concrete specification even when the declared parameter type is a base orchestration class.

This branch uses `javax.persistence` and EclipseLink 2.7.16. Changes should remain targeted and introduce no dependencies.

## Goals / Non-Goals

**Goals:**

- Allow entity types to opt into pessimistic write locking on Causeway bookmark loads.
- Acquire the lock during REST reference resolution, before parameter validation and callback execution, and retain it through transaction completion.
- Follow existing annotation/configuration/facet conventions while preserving current default loading behavior.
- Verify that callbacks for one orchestration serialize while callbacks for different orchestrations remain independent.

**Non-Goals:**

- Automatically lock query results, Spring Data repository loads, QueryDSL loads, or lazy relationships.
- Add JDO locking behavior, action-level policies, pessimistic read modes, force-increment modes, automatic retries, or new timeout settings.
- Implement application-specific callback deduplication or batch-completion logic.
- Hold locks across separate HTTP requests or the external processing interval.

## Decisions

### Annotation and configuration policy

Add `Locking` in the applib annotation package with `OPTIMISTIC`, `PESSIMISTIC`, `AS_CONFIGURED`, and `DEFAULT`. Add `DomainObject.locking()` defaulting to `DEFAULT`.

Interpret `DEFAULT` as the unspecified value, analogous to `NOT_SPECIFIED` elsewhere. Causeway's merged annotation handler ignores values equal to the annotation method's default while searching other annotation sources, so a superclass or meta-annotation can supply an explicit policy. If none does, use configuration. `AS_CONFIGURED` is an explicit choice that selects configuration instead of inheriting a different policy.

Add `causeway.applib.annotation.domain-object.locking` under the existing domain-object configuration section. Use a separate configuration enum restricted to `OPTIMISTIC` and `PESSIMISTIC`, defaulting to `OPTIMISTIC`, following the `DomainObjectConfigOptions` pattern. Reject unresolved annotation sentinel values in configuration.

An explicit annotation policy overrides configuration. An entity without `@DomainObject` uses configuration. Avoid a global pessimistic default in existing applications unless they intend all affected bookmark loads to require transactions and lock rows.

### Persistence-neutral facet and adapter-local translation

Introduce a locking-policy facet in `core/metamodel`, populated by `DomainObjectAnnotationFacetFactory`, carrying only a resolved optimistic/pessimistic policy. Follow existing facet precedence and annotation/configuration provenance patterns; read the facet at load time to avoid relying on factory execution order.

Keep `javax.persistence.LockModeType` out of the public annotation and shared core facet. Translate only in the JPA adapter. Non-JPA objects do not acquire new locks; document the JPA scope rather than imply a cross-adapter guarantee.

### Map pessimistic policy to a write lock on bookmark loading

For `PESSIMISTIC`, call `EntityManager.find(entityClass, primaryKey, LockModeType.PESSIMISTIC_WRITE)`. For `OPTIMISTIC`, retain the existing two-argument `find()` so ORM version handling remains unchanged. The policy does not add optimistic versioning to entities lacking a version mapping.

Retain missing-entity handling and use the concrete bookmark specification's facet. Do not start a separate transaction around the load: releasing the lock before callback execution would fail to protect its decisions and updates. A pessimistic load without an active transaction must fail rather than silently downgrade.

Alternatives considered: acquiring a lock in the action would be too late for reference-dependent validation and could leave stale state; an action-specific hook would widen scope; a JVM mutex would not coordinate multiple application instances; automatic retry would require replaying the entire callback transaction and auditing its side effects.

### Cache behavior and failure handling

Prove EclipseLink behavior using separate persistence contexts with shared cache enabled. A waiter resolving a previously unloaded orchestration must see the previous transaction's committed state before callback execution. EclipseLink 2.7.16 tests and source show that the first pessimistic `find()` refreshes previously unlocked managed state, including local changes, and can refresh stale versions instead of throwing a version failure. Once the entity is already pessimistically locked in that transaction, subsequent locking finds return it without refreshing. Use the provider's standard `find()` behavior without extra adapter refresh calls or vendor hints; require callbacks to resolve the lock before changing state. Test both first acquisition and repeated locked loads, and preserve any provider lock/version failures.

Do not introduce timeout configuration or automatic retry. Preserve the provider exception at the adapter boundary. REST argument parsing currently wraps load failures as parameter-validation failures; document and test that existing behavior so callers do not receive a successful callback response on lock failure. Changing HTTP error classification is a separate concern.

### Validation

Unit tests cover policy resolution, annotation inheritance/meta-annotations, explicit `AS_CONFIGURED`, configuration binding, default compatibility, and adapter calls. EclipseLink integration tests use two independent transactions, bounded waits, and synchronization to show contention on the same entity, release on commit and rollback, updated state after waiting, and independence across entity identifiers. Add a focused REST parameter-resolution test using an orchestration reference link, ideally through an existing REST integration harness.

## Risks / Trade-offs

- [A configured pessimistic default affects all JPA bookmark loads, including reads and parameter validation] → Default to optimistic and document the transaction and contention consequences.
- [First lock acquisition can overwrite previously unlocked local changes in EclipseLink] → Document provider behavior and resolve orchestration references before reading or modifying their state; test that repeated already-locked loads preserve callback changes.
- [Lock waits or deadlocks] → Keep callback transactions short, test bounded contention, and retain database/provider timeout behavior.
- [Other loading paths bypass the facet] → Explicitly document bookmark scope and require cooperating callback writers to use the same locking path.
- [Duplicate callback delivery] → Serialization does not itself deduplicate callbacks; the application's idempotent callback implementation remains responsible.
- [Lock exceptions are reported as invalid REST arguments] → Preserve and document the existing wrapping behavior, with regression coverage.

## Migration Plan

Existing applications require no configuration changes. Enable `@DomainObject(locking = Locking.PESSIMISTIC)` on orchestration entities whose callbacks need serialization. Retain existing version mappings. Roll back the application policy to `OPTIMISTIC` or remove the annotation override to restore the configured behavior.

## Open Questions

- Which existing integration harness can demonstrate REST parameter resolution plus overlapping transactions with the least additional setup?
- Verify cached-state behavior on the application's production database as well as the existing regression database; database-specific timeout and lock behavior remain deployment concerns.
