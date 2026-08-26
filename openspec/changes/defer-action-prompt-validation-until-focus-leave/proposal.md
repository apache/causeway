## Why

Action prompts currently expose mandatory and other parameter-validity messages as soon as the prompt opens and can recompute them while the user is still typing.
This produces distracting noise before the user has completed a field.

## What Changes

- Keep newly opened action parameters visually free of validation errors while preserving labels, required semantics, defaults, choices, disabled reasons, and GraphQL-authoritative preparation.
- Capture pending input locally without presenting parameter validation or recomputing dependent parameter state while focus remains in that field.
- Recompute authoritative parameter state and reveal validation when focus leaves the edited field.
- Preserve whole-action validation when Invoke is activated, including when the current field has not been blurred.
- Add foundation and Petclinic browser regression coverage for open, typing, focus-leave, correction, and submit behavior.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Refine rich action-parameter negotiation so validation presentation and dependent recomputation occur after field focus leaves, while invocation still validates the complete pending argument set.

## Impact

The change affects the standard interaction controller's prompt-local value capture, validation visibility, focusout lifecycle, and focused tests.
It does not change GraphQL schemas or operations, action authority, codecs, autocomplete search, protected-value handling, public components, toolkit adapters, or HTMX routing.
