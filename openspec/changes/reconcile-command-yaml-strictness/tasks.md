## 1. Re-verify on current HEAD

- [x] 1.1 Confirm the legacy `CommandReplayManager.importCommands` uses `CommandDtoUtils.fromYaml` (lenient) while
      the unified `CommandManager_importCommands` uses `fromYamlForReplay` (strict), and that the strict decoder
      accepts wrapped + legacy multi-document and fails on unparseable input.

## 2. Harden the legacy importer

- [x] 2.1 Change the legacy importer to decode with `fromYamlForReplay(...)` and persist each carrier's embedded
      command via `saveForReplay(carrier.getCommand())`.
- [x] 2.2 Derive `moveBaselineToOldest` from the decoded carriers' commands (mirroring the unified importer).

## 3. Tests

- [x] 3.1 A malformed-YAML upload through the legacy importer fails with a clear error (nothing imported, failure
      reported) rather than a silent empty success.
- [x] 3.2 A valid canonical (wrapped, result-bearing) stream imports correctly through the legacy path, storing
      result bookmarks where present.
- [x] 3.3 A valid legacy multi-document stream imports correctly through the legacy path.
- [x] 3.4 Confirm the general-purpose `CommandDtoUtils.fromYaml` API is unchanged for non-replay callers.

## 4. Verification

- [x] 4.1 Run focused `api/applib` YAML tests and commandlog applib import tests plus the affected reactor under
      JDK 21, and strict OpenSpec validation.
- [x] 4.2 Confirm the legacy managers, `EXPORTED` state, and `makeSelectedExportable` are retained unchanged.
