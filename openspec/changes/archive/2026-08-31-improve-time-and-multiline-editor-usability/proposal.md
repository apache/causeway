## Why

Qualified time editors currently expose millisecond precision even though ordinary property and action-parameter entry needs minute precision, and their clock affordance is not reliably reachable or activatable by keyboard or pointer.
Multiline Vaadin editors also receive both the application-wide host outline and the toolkit input focus ring, producing a distracting double border.

## What Changes

- Configure editable `LocalTime` and `LocalDateTime` Vaadin controls for minute-resolution entry rather than millisecond-resolution entry.
- Expose each editable time-picker clock affordance as a semantic-field-labelled button in the normal Tab sequence, with Enter, Space, and pointer activation.
- Preserve authoritative temporal codec semantics while ensuring newly entered values are minute-aligned.
- Suppress the redundant application-level outline on focused Vaadin multiline controls while retaining one visible toolkit focus ring.
- Add foundation qualification and Petclinic browser regression coverage for property and action-parameter time entry, clock-trigger operation, and multiline focus appearance.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `vaadin-semantic-editor-families`: Require minute-resolution editable local-time controls, accessible clock triggers, and a single visible multiline focus indicator.
- `domain-web-components`: Require property and action-parameter local-time entry to share minute precision and operable time-picker semantics without changing authoritative codec ownership.
- `generic-htmx-web-component-viewer`: Verify the Petclinic date-time parameter and multiline parameter demonstrate the corrected time and focus behavior.

## Impact

The change affects the shared field-widget adapter, structural and application theme rules, Vaadin field-family qualification tests, and Petclinic Playwright coverage.
It introduces no GraphQL schema, route, domain-model, or dependency-version changes.
