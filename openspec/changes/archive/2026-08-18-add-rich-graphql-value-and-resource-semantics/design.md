## Context

The default marshaller set explicitly handles primitives, wrappers, strings, big numbers, selected `java.time` values, UUID, enums, and void.
Executable reference probes showed that `LocalDateTime`, `URL`, Blob, Clob, and a custom `ComplexNumber` are represented by GraphQL `String` but remain Java `String` during invocation.
The domain member then rejects them as incompatible with its declared type.

The confirmed missing standard equivalence class also contains `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp`.
The full Causeway value-semantics inventory additionally includes scalar-shaped values, structured values, resources, sensitive values, XML DTOs, images, and tree values that need an explicit GraphQL classification even when they remain unsupported.
Blob and Clob have specialized property output wrappers, but updates, action parameters, action results, and transfer constraints do not yet share one reversible policy.
The archived resource-link safety change corrects malformed URLs, forbidden link exposure, metadata-versus-content policy, and authorization rechecks.
This change reuses those established contracts.

The evidence categories map to implementation as follows:

| Matrix entry | Classification | Contract |
|---|---|---|
| `REF-VALUE-02` | Default support | Reversible built-in scalars for `LocalDateTime`, `URL`, `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp` |
| `REF-VALUE-03` | Application extension | An explicit reversible `ScalarMarshaller`, demonstrated with `ComplexNumber`; absent registration remains output-only and input-rejecting |
| `REF-RESOURCE-01` | Default bounded support | Structured Blob and Clob inputs plus metadata-first bounded outputs layered over value-content policy |
| Framework value-semantics inventory | Closed classification | Explicit reversible, structured, protected, output-only, or unsupported treatment for every Causeway-provided value semantics |

The previous fallback exposed all unknown values as GraphQL `String`, allowing safe non-sensitive output through GraphQL Java serialization but leaving inputs as raw Java `String` values.
Existing Blob and Clob property reads exposed name, media type, and policy-gated download links, while property updates and action arguments still used that non-reversible fallback.
The new contract preserves compatible non-sensitive fallback output and existing resource links while replacing fallback input and adding structured resource input.

## Goals / Non-Goals

**Goals:**

- Make every advertised input representation reversible.
- Support the confirmed standard datatype equivalence class with canonical formats.
- Close the framework-provided value-semantics inventory so every built-in is deliberately reversible, structured, protected, output-only, or unsupported.
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
`java.net.URL` uses a documented `Url` scalar and its normalized external form, with redacted coercion errors.
`java.util.Date` uses an ISO-8601 UTC instant, while `java.sql.Date` uses an ISO date-only value.
Temporal precision, date-only semantics, offsets, zones, URL normalization, and SQL-versus-util distinctions are explicit.
Existing scalar names are preserved where they already express the required semantics safely.

### Keep custom values explicit and fail closed

Applications register a `ScalarMarshaller` and application-defined GraphQL scalar for scalar-shaped custom values.
A marshaller is output-only by default and must explicitly opt into input after establishing reversible coercion for its declared Java type.
This behavioural compatibility break is binary-safe for existing implementations: previously compiled marshallers remain usable for output but no longer imply input capability.
Applications needing structured custom input or output continue to replace the `TypeMapper` SPI explicitly rather than invoking constructor discovery.
The fallback marshaller maps inputs to a documented unsupported scalar that always rejects coercion before domain invocation.
Unknown output also uses a non-disclosing unsupported representation and never invokes arbitrary `toString()` by default.
An explicit `LEGACY_STRING` policy temporarily restores legacy output while applications register safe output-only or reversible marshallers.
Opaque values therefore remain unsupported rather than falling back to constructor discovery or implicit textual disclosure.

### Classify every built-in value semantics

Framework-provided value semantics are maintained as a closed GraphQL inventory.
Scalar-shaped values such as `java.sql.Time`, `Locale`, `Bookmark`, and `ApplicationFeatureId` receive canonical reversible scalars.
`Password` receives protected input coercion and suppressed output.
`Markup` is explicit output-only content.
`LocalResourcePath` receives a structured representation preserving both path and open strategy.
Blob and Clob retain their structured bounded resource contract.
Composite identifiers, schema DTOs, images, and tree values remain explicitly unsupported until dedicated representations are defined.
An integration test compares framework value-semantics providers with this inventory so a newly introduced built-in cannot silently inherit fallback behavior.

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

- [Stricter fallback behavior may break applications] → Inventory affected logical types, default to non-disclosing behavior, provide an explicit temporary legacy-output policy, and document registration paths.
- [A newly introduced Causeway value may bypass deliberate review] → Compare framework value-semantics providers against a closed GraphQL classification in integration tests.
- [Multiple Java types may share GraphQL String] → Preserve existing rich datatype identity and document canonical scalar semantics.
- [Resource transfer can create security and memory risks] → Require the resource-link safety capability and enforce authorization, media, size, and bounded-content policy.
- [Custom value support can become unconstrained serialization] → Require explicit bidirectional registration and never infer from arbitrary constructors or `toString()`.

## Migration Plan

Add standard marshallers ahead of generic fallback.
Replace fallback input coercion immediately with the documented unsupported scalar because the established behavior never reconstructed the declared Java value successfully.
Retain existing safe output fields where they are non-sensitive and label them output-only when no inverse exists.
Existing custom `ScalarMarshaller` beans remain binary compatible but become output-only until they explicitly declare reversible input support.
Unknown output becomes non-disclosing by default; applications that need a migration interval may select `LEGACY_STRING` while registering explicit safe marshallers.

## Resolved Questions

- Missing standard temporal equivalence classes use dedicated documented scalars while existing safe scalar identities remain unchanged.
- Blob and Clob input use generated structured input objects with configurable one-mebibyte limits; bounded action-result content uses generated metadata output objects.
- Scalar-shaped application values use application-defined scalars through `ScalarMarshaller`; structured application values require an explicit `TypeMapper` replacement.
- Input capability is an explicit opt-in and never follows merely from registering a marshaller.
- Every framework-provided value semantics has a closed GraphQL classification, while future structured support can be added without reopening implicit fallback.
- Unknown output is non-disclosing by default, with `LEGACY_STRING` available only as a temporary explicit migration policy.
