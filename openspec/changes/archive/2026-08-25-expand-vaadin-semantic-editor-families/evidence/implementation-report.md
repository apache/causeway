# Implementation report

## Adapter boundary

`field-widget.mjs` introduces one Causeway-owned internal adapter with explicit `basic`, `numeric`, and `local-temporal` policy.
The editor registry passes the already selected Causeway codec into family eligibility and keeps the reference adapter at higher precedence.
Applications continue to render semantic Causeway properties and action prompts and receive ordinary Causeway events.

The adapter imports only its selected family after connection, guards import and definition completion by connection generation, maps semantic state onto the internal control, and removes toolkit-selection attributes from the wrapper after upgrade so each active editor has one stable event target.
Property and action hosts keep pending state, parsing, validation, dependencies, save or invoke behavior, cancellation, focus recovery, and GraphQL variables.
A failed module marks only its family unavailable, emits a bounded event, and causes the host to rerender through the native registry.

## Value correctness

Exact `Long`, `BigInteger`, and `BigDecimal` inputs use lexical text fields and still pass through the exact codec.
Machine numeric values use integer or number fields and still pass through their bounded codec.
Nullable Boolean uses a three-state select while required Boolean uses a checkbox.
Text, multiline, protected, URL, enum, and bounded scalar choices use the basic closure.
`LocalDate`, `LocalTime`, and `LocalDateTime` use pickers with millisecond step where current precision is representable.
Local time or date-time values with more than three fractional digits remain native, as do offset, zone, legacy temporal, resource, custom, reference, collection, and unsupported values.

Protected controls always initialize empty.
The sensitive codec continues to redact semantic events, prompt state, errors, and public interaction values, while adapter attributes and load diagnostics contain no prior or pending protected value.

## Delivery and rollback

The HTMX property `causeway.viewer.webcomponents.htmx.vaadin-field-families` accepts a unique normalized subset of `basic,numeric,local-temporal` and defaults empty.
The shell advertises only that bounded list.
Assets and legal metadata are packaged under same-origin foundation resources.
CSP includes only the enabled families' deterministic hash union and retains `style-src-attr 'none'`.

Petclinic and the Reference Application explicitly enable all qualified families for candidate runs.
Empty family configuration and disabled reference configuration exercise the unchanged native rollback.
The vanilla sample uses only Causeway elements while demonstrating basic, numeric, and local temporal semantic members.
