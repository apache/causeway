# Baseline and boundaries

## Reproduced defects

The supplied Notes, Email Address, and Known As screenshots show a visible Vaadin clear `×` inside each optional field.
Vaadin's current documentation states that its built-in clear button is not keyboard-focusable and that Escape is the keyboard clearing mechanism.
Causeway already assigns Escape to cancellation of the complete property interaction, so the visible internal clear control is pointer-only in this context.

The supplied keyboard sequence reproduces a second defect.
After Edit Known As moves focus to the field, typing schedules debounced authoritative validation.
Tab initially moves focus to Save, but validation-driven light-DOM replacement creates a transient body-focus window before queued action restoration.
A following Tab therefore restarts at the editor rather than continuing from Save.

## Preserved boundaries

- Causeway owns public elements, semantic interaction state, pending values, validation, cancellation, focus, and events.
- GraphQL remains authoritative for property preparation, validation, mutation, visibility, and usability.
- The solution uses Vaadin's public suffix slot and does not mutate private shadow parts.
- Required, protected, disabled, unsupported, and native-fallback editors do not gain an inappropriate clear control.
- Protected prior or pending values remain absent from markup, events, errors, diagnostics, and evidence.
- Escape continues to cancel the Causeway property interaction.
- External focus departure remains possible; no focus trap or global listener is introduced.
- Strict CSP, exact hashes, `style-src-attr 'none'`, route-lazy delivery, same-origin packaging, and toolkit rollback remain unchanged.
- No GraphQL operation, codec, route, dependency, lockfile, or application-authored HTML changes.
