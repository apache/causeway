## Context

The standard action controller must call `prepareAction` when a prompt opens to discover parameters, hidden and disabled state, defaults, choices, autocomplete, input types, and dependency metadata.
That preparation can also return mandatory or other validity reasons, which the prompt currently renders immediately.
Text input additionally schedules `setParameterValue` after 250 milliseconds, causing preparation and validity rendering while the user is still composing a value.

## Goals / Non-Goals

**Goals:**

- Keep initial and actively edited parameter fields free of validation messages until focus leaves the field.
- Capture every pending value locally so Invoke always validates the current text even without a prior blur.
- Recompute dependent parameter state once the edited field is committed by focus departure.
- Preserve initial GraphQL preparation for structure, defaults, choices, hidden state, disabled state, and capability discovery.
- Preserve autocomplete search while distinguishing it from parameter validation.

**Non-Goals:**

- Do not weaken GraphQL authority or invoke without whole-action validation.
- Do not defer disabled reasons, structural preparation, autocomplete search, or local required semantics.
- Do not change property-editor validation timing.
- Do not add native browser constraint-validation messages or toolkit-specific APIs.

## Decisions

### Separate pending capture from committed parameter recomputation

`input` and `change` events will parse and retain the latest pending value locally without calling `prepareAction` for that value.
A bubbling `focusout` from a parameter editor will commit the current value through the existing `setParameterValue` path, which recomputes dependent parameter state and retains stale-generation protection.

The existing text debounce will no longer trigger parameter preparation.
Autocomplete search remains debounced because suggestion retrieval is a distinct user-requested capability rather than validity presentation.

Debouncing validation more slowly was rejected because it still interrupts a user who pauses while typing.
Validating only on Invoke was rejected because users benefit from field feedback after completing each field and dependent choices must update before submission.

### Track validation visibility independently from prepared state

The controller will maintain a prompt-local set of parameter ids whose fields have lost focus or whose values are being submitted.
Prepared `validity` and parameter `error` reasons are rendered and linked to controls only for those ids.
Disabled reasons remain visible regardless of this set because they describe usability rather than an incomplete user value.

Mutating GraphQL response objects to remove validity was rejected because the controller must retain authoritative prepared state and may need to reveal it later.
Encoding touched state in public semantic events was rejected because this is presentation-local lifecycle state.

### Invoke reveals and validates the complete argument set

Submit will mark all prompt parameters validation-visible before running `validateAction`.
The pending values already captured from input and change events are therefore authoritative inputs even if the current editor never blurred.
Existing mapped action and parameter errors, first-invalid focus, invocation gating, cancellation, and stale response handling remain unchanged.

## Risks / Trade-offs

- [Risk] Dependent parameter choices update later than today. → Commit and recompute immediately on focus departure, matching the requested completion boundary.
- [Risk] A pointer click on Invoke races with blur recomputation. → Pending capture occurs before blur, and generation protection lets submit supersede obsolete preparation while validating current values.
- [Risk] Toolkit editors emit different input/change sequences. → Use the existing semantic `data-causeway-editor` event boundary and bubbling focusout used by native and qualified adapters.
- [Risk] Protected input could leak through new state. → Retain existing protected prompt filtering and add no diagnostics or public value events.

## Migration Plan

Deploy the foundation controller and tests together.
Rollback restores debounced parameter preparation and eager validity presentation without schema, data, route, or resource migration.

## Open Questions

None.
