## Context

The foundation editor registry currently combines widget selection, HTML rendering, and conversion to GraphQL variable values in each editor.
The standard number editor converts `Long`, `BigInteger`, and `BigDecimal` through JavaScript `Number`, so valid values outside the safe integer range or with significant decimal scale can change before validation or mutation.
The checkbox editor collapses nullable `Boolean` values to `false`, and the temporal editor accepts only `LocalDate`, `LocalDateTime`, and `LocalTime` without explicit offset, zone, or fractional-second policy.
Rich GraphQL advertises exact numerics as strings and has scalar marshallers, but the Reference Application demonstrated a property mutation where the advertised string reached domain invocation without the required `BigDecimal` conversion.
The change crosses the framework-neutral component library, rich GraphQL value conversion, interaction reconciliation, and the pinned Reference Application regression baseline.
Public Causeway elements, semantic events, GraphQL operations, object identities, routes, and application-owned presentation remain stable.

## Goals / Non-Goals

**Goals:**

- Introduce one toolkit-neutral codec boundary for conversion between authoritative GraphQL values, control values, validation values, and submitted variables.
- Preserve exact integer and decimal lexical forms without JavaScript binary floating-point conversion.
- Preserve nullable boolean state as distinct from `false`.
- Support the complete accepted temporal input catalogue with explicit local, offset, zone, and precision rules.
- Enforce URL, protected-value, Blob, Clob, local-resource, and custom-value capabilities from advertised metadata and fail closed when reversible input is unavailable.
- Make rich GraphQL schema declarations and runtime unmarshalling agree for every accepted built-in input.
- Qualify successful and rejected behavior against foundation tests, GraphQL integration tests, and the pinned Reference Application.

**Non-Goals:**

- Selecting Vaadin as the default internal widget toolkit.
- Adding Vaadin field families or exposing raw toolkit elements as public APIs.
- Adding paged reference autocomplete or interactive grid sorting and filtering.
- Broadening password output, protected-value disclosure, resource size limits, or media authorization.
- Treating display-only markup or unknown custom values as generic string inputs.

## Decisions

### Separate value codecs from editor widgets

A value codec registry will own type recognition, authoritative-value normalization, control-value conversion, control parsing, and GraphQL-variable construction.
Editors will remain responsible for control selection, accessible markup, and control-specific state only.
Property and action code will submit codec output rather than applying editor-specific ad hoc coercion.
This allows native controls and later Vaadin adapters to share identical value semantics.

An alternative was to patch each existing editor's `parse` function independently.
That would leave conversion rules duplicated across property and action flows and would make later toolkit adapters repeat the same correctness work.

### Preserve exact numerics as validated lexical strings

`BigDecimal`, `BigInteger`, and exact `Long` inputs will use canonical lexical strings end to end.
The browser will validate their grammar and declared constraints without converting them through `Number`.
`Int`, `Short`, `Byte`, `Float`, and `Double` may use JavaScript numbers only where the advertised GraphQL scalar and range semantics are representable and explicitly tested.
Decimal scale and trailing zeros remain preserved until the authoritative server response chooses a normalized display.

An alternative was to use a third-party arbitrary-precision JavaScript library.
No arithmetic is required for semantic editing, so grammar validation and lexical preservation are smaller, deterministic, and avoid a new runtime dependency.

### Represent nullable boolean with an explicit three-state control contract

A required Boolean may continue to use a two-state checkbox.
A nullable Boolean will use a semantic three-state editor that distinguishes `null`, `true`, and `false` and submits those exact values.
The public property or action component remains unchanged regardless of the internal native control.

An alternative was to treat an unchecked checkbox as `null` until touched.
That creates hidden interaction state and makes cancel, defaults, refresh, and automation ambiguous.

### Keep temporal representations type-specific

Local date and time values will use native date, time, and datetime-local controls where those controls preserve the advertised lexical contract.
Offset and zoned values will use textual controls with codec validation because native datetime-local controls discard offsets and zone identifiers.
Fractional seconds will be preserved to the precision supplied by GraphQL rather than truncated to whole seconds.
The codec will not silently convert between browser timezone, UTC, an offset, and a named zone.

An alternative was to normalize all temporal input to UTC.
That would change domain meaning for local and named-zone types and would violate reversible input semantics.

### Derive sensitive, resource, and custom input eligibility from capabilities

Password or protected values remain undisclosed and may be submitted only through an explicitly advertised write-only strategy.
Blob, Clob, URL, and local-resource inputs require the public datatype metadata needed to enforce media, size, path, and open-strategy constraints before invocation.
Markup and presentation-only values remain output-only.
Custom values require an explicitly registered reversible strategy; an output formatter alone never enables input.
Unsupported cases render the existing bounded Causeway-owned unsupported state and do not pass a raw string to GraphQL.

### Enforce schema and runtime marshaller agreement

Rich GraphQL integration tests will invoke representative property and action inputs through the scalar type actually advertised by introspection.
The runtime must deliver the declared Java value type to domain invocation or reject malformed input before invocation with a bounded validation error.
Fixes will remain in shared marshaller or mutation conversion code rather than adding Reference Application special cases.

### Reconcile only authoritative values

Pending lexical input remains visible after local or GraphQL validation failure.
A successful mutation or action result replaces pending state only with authoritative server data.
Cancellation, superseded validation, route replacement, and disconnect abort pending work without changing the last authoritative value.

## Risks / Trade-offs

- [Risk] Native number controls can normalize or reject exact lexical forms before JavaScript reads them. → Use text controls with numeric input hints for exact types and test precision, sign, scale, exponent, and boundary cases.
- [Risk] Existing applications may rely on lossy implicit conversion. → Treat changed rejection as an intentional correctness tightening and document exact accepted lexical forms.
- [Risk] Browser temporal controls differ in fractional-second behavior. → Keep offset and zoned values textual and cover local controls in Chromium with codec-level cross-platform tests.
- [Risk] Resource input can allocate excessive memory or expose paths. → Require existing public size, media, authorization, and open-strategy metadata and fail closed when any required capability is absent.
- [Risk] Fixing marshaller conversion can affect simple and rich schemas. → Add focused model and end-to-end tests for both advertised scalar shape and invocation value type.
- [Risk] Reference Application classifications will change as defects become supported. → Regenerate only after focused journeys pass and review every count and identifier change.

## Migration Plan

1. Add codec tests that reproduce exact numeric, nullable boolean, temporal, protected, resource, and unsupported custom-value failures.
2. Introduce the codec registry behind the existing editor registry without changing public element or event APIs.
3. Move standard editors and property and action submission paths to codec-owned conversion.
4. Add and fix rich GraphQL marshaller and mutation integration tests for advertised built-in input types.
5. Expand the Reference Application target catalogue and browser journeys and review inventory classification changes.
6. Run foundation, GraphQL, HTMX, Petclinic, Reference Application, CSP, accessibility, and production-isolation gates.

Rollback restores the prior codec selection through one foundation implementation change while leaving public Causeway components and GraphQL operations unchanged.
Lossy behavior is not retained as a silent fallback.

## Open Questions

- Whether GraphQL `Long` remains a string scalar everywhere or uses an extended scalar in any schema style must be confirmed from effective introspection before implementation.
- The exact public metadata available for Blob, Clob, and local-resource input constraints must be inventoried before enabling any new resource editor.
- Zoned-date-time parsing must confirm whether the configured server format is ISO-compatible or requires a separately advertised format descriptor.
