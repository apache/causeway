## Context

The completed property-range work provides a shared immutable range model, ISO lexical validation, relative local-calendar resolution, precision-safe comparison, native attributes, and Vaadin host propagation.
Action parameters already use the same editor registry and field adapters, but optional `<cw-parameter>` declarations currently carry only name, description, description presentation, and multiline hints.
The interaction controller owns parameter defaults, choices, pending values, canonical preparation and validation, confirmation, invocation, cancellation, focus, and all prompt styles.
The extension must therefore add local range behavior without moving authority into hidden declaration elements or application markup.

## Goals / Non-Goals

**Goals:**

- Give `<cw-parameter>` optional `min` and `max` attributes and matching JavaScript properties.
- Apply the existing `LocalDate`, `LocalTime`, and `LocalDateTime` absolute and relative grammar consistently with properties.
- Resolve relative bounds once per prompt after canonical parameter types are known.
- Prevent out-of-range parameter values from reaching GraphQL preparation, validation, confirmation, or invocation.
- Preserve pending values, field-specific errors, focus, correction, cancellation, and all prompt styles.
- Demonstrate future-date and daily office-hour constraints in Petclinic without raw Vaadin markup.

**Non-Goals:**

- Derive bounds from Causeway domain annotations or add GraphQL range metadata.
- Let authored declarations create, remove, reorder, default, require, hide, disable, choose, validate canonically, or invoke parameters.
- Treat a single `LocalDateTime` interval as a recurring daily office-hours rule.
- Change offset, zoned, legacy, read-only, reference, or unsupported temporal behavior.

## Decisions

### Carry authored bounds through normalized parameter presentation

`CausewayParameterElement` will observe `min` and `max`, expose matching properties, and include explicitly authored values in its normalized configuration.
Normalization will preserve null versus blank and bound untrusted declaration text, but it will not parse the values because the declaration does not own the authoritative parameter datatype.
The existing direct-child matching by authoritative parameter ID remains the only association mechanism.

An alternative was to let `<cw-parameter>` resolve its own range, but that hidden presentation element has neither canonical type metadata nor prompt-generation ownership.

### Resolve immutable ranges in the interaction controller

After action preparation and default recomputation establish the canonical parameter list, the interaction controller will match declarations, normalize semantic type names, and resolve one immutable range per applicable parameter.
The resolved map remains unchanged for the complete prompt generation, including recomputation, validation, confirmation, correction, and invocation.
A later prompt opening resolves relative tokens again.

An alternative was to resolve on every render, but `today`, `tomorrow`, or `now` could then drift while a prompt remains open.

### Keep local range errors distinct from canonical parameter state

Prompt state will retain local range errors separately from GraphQL-provided parameter validity.
Committed edits and submission will check the frozen range before any GraphQL request.
A local failure records the pending value, marks the parameter as validated, renders a field-specific reason, focuses the first invalid control on submission, and leaves canonical state untouched.
Correction clears the local reason and resumes the existing preparation and validation lifecycle.

An alternative was to overwrite `parameter.state.error`, but that risks confusing authored local failures with authoritative supporting-method results during recomputation.

### Reuse editor context and adapter propagation

A valid resolved range contributes `min` and `max` to the existing parameter editor context.
The existing native temporal editor and semantic Vaadin field host already consume those fields, so parameter support will not duplicate toolkit logic.
Absent, invalid, or inapplicable ranges contribute no bounds.
The rendered parameter wrapper will expose `data-causeway-temporal-range-status="valid"` or `"invalid"` only while the prompt is active.

### Split Petclinic booking date and time

Petclinic `bookVisit` will replace its single `LocalDateTime visitAt` input with authoritative `LocalDate visitDate` and `LocalTime visitTime` inputs and combine them into the existing persisted `LocalDateTime`.
The declaration will use `min="tomorrow"` for the date and `min="08:00" max="17:00"` for the time.
Canonical Java validators will independently enforce a future calendar date and the inclusive office-hour interval.

A single `LocalDateTime` minimum and maximum was rejected because one closed interval cannot express recurring office hours across every future day.

## Risks / Trade-offs

- [Risk] Parameter recomputation may replace canonical parameter state after a local correction. → Keep ranges and local errors in controller-owned prompt state keyed by stable authoritative IDs.
- [Risk] Browser controls can flag a bound violation before Causeway commits the value. → Retain Causeway validation as the request gate and use toolkit validity only as complementary presentation.
- [Risk] Relative bounds can cross midnight while a prompt is open. → Resolve once by design and document that reopening starts a new range generation.
- [Risk] Refactoring Petclinic parameters changes generated GraphQL argument names. → Update all sample tests and preserve the persisted visit representation and action outcome.
- [Risk] Authored invalid ranges could make prompts unusable. → Ignore the complete interval atomically and expose a deterministic diagnostic state.

## Migration Plan

No migration is required for existing declarations because omitted bounds normalize to null and preserve current behavior.
Applications can add bounds incrementally to direct child `<cw-parameter>` declarations.
Rollback consists of removing the attributes and reverting the Foundation controller/configuration changes; no stored data or GraphQL schema migration is involved.

## Open Questions

None.
