## 1. Re-verify anchors on current HEAD

- [x] 1.1 Confirm `getDto()` renders the raw `CommandDto`, `actualBookmarkFor` falls back only for `OK`, the
      title uses the ellipsified `getTargetId()`, there are no top-level `getTarget()/getActualTarget()`, and the
      two `openTarget` mixins exist but are unregistered.

## 2. MA-6 — displayed DTO includes the recorded result

- [x] 2.1 Change `getDto()` to serialise `CommandDtoUtils.CommandExportDto.of(commandDto, result)`.

## 3. MA-7 — actual-bookmark fallback covers UNDEFINED

- [x] 3.1 Add `ReplayState.isExecutedOk()` returning true for `UNDEFINED` or `OK`.
- [x] 3.2 Use `isExecutedOk()` in `actualBookmarkFor` for both the result-role gate and the unmapped fallback.

## 4. MA-8 — untruncated title

- [x] 4.1 Build the replayable title from the full recorded target bookmark; leave `getTargetId()` (10-char) for
      column use.

## 5. MA-9 (D-A) — recorded-vs-actual target UI

- [x] 5.1 Add top-level `@Property getTarget()` and `getActualTarget()` projections (actual via the
      result-remapping / actual-bookmark lookup with `isExecutedOk()` semantics).
- [x] 5.2 Extend `ReplayableCommand_openTarget` and `_openTargetTR` with a `TargetType {RECORDED, ACTUAL}`
      parameter; open the chosen target; disable with user feedback when it cannot be resolved.
- [x] 5.3 Register both mixins in `CausewayModuleExtCommandLogApplib`.

## 6. Tests

- [x] 6.1 `getDto()` output contains the result envelope + result bookmark.
- [x] 6.2 A recorded-only (`UNDEFINED`) command exposes recorded bookmarks as actual for target/parameter/result;
      a genuinely-pending unmapped participant still exposes no actual bookmark.
- [x] 6.3 Title contains the full recorded target id.
- [x] 6.4 `openTarget` RECORDED/ACTUAL opens the correct target and disables with feedback when unresolved.
- [x] 6.5 Update `ReplayableCommandPresentationTest` to assert the `openTarget` mixins are now registered with the
      RECORDED/ACTUAL choice.

## 7. Verification

- [x] 7.1 Run focused commandlog applib presentation tests plus the affected reactor under JDK 21, and strict
      OpenSpec validation.
- [x] 7.2 Confirm no change to replay execution, eligibility, mapping SPI, or persistence.
