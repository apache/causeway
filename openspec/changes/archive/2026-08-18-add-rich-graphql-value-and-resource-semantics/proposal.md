## Why

The executable reference-application analysis confirmed that generic GraphQL `String` fallback does not reconstruct declared Java values for `LocalDateTime`, `URL`, Blob, Clob, or a custom `ComplexNumber`.
The same source equivalence class includes `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp`, for which the current viewer has no explicit marshaller.
A wider inventory shows that Causeway also provides value semantics for types such as `java.sql.Time`, `Locale`, `Bookmark`, `ApplicationFeatureId`, `LocalResourcePath`, `Markup`, `Password`, identifiers, images, schema DTOs, and tree values.
Clients need reversible schema shapes and explicit extension or rejection behavior rather than input fields that accept strings and fail only during invocation.
Every framework-provided value semantics also needs a closed GraphQL classification so newly added built-ins cannot silently inherit generic fallback behavior.

The evidence is recorded in `coverage-matrix.yaml` entries `REF-VALUE-02`, `REF-VALUE-03`, and `REF-RESOURCE-01`.
Resource URL and policy safety are provided by the archived `fix-rich-graphql-resource-link-safety` change under entries `REF-RESOURCE-02` and `REF-RESOURCE-03`.
This change builds on its same-origin `ResourcePath`, independent value-content policy, forbidden-field omission, and dereference authorization rather than replacing them.

## What Changes

- Add canonical reversible marshalling for the confirmed missing standard reference-application datatype equivalence class and the remaining scalar-shaped Causeway built-ins.
- Classify every framework-provided value semantics as reversible, structured, protected, output-only, or unsupported, with coverage that fails when a new built-in lacks a classification.
- Replace silent input-capable object-string fallback with explicit opt-in support; custom marshallers are output-only unless they declare reversible input.
- Replace implicit unknown-value `toString()` output with a non-disclosing unsupported representation by default, retaining legacy string output only as an explicit migration policy.
- Make canonical value formats and GraphQL shapes discoverable through the generated scalar, input, output, and existing rich datatype identities.
- Define extension contracts for Causeway and application custom values without inferring constructors from `toString()`.
- Define consistent Blob, Clob, file, and accepted resource input or result semantics above the corrected resource-link policy.
- Preserve strict non-disclosure for passwords, hidden values, and unsupported opaque values.
- Add round-trip, invalid-input, size, media-type, compatibility, and targeted-introspection tests.

## Capabilities

### New Capabilities

- `rich-graphql-value-semantics`: Defines reversible and discoverable rich GraphQL contracts for standard, custom, and resource values.

### Modified Capabilities

None.

## Impact

- Affects GraphQL scalar marshallers, type mapping, property mutation, action invocation and results, resource transfer strategy, unsupported-output configuration, tests, and documentation.
- Depends on completed reference-application analysis, corrected object argument conversion, and corrected resource-link safety.
- Deliberately changes custom marshaller input capability to fail closed and requires reversible marshallers to opt in explicitly.
- Deliberately changes unknown output to fail closed; applications temporarily relying on arbitrary `toString()` output can opt into the documented legacy policy.
- Does not implement browser editors, resource widgets, member metadata, or collection windowing.
