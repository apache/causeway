## Why

The web-component viewer currently owns low-level styling, design tokens, responsive layout, menus, dialogs, forms, tabs, tables, focus handling, and transient disclosure behavior.
A maintained theming or component toolkit might improve consistency, accessibility, and visual quality while reducing custom frontend infrastructure, but adopting one could also introduce global CSS conflicts, toolkit-specific markup, build tooling, runtime weight, or dependency lock-in.
Research and representative prototypes are needed before changing the established semantic component contracts or committing to a toolkit.

## What Changes

- Evaluate a CSS-oriented toolkit, a Web Component toolkit, and a low-lock-in token-and-native-primitives approach against the same Causeway viewer scenarios.
- Prototype the Petclinic home page, a generic object page, application menus, an action prompt, forms, tabs, collections, and tables with each shortlisted approach.
- Compare accessibility, keyboard and focus behavior, responsive behavior, light and dark themes, reduced motion, forced colors, visual quality, bundle size, runtime performance, packaging, and maintenance cost.
- Define an adapter boundary that keeps public `<causeway-*>` contracts and `--causeway-*` application overrides stable while allowing toolkit details to remain internal.
- Require deterministic Maven-packaged production assets without a runtime CDN dependency.
- Decide whether a toolkit should provide only theme primitives, selected visual components, transient interaction behavior, or a broader internal rendering layer.
- Produce a scored decision record, disposable prototypes, migration estimate, staged rollout plan, and explicit adopt-or-retain recommendation.
- Do not change production viewer behavior as part of the analysis.

## Initial Research Baseline

### Current architecture

The web-component modules currently have no npm package, lockfile, or frontend bundler configuration.
Foundation ES modules and CSS are copied directly into Maven resources, and the HTMX shell loads those packaged assets.
The existing generic HTMX viewer specification requires documented semantic-component hooks and CSS variables rather than Wicket or Bootstrap markup.
Any selected approach must either preserve that boundary or explicitly propose a reviewed specification change.

### Candidate shortlist

- **Bootstrap 5.3** is maintained, mature, Maven/WebJar-friendly, and can be loaded without a build step, but its dropdowns, overlays, and responsive navbar require Bootstrap JavaScript and Popper, and its global classes can couple Causeway markup to Bootstrap.
- **Web Awesome** is the active open-source successor to sunset Shoelace, provides Web Components, themes, utilities, and browser-ready `dist-cdn` assets, and is a strong full-component candidate whose governance, licensing tiers, packaging, and theming boundaries require due diligence.
- **Spectrum Web Components** provides a broad Lit-based accessible component set and theme package, but recommends selective package imports rather than its large all-component bundle and may impose an Adobe-oriented visual language and frontend toolchain.
- **Open Props plus native browser primitives** offers reusable design tokens with low lock-in while retaining Causeway semantic elements; native Dialog and Popover APIs can own modal and transient disclosure lifecycle, but Causeway would continue to own more component behavior and composition.
- **Material Web** provides Apache-licensed Material 3 Web Components but is currently in maintenance mode, so it should be treated as a comparison baseline rather than a preferred strategic dependency.
- **Shoelace** is sunset with no active development and should not be evaluated independently of its Web Awesome successor.

### Recommended prototype set

1. Bootstrap CSS, variables, and selected utilities while Causeway retains semantic behavior.
2. Web Awesome components behind Causeway-owned custom-element contracts.
3. Open Props with native Dialog and Popover APIs behind Causeway-owned rendering and behavior.

Spectrum Web Components should remain a benchmark candidate if the first three do not provide sufficient evidence.
Material Web and Shoelace should be documented as rejected or comparison-only candidates unless their maintenance status changes.

### Evaluation criteria

- ASF-compatible licensing, provenance, dependency governance, and security posture.
- Active maintenance, release cadence, browser support, and credible project continuity.
- Compatibility with framework-neutral custom elements, light DOM, semantic events, HTMX fragment lifecycle, and server-rendered shells.
- Deterministic offline Maven packaging without mandatory application-side npm tooling or production CDN access.
- Incremental adoption without exposing toolkit-specific markup or events as public Causeway contracts.
- Coverage for menus, dialogs, buttons, forms, validation, tabs, cards, tables, responsive navigation, loading state, and result presentation.
- Keyboard navigation, focus restoration, screen-reader semantics, forced-colors support, reduced motion, and automated accessibility results.
- Light and dark theming, CSS isolation, application overrides, branding, and compatibility with existing `--causeway-*` variables.
- Bundle size, request count, startup time, rendering performance, and tree-shaking or selective packaging options.
- Migration cost, testing cost, visual regression strategy, and the feasibility of removing replaced custom CSS and JavaScript.

## Capabilities

### New Capabilities

- `web-component-theming-kit-analysis`: Defines the reproducible toolkit evaluation, representative prototypes, decision matrix, adapter boundary, and adoption roadmap for Causeway web-component viewers.

### Modified Capabilities

None during the analysis.
A later implementation proposal may modify `generic-htmx-web-component-viewer` and shared web-component theming requirements if the selected approach changes established markup or styling boundaries.

## Impact

- Adds research notes, disposable prototypes, visual comparisons, accessibility evidence, performance measurements, a dependency and licensing assessment, and an architectural decision record.
- May identify future changes to foundation theme CSS, web-component renderers, HTMX shell asset loading, Maven dependencies, or frontend build tooling.
- Does not change GraphQL contracts, domain interaction semantics, canonical routing, HTMX fragment ownership, or public Causeway component APIs.
- Requires complete design, specification, task, and evidence artifacts before any toolkit implementation is proposed.
