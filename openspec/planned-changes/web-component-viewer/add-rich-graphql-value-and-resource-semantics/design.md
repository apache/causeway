## Context

The default marshaller set explicitly handles primitives, wrappers, strings, big numbers, selected `java.time` values, UUID, enums, and void.
Executable reference probes showed that `LocalDateTime`, `URL`, Blob, Clob, and a custom `ComplexNumber` are represented by GraphQL `String` but remain Java `String` during invocation.
The domain member then rejects them as incompatible with its declared type.

The confirmed missing standard equivalence class also contains `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp`.
Blob and Clob have specialized property output wrappers, but updates, action parameters, action results, and transfer constraints do not yet share one reversible policy.
The resource-link safety proposal separately corrects malformed URLs, forbidden link exposure, and metadata-versus-content policy.

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
Temporal precision, date-only semantics, offsets, zones, URL normalization, and SQL-versus-util distinctions are explicit.
Existing scalar names are preserved where they already express the required semantics safely.

### Keep custom values explicit

Applications register a reversible marshaller and its GraphQL scalar or structured input and output mapping.
Composite values may use dedicated generated objects or an application-defined scalar when both directions are deterministic.
Opaque values remain unsupported for input rather than falling back to `toString()` or arbitrary constructor discovery.

### Layer resource transfer above safe references

Resource metadata can expose filename, media type, bounded size information, transfer mode, and member acceptance constraints without retrieving content.
A registered input strategy defines how authorized bounded content reconstructs Blob, Clob, or another declared resource type.

Large content uses the corrected secured resource-reference contract.
Inline input or output is permitted only under configured media and size limits.
Password and hidden values never enter the generic resource path.

## Risks / Trade-offs

- [Stricter fallback behavior may break applications] → Inventory affected logical types, provide diagnostics and a compatibility period, and document registration paths.
- [Multiple Java types may share GraphQL String] → Preserve existing rich datatype identity and document canonical scalar semantics.
- [Resource transfer can create security and memory risks] → Require the resource-link safety capability and enforce authorization, media, size, and bounded-content policy.
- [Custom value support can become unconstrained serialization] → Require explicit bidirectional registration and never infer from arbitrary constructors or `toString()`.

## Migration Plan

Add standard marshallers ahead of generic fallback.
Introduce diagnostics for non-reversible input types before strict input rejection becomes the default if compatibility evidence requires a transition.
Retain existing safe output fields where they are non-sensitive and label them output-only when no inverse exists.

## Open Questions

- Whether each temporal equivalence class should use a dedicated scalar or a documented existing scalar with distinct rich datatype identity.
- Which resource inputs can safely use bounded inline base64 or text and at what configured limit.
- Whether structured application values should use application-defined scalars or generated input and output objects by default.
