## Why

Disabled properties currently place a separate circular information control between the label and value, which disrupts row alignment and makes the reason feel detached from the property it describes.
Read-only string values can also inherit end alignment even though their corresponding editors are left aligned, producing an inconsistent transition between view and edit modes.

## What Changes

- Remove the standalone disabled-reason information indicator from the property grid.
- Make the property label itself the hover and keyboard-focus tooltip target for a bounded disabled reason while retaining accessible description semantics.
- Preserve an independent non-redundant property-description tooltip when one exists.
- Mark GraphQL `String` property values with a semantic presentation class and explicitly align them to the logical start in baseline and cohesive-theme styles.
- Preserve non-string value alignment policy and property edit, disabled, responsive, and multiline behavior.
- Add regression coverage for label-owned disabled tooltips, keyboard accessibility, absence of the standalone indicator, and string alignment.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Move disabled-reason presentation onto the property label tooltip and require logical-start alignment for string property values.

## Impact

The change affects the foundation property custom element, shared component styles, the cohesive theme, and browser-side tests.
It adds no dependency and changes no GraphQL operation, disabled-state decision, property value, edit command, or application configuration.
