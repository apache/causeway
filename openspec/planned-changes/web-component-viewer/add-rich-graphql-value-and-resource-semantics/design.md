## Context

The default marshaller set explicitly handles primitives, wrappers, strings, big numbers, selected `java.time` values, and UUID.
The preliminary audit found reference-app examples such as `LocalDateTime`, `URL`, `java.util.Date`, `java.sql.Date`, and `java.sql.Timestamp` that currently fall through to the last-priority object marshaller.
That marshaller serializes through GraphQL String but returns the GraphQL input object unchanged, so it is not a reliable round-trip contract.

Blob and Clob have specialized property output wrappers, while file acceptance, property updates, action parameters, action results, images, markup, passwords, local resources, trees, composite values, and application values do not share one explicit policy.

## Goals / Non-Goals

**Goals:**

- Make every advertised input representation reversible.
- Publish editor-neutral datatype and resource semantics.
- Support the confirmed standard reference-app datatype set with canonical formats.
- Provide safe extension points for application values.
- Unify supported resource values across properties and actions.
- Fail unsupported values explicitly and safely.

**Non-Goals:**

- Choosing HTML controls.
- Automatically supporting every serializable Java object.
- Exposing passwords or hidden values.
- Requiring multipart GraphQL transport unless analysis proves it necessary and safe.
- Implementing collection windowing or member presentation metadata.

## Decisions

### Reversibility is required for input capability

A type may be advertised as mutation or action input only when GraphQL input coercion can reconstruct the declared domain value.
Output-only diagnostic conversion does not establish input support.
Unsupported types produce schema-build or capability diagnostics rather than receiving arbitrary raw strings.

### Describe semantic categories, not widgets

Datatype descriptors identify categories such as text, boolean, integer, decimal, temporal, enum, object reference, binary resource, character resource, composite, and opaque.
They also identify canonical format, nullability, constraints, GraphQL shape, and extension ownership.
Frontend registries decide how to render or edit the value.

### Use canonical standard formats

Confirmed standard datatypes receive documented locale-independent representations and coercion errors.
Temporal precision and timezone rules are explicit.
Existing scalar names are preserved where possible, with additive descriptor metadata distinguishing logical semantics that share a GraphQL scalar.

### Separate resource metadata from transfer

Clients can request filename, media type, size policy, and transfer mode without retrieving content.
Bounded inline transfer is permitted where safe; larger content uses secured resource references.
A registered strategy must define both directions before resource input is advertised.

### Keep custom values extensible

Applications can register a reversible marshaller plus descriptor.
Composite or structured values may use dedicated input and output shapes when proven by analysis.
Opaque values remain explicitly unsupported rather than falling back to `toString()`.

## Risks / Trade-offs

- [Stricter fallback behavior may break applications] → Inventory affected logical types, provide diagnostics and a compatibility period, and document registration paths.
- [Multiple Java types may share GraphQL String] → Publish logical type and canonical format in datatype descriptors.
- [Resource transfer can create security and memory risks] → Enforce authorization, media, size, and bounded-content policy before transfer.
- [Custom value support can become unconstrained serialization] → Require explicit marshaller registration and never infer from arbitrary constructors or `toString()`.

## Migration Plan

Add standard marshallers ahead of the generic fallback.
Introduce diagnostics for non-reversible input types before making strict rejection the default if compatibility evidence requires a transition.
Existing safe output fields remain available.

## Open Questions

- Which confirmed reference-app types belong in the default marshaller module?
- Should canonical temporal representations use dedicated scalar names or descriptor-qualified strings?
- Which resources can safely use inline base64 or text and at what configured limit?
- Should structured custom values use generated GraphQL input objects or application-defined scalars?
