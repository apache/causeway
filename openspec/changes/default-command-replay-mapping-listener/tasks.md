## 1. Default Listener Implementation

- [ ] 1.1 Add a default `CommandReplayMappingListener` implementation in the command log applib that stores recorded-to-actual mappings in a `HashMap<Bookmark, Bookmark>`.
- [ ] 1.2 Implement `onReplayResultMapped(...)` to store or replace the actual bookmark for the recorded result bookmark.
- [ ] 1.3 Implement `remap(...)` to return the remembered actual bookmark for a recorded bookmark, or `Optional.empty()` when none exists.
- [ ] 1.4 Keep the implementation bookmark-based and avoid resolving recorded or actual bookmarks to domain objects.

## 2. Autoconfiguration

- [ ] 2.1 Register the default listener as a Spring bean from the command log applib module configuration.
- [ ] 2.2 Guard the bean with `@ConditionalOnMissingBean(CommandReplayMappingListener.class)` so custom listener beans replace the default.
- [ ] 2.3 Ensure the autoconfigured listener is available to the existing replay listener collection used by `ReplayContext`.

## 3. Tests

- [ ] 3.1 Add unit tests proving the default listener records result mappings and returns them from `remap(...)`.
- [ ] 3.2 Add a unit test proving a repeated recorded bookmark mapping uses the latest actual bookmark.
- [ ] 3.3 Add a unit test proving an unmapped bookmark returns no replacement.
- [ ] 3.4 Add Spring context tests proving the default listener bean is created when missing and backs off when a custom listener bean exists.

## 4. Verification

- [ ] 4.1 Run the command log applib test suite for the new listener and autoconfiguration coverage.
- [ ] 4.2 Run `openspec validate --change default-command-replay-mapping-listener --strict`.
