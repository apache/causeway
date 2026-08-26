## Why

Optional Vaadin-backed field widgets expose a visible clear `×` that pointer users can activate but keyboard users cannot reach through Tab navigation.
Property-editor validation rerenders can also remove focus from Save after the user tabs out of a changed field, causing the next Tab to restart at the editor instead of continuing through the action controls.

## What Changes

- Replace the non-tabbable internal Vaadin field clear affordance with a Causeway-owned, labelled, keyboard-focusable suffix button for eligible optional fields.
- Preserve pointer clearing, pending-value updates, protected-value handling, field focus, and native fallback behavior.
- Restore property action focus synchronously when validation rerenders replace Save or Cancel controls.
- Add foundation regressions for clear-button semantics and validation-rerender focus continuity.
- Add Petclinic browser coverage for the editor, clear, Save, and Cancel tab sequence and for Save focus surviving validation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `vaadin-semantic-editor-families`: Require Causeway-owned keyboard operation for visible optional-field clearing.
- `domain-web-components`: Require property-editor action focus to remain stable across validation rerenders.

## Impact

The change affects the internal field adapter, standard property interaction focus restoration, theme styling for the Causeway clear suffix, foundation tests, and Petclinic browser qualification.
It does not change GraphQL operations, public Causeway element names, semantic value codecs, routes, CSP, dependencies, application markup, or the explicit-native rollback policy.
