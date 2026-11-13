# Production isolation verification

No Maven POM, npm package manifest, lock file, or dependency version changes in this change.
The GraphQL changes retain every existing query, mutation, field, argument, and route name.
The `DateTime` and `Time` scalar names remain unchanged while their coercion now preserves accepted fractional precision.
ZonedDateTime remains represented by the existing GraphQL string scalar and gains ISO named-zone input before the configured compatibility fallback.

The public `<causeway-*>` element vocabulary and semantic event names remain unchanged.
Protected property and action event payloads retain their existing shape but publish `null` instead of a secret value.
No HTMX controller, page renderer, canonical route grammar, application default, or browser asset URL changes.

No Vaadin package, generated bundle, checksum, CSP hash, qualification policy, or default-selection property changes.
The optional reference adapter still loads lazily only for eligible reference inputs.
Native and Vaadin reference editors now share the toolkit-neutral value codec boundary.

The production foundation, HTMX, GraphQL model, Petclinic, and Reference Application modules package and test from their existing reactor positions.
The archived Vaadin pilot server received only a path-discovery repair so its qualification harness continues to locate the project after archival.
That harness completed five production-like pilot scenarios with zero axe, CSP, console, page, external-request, or overflow failures.
