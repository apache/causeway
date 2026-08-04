## Context

This change is the fifth dependency-ordered reconciliation slice and covers D2, M1, and M2 from `openspec/reconciliation/maintenance-branch/dependency-graph.md`.
Causeway 4 already executes imported `CommandDto` instances through the record-based `CommandExecutorServiceDefault`, and commandlog already wraps persisted entries as `ReplayableCommand` view models.
D1 supplies structurally independent command DTO copying and portable recorded result bookmarks.
The remaining gap is an execution policy for interaction advisors and a commandlog-owned mechanism that remaps recorded bookmarks without making core runtime services depend on the commandlog extension.

Maintenance behavior is authoritative for advisor policy, target/reference remapping, result observation, conflict handling, and conditional defaults.
The implementation must adapt mutable configuration to Causeway 4 records, Jakarta APIs, Spring Boot 4 conditional wiring, and the current `ReplayContext`/`ReplayableCommand` transaction flow.

## Goals / Non-Goals

**Goals:**

- Add D2 interaction-advisor policy to command DTO execution for actions and properties.
- Add M1 bookmark-only replay mapping SPI, replay input copying/remapping, result notification, and imported-entry preservation.
- Add M2 in-memory mapping with deterministic conflict handling and conditional default wiring.
- Preserve compatibility when no custom listener or configuration is supplied.
- Reuse D1 command copying and existing replay transaction boundaries.

**Non-Goals:**

- Persistent mapping entities, repositories, JPA/JDO adapters, or mapping menu actions from M3.
- Rich `ReplayableCommand` participants, mapping inspection, result-presence policy, or adjacent navigation from P1.
- Unified manager, reference-data, reachability, export remapping/validation, workflow, or background-gate behavior from P2, R1/R2, E1/W1, or B1/B2.
- Changing plain or result-bearing command YAML formats.
- Changing which command log entries are eligible for replay.

## Decisions

### Use immutable nested configuration records

Add `CommandExecutorService` beneath `CausewayConfiguration.Core.RuntimeServices` with `interactionAdvisorPolicy` defaulting to `NO_CHECK`.
The policy values are `CHECK`, `CHECK_BUT_IGNORE`, and `NO_CHECK`, matching maintenance semantics while retaining the Causeway 4 record construction model.

Add `ReplayResultMapping` beneath `CausewayConfiguration.Extensions.CommandLog` with `storageStrategy` defaulting to `IN_MEMORY` and `onConflictPolicy` defaulting to `THROW_EXCEPTION`.
The `PERSISTENT` strategy value is declared now as the selection contract required by M2, but this change deliberately supplies no persistent implementation.

An alternative was to place advisor policy in commandlog configuration.
That was rejected because `CommandExecutorService` is a core service and the policy applies to every DTO-driven execution caller, not only commandlog replay.

### Apply advisors inside the existing executor

`CommandExecutorServiceDefault` consults configuration immediately before action execution or property mutation.
For actions, `CHECK` evaluates visibility, usability, and the complete argument set in that order and raises the framework's standard hidden, disabled, or invalid exception with the corresponding interaction event.
For properties, `CHECK` evaluates visibility, usability, and proposed-value validity in that order and raises the corresponding standard exception.
`CHECK_BUT_IGNORE` invokes the same advisors in the same order but continues regardless of veto, preserving advisor side effects such as event-phase processing.
`NO_CHECK` invokes no advisors and preserves current default behavior.

An alternative was to enforce advisors in `ReplayableCommand`.
That was rejected because it would make DTO execution semantics depend on the commandlog caller and would leave other `CommandExecutorService` clients inconsistent.

### Keep replay mapping in commandlog applib

Define public `CommandReplayMappingListener` in commandlog applib with default methods `lookup(Bookmark)` and `onReplayResult(Bookmark, Bookmark, UUID)`.
The SPI exchanges bookmark values only and never requires recorded identities to resolve to local objects.
Passing the interaction id rather than `CommandLogEntry` avoids exposing commandlog persistence state through the SPI and follows the current maintenance contract.

A commandlog `ResultRemappingService` owns an ordered list of listeners.
Lookup checks listeners in injection order and uses the first non-empty replacement; a lookup exception is logged and lookup continues.
Result notification is sent to every listener, and notification exceptions are deliberately not swallowed.

An alternative was a core runtime mapping SPI.
That was rejected because remapping is replay policy, while the core executor should remain unaware of commandlog import and result-observation semantics.

### Remap an independent execution DTO

Before each replay or retry, `ResultRemappingService` calls `CommandDtoUtils.copy(...)` and mutates only that execution copy.
It applies the common lookup flow to each target bookmark and to action parameters whose schema value type is `REFERENCE` and whose reference OID is populated.
Non-reference parameters and property values are unchanged.
Every retry begins again from `CommandLogEntry.getCommandDto()` so a prior replacement or failed execution cannot become recorded input.

An alternative was to remap managed objects after unmarshalling.
That was rejected because an unresolved recorded bookmark must still be replaceable before local object resolution.

### Preserve imported command-log facts during replay lifecycle synchronization

`CommandLogEntry.sync(...)` continues to update execution timing while replay is enabled for the entry, but it does not overwrite imported command DTO, target, member, result, exception, username, or original timestamp.
This prevents the normal command publisher callbacks from replacing recorded audit facts with the remapped execution DTO.
Normal foreground and background command synchronization remains unchanged.

### Observe successful results inside the replay transaction

`ReplayableCommand` remaps the DTO immediately before execution.
Within the existing `REQUIRES_NEW` replay transaction, it executes the copied DTO, obtains the actual bookmark, clears successful analysis, and notifies mappings when the imported entry has a recorded result and execution produced an actual result.
Equal bookmarks are notified because identity mappings are meaningful lookup facts.
Missing recorded or actual results and failed replay produce no notification.
Listener failure propagates through the transaction, causing replay failure and rollback before the existing failure-analysis path runs.

An alternative was to notify in the separate post-execution state-update transaction.
That was rejected because maintenance requires mapping acceptance to be atomic with replayed domain changes.

### Provide one conditional in-memory default

`CommandReplayMappingListenerInMemory` stores the first mapping for each recorded bookmark, including identity mappings and the originating interaction id.
Repeating the same mapping is idempotent and retains the first interaction id.
A conflicting actual bookmark never replaces the first mapping: `THROW_EXCEPTION` rejects it, while `LOG_AND_CONTINUE` records an error and continues.

Spring Boot 4 configuration creates this listener only when storage is `IN_MEMORY` and no application-defined `CommandReplayMappingListener` bean exists.
Any custom listener causes the built-in listener to back off; selecting `PERSISTENT` also creates no in-memory listener.

## Risks / Trade-offs

- [Advisor checks can make previously executable DTOs fail when `CHECK` is selected] → Keep `NO_CHECK` as the compatibility default and document each policy.
- [Advisor callbacks can have side effects even when vetoes are ignored] → Make `CHECK_BUT_IGNORE` explicit and test invocation order separately from enforcement.
- [Multiple custom listeners can disagree] → Use deterministic first-replacement lookup order and notify all observers.
- [A remapped DTO can be written back by normal command lifecycle callbacks] → Preserve imported fields in replay-enabled `CommandLogEntry.sync(...)` and test started/completed synchronization.
- [Result-listener failures roll back otherwise successful domain work] → Treat this as required consistency behavior and cover it with transaction-oriented replay tests.
- [The `PERSISTENT` selector has no implementation in this slice] → Document that selection suppresses the in-memory default and deliver persistence in the immediately following M3 change.
