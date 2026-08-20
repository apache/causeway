## 1. Baseline and Evaluation Definition

- [ ] 1.1 Inventory the current Causeway theme assets, semantic component vocabulary, responsive states, customization variables, asset packaging, browser policy, and representative Petclinic UI states.
- [ ] 1.2 Freeze the evaluated candidate versions and record source, maintenance status, license, provenance, distribution formats, transitive dependencies, and known commercial or governance constraints.
- [ ] 1.3 Define the shared fixture state catalogue, desktop and narrow viewports, user-preference combinations, browser matrix, accessibility journeys, performance metrics, weighted scoring rubric, and hard rejection gates.

## 2. Reproducible Analysis Harness

- [ ] 2.1 Create an analysis-only harness and evidence directory under the change that renders the shared stable shell, menus, prompts, forms, validation, tabs, properties, collections, tables, loading states, result states, long labels, disabled controls, errors, and overflow fixtures.
- [ ] 2.2 Capture the current Causeway implementation as the baseline using the same harness states, screenshots, accessibility checks, keyboard journeys, and performance measurements used for candidates.
- [ ] 2.3 Document deterministic local asset acquisition and serving for every prototype without runtime CDN access, including checksums, licenses, and cleanup or regeneration instructions.

## 3. Candidate Prototypes

- [ ] 3.1 Build the Bootstrap 5.3 prototype using CSS variables and selected utilities while retaining Causeway-owned semantic behavior, and separately record any Bootstrap JavaScript or Popper behavior that would be required for broader adoption.
- [ ] 3.2 Build the Web Awesome prototype behind Causeway-owned host elements and adapters, covering token mapping, events, focus, disabled state, validation, slots, CSS parts, and browser-ready versus bundled distribution.
- [ ] 3.3 Build the Open Props and native Dialog/Popover prototype behind Causeway-owned rendering, including progressive fallback implications for the documented browser floor.
- [ ] 3.4 Record whether Spectrum Web Components is needed as a fallback benchmark and either build the bounded benchmark or retain evidence explaining why the primary candidates are sufficient.

## 4. Comparative Evidence

- [ ] 4.1 Capture comparable desktop and narrow screenshots for the baseline and each candidate in light and dark modes, reduced motion, and forced colors, and record hierarchy, branding, overflow, collision, and missing-state observations.
- [ ] 4.2 Run automated accessibility checks and manual keyboard and focus journeys for menus, responsive navigation, dialogs, validation, tabs, disabled controls, and error states, and retain all failures and limitations.
- [ ] 4.3 Measure compressed CSS and JavaScript size, request count, startup and rendering costs, and selective versus full-bundle packaging for the baseline and each candidate.
- [ ] 4.4 Evaluate semantic contract leakage, adapter complexity, global CSS effects, shadow DOM styling, application token overrides, HTMX lifecycle compatibility, and test automation implications.
- [ ] 4.5 Demonstrate a pinned offline Maven packaging path for each viable candidate and complete the licensing, notice, provenance, update, security, and release-process assessment.
- [ ] 4.6 Run a bounded integration check for each leading strategy against the real Petclinic HTMX viewer and record differences from the fixture harness.

## 5. Decision and Roadmap

- [ ] 5.1 Complete the hard-gate assessment and weighted decision matrix with every score linked to retained evidence.
- [ ] 5.2 Publish an architectural decision record recommending adoption, constrained adoption, or retention of the current implementation, including rejected alternatives and unresolved limitations.
- [ ] 5.3 Produce a staged migration and rollback outline, bundle and browser budgets, compatibility policy, and separate follow-on implementation proposal outline when adoption is recommended.
- [ ] 5.4 Verify that another maintainer can reproduce the prototypes and evidence from the documented commands and that all analysis deliverables contain no production dependency or runtime behavior changes.
