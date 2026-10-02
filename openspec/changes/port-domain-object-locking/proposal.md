## Why

Parallel external workers can call back through REST and update the same orchestration, causing optimistic locking failures and racing completion decisions. The maintenance branch now supports declarative pessimistic locking during JPA bookmark resolution; main needs the same capability, adapted to its current architecture and verified against its newer EclipseLink provider.

## What Changes

- Pull the completed CAUSEWAY-4070 behavior from the maintenance implementation into main, preserving `@DomainObject(locking = OPTIMISTIC|PESSIMISTIC|AS_CONFIGURED|DEFAULT)` and the optimistic configuration default.
- Install a persistence-neutral locking facet and request `PESSIMISTIC_WRITE` when a pessimistic JPA entity is loaded by bookmark, including REST reference parameters before validation and invocation.
- Adapt configuration binding, facet registration, Jakarta APIs, bookmark accessors, and JPA observation integration to main.
- Port policy, REST resolution, and concurrent callback regressions into main's test modules; verify cached-state behavior against main's EclipseLink version and regenerate affected approval snapshots.
- Add annotation/configuration and adoption guidance to main's Antora documentation.

## Capabilities

### New Capabilities

- `domain-object-locking`: Declarative locking policy resolution and transactional pessimistic JPA bookmark loads, including REST callback serialization and documented scope.

### Modified Capabilities

None. Main has no existing domain-object-locking capability specification.

## Impact

Applib annotations; core configuration and metamodel; the JPA integration adapter; `core/mmtest`, configuration and REST tests, and JPA regression fixtures/tests; affected metamodel approval snapshots; and Antora reference documentation. No new dependencies or provider upgrade are required. Existing applications retain ordinary JPA loading by default; applications opting into pessimistic locking require an active transaction for affected bookmark loads.

The work is authored in the `main` worktree, reading the maintenance source at commits `746fd6393ce` (implementation), `04029032473` (approval corrections), and `47f055e3cbd` (archived specification). The maintenance archive remains historical; this proposal tracks the forward port separately.
