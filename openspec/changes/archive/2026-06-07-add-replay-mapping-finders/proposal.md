## Why

Replay result mappings now represent every replayed result object, including identity results.
Operators need menu actions and repository finders to inspect those mappings, distinguish changed mappings, and locate mappings by either recorded or actual bookmark while working toward a richer ReplayableCommand UI.

## What Changes

- Add repository finder support for all replay mappings, changed-only mappings, lookup by recorded bookmark, and lookup by actual bookmark.
- Add command log menu actions for listing all replay mappings, listing changed replay mappings, finding by recorded bookmark, and finding by actual bookmark.
- Keep the existing list action behavior available, but align naming and behavior with the new finder set.
- Add persistence-specific named queries and indexes where needed for actual bookmark lookup and changed-only listing.
- Add tests for repository finders and menu actions.

## Capabilities

### New Capabilities

### Modified Capabilities
- `persistent-command-replay-mapping-listener`: Add finder and menu requirements for querying persisted replay mappings by all, changed-only, recorded bookmark, and actual bookmark views.

## Impact

- Affects the command log applib repository contract and repository abstract implementation for replay result mappings.
- Affects JDO and JPA replay result mapping entities through additional named queries and actual-bookmark indexing.
- Affects `CommandLogMenu` by adding user-facing safe actions for the new replay mapping finders.
- Affects command log applib tests and persistence integration tests.
