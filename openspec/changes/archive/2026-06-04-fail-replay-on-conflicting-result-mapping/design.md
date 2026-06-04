## Context

Command replay imports recorded command DTOs and re-executes them locally.
The replay mapping SPI lets applications remap replay inputs and observe mappings from recorded result bookmarks to actual replay result bookmarks.

The current replay flow executes the command in one `REQUIRES_NEW` transaction and then handles replay success or failure in a second `REQUIRES_NEW` transaction.
Result-mapping listener notification happens in the second transaction, and listener exceptions are caught and logged.
That means a listener consistency failure cannot fail or roll back the replayed command.

The default listener stores mappings in an in-memory `HashMap<Bookmark, Bookmark>` keyed by recorded result bookmark.
It currently replaces an existing mapping with the latest actual bookmark.
For replay consistency, a recorded original result that was already mapped to one actual bookmark must not later be mapped to a different actual bookmark.

## Goals / Non-Goals

**Goals:**

- Detect conflicting result mappings in the default listener.
- Make repeated identical result mappings idempotent.
- Keep ignoring identity mappings where recorded and actual bookmarks are equal.
- Run replay execution, success bookkeeping, and result-mapping listener notification in one transaction.
- Propagate result-mapping listener exceptions so consistency failures mark replay as failed and prevent successful command execution.

**Non-Goals:**

- Do not persist default listener mappings beyond the existing in-memory listener instance.
- Do not change the `CommandReplayMappingListener` method signatures.
- Do not change target or reference parameter remapping exception handling before command execution.
- Do not introduce distributed coordination for mapping state across JVMs or nodes.

## Decisions

### Use the default listener as the consistency guard

The default listener already owns the in-memory recorded-to-actual result map.
It will inspect the existing mapping before writing a new non-identity mapping.
If the recorded result has no existing mapping, it records the actual result.
If the existing mapping equals the new actual result, it leaves the map unchanged and returns normally.
If the existing mapping differs from the new actual result, it throws an exception with the conflicting bookmarks.

An alternative was to let the latest mapping continue to win and add a separate audit warning.
That would not protect downstream remapping from ambiguous original results, so it is rejected.

### Notify result-mapping listeners inside the replay command transaction

`ReplayableCommand` will move successful replay bookkeeping and result-mapping listener notification into the transaction that calls `CommandExecutorService#executeCommand(...)`.
The command execution result bookmark is available before that transaction returns, so the replay can save success analysis and notify listeners before commit.
If a listener throws, the transaction fails and the command replay result is reported as a failure.

An alternative was to keep the second transaction and rethrow listener exceptions there.
That would fail the post-processing transaction but still leave the replayed command execution committed, which does not satisfy the consistency requirement.

### Keep replay failure bookkeeping in a separate transaction

When command execution or listener notification fails, the existing failure bookkeeping still needs to save replay analysis on the imported command log entry.
That failure analysis should run after the failed command transaction has rolled back, using a separate transaction as today.
Success bookkeeping does not need a second transaction because it now belongs to the successful command transaction.

## Risks / Trade-offs

- Custom listeners that previously threw without affecting replay will now cause replay failure.
  → This is intentional for result-mapping notifications; document it in tests and specs.
- Failure analysis still depends on a follow-up transaction after rollback.
  → Preserve the existing failure handling pattern for replay errors.
- The default listener remains in-memory and is not synchronized across application nodes.
  → This change only strengthens consistency for one listener instance, matching the current non-persistence scope.
