## 1. ReplayableCommand Cleanup

- [x] 1.1 Remove the `openTarget` action and its `BookmarkService` dependency from `ReplayableCommand`.
- [x] 1.2 Remove exposed `targetType` and `targetId` properties from `ReplayableCommand`.
- [x] 1.3 Preserve any needed title logic using non-exposed helpers if required.

## 2. Layout Metadata

- [x] 2.1 Remove `openTarget`, `targetType`, and `targetId` references from `ReplayableCommand.layout.fallback.xml`.
- [x] 2.2 Keep the `member`, replay state, DTO, and participants table layout unchanged except for removed target members.

## 3. Tests and Validation

- [x] 3.1 Update focused command log applib tests for the removed target members if needed.
- [x] 3.2 Run focused command log applib tests for replayable command mapping and participant behaviour.
- [x] 3.3 Run `openspec validate replayable-command-cosmetics --strict`.
