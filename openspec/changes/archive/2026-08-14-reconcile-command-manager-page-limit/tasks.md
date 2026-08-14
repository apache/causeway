## 1. Re-verify on current HEAD

- [x] 1.1 Confirm `CommandManager.DEFAULT_LIMIT = 100` with no `MAX_LIMIT`, `HasLimit_changeLimit.validateNewLimit`
      enforces only `> 0`, and the standard `CommandLogMenu` action opens at `DEFAULT_LIMIT`.

## 2. Cap the page limit at 320

- [x] 2.1 Add `MAX_LIMIT = 320` to `CommandManager`.
- [x] 2.2 Cap the derived limit: memento/constructed limit above 320 clamps to 320; non-positive uses
      `DEFAULT_LIMIT = 100`.
- [x] 2.3 Update `HasLimit_changeLimit.validateNewLimit` to accept only `1..320` (reject out-of-range with a
      clear message).

## 3. Open the standard menu at 320

- [x] 3.1 Change the standard `CommandLogMenu` unified-manager action to open at `MAX_LIMIT` (320); keep legacy
      `openCommandManager` shims at `DEFAULT_LIMIT` (100).

## 4. Tests

- [x] 4.1 `validateNewLimit` rejects 0 and 321, accepts 1 and 320.
- [x] 4.2 A memento with limit 500 reconstructs to 320; with a non-positive limit reconstructs to 100; a valid
      limit ≤ 320 round-trips unchanged.
- [x] 4.3 The standard menu action opens the manager at page limit 320.

## 5. Verification

- [x] 5.1 Run focused commandlog applib manager tests plus the affected reactor under JDK 21, and strict OpenSpec
      validation.
- [x] 5.2 Confirm baseline handling, collections, mementos (other than the limit bound), and the
      `replayOrRetryMultiple` batch enum are unchanged.
