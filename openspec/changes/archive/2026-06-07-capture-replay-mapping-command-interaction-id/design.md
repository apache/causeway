## Context

Replay result mappings are created when `CommandReplayMappingListener#onReplayResult(...)` receives the recorded result bookmark, actual result bookmark, and replayed `CommandLogEntry`.
The existing `CommandReplayResultMapping` abstraction and concrete JDO/JPA entities store only recorded and actual bookmarks.
The in-memory listener stores only the actual bookmark in a `Map<Bookmark, Bookmark>`.
The replayed `CommandLogEntry` already exposes the interaction id, so the change can capture the originating interaction without changing the replay notification SPI signature.

## Goals / Non-Goals

**Goals:**
- Store the replayed command interaction id that first created each replay result mapping.
- Preserve that first interaction id when replay reports an existing mapping again.
- Keep persistent and in-memory replay mapping behavior aligned.
- Show the command interaction id in replay result mapping layout metadata.

**Non-Goals:**
- Do not add a new finder by command interaction id unless a later change requests it.
- Do not change conflict policy semantics for repeated mappings.
- Do not change the `CommandReplayMappingListener` method signatures.

## Decisions

- Reuse `CommandLogEntry#getInteractionId()` as the source of the mapping origin.
  The replay notification already receives the command log entry, so this avoids an SPI change and keeps custom listener compatibility.
  The alternative was to add an explicit interaction id parameter to `onReplayResult(...)`, but that would create broader API churn without adding information.

- Add `commandInteractionId` to the abstract applib mapping contract and to concrete JDO/JPA entities as a nullable `UUID` property.
  Nullable storage protects upgrades and existing rows that predate the field.
  The alternative was a non-null field, but that would require a data migration for existing mappings and complicate rollout.

- Extend repository creation to accept the command interaction id, while preserving a compatibility overload if useful during implementation.
  The persistent listener should create new rows with the interaction id and should not mutate existing rows during idempotent or conflict handling.
  The alternative was to set the field after creation, but one creation path keeps the persistence contract simpler and easier to test.

- Replace the in-memory listener value type with a small mapping value that stores both actual bookmark and command interaction id.
  This keeps the in-memory storage strategy semantically equivalent to persistent storage.
  The alternative was to leave in-memory storage unchanged, but that would make the data structure diverge from the new mapping contract.

## Risks / Trade-offs

- Existing persisted rows will have no command interaction id after upgrade → Treat the property as optional and display it only when available.
- JDO query classes may need regeneration after adding the field → Include generated-source refresh or targeted compile in implementation tasks.
- Layout metadata can become stale if the property id differs from the Java getter name → Use `commandInteractionId` consistently in the property, column order, and fallback layout.
