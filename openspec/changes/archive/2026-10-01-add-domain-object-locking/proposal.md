## Why

Parallel REST callbacks for payment lines can update the same orchestration entity concurrently, causing optimistic locking failures and racing batch-completion decisions. Applications need a declarative way to serialize these interactions by acquiring a database write lock when Causeway resolves the orchestration reference.

## What Changes

- Add `@DomainObject(locking = ...)` with a new `Locking` enum containing `OPTIMISTIC`, `PESSIMISTIC`, `AS_CONFIGURED`, and `DEFAULT`; the annotation defaults to `DEFAULT`.
- Add `causeway.applib.annotation.domain-object.locking`, accepting `OPTIMISTIC` or `PESSIMISTIC`, with `OPTIMISTIC` as its default.
- Install a persistence-neutral locking-policy facet using the existing annotation and configuration resolution conventions. `DEFAULT` leaves the annotation attribute unspecified; `AS_CONFIGURED` explicitly selects the configured policy.
- Apply the resolved policy to JPA entity loads by bookmark, including REST action parameters: `PESSIMISTIC` requests `PESSIMISTIC_WRITE`; `OPTIMISTIC` preserves the existing ordinary `find()` and ORM version behavior.
- Document transaction requirements and the boundary of this policy: Causeway bookmark loads, rather than every possible ORM loading path.
- Verify concurrent REST-style parameter resolution and callback updates with EclipseLink.

## Capabilities

### New Capabilities

- `domain-object-locking`: Annotation/configuration policy resolution and pessimistic JPA bookmark loading to serialize updates to a shared entity.

### Modified Capabilities

None.

## Impact

- Public annotation API in `api/applib`.
- Configuration and metadata in `core/config`, and facet creation/tests in `core/metamodel`.
- JPA integration in `persistence/jpa/integration`.
- Focused regression coverage and reference documentation.
- No new dependencies or JDO runtime behavior changes. Existing applications retain their loading behavior under the default configuration.
