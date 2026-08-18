## Why

The executable reference-application analysis confirmed that generic GraphQL `String` fallback does not reconstruct declared Java values for `LocalDateTime`, `URL`, Blob, Clob, or a custom `ComplexNumber`.
The same source equivalence class includes `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp`, for which the current viewer has no explicit marshaller.
Clients need reversible schema shapes and explicit extension or rejection behavior rather than input fields that accept strings and fail only during invocation.

The evidence is recorded in `coverage-matrix.yaml` entries `REF-VALUE-02`, `REF-VALUE-03`, and `REF-RESOURCE-01`.
Resource URL and policy safety are provided by the archived `fix-rich-graphql-resource-link-safety` change under entries `REF-RESOURCE-02` and `REF-RESOURCE-03`.
This change builds on its same-origin `ResourcePath`, independent value-content policy, forbidden-field omission, and dereference authorization rather than replacing them.

## What Changes

- Add canonical reversible marshalling for the confirmed missing standard reference-application datatype equivalence class.
- Replace silent input-capable object-string fallback with explicit supported, output-only, or unsupported behavior.
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

- Affects GraphQL scalar marshallers, type mapping, property mutation, action invocation and results, resource transfer strategy, tests, and documentation.
- Depends on completed reference-application analysis, corrected object argument conversion, and corrected resource-link safety.
- May require a compatibility mode for applications relying on arbitrary raw-string fallback input.
- Does not implement browser editors, resource widgets, member metadata, or collection windowing.
