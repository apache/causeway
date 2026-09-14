# Vaadin reference-widget pilot implementation

## Scope delivered

The foundation now packages one selective Vaadin 25.2.8 free-core bundle containing Combo Box, Multi-Select Combo Box, and their required closure.
The default semantic editor registry selects the internal adapter only when the feature is explicitly enabled and the introspected input has stable object-reference choices or advertised autocomplete.

The public application contract remains Causeway-owned:

- `<causeway-property>`, `<causeway-action>`, and `<causeway-interaction-controller>` remain the ordinary composition elements.
- `<causeway-reference-editor>` is a semantic adapter and emits existing property or prompt state behavior rather than Vaadin value events to application code.
- GraphQL contexts remain authoritative for choices, autocomplete, pending values, validation, mutation, interaction outcomes, cancellation, and domain identity.
- Canonical object routes and object-link navigation are unchanged.
- Vaadin tags, data-provider callbacks, package APIs, shadow parts, and bundle chunks remain internal.

Flow, Grid, broad field replacement, raw application-facing Vaadin APIs, private GraphQL endpoints, and server-side Vaadin behavior are absent.

## Editor qualification

The feature is disabled by default.
When enabled, the registry chooses the candidate for object or input-object values when fixed choices contain stable identities within the configured bound or the public schema advertises autocomplete.
List-valued inputs choose Multi-Select Combo Box only when the existing introspected interaction already accepts an authoritative list pending value.

Disabled configuration, unsupported descriptors, over-bound fixed choices, module failure, and rollback retain or recover the existing native editor.
The candidate asset is imported only from the semantic adapter's connection path, so merely importing the foundation index does not request Vaadin.

## State and lifecycle

The adapter maps labels, current values, stable identities, required state, disabled state, descriptions, errors, loading, fixed choices, and current suggestions into the internal control.
Single and multi-selection dispatch one bubbling semantic input change from the Causeway adapter, after which property or action controllers parse, validate, recompute, and reconcile the authoritative state.

Autocomplete filter changes are debounced for 250 milliseconds without replacing the current pending reference with search text.
A newer search aborts the previous request and has an independent generation guard, so an obsolete response cannot replace current suggestions or validation.
Disconnect, HTMX replacement, prompt cancellation, and route supersession abort pending lookups and suppress late callbacks.

Escape first remains available to close an open candidate overlay.
When the overlay is closed, the adapter emits a Causeway escape signal so property editing or the standard action prompt cancels and restores focus through the existing controller.

## Honest autocomplete bound

The current public GraphQL autocomplete operation accepts search text but no page, size, total count, or continuation token.
The pilot therefore configures a minimum search length and a maximum accepted complete response.
It never describes local callback slicing as server paging.
An over-bound response produces `AUTOCOMPLETE_RESULT_LIMIT` with a refinement message and does not silently truncate a supposedly complete result.

True server paging remains deferred to a public GraphQL capability change.

## Packaging

The production closure contains 19 unique runtime packages:

- 12 Apache-2.0.
- 5 BSD-3-Clause.
- 2 MIT.

All Vaadin component packages are Apache-2.0, no Pro component is present, every archive has pinned npm integrity, every runtime package has packaged license material, and every repository is recorded.
The one upstream package that omits a license file, `@lit-labs/ssr-dom-shim`, uses the BSD-3-Clause license file from the same pinned Lit repository and records that provenance explicitly.

The build aliases Vaadin's usage-statistics collector import to Vaadin's published no-op opt-out module.
Verification rejects the collector endpoint and marker, Flow client marker, Pro package names, external runtime imports, missing integrity or license data, checksum drift, CSP hash drift, and budget regression.

Normal Maven builds verify checked-in deterministic outputs without contacting npm.
The `regenerate-vaadin-reference-assets` profile performs pinned acquisition with lifecycle scripts disabled and rebuilds the exact closure.
The foundation JAR packages the bundle and CSP manifest beneath `META-INF/resources/causeway-webcomponents/vaadin-reference` and 19 package licenses plus `THIRD-PARTY.json` beneath `META-INF/licenses/vaadin-reference`.

## CSP integration

The enabled HTMX shell adds only four byte-exact SHA-256 sources to `style-src` and `style-src-elem`, explicitly sets `style-src-attr 'none'`, and never adds `unsafe-inline`.
The disabled shell returns the original policy unchanged.
The hashes are coupled to the pinned bundle checksum and fail review on dependency drift.

See `csp-decision.md` and `results/csp-matrix.json` for the source trace and remedy comparison.

## Samples

Petclinic explicitly enables the HTMX properties and exercises real object-reference action parameters such as `removePet` and `bookVisit`.
The vanilla HTML sample configures the foundation API before importing `index.mjs` and adds `selectRelated(related)` as a fixed-choice object-reference action.
Both samples request the candidate only when the eligible prompt opens, preserve stable bookmark identity, cancel with focus restoration, and make no external request.

The live vanilla run also exposed a pre-existing invalid object-reference projection that requested `relatedObject.get.name` without the rich property wrapper subselection.
The projection now prefers `_meta` for object-valued rich types, matching the established object-reference renderer and eliminating the baseline GraphQL 400 responses.
