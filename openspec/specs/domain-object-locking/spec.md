## Purpose

Defines declarative domain-object locking, JPA bookmark-load behavior, and concurrency guarantees.

## Requirements

### Requirement: Declarative locking policy

The applib SHALL provide `Locking` values `OPTIMISTIC`, `PESSIMISTIC`, `AS_CONFIGURED`, and `DEFAULT`, and `@DomainObject.locking()` SHALL default to `DEFAULT`. The annotation and its policy SHALL remain independent of JPA types.

#### Scenario: Existing annotation usage
- **WHEN** an entity declares `@DomainObject` without a locking attribute
- **THEN** its locking attribute SHALL be unspecified and resolve through annotation synthesis and configuration

#### Scenario: Explicit pessimistic policy
- **WHEN** an entity declares `@DomainObject(locking = Locking.PESSIMISTIC)`
- **THEN** its resolved locking policy SHALL be pessimistic regardless of the configured policy

#### Scenario: Explicit optimistic policy
- **WHEN** an entity declares `@DomainObject(locking = Locking.OPTIMISTIC)` and configuration selects pessimistic locking
- **THEN** its resolved locking policy SHALL be optimistic

### Requirement: Configured default and annotation resolution

The system SHALL expose `causeway.applib.annotation.domain-object.locking`, accepting only `OPTIMISTIC` and `PESSIMISTIC` and defaulting to `OPTIMISTIC`. `DEFAULT` SHALL allow existing merged annotation resolution to find an explicit policy in superclass or meta-annotation sources before falling back to configuration. `AS_CONFIGURED` SHALL explicitly select configuration. Entities with no applicable annotation SHALL use configuration.

#### Scenario: Unconfigured application
- **WHEN** no annotation source specifies a locking policy and the configuration property is absent
- **THEN** the effective policy SHALL be optimistic

#### Scenario: Configured pessimistic policy without an annotation
- **WHEN** configuration selects `PESSIMISTIC` and a JPA entity has no `@DomainObject` annotation
- **THEN** the effective policy SHALL be pessimistic

#### Scenario: Default permits inherited policy
- **WHEN** a concrete entity has `locking = DEFAULT` and merged annotation resolution finds a superclass or meta-annotation specifying `PESSIMISTIC`
- **THEN** the effective policy SHALL be pessimistic

#### Scenario: Explicit configuration selection
- **WHEN** a concrete entity specifies `AS_CONFIGURED`, an inherited annotation specifies `PESSIMISTIC`, and configuration selects `OPTIMISTIC`
- **THEN** the effective policy SHALL be optimistic

#### Scenario: Invalid configuration sentinel
- **WHEN** the configuration property is assigned `DEFAULT` or `AS_CONFIGURED`
- **THEN** configuration binding SHALL reject the unresolved value

### Requirement: Locking policy facet

The metamodel SHALL install a persistence-neutral facet exposing the resolved locking policy, using the existing annotation and configuration precedence conventions. The JPA adapter SHALL consult this facet when loading an entity by bookmark.

#### Scenario: Policy on the concrete entity
- **WHEN** a REST action parameter is declared as a base type and its reference bookmark identifies a concrete JPA entity with pessimistic policy
- **THEN** bookmark loading SHALL use the concrete entity specification's pessimistic policy

### Requirement: Pessimistic JPA bookmark loading

For a pessimistic entity policy, `JpaEntityFacet.fetchByBookmark()` SHALL request `LockModeType.PESSIMISTIC_WRITE` through the locking overload of `EntityManager.find()`, in the caller's transaction. The system SHALL NOT silently downgrade the lock or create a separate transaction that releases it before the caller's work completes.

#### Scenario: Entity exists
- **WHEN** a pessimistic entity is loaded by bookmark in an active transaction
- **THEN** loading SHALL request a pessimistic write lock and return the entity
- **AND** the lock SHALL remain held until that transaction commits or rolls back

#### Scenario: Entity is missing
- **WHEN** a pessimistic bookmark load finds no entity
- **THEN** the adapter SHALL retain the existing empty-result behavior

#### Scenario: No transaction
- **WHEN** a pessimistic bookmark load occurs without an active transaction
- **THEN** the load SHALL fail rather than returning an unlocked entity

### Requirement: Existing optimistic loading behavior

For an optimistic policy, JPA bookmark loading SHALL retain the existing ordinary `EntityManager.find(entityClass, primaryKey)` behavior and existing ORM version handling. It SHALL NOT add an explicit JPA optimistic lock or require a new version mapping.

#### Scenario: Default policy compatibility
- **WHEN** an existing application's JPA entity is loaded with the default optimistic policy
- **THEN** the adapter SHALL use the ordinary two-argument `find()` and retain existing missing-entity and version behavior

### Requirement: REST callback parameter serialization

When a RESTful Objects action resolves a pessimistic JPA entity reference parameter, the system SHALL acquire its write lock during argument loading, before parameter validation and action invocation, within the request transaction. Concurrent callbacks resolving the same previously unloaded orchestration in independent persistence contexts SHALL serialize access and observe committed progress after waiting for the preceding callback.

#### Scenario: Concurrent line-completion callbacks
- **WHEN** callback A resolves and updates an orchestration and callback B resolves the same orchestration before A completes its transaction
- **THEN** callback B SHALL wait for the conflicting write lock or fail according to the provider's lock failure behavior
- **AND** if A commits and B obtains the lock, B SHALL execute against A's committed progress and both callbacks' updates SHALL persist after B commits

#### Scenario: Completion decision
- **WHEN** two successful serialized callbacks complete the last two lines and each checks whether all lines are complete
- **THEN** the callback test fixture SHALL record both completions and publish its final completion event once

#### Scenario: Rollback releases the lock
- **WHEN** callback A rolls back while callback B is waiting for the same orchestration lock
- **THEN** callback B SHALL be able to acquire the released lock and continue without observing A's rolled-back changes

#### Scenario: Different orchestrations
- **WHEN** callbacks load separate independent orchestration rows
- **THEN** this policy SHALL NOT impose a shared application-wide lock across those orchestrations

### Requirement: Failure and cached-state behavior

The JPA adapter SHALL preserve provider lock and version failures and SHALL NOT add explicit refresh calls around the locking `find()`. It SHALL retain the provider's state-refresh behavior and document that EclipseLink's first pessimistic load can refresh previously unlocked managed state. REST reference loading SHALL retain the existing exception-to-parameter-validation-failure handling and SHALL NOT invoke the callback after a failed argument load.

#### Scenario: Provider lock failure
- **WHEN** the JPA provider throws a lock failure while resolving an orchestration argument
- **THEN** the failure SHALL follow the existing REST argument-validation error path
- **AND** the callback action SHALL NOT execute

#### Scenario: Previously managed entity
- **WHEN** a pessimistic bookmark resolves an entity already managed by the persistence context
- **THEN** the adapter SHALL still request the locking `find()`
- **AND** it SHALL preserve any provider version-check failures without adding adapter refresh calls

#### Scenario: EclipseLink first acquisition refreshes managed state
- **WHEN** EclipseLink first pessimistically loads an entity previously managed without a pessimistic lock
- **THEN** the provider's refresh behavior SHALL be retained, including replacement of unflushed local changes
- **AND** documentation SHALL instruct callers to acquire the lock before modifying orchestration state

#### Scenario: EclipseLink repeated locked load
- **WHEN** an entity is already pessimistically locked in the current EclipseLink transaction and the callback has changed its progress
- **THEN** another bookmark load SHALL return the same entity with those callback changes retained

### Requirement: Documented policy boundary

Reference documentation SHALL explain annotation/configuration resolution, the optimistic compatibility meaning, pessimistic write-lock semantics, active transaction requirements, cached-state limitations, and the REST callback parameter use case. It SHALL define the runtime policy as applying to Causeway JPA bookmark loads and SHALL NOT claim automatic coverage of queries, repository loads, lazy relationships, JDO, or callback deduplication.

#### Scenario: Application developer adopts the policy
- **WHEN** a developer reads the locking reference documentation
- **THEN** it SHALL show how to opt an orchestration entity into pessimistic loading and explain which loading paths acquire the lock and when the transaction releases it

### Requirement: Preserve bookmark load observations

The JPA adapter SHALL retain main's existing observation operation around bookmark loading for both resolved policies, including pessimistic lock acquisition and provider failures.

#### Scenario: Observed pessimistic lookup
- **WHEN** a pessimistic entity is loaded by bookmark
- **THEN** the decoding and locking find SHALL execute within the existing bookmark-load observation
- **AND** the enclosing caller transaction SHALL remain the transaction holding the lock

#### Scenario: Observed optimistic lookup
- **WHEN** an optimistic entity is loaded by bookmark
- **THEN** the ordinary find SHALL retain the existing bookmark-load observation
