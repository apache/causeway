## Why

The prior time and multiline usability change made the clock affordance focusable and attempted to remove a redundant host outline, but real browser use still exposes three presentation defects.
Keyboard activation can set time-picker state without presenting a usable visible overlay, Vaadin's internal `slot="textarea"` still matches application native-textarea styling and produces a second border, and bounded collection pagers omit an available authoritative total from their range label.

## What Changes

- Open the real Vaadin time-picker overlay synchronously from Enter or Space activation while the trusted keyboard event is active, and verify visible popover presentation rather than only the `opened` property.
- Preserve the clock trigger's Tab and reverse-Tab accessibility without using delayed activation that can lose browser activation context or target a superseded control.
- Restrict application native textarea styling to unslotted native controls so Vaadin's internal textarea receives only toolkit-owned boundaries and focus indication.
- Render bounded collection ranges as `Items <start>–<end> of <total>` whenever a valid authoritative total is available, while retaining range-only wording when totals are unavailable.
- Add foundation browser and unit coverage plus Petclinic acceptance for visible keyboard-opened time overlays, a single multiline border, and correct collection totals across pages.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `vaadin-semantic-editor-families`: Require trusted keyboard activation to present a visible time overlay and prevent native textarea chrome from crossing into Vaadin multiline internals.
- `domain-web-components`: Require accessible time-picker activation and bounded collection range labels to reflect available authoritative totals.
- `vaadin-collection-grid-adapter`: Require bounded pager labels to include safe authoritative totals without inventing unavailable counts.
- `generic-htmx-web-component-viewer`: Qualify the corrected time overlay, multiline boundary, and paged Petclinic collection labels in the executable browser profile.

## Impact

The change affects the shared field-widget keyboard bridge, application theme selectors, bounded collection pager rendering, foundation and Vaadin audit tests, and Petclinic Playwright assertions.
It changes no GraphQL schema, domain model, route, persisted value, dependency version, or collection window request semantics.
