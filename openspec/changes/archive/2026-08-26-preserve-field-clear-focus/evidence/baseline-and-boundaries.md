# Baseline and boundaries

## Reproduced defect

After Edit Known As focuses the field, typing schedules debounced authoritative validation.
Tab moves focus to the Causeway clear `×`, but the brief transition near the “Editing” status shows the property entering and leaving validation.
Those consecutive renders replace the asynchronously upgraded field adapter while its suffix owns focus.
The first replacement temporarily leaves body focus active, the second no longer infers Clear as the intended target, and the next Tab restarts at the field.

The prior browser regression moved immediately from Clear to Save and therefore did not hold Clear long enough to expose the validation lifecycle.
The strengthened regression pauses for validation and fails against the pre-change implementation because active focus becomes the body.

## Preserved boundaries

- GraphQL remains authoritative for property validation and mutation.
- Validation debounce, status announcements, error semantics, and the visible validating/editing transition remain unchanged.
- Causeway retains ownership of public elements, focus policy, clear semantics, cancellation, and events.
- The adapter uses no private Vaadin shadow access and exposes no raw toolkit API to applications.
- Clear focus is restored only for a connected visible suffix carrying a current property-owned intent.
- Genuine external focus departure clears that intent and is never reversed.
- Save/Cancel restoration, editor selection restoration, protected-value filtering, native fallback, strict CSP, routes, and packaging remain unchanged.
