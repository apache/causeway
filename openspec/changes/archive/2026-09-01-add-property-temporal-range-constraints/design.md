## Context

`<cw-property>` already selects reversible native and qualified Vaadin editors from the authoritative GraphQL datatype.
`LocalDate`, `LocalTime`, and `LocalDateTime` preserve ISO lexical values without timezone conversion, and qualified time controls use a supported 15-minute picker interval while retaining accepted seconds and fractional precision.

The authored component can override labels and multiline presentation but cannot currently constrain a temporal editor to an application-page interval.
Vaadin and native temporal controls both expose minimum and maximum concepts, but simply decorating the control is insufficient because property save must also reject an out-of-range pending value before validation or mutation.

## Goals / Non-Goals

**Goals:**

- Let authored `<cw-property>` declarations set optional `min` and `max` bounds for local date, time, and date-time editors.
- Use the authoritative datatype to parse and compare bounds and pending values.
- Support stable edit-time-relative bounds for common future-date and future-date-time use cases.
- Propagate one resolved interval through native and qualified Vaadin controls.
- Retain an out-of-range pending value with an accessible field error and perform no GraphQL validation or mutation until corrected.
- Keep malformed configuration diagnosable without replacing or disabling the canonical editor.

**Non-Goals:**

- Do not derive constraints from non-authoritative labels, values, browser input types, or naming conventions.
- Do not add bounds to action parameters in this change.
- Do not constrain offset, zoned, legacy, or text-fallback temporal types.
- Do not replace canonical domain validation or secure direct GraphQL clients.
- Do not round accepted values to picker steps or convert local values through a timezone.
- Do not constrain read-only rendering.

## Decisions

### Use standard `min` and `max` component attributes

`<cw-property min="..." max="...">` and matching JavaScript properties provide a familiar interval vocabulary without a new child component.
Absolute bounds use the same ISO lexical family as the authoritative `LocalDate`, `LocalTime`, or `LocalDateTime` value.

`LocalDate` additionally accepts `today` and `tomorrow`.
`LocalDateTime` additionally accepts `now`.
`LocalTime` accepts only an absolute local time because a dayless value has no coherent future-relative interpretation.

A separate `range`, `future`, or business-hours mini-language was rejected because `min` and `max` compose the required cases with less grammar and map directly to both control families.

### Resolve and freeze bounds when editing begins

A temporal-range helper will validate authored tokens against the authoritative semantic type, resolve relative tokens from the browser's local calendar and clock, and produce immutable resolved ISO bounds plus comparable calendar and clock parts.
The resolved range is stored in the property interaction state before editor selection and remains unchanged until save or cancellation.
A later edit resolves relative tokens again.

Freezing avoids a long-running `min="now"` edit becoming invalid merely because wall-clock time advanced.
Using local calendar fields rather than UTC conversion preserves the established local-temporal semantics.

### Treat malformed or inverted ranges as non-authoritative configuration

If either authored bound is blank, malformed, incompatible with the semantic type, or if the resolved minimum exceeds the maximum, the complete authored interval is ignored atomically.
The canonical editor remains usable and `<cw-property>` exposes `data-causeway-temporal-range-status="invalid"` while editing.
Valid configured ranges expose `data-causeway-temporal-range-status="valid"`; absent or inapplicable ranges expose no status.

Applying only one side of a malformed pair was rejected because it would silently enforce a different interval from the one the author declared.
Blocking all editing was rejected because HTML presentation must not make an otherwise canonical domain property unavailable.

### Share local-temporal parsing and comparison

A small local-temporal range module will use the same accepted lexical grammar as the value codec and compare parsed date and time parts rather than browser `Date` instants or raw strings.
This preserves semantic equality between values such as `08:00` and `08:00:00`, supports up to nine fractional digits, and avoids timezone conversion and unsafe large numeric date-time encodings.

An empty nullable pending value remains governed by existing required semantics rather than range validation.

### Enforce range before GraphQL validation and mutation

The property validation path will check the frozen range before calling `validateProperty`.
A value below or above the interval transitions to the existing failed interaction state with a bounded message naming the resolved ISO boundary.
Save therefore remains unavailable, no GraphQL validation or update request occurs, the pending lexical value remains in the editor, and normal correction resumes the existing validation lifecycle.

Canonical GraphQL validation still runs after local range acceptance and remains authoritative for all domain rules.

### Propagate resolved bounds through editor context

The editor context gains optional resolved `min` and `max` strings.
The native temporal editor emits escaped `min` and `max` attributes only for supported local controls.
The semantic Vaadin host carries equivalent `data-min` and `data-max` attributes and applies them to the qualified date, time, or date-time control before assigning its value.

Read-only field views omit bounds.
Fallback from Vaadin to native retains the same frozen editor context and interval.

## Risks / Trade-offs

- [Browser and Vaadin controls interpret bounds differently] → Use their documented ISO min/max APIs and retain Causeway-owned local validation as the common enforcement layer.
- [Relative bounds vary by timezone] → Resolve from local calendar fields deliberately, document that semantics, and never convert through UTC for comparison.
- [A malformed declaration could silently surprise an author] → Ignore it atomically and expose a deterministic host diagnostic attribute with unit coverage.
- [A value with seconds could compare incorrectly to a minute-only bound] → Compare parsed padded temporal parts, not lexical string lengths or picker steps.
- [A control can visually mark invalid before Causeway validation settles] → Keep the existing field error, interaction state, focus, and save lifecycle authoritative.

## Migration Plan

The attributes are optional, so existing pages and editors remain unchanged.
Applications can add ISO or supported relative bounds incrementally.
Rollback removes the attributes or reverts the range helper and propagation without changing persisted values, GraphQL, routes, or domain code.

## Open Questions

None.