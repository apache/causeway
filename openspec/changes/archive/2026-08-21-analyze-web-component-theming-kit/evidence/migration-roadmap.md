# Staged migration and rollback outline

## Scope

This roadmap applies only if a separate implementation proposal accepts the Bootstrap-derived theme pilot recommendation.
No production migration occurs in the analysis change.

## Stage 0: Policy and budgets

- Define the supported browser floor for generic web-component viewers.
- Confirm the initial 40 KB gzip theme-CSS budget and no-Bootstrap-JavaScript rule.
- Clarify that public Causeway elements, semantic events, and `--causeway-*` variables remain compatibility contracts while generated internal classes do not.
- Define contrast, Lighthouse, keyboard, responsive, dark, reduced-motion, forced-colors, screenshot, and Playwright gates.

## Stage 1: Reproducible theme build

- Add an analysis-derived pinned Bootstrap source dependency through an explicit WebJar or a repository-owned package lock.
- Prefer selected Bootstrap Sass variables, functions, and mixins compiled into Causeway-owned selectors rather than loading complete global Bootstrap CSS.
- Invoke the theme build from Maven so downstream applications continue using Maven only.
- Package generated CSS and required license material in the foundation JAR.
- Fail the build when generated assets differ from committed or expected checksums.

## Stage 2: Opt-in sample pilot

- Add a separately named Bootstrap-derived Causeway theme while retaining the current default theme unchanged.
- Enable it only in the HTMX Petclinic and vanilla samples through configuration or an additional stylesheet.
- Cover shell, menus, buttons, prompts, forms, validation, tabs, properties, collections, tables, status cards, loading, errors, and responsive navigation.
- Map every application-facing customization through existing or deliberately added `--causeway-*` variables.

## Stage 3: Evidence and compatibility review

- Run the complete foundation tests and Petclinic Playwright suite.
- Run desktop and narrow visual regression checks in light and dark modes.
- Run reduced-motion and forced-colors checks.
- Require Lighthouse accessibility 100 for representative fixture states or document a reviewed product-content exception.
- Require zero page-level overflow and no hidden focus.
- Compare generated CSS size, requests, route-ready timing, and LCP with the baseline.
- Verify that existing applications that do not select the pilot theme are unchanged.

## Stage 4: Adoption decision

- Review sample feedback, accessibility evidence, maintenance effort, CSS budget, and application override compatibility.
- Promote the pilot to an optional supported theme only if it improves evidence without public toolkit leakage.
- Consider making it the default only in a later compatibility-reviewed change.
- Remove the pilot if it does not beat the current theme on accessibility and maintainability after its build cost is included.

## Rollback

Rollback keeps the existing Causeway theme and component behavior as the permanent fallback.
The implementation must isolate the pilot in separate generated assets and configuration so rollback consists of disabling the pilot stylesheet and removing its build inputs.
No persisted data, GraphQL contract, route, semantic event, or application page requires migration.
The current Playwright screenshots and acceptance tests define the rollback reference.

## Compatibility policy

- `<causeway-*>` tags, documented attributes and properties, semantic events, context behavior, and `--causeway-*` variables remain stable.
- Bootstrap package names, Sass symbols, classes, data attributes, and JavaScript events are internal and unsupported.
- Bootstrap JavaScript and Popper remain absent unless a later proposal demonstrates a distinct requirement not already served by Causeway or native behavior.
- Applications may override Causeway variables and documented selectors without importing Bootstrap.
- Theme build tooling remains a repository and release concern rather than an application requirement.

## Initial budgets

- Generated pilot CSS: at most 40 KB gzip.
- Added initial JavaScript: 0 bytes for the theming pilot.
- Added initial requests: at most 1 cacheable stylesheet.
- Route-ready and LCP regression: less than 10% under a production-like throttled profile.
- Accessibility: no critical or serious automated violations and no known keyboard or hidden-focus failure.
- Page-level horizontal overflow: 0 pixels at documented desktop and narrow viewports.
