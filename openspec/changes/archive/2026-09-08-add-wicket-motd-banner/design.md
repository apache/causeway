## Context

The Wicket viewer footer currently renders a Select2 breadcrumb/history dropdown when `causeway.viewer.wicket.bookmarked-pages.show-drop-down-on-footer` is enabled, and that property currently defaults to `true`.
Applications have no viewer-neutral SPI for supplying a scheduled operational notice.
The footer is a stateful Wicket component, so a notice can become active or inactive after a page instance has been initialized.
The repository targets Java 11 and already exposes `Instant`, `Duration`, `ClockService`, service-registry lookup, trusted HTML rendering, and a reusable Bootstrap modal dialog without requiring another dependency.

## Goals / Non-Goals

**Goals:**

- Define a viewer-neutral applib SPI for supplying zero or one message of the day.
- Encapsulate the message title, trusted HTML detail, display start, positive display duration, derived display end, and active-time test in an immutable applib value.
- Render an active message as a compact, clickable title in the Wicket footer and show its detail in a modal dialog.
- Re-evaluate the provider and current time on page render so a reused Wicket page reflects scheduling and content changes.
- Keep the footer history dropdown available while making it hidden by default.

**Non-Goals:**

- Supporting multiple simultaneous messages, ordering, or conflict resolution.
- Polling, server-sent events, or other live appearance while the browser remains idle.
- Per-user dismissal, acknowledgement, severity levels, or audience targeting.
- Sanitizing application-provided HTML.
- Changing the existing rules that show or suppress the footer itself.

## Decisions

### Define the SPI in regular applib

Add a `MessageOfTheDayProvider` service contract and an immutable `MessageOfTheDay` value under a dedicated applib service package.
The provider returns an `Optional<MessageOfTheDay>`, allowing the service to be absent or to report no candidate without requiring a framework default implementation.
The Wicket viewer resolves the provider through the service registry so applications that do not implement the SPI retain an empty footer notice area.
This location keeps the contract independent of Wicket and permits another viewer to consume it later.
A Wicket-specific SPI was rejected because the supplied data and schedule are not inherently Wicket concepts.

### Model scheduling with an instant and duration

The value stores `displayFrom` as an `Instant` and `displayDuration` as a strictly positive `Duration`.
It derives `displayUntil` as `displayFrom.plus(displayDuration)` and defines activity using the half-open interval `displayFrom <= now < displayUntil`.
The value owns this logic so consumers do not reproduce boundary semantics.
A pair of start and end instants was rejected in favor of the duration-based contract requested for convenient construction with `Duration.ofHours`, `Duration.ofMinutes`, and related factories.
A third-party interval type was rejected to avoid another public API dependency.

### Treat the title as text and the detail as trusted HTML

The title is escaped and rendered as the compact clickable content in the footer.
The detail is a plainly named HTML string that the dialog renders without escaping or sanitizing.
The API documentation will identify the detail as trusted application content because rendering untrusted input would create a cross-site-scripting vulnerability.
Causeway's `Markup` and optional AsciiDoc value types were rejected to keep the contract elementary and avoid coupling this capability to an optional value-type module.

### Resolve the message at render time

The footer consults the provider and `ClockService` during Wicket configuration for each page render rather than only during component initialization.
This allows an existing page instance to reflect a newly supplied, changed, started, or expired message on its next render.
The viewer does not install a browser timer, so an idle page changes only after another request causes rendering.

### Use the existing modal infrastructure

The footer title is rendered as a keyboard-accessible Ajax link or button.
Activating it opens the existing Bootstrap-based Wicket modal with the same title in its header and the trusted HTML detail in its body.
Reusing the existing modal infrastructure preserves established focus, keyboard, sizing, and Ajax behavior.

### Let the MOTD and history dropdown coexist

The existing `show-drop-down-on-footer` property remains the sole control for the footer history dropdown, but its default changes from `true` to `false`.
When explicitly enabled, the dropdown and an active MOTD are both rendered, with the MOTD occupying the footer's flexible region.
Removing or renaming the existing property was rejected because applications may still value the history dropdown and already know its configuration key.

## Risks / Trade-offs

- [Risk] Trusted HTML can execute unwanted markup or script if an application passes untrusted administrator input → Document the trust boundary prominently and require provider implementations to sanitize externally managed content before returning it.
- [Risk] A long title can crowd the other footer controls, especially when the history dropdown is enabled → Constrain the title region with responsive styling and ellipsis while keeping the complete detail available in the dialog.
- [Risk] A provider backed by a database or remote system can add work to every page render → Document that providers should be inexpensive or internally cached and perform only one lookup per render.
- [Risk] Adding `displayDuration` to `displayFrom` can overflow the supported `Instant` range → Validate construction and reject values whose derived end cannot be calculated.
- [Trade-off] The banner does not appear or disappear at an exact wall-clock instant on an idle browser page → Accept page-render semantics to avoid polling or push infrastructure.
- [Trade-off] Changing the history dropdown default alters existing applications that rely on the implicit default → Retain the property and document the explicit opt-in migration.

## Migration Plan

Applications that want the current footer history behavior set `causeway.viewer.wicket.bookmarked-pages.show-drop-down-on-footer=true` explicitly.
Applications that do not provide `MessageOfTheDayProvider` require no migration and render no MOTD.
Applications adopting the feature register one provider and ensure every returned message has a valid start, positive duration, plain-text title, and trusted HTML detail.
Rollback consists of removing the provider and restoring the previous configuration default or explicitly enabling the history dropdown.

## Open Questions

None.
