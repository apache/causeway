## 1. Immutable configuration

- [ ] 1.1 Add the Causeway 4 `CommandExecutorService` configuration record and `InteractionAdvisorPolicy` values under core runtime services with `NO_CHECK` as the compatibility default.
- [ ] 1.2 Add commandlog replay-result-mapping storage strategy and conflict policy records with `IN_MEMORY` and `THROW_EXCEPTION` defaults while reserving `PERSISTENT` for M3.
- [ ] 1.3 Add configuration binding/default tests and regenerate or update the core configuration reference documentation.

## 2. Command execution advisor policy

- [ ] 2.1 Apply `CHECK` action visibility, usability, and complete argument-set validation in order within `CommandExecutorServiceDefault`, raising the standard interaction exceptions and events.
- [ ] 2.2 Apply `CHECK` property visibility, usability, and proposed-value validation in order within `CommandExecutorServiceDefault`, raising the standard interaction exceptions and events.
- [ ] 2.3 Implement `CHECK_BUT_IGNORE` for actions and properties so every advisor phase runs in order while vetoes do not prevent execution.
- [ ] 2.4 Preserve `NO_CHECK` behavior without advisor calls and add focused executor tests for defaults, phase ordering, veto short-circuiting, ignored vetoes, invocation, and mutation.

## 3. Replay mapping SPI and input remapping

- [ ] 3.1 Add the public bookmark-only `CommandReplayMappingListener` SPI with default lookup and result-observation methods, including command interaction id on observations.
- [ ] 3.2 Add `ResultRemappingService` with deterministic listener ordering, first non-empty lookup, logged lookup failures, and unsuppressed result-observation failures.
- [ ] 3.3 Deep-copy recorded command DTOs through `CommandDtoUtils.copy(...)` and remap every command target without resolving the recorded bookmark.
- [ ] 3.4 Remap only populated `REFERENCE` action parameters through the common lookup flow while preserving scalar parameters, property values, and unmapped references.
- [ ] 3.5 Add focused remapping tests for no listeners, unmapped and mapped targets, unresolved bookmarks, listener ordering/failure, reference parameters, and non-reference values.

## 4. Replay lifecycle integration

- [ ] 4.1 Wire replay mapping into the current `ReplayContext` and create a fresh remapped execution DTO from the imported command log entry for every replay or retry.
- [ ] 4.2 Adapt replay-enabled `CommandLogEntry.sync(...)` behavior so lifecycle callbacks update execution timing without overwriting imported command DTO, target, member, result, exception, username, or original timestamp.
- [ ] 4.3 Notify all mapping listeners inside the replay transaction after successful execution when recorded and actual result bookmarks exist, including identity mappings and interaction id.
- [ ] 4.4 Propagate result-observation failures so replay rolls back and follows the existing failure-analysis path, while failed or incomplete-result replay produces no observation.
- [ ] 4.5 Add replay integration tests for DTO preservation across success, failure, and retry; timing synchronization; result notification conditions; transaction failure; and later target/reference remapping from earlier results.

## 5. Conditional in-memory listener

- [ ] 5.1 Implement instance-scoped in-memory storage for identity and changed mappings, retaining the first actual bookmark and originating interaction id.
- [ ] 5.2 Implement idempotent repeat handling plus `THROW_EXCEPTION` and `LOG_AND_CONTINUE` conflict behavior without replacing the original mapping.
- [ ] 5.3 Add Spring Boot 4 conditional wiring that supplies the configured in-memory listener only for `IN_MEMORY` when no application listener exists and supplies none for `PERSISTENT`.
- [ ] 5.4 Add listener and application-context tests for lookup, identity mappings, interaction-id retention, repeat/conflict handling, custom-listener backoff, defaults, and persistent-strategy suppression.

## 6. Compatibility, documentation, and verification

- [ ] 6.1 Document command-executor advisor policies, replay mapping SPI behavior, in-memory defaults, conflict policy, custom-listener override, and the deferred persistent strategy.
- [ ] 6.2 Verify synthetic selector scalar parameters remain unchanged, synthetic/reference results use normal D1/C4a metadata, and existing C1/C2/C3/C4b suppression and publication paths remain intact.
- [ ] 6.3 Run focused core configuration, runtime-service, commandlog remapping, replay, and listener tests with JDK 21.
- [ ] 6.4 Run the affected aggregate Maven build, IDE compilation and inspections, strict OpenSpec validation, repository checks, and a scope guard confirming no M3, P1/P2, R1/R2, E1/W1, or B1/B2 implementation is introduced.
