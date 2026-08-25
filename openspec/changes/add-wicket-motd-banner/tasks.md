## 1. Applib MOTD Contract

- [ ] 1.1 Add the immutable `MessageOfTheDay` applib value with title, trusted HTML detail, display start, positive display duration, derived display end, and half-open `isActiveAt` semantics.
- [ ] 1.2 Add unit tests for valid construction, non-positive-duration rejection, derived-end overflow rejection, and activity before, at, during, and at the end of the interval.
- [ ] 1.3 Add the `MessageOfTheDayProvider` applib SPI returning an optional single message, and expose and document its package through the applib module metadata.

## 2. Wicket Footer MOTD

- [ ] 2.1 Add the Wicket MOTD title component and markup with escaped text, keyboard-accessible activation, responsive overflow styling, and an initially hidden state.
- [ ] 2.2 Add the MOTD detail modal using the existing Wicket Bootstrap modal infrastructure, with the message title as its heading and trusted unescaped HTML as its body.
- [ ] 2.3 Integrate the MOTD component into `FooterPanel` so it resolves the optional provider, obtains the current instant from `ClockService`, and refreshes visibility and content during each page render.
- [ ] 2.4 Ensure an explicitly enabled footer history dropdown and an active MOTD render together without either suppressing the other.

## 3. Wicket Configuration and Documentation

- [ ] 3.1 Change the default of `causeway.viewer.wicket.bookmarked-pages.show-drop-down-on-footer` to `false` while retaining its explicit opt-in behavior.
- [ ] 3.2 Update Wicket configuration and applib documentation for the new dropdown default, MOTD SPI, render-time scheduling semantics, positive duration requirement, and trusted-HTML security boundary.

## 4. Verification

- [ ] 4.1 Add Wicket viewer tests covering absent and empty providers, inactive and active messages, escaped titles, trusted HTML detail dialogs, render-time expiry, and history-dropdown coexistence.
- [ ] 4.2 Run focused applib and Wicket viewer builds and tests, and resolve any compilation, module-export, markup, or inspection failures.
- [ ] 4.3 Validate the completed OpenSpec change and confirm every normative scenario is represented by implementation or automated coverage.
