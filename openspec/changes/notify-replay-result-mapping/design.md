## Context

Command replay imports can now store the recorded returned object bookmark on the replay `CommandLogEntry`.
During replay, `CommandExecutorService#executeCommand(...)` returns the actual result bookmark produced by executing the command in the target environment.

Those bookmarks can differ when replay creates a new object or resolves to an environment-specific instance.
Applications that maintain cross-environment references need a supported SPI to observe the mapping from the recorded bookmark to the actual bookmark.

## Goals / Non-Goals

**Goals:**

- Introduce a public SPI for replay result mapping notifications.
- Notify the SPI after successful replay when both recorded and actual result bookmarks are known.
- Pass bookmarks as bookmark values and avoid resolving domain objects inside the replay infrastructure.
- Keep replay state handling and existing replay success behavior intact.

**Non-Goals:**

- Do not persist a generic mapping table in the command log extension.
- Do not transform future replay commands using mappings in this change.
- Do not require applications to implement the SPI.
- Do not notify the SPI for failed replay attempts.

## Decisions

- Add an SPI interface in the command log applib replay package or an adjacent SPI package.
  The SPI should expose a method such as `onReplayResultMapped(Bookmark recordedResult, Bookmark actualResult, CommandLogEntry commandLogEntry)` or an equivalent event object.
  Alternative considered: publish a domain event, but an SPI is clearer for application services that need deterministic post-replay callbacks.

- Invoke the SPI from `ReplayableCommand` after replay execution succeeds and before or during the existing replay success handling transaction.
  This location already has access to the imported command log entry and the actual bookmark returned by `CommandExecutorService`.
  Alternative considered: hook into `CommandLogEntry` persistence, but that layer cannot distinguish imported recorded results from actual replay results without additional context.

- Only notify when both recorded and actual bookmarks are non-null.
  The mapping is only meaningful when there is a recorded returned object and replay produced an actual returned object.
  Alternative considered: notify for null values as well, but that pushes non-mapping cases onto every application implementation.

- Allow notification when the recorded and actual bookmarks are equal.
  Equality can be useful for auditing and keeps the SPI semantics simple: successful replay produced a known pair.
  Alternative considered: suppress equal pairs, but that hides successful correlation from applications that want a complete replay result stream.

- Provide a no-op default implementation if module registration requires a concrete bean.
  This keeps the extension opt-in for applications and avoids changing behavior when no application SPI is present.

## Risks / Trade-offs

- [Risk] SPI exceptions could interfere with replay success state updates → Catch and record SPI failures as replay failures only if the implementation policy explicitly requires it; otherwise log and keep replay success behavior stable.
- [Risk] Applications may expect bookmarks to resolve to objects → Document that the SPI receives bookmarks only and object lookup is the application's responsibility.
- [Risk] Multiple SPI implementations may exist → Invoke all discovered implementations in a deterministic order if the framework supplies a list, or document single-bean semantics if only one service is injected.
