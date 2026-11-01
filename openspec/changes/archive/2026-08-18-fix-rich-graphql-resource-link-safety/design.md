## Context

Rich object metadata and specialized Blob or Clob wrappers publish links to the GraphQL resource controller.
The current formatter prefixes the configured endpoint with two slashes even when the endpoint already begins with `/`, producing `///graphql/...`.
Browsers parse that string as an authority-bearing URL and attempt to contact host `graphql`.

The default resource response type is `FORBIDDEN`.
The reference probe still obtained object-bearing Blob link values from GraphQL and received HTTP 403 only after manually normalizing and dereferencing the path.
The same global response policy controls effective grids and icons, which are structural metadata rather than domain value content.

## Goals / Non-Goals

**Goals:**

- Produce browser-valid same-origin references under root and prefixed deployments.
- Advertise only resource categories enabled by policy.
- Keep structural metadata policy independent from sensitive value-content policy.
- Reapply authentication, authorization, visibility, and type checks during dereference.
- Preserve bounded and redacted errors.

**Non-Goals:**

- Uploading Blob or Clob values.
- Defining reversible Blob or Clob action inputs.
- Making all metadata or content resources public by default.
- Embedding complete grid or menu XML in every object query.
- Parsing layout resources in GraphQL.

## Decisions

### Publish application-path references

GraphQL publishes an origin-relative path beginning with exactly one slash, or another documented relative-reference form that resolves against the current application origin.
It does not publish a protocol-relative authority.

URL construction uses `spring.graphql.http.path`, with temporary fallback to deprecated `spring.graphql.path`, together with the servlet context and configured external reverse-proxy prefix as path components rather than string concatenation.
Tests cover root deployment, servlet context, reverse-proxy prefix, encoded object identity, and non-default GraphQL endpoint paths.

### Separate metadata and value-content policy

Configuration distinguishes structural metadata resources from domain value-content resources.
Structural metadata includes effective grid, icon, and accepted application layout resources.
Value content includes Blob bytes and Clob characters.

Each category defaults to the least-privilege behavior compatible with its contract.
Enabling structural metadata does not enable Blob or Clob download.
Enabling value content does not bypass object or member authorization.

### Do not advertise forbidden references

When a category is statically forbidden, its optional resource field is absent from the generated schema.
Blob and Clob descriptive name and media-type fields remain available while their `bytes` or `chars` link field is omitted.
It never publishes a dereferenceable or object-bearing URL that is guaranteed to fail by policy.

Schema compatibility tests pin this omission behavior.
The behavior and policy are discoverable without attempting a download.

### Authorize every dereference

A resource request resolves the target through the public logical type and encoded identifier, authenticates the current request, rechecks object and member visibility, and verifies the requested resource category.
Errors distinguish neither hidden from absent objects nor hidden from absent members where doing so would reveal policy.

No resource token, URL, log, diagnostic record, or error contains resource content.
Identifiers in diagnostics remain subject to the diagnostics redaction policy.

### Apply explicit HTTP semantics

Successful resources return their declared media type and a bounded content length where available.
Cache controls account for authorization and mutable object state.
Malformed, stale, unauthorized, and forbidden requests return bounded statuses without stack traces or redirecting to another host.

## Risks / Trade-offs

- [Correcting URLs can affect clients that manually normalized them] → Document the defect correction and retain endpoint path structure while removing only malformed authority syntax.
- [Policy separation adds configuration] → Provide explicit defaults and migration aliases with startup diagnostics.
- [Omitting forbidden fields changes schema snapshots] → Evaluate nullable-field compatibility and choose the least disruptive shape that does not disclose unusable references.
- [Layout resources may contain hidden action identities] → Keep effective-resource authorization and require consuming components to honor authoritative member hidden state.

## Migration Plan

Introduce separate metadata and value-content settings while reading the existing global resource response type as a temporary compatibility fallback.
Both categories therefore retain the legacy default of `FORBIDDEN` until explicitly enabled.
Correct generated paths for all enabled categories.
Deprecate the global setting after applications can select each category explicitly.
Document that clients must treat returned references as opaque same-origin references rather than rewriting slash prefixes.

## Resolved Questions

- Forbidden optional link fields are omitted at schema construction while Blob and Clob descriptive fields remain available.
- Structural metadata remains forbidden through the legacy default unless its category-specific setting explicitly enables it.
