# Accepted input value semantics

## Exact numerics

`Long`, `BigInteger`, and `BigDecimal` are edited and submitted as lexical strings.
The browser never converts those values through JavaScript `Number`.
Long accepts signed whole numbers in the range `-9223372036854775808` through `9223372036854775807`.
BigInteger accepts signed whole numbers without a browser-imposed precision limit.
BigDecimal accepts signed decimal and exponent notation and preserves pending scale and trailing zeros.
Malformed, empty-required, overflowed, and partially parsed values remain local bounded errors and do not invoke GraphQL.
An authoritative server response may apply its documented output normalization after a successful mutation.

## Machine numerics

Byte, Short, and Int use JavaScript numbers only after strict integral grammar and declared range checks.
Float and Double accept only finite JavaScript numbers.
Empty optional input becomes `null`, while empty required input is rejected.

## Boolean

A required Boolean uses a two-state checkbox and submits only `true` or `false`.
A nullable Boolean uses explicit no-value, true, and false choices and submits `null`, `true`, or `false` exactly.
No untouched or unchecked state is inferred implicitly.

## Temporal

LocalDate, LocalTime, and LocalDateTime preserve local semantics without adding a browser timezone.
LocalTime and LocalDateTime preserve accepted fractional seconds.
OffsetTime and DateTime preserve explicit offsets and accepted fractional seconds.
ZonedDateTime accepts ISO zoned input with a named zone and preserves the corresponding instant, offset, and zone.
The server retains the configured zoned-date-time format as a compatibility input fallback.
LegacyDateTime is treated as an explicit offset or instant representation rather than a browser-local date.
Malformed temporal values fail before invocation and retain the pending lexical value for correction.

## URL, protected, resource, and custom values

URL input accepts absolute HTTP and HTTPS URLs in the generic browser codec.
Existing GraphQL URL coercion and safe resource-link output policy remain authoritative.
Password and protected inputs are write-only.
Prior and submitted protected values are absent from generated value attributes, semantic state event values, and displayed error text.
Blob, Clob, and local-resource inputs remain generically unsupported when complete public authorization, media, size, and representation metadata is unavailable.
GraphQL continues to enforce accepted Blob and Clob inline limits, base64 validity, file-accept rules, non-disclosure, and metadata-only output.
A custom value requires an explicitly registered reversible codec in addition to any output renderer.
Unknown and output-only values fail closed and never receive generic string input.

## Compatibility tightening

Exact numeric coercion through JavaScript Number is intentionally removed.
Nullable Boolean no longer collapses `null` to `false`.
Offset and zoned values no longer use controls that discard offset or zone information.
Malformed built-in scalar errors are bounded and do not include submitted values, Java class names, or parser exception details.
Applications relying on lossy implicit conversion must register a deliberate reversible codec or correct their advertised GraphQL strategy.

## Rollback

The public Causeway elements, semantic events, GraphQL operation names, object identities, and routes are unchanged.
The codec registry is the single internal rollback boundary for editor conversion.
Rollback must not restore silent lossy conversion as an unsupported-value fallback.
