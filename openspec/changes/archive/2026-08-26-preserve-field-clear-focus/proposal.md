## Why

The Causeway-owned field clear button is now tabbable, but debounced property validation replaces the editor while focus is on that button.
Because the replacement adapter upgrades asynchronously, focus falls to the document body and the next Tab restarts at the field.

## What Changes

- Track clear-button focus as an owned property-interaction focus intent.
- Carry that intent across consecutive validation rerenders while the replacement field adapter upgrades.
- Ask the replacement adapter to focus its clear suffix once the non-empty control is ready.
- Clear the intent when focus moves to another owned control or genuinely leaves the property.
- Extend foundation and Petclinic regressions to hold focus on Clear through validation and continue naturally to Save.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Extend deterministic property-editor focus preservation to asynchronously replaced owned controls.
- `vaadin-semantic-editor-families`: Require the Causeway-owned clear suffix to retain focus across owner validation rerenders.

## Impact

The change affects property interaction focus bookkeeping, the internal field adapter focus API, foundation tests, and Petclinic browser qualification.
It does not change GraphQL operations, validation timing, values, semantic events, public element names, routes, CSP, dependencies, toolkit assets, or native fallback.
