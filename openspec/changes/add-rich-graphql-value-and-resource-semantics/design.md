## Context

The default marshaller set explicitly handles primitives, wrappers, strings, big numbers, selected `java.time` values, UUID, enums, and void.
Executable reference probes showed that `LocalDateTime`, `URL`, Blob, Clob, and a custom `ComplexNumber` are represented by GraphQL `String` but remain Java `String` during invocation.
The domain member then rejects them as incompatible with its declared type.

The confirmed missing standard equivalence class also contains `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp`.
Blob and Clob have specialized property output wrappers, but updates, action parameters, action results, and transfer constraints do not yet share one reversible policy.
The archived resource-link safety change corrects malformed URLs, forbidden link exposure, metadata-versus-content policy, and authorization rechecks.
This change reuses those established contracts.

## Goals / Non-Goals

**Goals:**

- Make every advertised input representation reversible.
- Support the confirmed standard datatype equivalence class with canonical formats.
- Keep value semantics discoverable through standard GraphQL type introspection and existing rich datatype identities.
- Provide safe registration points for application values.
- Define bounded resource input and result strategies above the corrected link policy.
- Fail unsupported values explicitly and safely.

**Non-Goals:**

- Choosing HTML controls.
- Automatically supporting every serializable Java object.
- Adding a duplicate datatype catalogue or member-list API.
- Exposing passwords or hidden values.
- Reimplementing resource URL, dereference authorization, or policy separation from the resource-link safety change.
- Implementing collection windowing or member presentation metadata.

## Decisions

### Reversibility is required for input capability

A type is advertised as property or action input only when GraphQL coercion can reconstruct the declared domain value.
Output-only diagnostic conversion does not establish input support.
Unsupported types produce bounded schema or capability diagnostics rather than receiving arbitrary raw strings.

Existing raw-string fallback may remain temporarily for explicitly configured output-only compatibility when serialization is non-sensitive.
It never establishes mutation or action input capability.

### Use standard GraphQL type discovery

Canonical formats are represented by dedicated scalars, documented scalar descriptions, or explicit generated input and output object shapes.
The existing rich `datatype` identity distinguishes logical semantics that legitimately share a GraphQL scalar.

Clients use targeted `__type` introspection to discover those shapes.
The change does not add a central datatype catalogue that duplicates GraphQL's type system.

### Use canonical standard formats

The confirmed missing standard datatypes receive documented locale-independent representations and typed coercion errors.
`LocalDateTime` and `java.sql.Timestamp` use ISO local date-time text and preserve fractional precision without inventing an offset.
`java.net.URL` uses the GraphQL Java `Url` scalar and its normalized external form.
`java.util.Date` uses an ISO-8601 UTC instant, while `java.sql.Date` uses an ISO date-only value.
Temporal precision, date-only semantics, offsets, zones, URL normalization, and SQL-versus-util distinctions are explicit.
Existing scalar names are preserved where they already express the required semantics safely.

### Keep custom values explicit

Applications register a reversible `ScalarMarshaller` and application-defined GraphQL scalar for scalar-shaped custom values.
Applications needing structured custom input or output continue to replace the `TypeMapper` SPI explicitly rather than invoking constructor discovery.
The fallback marshaller remains output-only for compatibility and maps inputs to a documented unsupported scalar that always rejects coercion before domain invocation.
Opaque values therefore remain unsupported for input rather than falling back to `toString()` or arbitrary constructor discovery.

### Layer resource transfer above safe references

Resource metadata exposes filename, media type, bounded size information, transfer mode, and member `fileAccept` constraints without retrieving content.
Blob input uses a generated `BlobInput` object containing `name`, `mimeType`, and base64 content.
Clob input uses a generated `ClobInput` object containing `name`, `mimeType`, and character content.
Both reconstruct Causeway values only when value-content policy is enabled and the decoded content satisfies the configured inline-input byte limit and member acceptance constraint.

Action results use generated metadata objects with bounded inline content only when value-content policy is enabled and content is within the configured inline-output byte limit.
Larger property content continues to use the corrected secured resource-reference contract.
Inline limits default to one mebibyte and are independently configurable for input and output.
Password and hidden values never enter the generic resource path.

## Risks / Trade-offs

- [Stricter fallback behavior may break applications] → Inventory affected logical types, provide diagnostics and a compatibility period, and document registration paths.
- [Multiple Java types may share GraphQL String] → Preserve existing rich datatype identity and document canonical scalar semantics.
- [Resource transfer can create security and memory risks] → Require the resource-link safety capability and enforce authorization, media, size, and bounded-content policy.
- [Custom value support can become unconstrained serialization] → Require explicit bidirectional registration and never infer from arbitrary constructors or `toString()`.

## Migration Plan

Add standard marshallers ahead of generic fallback.
Replace fallback input coercion immediately with the documented unsupported scalar because the established behavior never reconstructed the declared Java value successfully.
Retain existing safe output fields where they are non-sensitive and label them output-only when no inverse exists.
Existing custom `ScalarMarshaller` beans remain compatible and become the explicit registration path for reversible scalar-shaped values.

## Resolved Questions

- Missing standard temporal equivalence classes use dedicated documented scalars while existing safe scalar identities remain unchanged.
- Blob and Clob input use generated structured input objects with configurable one-mebibyte limits; bounded action-result content uses generated metadata output objects.
- Scalar-shaped application values use application-defined scalars through `ScalarMarshaller`; structured application values require an explicit `TypeMapper` replacement.
