# Effective input capability inventory

## Discovery boundary

The foundation currently receives the GraphQL argument `inputType` plus rich interaction state.
Rich property and action-parameter state already selects a public `datatype` string, but the editor context previously discarded it.
That datatype is the generated rich type name for the declared domain element type and is required to distinguish domain types that intentionally share one GraphQL scalar.
This change carries the existing public datatype value into the toolkit-neutral codec context as `semanticType`.
No new GraphQL operation or field is introduced.

## Built-in scalar and semantic mappings

| Domain type | Advertised GraphQL scalar | Semantic datatype needed by editor | Nullability source | Current input notes |
| --- | --- | --- | --- | --- |
| `BigDecimal` | `String` | generated datatype ending `BigDecimal` | GraphQL `NON_NULL` wrapper | String avoids precision loss; runtime marshaller must reconstruct `BigDecimal`. |
| `BigInteger` | `String` | generated datatype ending `BigInteger` | GraphQL `NON_NULL` wrapper | String avoids precision loss; runtime marshaller must reconstruct `BigInteger`. |
| primitive and wrapper `long` | extended `Long` scalar | generated datatype ending `Long` | primitive or GraphQL `NON_NULL` versus nullable wrapper | Values can exceed JavaScript safe integer range and remain lexical in the browser. |
| primitive and wrapper Boolean | `Boolean` | generated datatype ending `Boolean` | primitive or GraphQL `NON_NULL` versus nullable wrapper | Nullable wrapper requires three distinct states. |
| `LocalDate` | `String` | generated datatype ending `LocalDate` | GraphQL wrapper | ISO local date marshaller. |
| `LocalTime` | `String` | generated datatype ending `LocalTime` | GraphQL wrapper | Local time and fractional precision must remain lexical. |
| `LocalDateTime` | custom local-date-time scalar | generated datatype ending `LocalDateTime` | GraphQL wrapper | Native datetime-local is usable only without timezone conversion. |
| `OffsetDateTime` | extended `DateTime` scalar | generated datatype ending `OffsetDateTime` | GraphQL wrapper | Explicit offset is semantically significant. |
| `OffsetTime` | extended `Time` scalar | generated datatype ending `OffsetTime` | GraphQL wrapper | Explicit offset is semantically significant. |
| `ZonedDateTime` | `String` | generated datatype ending `ZonedDateTime` | GraphQL wrapper | Server format is configurable and must remain explicit. |
| `URL` | custom URL scalar | generated datatype ending `URL` | GraphQL wrapper | Existing safe resource-link output policy remains authoritative. |
| `Password` | custom password scalar | generated datatype ending `Password` | GraphQL wrapper and member capability | Input is protected and output remains undisclosed. |
| `Markup` | custom markup scalar | generated datatype ending `Markup` | GraphQL wrapper | Presentation value remains output-only. |

## Resource inputs

Blob and Clob output use dedicated rich resource projections and same-origin resource links.
Local resource paths include representation and open-strategy semantics.
The current generic editor context does not carry a complete authorized media-and-size input descriptor for these resource families.
They therefore remain fail-closed for generic input until the required public metadata is present and tested.

## Custom values

Application marshallers can advertise output behavior without reversible input.
A custom foundation codec must register explicitly against the existing semantic datatype and provide parsing and GraphQL-variable conversion.
Unknown custom scalars and generated semantic datatypes select the unsupported codec and never fall back to an arbitrary string submission.

## Consequences

GraphQL `inputType` alone is insufficient for exact numeric, local temporal, zoned temporal, protected, resource, and custom values because several domain types share `String`.
Codec selection therefore uses `semanticType` when rich datatype state is available and falls back to the GraphQL named type for standard scalars and enums.
Property and action contexts retain required state from the GraphQL `NON_NULL` wrapper.
Choices and references continue to preserve their existing identity representation before codec submission.
