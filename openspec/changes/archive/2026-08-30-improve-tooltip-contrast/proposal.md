## Why

Tooltips currently use the same dark surface and light text as nearby action buttons, so an action description can appear to be another control rather than transient explanatory content.
A dedicated light neutral tooltip treatment will create clearer visual hierarchy while retaining accessible contrast and theme customization.

## What Changes

- Give component-owned tooltips a light neutral background, dark text, subtle border, and shadow.
- Apply the treatment consistently to action, member, and disabled-property tooltips.
- Expose documented `--causeway-*` tooltip color, border, and shadow variables with safe installable-style fallbacks.
- Preserve existing pointer, keyboard, positioning, responsive, multiline, and accessible-description behavior.
- Add structural and theme regression coverage for the tooltip tokens and shared presentation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Require visually distinct, high-contrast, customizable presentation for component-owned tooltips.

## Impact

The change affects the foundation component stylesheet, its installable JavaScript mirror, default theme tokens, usage documentation, and style tests.
No component markup, semantic event, domain interaction, or third-party dependency changes are expected.
