## Context

The framework-neutral web-component foundation currently ships handwritten ES modules and CSS directly as Maven resources without an npm package, lockfile, or bundler.
The generic HTMX viewer composes those semantic elements into a stable shell and exposes application customization through documented `--causeway-*` variables and selectors.
This gives Causeway full control and a small dependency surface, but it also makes Causeway responsible for visual coherence, responsive behavior, accessibility mechanics, interaction polish, and long-term maintenance across an expanding component vocabulary.
The existing viewer specification deliberately avoids Wicket or Bootstrap markup, so a toolkit cannot become part of public Causeway contracts accidentally.
The primary stakeholders are Causeway maintainers, application developers who theme viewers, accessibility users, and downstream projects that require reproducible offline Maven builds.

## Goals / Non-Goals

**Goals:**

- Determine whether an external theming or component toolkit offers enough measurable value to justify adoption.
- Compare materially different integration strategies against one representative UI and one evaluation rubric.
- Preserve public `<causeway-*>` elements, semantic events, domain behavior, and application-facing `--causeway-*` customization unless a later proposal explicitly changes them.
- Produce reproducible evidence covering visual quality, accessibility, responsiveness, packaging, performance, maintenance, and migration cost.
- Deliver an explicit recommendation and a bounded roadmap for either adoption or continued internal development.

**Non-Goals:**

- Add a production toolkit dependency during this analysis.
- Redesign GraphQL contracts, domain interactions, HTMX routing, or server-side page composition.
- Replace all existing web components or CSS as part of a prototype.
- Treat attractive screenshots as sufficient evidence without keyboard, accessibility, packaging, and performance checks.
- Commit Causeway to npm, a bundler, a CDN, or toolkit-specific public markup before the decision is reviewed.

## Decisions

### Evaluate three integration strategies rather than three visually similar libraries

The primary comparison will cover Bootstrap CSS and utilities with Causeway-owned behavior, Web Awesome components behind Causeway-owned adapters, and Open Props with native Dialog and Popover APIs.
These represent a global CSS toolkit, a Web Component toolkit, and a low-lock-in token-and-platform approach respectively.
Spectrum Web Components remains a fallback benchmark if one primary candidate cannot be evaluated credibly.
Material Web is comparison-only because it is in maintenance mode, and Shoelace is excluded because active development has moved to Web Awesome.

Comparing only Bootstrap, Material, and another branded component library was rejected because it would not test the more important choice between styling assistance, component replacement, and native-platform composition.

### Keep public Causeway semantics outside the toolkit boundary

Each prototype will retain representative `<causeway-*>` hosts and Causeway semantic events while placing toolkit markup, styles, or native primitives behind an internal rendering adapter.
Existing `--causeway-*` variables will remain the application-facing theme contract and will map to toolkit tokens where practical.
Any candidate that requires application code to depend directly on toolkit tags, classes, events, or global state will receive a material lock-in penalty and must justify a later specification change.

Exposing toolkit APIs directly was rejected as the default because it would make a dependency evaluation an irreversible public API decision.

### Use a reproducible analysis harness plus real-viewer verification

An analysis-only harness under the change directory will render a common fixture vocabulary for the stable shell, menus, prompt controls, tabs, properties, collections, tables, status cards, and responsive navigation.
The harness will freeze candidate versions and local assets, record provenance, and support deterministic screenshot and accessibility runs without modifying production viewer behavior.
The selected or leading prototype will also be injected or adapted into the running Petclinic viewer for a bounded integration check so fixture-only assumptions are identified.

Editing production rendering code for each candidate was rejected because disposable experiments should not create three migrations that later need to be reverted.

### Require offline deterministic packaging evidence

Prototype instructions may use upstream package tooling to acquire assets, but every candidate must demonstrate how pinned production assets could be built or packaged into Maven artifacts without runtime CDN access.
The analysis will distinguish browser-ready distributions, WebJars, selective package imports, and bundler-dependent outputs.
License files, notices, transitive dependencies, update procedures, and integrity or provenance information will be recorded with each candidate.

CDN-only demonstrations were rejected because they do not represent Apache Causeway release or downstream offline-build requirements.

### Score evidence using a published weighted rubric

The decision matrix will weight accessibility and interaction correctness at 25%, semantic and architectural compatibility at 20%, maintenance and governance at 15%, packaging and build integration at 15%, visual and theming capability at 15%, and performance at 10%.
A candidate must also pass non-scored gates for acceptable licensing, no runtime CDN requirement, no undisclosed sensitive data, and preservation of public Causeway domain semantics.
Scores will cite captured evidence rather than subjective recollection.

Choosing a toolkit by popularity or visual preference alone was rejected because those factors do not cover the repository's architectural and release constraints.

### Separate analysis completion from production adoption

This change will end with one of three outcomes: adopt a candidate, adopt a constrained subset, or retain and improve the current approach.
Any production change will require a separate proposal defining dependencies, compatibility policy, migration stages, rollback, and specification deltas.
The analysis may include a draft implementation roadmap but will not silently convert prototypes into supported runtime code.

## Risks / Trade-offs

- [Risk] Prototype fixtures may flatter a candidate by avoiding difficult real component states. → Include loading, disabled, validation, partial-error, overflow, long-label, and narrow-layout fixtures plus one real Petclinic integration check.
- [Risk] Web Component toolkits may hide styling and semantics inside shadow DOM. → Record CSS parts, slots, tokens, event translation, focus delegation, and automation implications explicitly.
- [Risk] Bootstrap may appear cheap while spreading global CSS and class coupling. → Separate CSS-only value from Bootstrap JavaScript behavior and score collision and markup costs.
- [Risk] Native Popover support may not match Causeway's supported browser floor. → Document the exact browser baseline, progressive fallback cost, and automated coverage before recommending it.
- [Risk] A no-bundler constraint may exclude an otherwise strong toolkit. → Measure both direct browser distribution and a small reproducible build step, then make the build-policy trade-off explicit.
- [Risk] Candidate projects, licenses, or free-versus-commercial packaging can change. → Pin versions, archive source references, verify licenses, and record project status at evaluation time.
- [Risk] Weighted scoring can disguise a failed hard requirement. → Apply licensing, offline packaging, security, and semantic-contract gates before comparing numerical scores.

## Migration Plan

1. Baseline the current viewer using the same fixture, screenshots, accessibility checks, and performance measurements used for candidates.
2. Freeze candidate versions, assets, licenses, and evaluation instructions.
3. Build the three analysis-only prototypes and record integration findings.
4. Run the published evidence suite at desktop and narrow viewports across required user preferences.
5. Complete the decision matrix and architectural decision record.
6. Draft a staged implementation proposal only if adoption or constrained adoption is recommended.

The analysis has no production rollback because its prototypes and evidence remain outside production modules.
A later implementation proposal must define independent rollback to the current Causeway theme and rendering behavior.

## Open Questions

- What is the minimum browser version policy, especially for the Popover API that reached Baseline status in 2025?
- Is a small repository-owned frontend build acceptable if Maven remains the single release entry point and generated assets are reproducible?
- Should Causeway retain a visually neutral default or deliberately align with a recognizable design system?
- What compressed JavaScript and CSS budgets should gate adoption?
- How much toolkit markup may exist inside a Causeway element before maintenance savings are outweighed by adapter complexity?
- Should a selected theme strategy apply first to the HTMX viewer only or to the shared foundation used by all generic viewers?
