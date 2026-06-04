## Context

The command log applib currently provides a built-in `CommandReplayMappingListenerDefault` that remembers non-identity replay result mappings in an in-memory `HashMap`.
The listener also remaps later replay inputs by looking up recorded bookmarks in that map.
A recently added conflict policy controls whether conflicting mappings throw an exception or are logged and ignored.

This is sufficient for a single JVM replay run, but the mapping state is lost when the listener instance is recreated.
A persistent implementation needs to follow the existing command log persistence split: abstract applib domain and repository contracts, with concrete JDO and JPA entities and repositories in their respective modules.

## Goals / Non-Goals

**Goals:**

- Add an optional persistent built-in replay mapping listener.
- Preserve the current in-memory listener as the default storage strategy.
- Use a configuration property to choose between in-memory and persistent built-in storage.
- Keep custom `CommandReplayMappingListener` beans authoritative over both built-in implementations.
- Persist recorded and actual bookmarks as logical type plus identifier columns.
- Provide abstract applib entity and repository types plus concrete JDO and JPA implementations.
- Provide menu access and fallback layout metadata for the persisted mappings.
- Reuse the existing conflict policy semantics for both built-in listener implementations.

**Non-Goals:**

- Do not change the `CommandReplayMappingListener` SPI.
- Do not persist equal recorded/actual mappings.
- Do not resolve bookmarks to live domain objects while recording or remapping.
- Do not introduce a distributed cache or external storage dependency.
- Do not change command replay transaction boundaries.

## Decisions

- Add a storage strategy enum near the existing replay result mapping configuration.
  Suggested values are `IN_MEMORY` and `PERSISTENT`, with `IN_MEMORY` as the default.
  This keeps the current listener behavior unchanged unless applications opt in to persistence.

- Keep the existing in-memory listener class as the implementation for `IN_MEMORY`.
  Its bean factory should be guarded by no custom `CommandReplayMappingListener` and by storage strategy `IN_MEMORY`.
  The persistent listener should use the same no-custom-listener guard and a storage strategy `PERSISTENT` guard.

- Introduce an abstract applib entity, for example `CommandReplayResultMapping`, with fields for recorded logical type, recorded identifier, actual logical type, and actual identifier.
  Store the bookmark components separately rather than serializing a whole bookmark string so queries and uniqueness constraints can target recorded bookmark identity directly.

- Introduce an abstract applib repository, for example `CommandReplayResultMappingRepository`, with operations to find by recorded bookmark, list all mappings, and create or retrieve mappings.
  Concrete JDO and JPA repositories should follow the `CommandLogEntryRepository` pattern and live in the matching persistence modules.

- Implement the persistent listener in applib against the abstract repository.
  `remap(...)` finds a mapping by recorded bookmark and returns the actual bookmark if present.
  `onReplayResultMapped(...)` ignores equal bookmarks, creates the first non-identity mapping, treats repeated identical mappings as idempotent, and delegates conflicts to the configured conflict policy.

- Put the command log menu action in applib so the UI contract is common across persistence technologies.
  The action should delegate to the abstract repository and return all persisted mappings.
  Concrete persistence modules provide the entity implementations that make the returned objects renderable.

- Add a `.fallback.layout.xml` for the abstract applib mapping entity or for each concrete entity following existing command log layout conventions.
  The layout should expose recorded and actual bookmark components and hide technical persistence details where appropriate.

## Risks / Trade-offs

- Persistent mode requires one of the command log persistence modules to supply the concrete repository.
  Mitigation: condition persistent listener creation on the configured storage strategy and the repository bean being available.

- Uniqueness semantics can differ between JDO and JPA implementations.
  Mitigation: enforce lookup-before-create in the repository/listener and add persistence-specific tests for repeated and conflicting mappings.

- Listing all persisted mappings can become large.
  Mitigation: implement the menu action consistently with existing command log menu patterns and consider repository ordering, but leave paging/filtering out of scope for this change.

- Two built-in listener factories could accidentally both create beans.
  Mitigation: use mutually exclusive storage strategy conditions and tests for `IN_MEMORY`, `PERSISTENT`, and custom listener back-off cases.
