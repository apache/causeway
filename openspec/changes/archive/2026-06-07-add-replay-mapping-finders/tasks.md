## 1. Repository finder contract

- [x] 1.1 Add `findChanged()` and `findByActualBookmark(Bookmark)` to `CommandReplayResultMappingRepository`.
- [x] 1.2 Implement the new finder methods in `CommandReplayResultMappingRepositoryAbstract` using named queries.
- [x] 1.3 Add named query constants for changed mappings and actual-bookmark lookup to `CommandReplayResultMapping.Nq`.

## 2. Persistence queries and indexes

- [x] 2.1 Add JDO named queries for changed mappings and actual-bookmark lookup.
- [x] 2.2 Add JDO index metadata for `actualBookmark`.
- [x] 2.3 Add JPA named queries for changed mappings and actual-bookmark lookup.
- [x] 2.4 Add JPA index metadata for `actualBookmark`.

## 3. Command log menu actions

- [x] 3.1 Keep or update the all replay mappings menu action to use the repository `findAll()` finder.
- [x] 3.2 Add a safe command log menu action to list changed replay mappings.
- [x] 3.3 Add a safe command log menu action to find a replay mapping by recorded bookmark.
- [x] 3.4 Add a safe command log menu action to find replay mappings by actual bookmark.
- [x] 3.5 Hide all replay mapping menu actions when no `CommandReplayResultMappingRepository` is available.

## 4. Tests and validation

- [x] 4.1 Update repository fake/test implementations for the new finder methods.
- [x] 4.2 Add applib tests for changed-mapping and actual-bookmark finder behavior.
- [x] 4.3 Add or update menu tests for all, changed, recorded-bookmark, and actual-bookmark actions.
- [x] 4.4 Run focused command log applib and persistence tests covering replay mapping repository and menu behavior.
- [x] 4.5 Run OpenSpec validation for `add-replay-mapping-finders` and fix any issues.
