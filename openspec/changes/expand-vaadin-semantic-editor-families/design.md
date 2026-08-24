## Context

The foundation already owns a toolkit-neutral editor registry, exact and machine numeric codecs, nullable Boolean semantics, local and offset temporal codecs, protected-value redaction, semantic interaction state, and native implementations for the target families.
The accepted Vaadin reference pilot established pinned same-origin Maven packaging, telemetry opt-out, exact style hashes, route-lazy loading, strict `style-src-attr 'none'`, internal custom-element adaptation, and native rollback.
This change extends that pattern only to value shapes whose existing codec is reversible and whose Vaadin control preserves the same semantics.

The public boundary remains `<causeway-*>` elements, Causeway semantic events and contexts, rich GraphQL operations, canonical routes, ordinary HTML composition, and `--causeway-*` variables.
Vaadin controls remain implementation details and do not own interaction, validation, navigation, or domain state.

## Goals / Non-Goals

**Goals:**

- Qualify internal Vaadin adapters for text, multiline, protected text, Boolean, scalar choices, enum, exact numeric, machine numeric, local date, local time, and local date-time values.
- Preserve the existing codec output byte-for-byte where lexical precision or temporal precision matters.
- Deliver independently lazy basic, numeric, and local-temporal browser closures with pinned policies and per-closure budgets.
- Preserve native behavior as explicit configuration, unsupported-shape fallback, module-failure fallback, and rollback.
- Exercise property and action-parameter interactions, dependent state, validation, focus, cancellation, replacement, theme, keyboard, accessibility, and strict CSP.

**Non-Goals:**

- Changing the default editor policy from native to Vaadin-first.
- Supporting offset time, offset date-time, zoned date-time, legacy date-time, resource upload, custom value objects, references, or collections through these field adapters.
- Adding GraphQL operations, persistence query behavior, Vaadin Flow, Binder, Pro controls, Grid, server-side Vaadin state, telemetry, CDN delivery, or raw application-facing Vaadin elements.
- Replacing the existing reference closure or merging all Vaadin controls into one eager bundle.

## Decisions

### Use existing codecs as the eligibility boundary

The adapter registration will first require an existing non-unsupported Causeway value codec and then map only an explicit semantic family allow-list.
Exact `Long`, `BigInteger`, and `BigDecimal` values use a Vaadin text field with numeric input hints rather than a number field, because JavaScript numeric coercion would lose lexical precision.
Machine numeric values use integer or number fields while still passing through the existing codec before GraphQL submission.
Only `LocalDate`, `LocalTime`, and `LocalDateTime` use pickers; offset-bearing, zoned, and legacy temporal values retain the native lexical editor.

Alternative considered: map every GraphQL scalar to the visually closest Vaadin control.
This was rejected because visual similarity is not proof of reversible Causeway semantics.

### Keep three independently lazy field closures

A new pinned `vaadin-fields` build area will emit basic, numeric, and local-temporal ESM assets.
The basic closure contains text, text-area, password, checkbox, and select controls.
The numeric closure contains integer and number controls.
The local-temporal closure contains date, time, and date-time controls.
Each asset has its own SHA-256 checksum, gzip ceiling, entry-point manifest, exact style-hash set, and verification result.
Shared transitive modules may be duplicated between closures; preventing an unaffected route from requesting a closure is more important than minimizing a hypothetical all-families download in this qualification change.

Alternative considered: add every control to the existing reference bundle.
This was rejected because it would make unrelated scalar editing pay the reference closure cost and weaken independent rollback.

### Adapt controls behind one Causeway-owned field element

The editor registry will render a `<causeway-field-editor>` adapter carrying only bounded, non-sensitive configuration needed to create the selected internal control.
The adapter dynamically imports the selected family asset after connection, sets the internal control's label and accessibility relationships, exposes compatible `value`, `checked`, focus, and selection behavior, and lets ordinary `input` and `change` events reach existing interaction controllers.
Protected values always initialize empty and are never serialized into adapter attributes, errors, diagnostics, or semantic events.
No public application contract includes a Vaadin tag or event.

Alternative considered: teach every property and action controller about each Vaadin control.
This was rejected because it duplicates toolkit knowledge and risks divergent semantic behavior.

### Fail one family closed and rerender natively

Configuration tracks enabled families and an independent failed state for each family.
A load or definition failure disables only the failing family for the current document and emits a bounded Causeway-owned load-failure event.
Property and action interaction hosts rerender through the same editor registry; because the family is then ineligible, the corresponding native editor is selected with the pending value preserved.
Failure diagnostics identify the family and policy outcome but omit protected or submitted values.

Alternative considered: disable all Vaadin controls after one field failure.
This was rejected because the closures and rollback boundaries are intentionally independent.

### Configure qualification without changing defaults

HTMX configuration will accept an explicit comma-separated field-family allow-list and render it as a bounded document dataset value.
The empty default enables no field family.
Samples and qualification applications explicitly enable selected families; explicit native runs clear the list.
The later default-policy change may alter selection defaults after all qualification gates pass, without changing adapter semantics.

### Extend exact-hash CSP from generated policy metadata

The generated field policy records every observed candidate-originated style element hash required by the adopted controls.
HTMX CSP adds the union of hashes for explicitly enabled reference and field families while retaining `style-src-attr 'none'` and never adding `unsafe-inline`.
Build verification compares generated metadata, Java policy constants, bundled resources, and browser observations so drift fails closed.

### Keep resources visibly unsupported

The existing codec intentionally classifies Blob, Clob, and local resource path inputs as non-reversible.
This change does not add a successful-looking upload control without a public authoritative GraphQL input contract.
Reference Application inventory and browser evidence continue to expose that limitation for a future protocol change.

## Risks / Trade-offs

- [Risk] Vaadin controls add new style elements or mutate styles after an interaction not covered by the initial matrix. → Exercise connection, editing, validation, overlays, themes, reconnect, and failure states under reporting and enforcing CSP; pin every accepted hash.
- [Risk] Picker normalization drops fractional seconds or transforms local values through a browser time zone. → Compare exact codec input and GraphQL variables and fall back to native whenever round-trip equality is not demonstrated.
- [Risk] Internal events cause duplicate validation or submission. → Assert one semantic pending-value transition and one GraphQL interaction per user event across property and action hosts.
- [Risk] Async upgrade races with HTMX replacement or prompt closure. → Guard imports and definition waits by connection generation and discard disconnected or superseded upgrades.
- [Risk] Separate closures duplicate transitive bytes. → Set independent and aggregate budgets, retain route-zero-request assertions, and defer optimization until measured evidence exists.
- [Risk] Error rerender loses focus or pending values. → Preserve Causeway-owned pending state, rerender through the registry, and restore focus to the native replacement.
- [Risk] New npm transitive dependencies or licenses drift. → Pin lock files, disable lifecycle scripts and telemetry, package legal metadata, run vulnerability checks, and require reviewed checksums.

## Migration Plan

1. Add the pinned build closure and generated policy metadata without enabling it by default.
2. Add the internal field adapter and unit tests against fake family modules.
3. Add HTMX configuration and package the verified assets.
4. Enable families only in qualification samples and run candidate plus explicit native suites.
5. Retain empty-family configuration as immediate rollback; disabling one family requires no GraphQL, route, markup, or data migration.
6. Leave default-policy selection to `make-vaadin-default-for-webcomponent-viewer` after archive.

## Open Questions

No architectural questions block implementation.
Exact per-closure gzip ceilings and CSP hashes will be fixed from reproducible generated evidence before qualification is marked complete.
