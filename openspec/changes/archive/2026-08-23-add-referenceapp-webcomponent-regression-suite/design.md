## Context

The existing Petclinic sample deliberately provides a small, coherent acceptance application and is unsuitable as an exhaustive programming-model corpus.
The separate Apache Causeway Reference Application is designed to exercise a broad range of domain semantics, but consuming it as another repository would couple regression tests to unpublished snapshots, repository availability, and a stale excluded GraphQL launcher.

The pinned upstream revision contains about nineteen hundred domain files and resources, including many executable descriptions.
Its source is Apache-licensed, but its build has repository-specific parent POMs, RAT exclusions, generated documentation inputs, Wicket-only UI extensions, operational integrations, and deployment configuration that must not be copied indiscriminately.

The immediate purpose is to expose and classify viewer and GraphQL gaps.
This change must not hide failures by deleting difficult domain cases, must not attempt to fix every discovered gap, and must not change the Vaadin default decision before the resulting inventory is available.

## Goals / Non-Goals

**Goals:**

- Retain an exact, pinned, provenance-recorded Reference Application corpus inside the Causeway reactor.
- Separate reusable domain and fixture support from the first HTMX-specific launcher and browser suite.
- Boot the corpus through JPA, the rich GraphQL viewer, HTMX, and Wicket over one metamodel and fixture.
- Produce deterministic machine-readable coverage and gap classifications.
- Exercise representative browser journeys without invoking every potentially destructive action.
- Make the corpus suitable for future Vue, Svelte, Wicket, and other viewer regression launchers.
- Keep ordinary builds browser-download independent and keep heavy browser execution explicit.

**Non-Goals:**

- Synchronize automatically with the upstream repository.
- Copy obsolete launchers, deployment configuration, generated sites, code generation, or Wicket-specific custom panels.
- Guarantee that every discovered domain feature is already supported.
- Fix GraphQL contracts, value conversion, Vaadin widget coverage, or collection query semantics in this change.
- Replace Petclinic, which remains the focused cohesive application journey.
- Make copied sample artifacts releasable public application modules.

## Decisions

### Use isolated regression modules rather than an external build

Create a `regressiontests/referenceapp` aggregator with a copied domain module, reusable JPA application support, and an HTMX launcher/test module.
The modules use Causeway reactor parents, have installation and deployment disabled where appropriate, and override regression parent packaging settings only where a reusable reactor JAR is required.

An external checkout, Git submodule, Maven source dependency, or live GitHub download was rejected because it would make tests nondeterministic and complicate release and offline builds.
Embedding the corpus directly in the HTMX launcher was rejected because it would prevent reuse by other viewers.

### Pin one upstream baseline and preserve provenance

Copy the relevant source from `apache/causeway-app-referenceapp` revision `29b43bfe4f77d525fb345394e5a52bd7d85a91ba`.
Add `PROVENANCE.adoc`, a path-and-omission manifest, and deterministic checksums for copied files.
Preserve upstream package names and avoid stylistic rewrites so future comparison remains mechanical.

The build never contacts GitHub.
Refreshing the corpus is a separately reviewed maintenance operation that updates the revision, manifest, checksums, exclusions, metamodel baseline, and gap report together.

### Copy the domain corpus but rebuild the launcher

Retain domain Java sources, domain resources required by descriptions and grids, common and JPA manifests, and deterministic JPA fixture support.
Create new reactor-native POMs and a new application class importing the rich GraphQL viewer, HTMX viewer, Wicket viewer, JPA persistence, and copied manifests.

Do not copy `wicket/jpa-and-graphql`, because it is excluded upstream, references an obsolete parent, and does not provide a reliable current launcher.
Do not copy Wicket custom panels or UI extension implementations into the neutral domain module.
Record every omitted path and the reason for omission.

### Treat the inventory as a completeness contract

Generate an inventory from the effective metamodel, rich GraphQL introspection, structural resources, and deterministic fixture identities.
Every in-scope discovered member and value shape receives exactly one classification:

- `SUPPORTED` when the generic semantic viewer provides complete expected behavior.
- `GRACEFUL_UNSUPPORTED` when the viewer safely and visibly declines an unsupported capability.
- `GRAPHQL_GAP` when the public rich schema cannot express required semantics.
- `VIEWER_DEFECT` when advertised public behavior is consumed incorrectly.
- `VIEWER_SPECIFIC` when a feature intentionally belongs to another viewer or extension.
- `NOT_EXERCISED` only with a checked-in reason, risk, and representative substitute.

The checked-in baseline records counts and stable identifiers but excludes credentials, hidden values, resource bodies, submitted arguments, and exception details.
Unexpected additions, removals, or classification changes fail verification until reviewed.

### Use staged acceptance rather than exhaustive destructive execution

Stage one compiles the copied corpus and validates provenance, RAT, metamodel, GraphQL schema, application entry points, grids, menus, and fixture identities.
Stage two generates the capability inventory and checks that no in-scope item is unclassified.
Stage three runs bounded headless journeys across representative feature families.
Stage four performs Wicket-relative comparison for shared semantic outcomes without requiring identical DOM or styling.

The browser suite discovers targets from checked-in stable fixture metadata rather than depending on incidental generated names.
Mutating and destructive journeys use disposable fixture data or restore state.

### Keep security and browser costs explicit

The default regression runtime uses the Reference Application security configuration and deterministic demonstration credentials.
If a bypass mode is needed to diagnose metamodel or schema startup, it remains a separate non-acceptance diagnostic profile.
Browser tests authenticate once, then exercise Wicket and HTMX routes in the same application session.

Playwright remains in an opt-in Maven profile and runs headlessly.
Ordinary regression builds compile the corpus and run non-browser contract tests without downloading a browser.

### Separate gap discovery from subsequent fixes

This change publishes the baseline and gaps but does not broaden implementation scope whenever a failure is found.
Hard correctness gaps become focused follow-on changes, including value-input correctness and GraphQL autocomplete paging.
Only after those prerequisites and expanded Vaadin adapter qualification pass does the separate default-adoption change alter viewer defaults.

## Risks / Trade-offs

- [Repository size and compilation time increase] → Isolate the corpus, measure clean and incremental reactor cost, retain browser tests behind a profile, and document targeted commands.
- [Copied source drifts from upstream] → Pin one revision, retain checksums and provenance, and require explicit reviewed refreshes rather than implicit synchronization.
- [Upstream resources fail Causeway RAT or packaging policy] → Inventory all exceptions, retain only required resources, and use narrow documented exclusions rather than blanket patterns.
- [Metamodel or GraphQL startup reveals many failures] → Preserve the corpus, classify each failure, establish a stable baseline, and split fixes into focused changes.
- [Security configuration makes browser setup brittle] → Provide deterministic users and one documented authentication path while keeping bypass diagnostic-only.
- [Inventory becomes a snapshot with little semantic value] → Derive stable categories from public contracts and pair counts with representative executable journeys.
- [A copied module becomes an accidental supported application] → Mark all modules non-release and document them as regression fixtures with no compatibility promise for their internal packages.
- [Future viewers couple to HTMX test utilities] → Keep domain, fixture, inventory schema, and target catalogue in neutral modules; keep browser drivers in viewer-specific launchers.

## Migration Plan

1. Add the neutral aggregator and provenance files without changing production viewer behavior.
2. Copy and checksum the pinned corpus in a baseline commit.
3. Adapt reactor POMs, resources, manifests, and deterministic JPA fixtures while preserving source packages.
4. Establish metamodel, GraphQL schema, structural resource, and fixture baselines.
5. Add inventory generation and review the first classification report.
6. Add the HTMX/Wicket launcher, integration checks, and opt-in headless browser profile.
7. Publish the measured reactor cost, unsupported-feature report, and follow-on recommendations.

Rollback removes the isolated regression modules and their aggregator entries.
No production API, persistence migration, viewer route, or application configuration depends on them.

## Open Questions

- Which upstream executable description resources are essential to semantic coverage and which are documentation-only?
- Can the existing SecMan demonstration fixtures provide one stable browser login without importing Wicket-specific support?
- Which initial representative objects provide the smallest stable set covering every inventory category?
- Should later viewer launchers share one browser-journey specification or only the target catalogue and semantic assertions?
