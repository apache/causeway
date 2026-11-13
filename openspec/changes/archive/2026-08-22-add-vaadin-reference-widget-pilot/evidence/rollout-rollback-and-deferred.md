# Rollout, rollback, compatibility, and deferred work

## Rollout status

The implementation remains an opt-in pilot.
It is enabled explicitly in Petclinic and explicitly through browser configuration in the vanilla HTML sample.
The generic HTMX viewer default is disabled, and this change does not make Vaadin the production-wide default.

Applications enable HTMX delivery with:

[source,properties]
----
causeway.viewer.webcomponents.htmx.vaadin-reference-widgets=true
causeway.viewer.webcomponents.htmx.reference-minimum-search-length=2
causeway.viewer.webcomponents.htmx.reference-maximum-results=50
----

A plain HTML application calls `configureCausewayReferenceWidgets()` before importing the complete foundation index.

## Stable and internal compatibility boundaries

Stable Causeway contracts remain:

- Public `<causeway-*>` composition.
- Property and action prompt semantic state events.
- Public GraphQL operations and object identities.
- Canonical routes and object-link navigation.
- Disposable route object contexts.
- Documented `--causeway-*` variables.

Internal and version-coupled details remain:

- `<vaadin-combo-box>` and `<vaadin-multi-select-combo-box>`.
- Vaadin events, properties, overlays, shadow parts, and theme variables.
- npm package names and transitive closure.
- Bundle chunks, checksum, and four CSP style hashes.
- Candidate callback and filtering behavior.

Applications remain free to bundle their own browser widgets on custom pages under their own dependency and CSP policy.
The viewer does not offer a supported raw Vaadin tier in this pilot.

## One-step rollback

Set the HTMX property to `false` or remove the plain HTML configuration call.
The semantic registry immediately returns to existing native choice and autocomplete editors.
The shell returns the original CSP without style hashes, and no Vaadin asset is requested.

The retained rollback journey verifies:

- The opt-in document attribute is absent.
- All candidate style hashes are absent.
- The native reference editor is restored.
- No `<causeway-reference-editor>` is rendered.
- No candidate asset is requested.
- No browser console error occurs.

Rollback requires no GraphQL operation, schema, route, bookmark, persisted object, pending-value, semantic event, custom page, or application data migration.
Removing generated assets is not required for operational rollback because disabled applications never request them.

## Promotion gates

The pilot must remain optional unless all of these continue to pass:

- Four reviewed exact style hashes and zero unexpected CSP violations.
- No blanket inline permission.
- No Flow runtime, commercial package, usage-statistics collector, external import, or external runtime request.
- Free-core package and license policy.
- Zero critical or serious automated accessibility violation.
- Keyboard single, multi, search, token removal, clear, required validation, disabled, Escape, and focus behavior.
- HTMX disconnect, stale request, overlay, and focus cleanup.
- Cold bundle no greater than 65 KiB gzip.
- Existing foundation, HTMX, vanilla sample, Petclinic integration, and Petclinic Playwright suites.
- Immediate native-editor fallback and rollback.

A dependency update is not routine patching of only `package-lock.json`.
It must regenerate and review the package closure, licenses, vulnerability audit, bundle checksum, style hashes, CSP matrix, real samples, accessibility evidence, and performance budget together.

## Deferred work

### True paged GraphQL autocomplete

The current adapter consumes one bounded search response and does not claim server paging.
A later GraphQL proposal must define requested offset or cursor, size, total count or continuation, stable identity, cancellation, and result-generation semantics before unbounded remote paging is enabled.

### Grid

Read-only GraphQL collection windows remain a viable later candidate.
Interactive Grid sorting and filtering remain deferred until public collection operations accept user-requested sort and filter semantics with stable ordering and row keys.
Grid Pro and other commercial behavior remain excluded.

### Raw widget profile

No raw `<vaadin-*>` contract is supported.
Any later profile must specify an allowlist, loading contract, version horizon, theme and CSP responsibility, accessibility responsibility, upgrade policy, and relationship to application-owned widgets.

### Broad field adoption

Date, time, date-time, upload, dialog, and other evaluated controls remain outside this pilot.
Each family requires its own value conversion, payload, accessibility, lifecycle, CSP, and rollback evidence before adoption.

### Server-side Vaadin viewer

A future server-side Vaadin viewer remains independent and may use Flow and Java APIs according to its own architecture.
Only visual vocabulary, widget familiarity, and mapping policy may be shared with this browser-side pilot.
