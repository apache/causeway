## 1. Visible keyboard time overlay

- [x] 1.1 Configure editable time and date-time pickers with Vaadin's supported 900-second dropdown interval while retaining minute-format presentation and precise read-only values.
- [x] 1.2 Replace delayed clock-trigger activation with synchronous trusted-event focus transfer and Vaadin `open()` invocation for Enter and Space.
- [x] 1.3 Preserve Tab, reverse Tab, disabled/read-only omission, current-generation safety, pointer behavior, and authoritative cancellation semantics.
- [x] 1.4 Strengthen foundation unit and Vaadin browser-audit coverage to require an actually visible `:popover-open` time overlay rather than only picker state.

## 2. Single multiline boundary

- [x] 2.1 Restrict application native textarea selectors to unslotted controls so Vaadin's `slot="textarea"` avoids native border, padding, sizing, and focus chrome.
- [x] 2.2 Update structural style tests and real-browser assertions for one Vaadin boundary while retaining native fallback textarea presentation.

## 3. Authoritative collection totals

- [x] 3.1 Append valid non-negative safe-integer `totalCount` metadata to non-empty bounded pager range labels.
- [x] 3.2 Preserve `No items` and unknown-total behavior without deriving counts from offsets, loaded rows, page size, or navigation flags.
- [x] 3.3 Extend collection unit and responsive tests across first, middle, final, unavailable-total, empty, and invalid-total windows.

## 4. Petclinic acceptance and documentation

- [x] 4.1 Extend Petclinic Playwright coverage to prove Enter and Space visibly open the current visit time overlay without premature booking.
- [x] 4.2 Verify the visit reason's slotted textarea has no nested native border or outline while the Vaadin input container remains visibly bounded.
- [x] 4.3 Verify paged Visits collection labels show correct ranges and the same authoritative total across pages.
- [x] 4.4 Update component usage documentation for visible keyboard time activation, unslotted native textarea styling, and authoritative pager totals.

## 5. Validation

- [x] 5.1 Run complete foundation Node and Maven suites plus the Vaadin field browser audit.
- [x] 5.2 Run focused Petclinic Playwright acceptance and verify console, page-error, CSP, external-request, overlay, focus, and overflow diagnostics.
- [x] 5.3 Run relevant IDE inspections or project compilation, strict OpenSpec validation, and diff checks.
