## 1. Re-verify on current HEAD

- [x] 1.1 Confirm `CollectionContentsAsSummaryFactory.appliesTo` has no summary-view-disabled short-circuit and
      no env-var lookup, and confirm how it reaches `CausewayConfiguration` (via `MetaModelContext`).

## 2. Configuration property

- [x] 2.1 Add `@DefaultValue("false") boolean summaryViewDisabled` to `CausewayConfiguration.Viewer.Wicket`,
      mapping to `causeway.viewer.wicket.summary-view-disabled`, in the existing boolean-property style.

## 3. Factory short-circuit

- [x] 3.1 In `CollectionContentsAsSummaryFactory.appliesTo(...)`, return `ApplicationAdvice.DOES_NOT_APPLY` when
      `…viewer().wicket().summaryViewDisabled()` is true, before the BigDecimal-column check.

## 4. Tests

- [x] 4.1 Property unset/false: a BigDecimal-bearing collection still applies the summary view.
- [x] 4.2 Property true: `appliesTo` returns `DOES_NOT_APPLY` for the same collection.

## 5. Verification

- [x] 5.1 Run focused Wicket viewer summary tests and core/config binding tests plus the affected reactor under
      JDK 21, and strict OpenSpec validation.
- [x] 5.2 Confirm no environment-variable lookup is introduced and no other collection presentation changes.
