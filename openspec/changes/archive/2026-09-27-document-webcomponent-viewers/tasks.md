## 1. Inventory and preview baseline

- [x] 1.1 Inspect existing Antora viewer conventions, site navigation, publication playbooks, and source-include support; confirm the new component identifier and integration points.
- [x] 1.2 Map foundation, HTMX, Vue, security, and Petclinic README guidance to current public APIs, configuration, source code, and tests; record the sources for planned examples.
- [x] 1.3 Run `./preview.sh -A` before documentation edits and record existing warnings or environmental blockers as the validation baseline.

## 2. Shared component documentation

- [x] 2.1 Add `viewers/webcomponents/adoc/antora.yml`, the ROOT page structure, and navigation partials following existing viewer conventions.
- [x] 2.2 Write the overview, host comparison, architecture diagram, and rich GraphQL prerequisites with links to existing GraphQL guidance.
- [x] 2.3 Write shared composition and interaction guidance covering client/object contexts, menus, layouts, properties, actions, collections, and previews.
- [x] 2.4 Write presentation guidance covering value/resources and PDF display, theming, accessibility, toolkit selection, and supported limitations.
- [x] 2.5 Write shared troubleshooting guidance for schema configuration, missing assets/resources, empty content, and interaction failures.

## 3. HTMX guide

- [x] 3.1 Write HTMX installation, bootstrap enablement, configuration/defaults, and canonical route documentation verified against current source.
- [x] 3.2 Write application-shell, generic/custom page, and resource-handling guidance with minimal source-grounded examples.
- [x] 3.3 Write a Petclinic getting-started walkthrough with prerequisites, commands, expected entry point, and representative navigation/customisation steps.
- [x] 3.4 Document optional HTMX SecMan setup, session and CSRF behaviour, authorization boundaries, development-only credentials, and troubleshooting.

## 4. Vue guide

- [x] 4.1 Write Vue installation, peer dependencies, custom-element compilation, router/plugin setup, and backend asset prerequisites verified against current source.
- [x] 4.2 Write application-owned shell, custom route page, public integration, and lifecycle guidance with minimal source-grounded examples.
- [x] 4.3 Write a Petclinic getting-started walkthrough with prerequisites, commands, expected entry point, and representative navigation/customisation steps.
- [x] 4.4 Document optional Vue SecMan setup, session and CSRF behaviour, authorization boundaries, development-only credentials, and troubleshooting.

## 5. Site integration and documentation consistency

- [x] 5.1 Register the component in applicable local, verification, and publication playbooks without referencing absent historical content.
- [x] 5.2 Add site-level viewer discovery links and verify navigation between shared pages, both host guides, and existing GraphQL documentation.
- [x] 5.3 Add guide links to relevant READMEs while retaining contributor verification material; update related GraphQL overview wording where necessary.
- [x] 5.4 Review all new pages for Apache notices, one-sentence-per-line formatting, current API/configuration names, clear limitations, and avoidance of duplicated reference material.

## 6. Verification

- [x] 6.1 Check setup and customisation examples against current sources and exercise documented sample startup paths where feasible; record execution results and any blockers.
- [x] 6.2 Rebuild with `./preview.sh -A`, compare diagnostics with the baseline, and resolve new cross-reference, include, and asset defects.
- [x] 6.3 Serve with `./preview.sh -S` and inspect the overview, shared guidance, both getting-started paths, security pages, navigation, and rendered code blocks in the browser.
- [x] 6.4 Record final verification results, unrelated pre-existing warnings, and any explicitly unverified checks.
