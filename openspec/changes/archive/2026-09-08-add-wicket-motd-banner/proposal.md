## Why

Applications need a simple way for developers and administrators to notify end users about pertinent operational information such as scheduled daytime downtime.
The Wicket footer currently gives prominent space to a little-used history dropdown while offering no application-defined, time-limited notice mechanism.

## What Changes

- Add an applib SPI through which an application can provide at most one message of the day with a plain-text title, trusted HTML detail, a display start instant, and a positive display duration.
- Add a Wicket footer banner that shows the active message title and opens its detailed HTML in a dialog when clicked.
- Evaluate message activity at page-render time using Causeway's `ClockService` and a half-open interval from the display start, inclusive, until the start plus the duration, exclusive.
- Keep the existing footer history dropdown and allow it to coexist with the MOTD banner when explicitly enabled.
- **BREAKING** Change `causeway.viewer.wicket.bookmarked-pages.show-drop-down-on-footer` to default to `false`, while retaining the property so applications can opt back in.

## Capabilities

### New Capabilities

- `wicket-footer-motd`: Defines the application-provided, time-limited MOTD and its title-banner and detail-dialog behavior in the Wicket footer, including coexistence with the configurable history dropdown.

### Modified Capabilities

None.

## Impact

- Adds a public SPI and MOTD value type to regular applib without introducing a new external dependency.
- Changes the default Wicket viewer configuration for the footer history dropdown.
- Affects Wicket footer composition, rendering, Ajax dialog behavior, styling, tests, and viewer configuration documentation.
- Treats the SPI-provided detail HTML as trusted and unsanitized application content.
