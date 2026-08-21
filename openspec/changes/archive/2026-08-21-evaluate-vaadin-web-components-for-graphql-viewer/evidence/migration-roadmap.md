# Staged migration, rollback, budgets, and follow-on outline

## Scope

This roadmap applies only if a separate proposal accepts the conditional reference-widget recommendation.
The evaluation change itself adds no production dependency or runtime behavior.

## Stage 0: CSP feasibility and security review

- Reproduce each component-originated inline-style violation under the real viewer policy.
- Identify whether it comes from theme styles, overlay positioning, Grid sizing, or another dynamic attribute.
- Evaluate Lit nonce support, Constructable StyleSheets, build-time style extraction, and a narrowly scoped CSP Level 3 `style-src-attr` approach.
- Retain `style-src 'self'` as the baseline and do not add blanket `unsafe-inline` merely for convenience.
- Obtain project security review and automated CSP regression coverage.
- Stop the change if no acceptable strategy exists.

## Stage 1: Selective build and packaging

- Add an explicit free-core allowlist for Combo Box, Multi-Select Combo Box, and required transitive modules.
- Pin npm integrity through a repository-owned lock.
- Invoke selective generation and verification from Maven.
- Package route-lazy JavaScript, license, and notice assets in the foundation JAR.
- Assert zero external requests, zero Pro packages, inert or excluded usage-statistics code, and deterministic hashes.

## Stage 2: Internal semantic adapters

- Implement Causeway-owned single and multi-reference wrappers.
- Map labels, required state, disabled reasons, current value, choices, autocomplete search, validation, cancellation, and stable identity through the existing object and interaction contexts.
- Emit only Causeway semantic events to ordinary application pages.
- Bound autocomplete results explicitly and record when a domain method exceeds the supported response policy.
- Keep the current editor registry implementation as fallback.

## Stage 3: Opt-in samples and evidence

- Enable the candidate only in Petclinic and the vanilla HTML sample through explicit configuration.
- Exercise property editing, action parameters, breadcrumb-like navigation, single selection, multi-selection, clearing, errors, disabled state, dark mode, forced colors, reduced motion, and narrow layouts.
- Run foundation tests, Petclinic Playwright tests, axe journeys, CSP checks, package checks, bundle checks, and no-external-request checks.
- Confirm that router-selected custom pages can use semantic wrappers without Vaadin knowledge.

## Stage 4: Adoption review

- Compare accessibility, support effort, GraphQL bounds, bundle budget, CSP posture, and application feedback with the existing editor.
- Promote the candidate to an optional supported editor only if every gate passes.
- Consider default use only in a later compatibility-reviewed change.
- Specify paged autocomplete separately if production datasets require it.
- Evaluate read-only Grid in another change after collection sort and filter policy is decided.

## Rollback

The current Causeway editor remains packaged and selectable throughout the pilot.
Rollback disables the candidate editor mapping and route-lazy asset import; GraphQL data, routes, persisted objects, semantic events, and custom page files require no migration.
Removing generated Vaadin assets and the package lock restores the prior release architecture.

## Compatibility policy

- Stable: `<causeway-*>` tags, documented attributes and properties, semantic events, object context behavior, canonical routes, GraphQL contract, and `--causeway-*` variables.
- Internal: Vaadin package names, tags, properties, data-provider callbacks, events, shadow parts, theme variables, bundle chunks, and version.
- Application-owned: arbitrary HTML, CSS, JavaScript, and third-party widgets supplied by custom page authors.
- Not initially supported: raw viewer-bundled `<vaadin-*>` tags as a long-lived application API.
- Independent: any future server-side Vaadin viewer and its Java Flow extension model.

## Initial budgets

- Cold reference-widget route closure: at most 65 KB gzip.
- Reference plus future Grid shared closure: at most 90 KB gzip before a separate approval.
- Shell and routes without adopted widgets: zero Vaadin requests.
- External runtime requests: zero.
- Unexpected CSP violations: zero.
- Automated critical or serious accessibility violations: zero.
- Page-level horizontal overflow: zero at documented viewports.
- Route-ready regression: less than 10% in a production-like cached profile.
- Pro or ambiguous-license package instances: zero.

## Follow-on proposal outline

Suggested change name: `add-vaadin-reference-widget-pilot`.

The proposal should:

- Resolve strict CSP first with an explicit stop gate.
- Add a Maven-invoked pinned selective free-core build.
- Add internal single and multi-reference adapters to the semantic editor registry.
- Preserve existing editors as default or fallback.
- Enable the pilot only in Petclinic and vanilla samples.
- Add GraphQL result-bound policy without introducing a private endpoint.
- Add accessibility, CSP, bundle, lifecycle, license, external-request, and Playwright acceptance gates.
- Keep raw Vaadin tags, Grid, Flow, and a server-side Vaadin viewer outside scope.
