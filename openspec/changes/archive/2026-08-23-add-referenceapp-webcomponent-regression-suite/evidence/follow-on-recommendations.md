# Prioritized follow-on recommendations

This document separates discoveries made by the regression suite from remediation.
No recommendation below expands the implementation scope of this change.

## Priority 0: value and interaction correctness

Promote `harden-webcomponent-input-value-semantics` first.

That change should address the GraphQL scalar declaration and runtime coercion mismatch demonstrated by BigDecimal property mutation.
It should define canonical parsing, serialization, validation, nullability, precision, temporal, resource, and custom-value behavior before additional widget families become default.
It should also add focused regression assertions for valid, invalid, cancelled, stale, and restored mutations.

The same correctness tranche should fix generic action dispatch for nested safe invocation and flat mutation operation shapes.
Parameterless service actions and valid parameterized object actions must not end in `Action invocation failed` when the public operation is executable.

## Priority 1: GraphQL identity and preparation contracts

Define whether versionless view models and view-model rows may participate in action preparation, property updates, and collection projections without `_meta.version`.
The generic viewer should request version only when the effective metadata type advertises it.
Alternatively, rich GraphQL should provide an explicit nullable concurrency token with documented semantics for versionless identities.

Correct polymorphic projections so `_meta` is selected through valid union fragments rather than directly from `rich__demo_ValueHolder__gqlv_union`.
Add schema tests for abstract collection rows, polymorphic rows, partial row failures, and versionless view-model rows.

Correct canonical route handling for long opaque composite view-model identifiers.
Route parsing, encoding, decoding, and length handling must preserve the identity returned by GraphQL without accepting malformed routes.

## Priority 1: paged reference autocomplete

Promote `add-paged-graphql-reference-autocomplete` only after the identity and preparation contracts above are stable.

The public operation should define offset or cursor semantics, stable ordering, bounded page size, total or continuation metadata, stale-request behavior, and cancellation.
The current 50-result bounded lookup must remain honestly described until that contract exists.

## Priority 2: Vaadin semantic adapter expansion

Promote `expand-vaadin-semantic-editor-families` after value correctness and GraphQL preparation work.

Adapters should remain behind public `<causeway-*>` elements and semantic events.
They should consume only public GraphQL contracts and must not introduce Flow, Binder, Java `DataProvider`, server-side Vaadin routing, Pro components, telemetry, CDN assets, or application-facing `<vaadin-*>` APIs.
Native editors must remain the unsupported-shape and load-failure fallback.

## Priority 2: default policy flip

Promote `make-vaadin-default-for-webcomponent-viewer` last.

The final change should be a small default-selection and rollback-policy change rather than a combined correctness migration.
Route-lazy delivery must remain intact so unaffected routes issue zero Vaadin requests.
The accepted exact CSP hashes must remain byte-exact and `style-src-attr 'none'` must be retained.

## Deferred scalability work

Do not add an interactive Vaadin Grid merely to mask current collection limitations.
Public GraphQL sorting, filtering, paging, polymorphic-row, partial-error, associated-action, and stale-window contracts should be specified first.
The Reference Application collection targets should then become acceptance cases for that separately reviewed work.

## Viewer-specific behavior

Keep Wicket custom panels, Wicket UI extensions, Wicket routes, and viewer-specific presentation outside the generic viewer contract.
Cross-viewer tests should continue comparing semantic outcomes and identity without requiring identical DOM, styling, route syntax, or extension behavior.

## Content exceptions

Retain invalid menu action references and malformed framework-extension grids as upstream content exceptions.
They should be corrected through an upstream Reference Application refresh or a separately justified local adaptation, not hidden through viewer configuration.
A future corpus refresh must repeat provenance, checksums, licensing, RAT, inventory, metamodel, GraphQL, and browser review.

## Recommended sequence

1. `harden-webcomponent-input-value-semantics`.
2. Focused GraphQL versionless-identity, action-dispatch, union-projection, and opaque-route changes derived from this report.
3. `add-paged-graphql-reference-autocomplete`.
4. `expand-vaadin-semantic-editor-families`.
5. `make-vaadin-default-for-webcomponent-viewer`.

Each promoted change should reuse this pinned corpus and update classifications only when a focused test demonstrates the improved behavior.
