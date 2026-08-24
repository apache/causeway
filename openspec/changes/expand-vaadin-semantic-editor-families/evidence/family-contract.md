# Semantic family contract

## Qualified families

| Family | Semantic types or capability | Existing codec | Internal control policy | Gzip ceiling |
|---|---|---|---|---:|
| `basic` | `String`, `ID`, `UUID`, `Locale`, `Char`, `URL`, `Url`, `Password`, `ProtectedValue`, `Boolean`, enum, bounded scalar choices | scalar, URL, protected, Boolean | text field, text area, password field, checkbox, or select | 80 KiB |
| `numeric` | `Int`, `Short`, `Byte`, `Float`, `Double` | machine numeric | integer field or number field | 65 KiB |
| `numeric` | `Long`, `BigInteger`, `BigDecimal` | exact numeric | lexical text field with numeric input hint; never a JavaScript numeric value | included above |
| `local-temporal` | `LocalDate`, `LocalTime`, `LocalDateTime` | temporal | date, time, or date-time picker | 100 KiB |

The accepted aggregate cold ceiling for requesting all field closures is 200 KiB gzip.
The independent ceilings are hard verification limits rather than expected transfer sizes.

## Explicit exclusions

`OffsetTime`, `OffsetDateTime`, `DateTime`, `LegacyDateTime`, and `ZonedDateTime` retain native lexical controls because a local picker cannot preserve their offset or zone contract.
Blob, Clob, and local resource path inputs remain explicitly unsupported because no reversible authoritative GraphQL upload input exists.
Custom value objects, references, collections, and unsupported codecs remain outside the field adapter; references retain their separate qualified adapter.

## Selection and authority

Eligibility requires both explicit family enablement and a reversible existing Causeway codec.
Causeway owns pending values, parsing, validation, semantic events, focus recovery, action dependencies, GraphQL variables, and errors.
Vaadin controls are internal presentation implementations only.
Protected controls initialize empty and protected values never enter adapter attributes, semantic events, diagnostics, errors, operation summaries, or route evidence.
