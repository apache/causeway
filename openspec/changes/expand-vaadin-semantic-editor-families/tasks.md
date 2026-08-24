## 1. Baseline and family contract

- [x] 1.1 Record the current native editor and codec mapping for every proposed and explicitly excluded value family.
- [x] 1.2 Record current reference closure checksum, gzip size, CSP hashes, package set, and zero-request behavior.
- [x] 1.3 Define stable `basic`, `numeric`, and `local-temporal` family identifiers and their exact semantic eligibility matrix.
- [x] 1.4 Define per-family and aggregate compressed budgets before generating candidate assets.
- [x] 1.5 Document why offset, zoned, legacy temporal, resource, custom, reference, and collection values remain outside field-family qualification.

## 2. Pinned selective Vaadin field packaging

- [x] 2.1 Create an Apache-licensed `vaadin-fields` build area independent from the reference closure.
- [x] 2.2 Pin Vaadin 25.2.8 free-core direct packages and esbuild with an exact npm lock.
- [x] 2.3 Add basic, numeric, and local-temporal entry points containing only approved controls.
- [x] 2.4 Alias Vaadin usage statistics to the opt-out module and prohibit lifecycle scripts.
- [x] 2.5 Emit deterministic independently named ESM assets for all three families.
- [x] 2.6 Generate per-asset raw, gzip, SHA-256, entry-point, package, license, and CSP policy metadata.
- [x] 2.7 Copy distributable license and notice files for the complete production dependency closure.
- [x] 2.8 Fail verification on dependency, integrity, license, entry-point, telemetry, checksum, size, style-hash, or generated-resource drift.
- [x] 2.9 Package field assets, policy metadata, and legal files through the foundation Maven module.
- [x] 2.10 Add deterministic build, verification, and npm audit commands to reproducing documentation.

## 3. Toolkit-neutral field-family policy

- [x] 3.1 Add a bounded field-family configuration API with empty defaults and normalized allow-list parsing.
- [x] 3.2 Reject unknown, malformed, duplicate-ambiguous, or unsafe module URL family configuration.
- [x] 3.3 Track module promise, load generation, and failed state independently for each family.
- [x] 3.4 Add semantic family classification driven by advertised input type and selected Causeway codec.
- [x] 3.5 Keep exact numeric values on lexical text controls and machine numeric values on numeric controls.
- [x] 3.6 Limit pickers to local date, local time, and local date-time values.
- [x] 3.7 Keep unsupported and unqualified codecs ineligible even when a family is enabled.
- [x] 3.8 Add exhaustive family policy and eligibility unit tests.

## 4. Causeway-owned internal field adapter

- [x] 4.1 Add and register the internal `<causeway-field-editor>` element without exposing raw Vaadin APIs to applications.
- [x] 4.2 Render bounded non-sensitive adapter attributes for family, control kind, label, value, and semantic relationships.
- [x] 4.3 Dynamically load only the selected family closure after the adapter connects.
- [x] 4.4 Guard import and definition waits against disconnect, replacement, and superseded generations.
- [x] 4.5 Map text, multiline, password, nullable and required Boolean, enum, and bounded scalar choices to approved basic controls.
- [x] 4.6 Map exact numeric values to lexical text fields with input hints and machine values to integer or number fields.
- [x] 4.7 Map local date, local time, and local date-time values to corresponding pickers without timezone conversion.
- [x] 4.8 Map required, disabled, invalid, described-by, test identity, clear behavior, and focus semantics.
- [x] 4.9 Expose compatible value, checked, focus, and text-selection behavior to existing interaction hosts.
- [x] 4.10 Forward one ordinary input or change path without duplicate semantic events or validation.
- [x] 4.11 Initialize protected controls empty and prevent protected values from entering attributes or diagnostics.
- [x] 4.12 On module failure, fail only that family and emit a bounded Causeway-owned fallback event.
- [x] 4.13 Add adapter unit tests using deterministic fake family modules and custom elements.

## 5. Registry and interaction integration

- [x] 5.1 Add a higher-priority qualified field registration while retaining reference adapter precedence.
- [x] 5.2 Preserve native editor selection when family policy is empty, excluded, failed, or unsupported.
- [x] 5.3 Generalize editor metadata for debounced text validation independently from toolkit identity.
- [x] 5.4 Integrate toolkit-backed property input, parsing, validation, saving, cancellation, and focus restoration.
- [x] 5.5 Integrate toolkit-backed object-action parameter input and dependent-parameter preparation.
- [x] 5.6 Integrate toolkit-backed service-action parameter input and submission.
- [x] 5.7 Rerender the matching native editor after a family load failure while preserving pending state.
- [x] 5.8 Ensure stale controls cannot replace current value, error, focus, prompt, or route state.
- [x] 5.9 Preserve protected-value redaction across property and action events and failures.
- [x] 5.10 Add foundation tests for each family across property, object-action, and service-action hosts.

## 6. HTMX configuration, CSP, and delivery

- [x] 6.1 Add an HTMX property for the explicit field-family allow-list with an empty default.
- [x] 6.2 Normalize and validate supported family names before rendering shell policy.
- [x] 6.3 Render bounded family configuration on the document without changing public application markup.
- [x] 6.4 Serve all field assets and policy metadata as same-origin packaged resources.
- [x] 6.5 Add deterministic per-family exact style-hash sets to HTMX CSP generation.
- [x] 6.6 Deduplicate the union of reference and enabled field hashes while retaining `style-src-attr 'none'`.
- [x] 6.7 Assert native policy contains no Vaadin hashes and never adds `unsafe-inline`.
- [x] 6.8 Add HTMX property, shell, CSP, route-fragment, and asset-isolation tests.

## 7. Samples, documentation, and theme integration

- [x] 7.1 Enable qualified families explicitly in candidate sample configuration while retaining a documented native override.
- [x] 7.2 Add vanilla sample fixtures demonstrating Causeway-owned markup for all adopted families.
- [x] 7.3 Map existing `--causeway-*` theme variables into internal controls without application-facing Vaadin selectors.
- [x] 7.4 Verify light, dark, reduced-motion, forced-colors, narrow, and wide presentation states.
- [x] 7.5 Document configuration, family eligibility, closure paths, budgets, CSP hashes, fallback, and rollback.
- [x] 7.6 Document resource upload and offset or zoned temporal values as retained explicit limitations.

## 8. Reference Application qualification

- [x] 8.1 Select deterministic retained Reference Application property and action targets for every adopted family.
- [x] 8.2 Add target-catalogue metadata for candidate and explicit native family journeys.
- [x] 8.3 Cover text, multiline, protected, nullable and required Boolean, enum, and bounded scalar choices.
- [x] 8.4 Cover exact numeric boundaries and precision plus representative machine numeric values.
- [x] 8.5 Cover local date, local time, and local date-time with supported fractional precision.
- [x] 8.6 Assert offset, zoned, resource, and custom values retain reviewed non-candidate classifications.
- [x] 8.7 Add candidate browser interactions for property save and object or service action submission.
- [x] 8.8 Add equivalent explicit native interactions and compare authoritative GraphQL outcomes.
- [x] 8.9 Add route replacement, prompt closure, disconnect, reconnect, and injected family-load-failure coverage.
- [x] 8.10 Assert protected values are absent from markup, events, errors, diagnostics, and route evidence.
- [x] 8.11 Verify clean and incremental capability inventory generation remain byte-identical unless reviewed capability additions require an intentional baseline update.

## 9. Security, accessibility, and performance evidence

- [x] 9.1 Measure per-family and aggregate raw and gzip assets against accepted budgets.
- [x] 9.2 Run npm production audit and retain reviewed zero-vulnerability results for the new closure.
- [x] 9.3 Capture every candidate-originated style hash across representative control states.
- [x] 9.4 Run enforcing exact-hash CSP with `style-src-attr 'none'` across all families.
- [x] 9.5 Run axe, keyboard, focus, screen-reader labeling, responsive, theme, reduced-motion, and forced-colors checks.
- [x] 9.6 Assert zero unexpected console errors, page errors, external requests, and overflow failures.
- [x] 9.7 Assert unaffected routes request zero basic, numeric, local-temporal, and reference assets as applicable.
- [x] 9.8 Verify no Flow, Binder, Pro, Grid, telemetry, CDN, or server-side Vaadin runtime enters the closure.
- [x] 9.9 Retain checksums, legal manifests, policy reports, screenshots, and reproduction commands as change evidence.

## 10. Final qualification

- [x] 10.1 Run foundation Node and web-component Maven suites.
- [x] 10.2 Run HTMX configuration, shell, CSP, route, and packaging suites.
- [x] 10.3 Run full Petclinic integration and Playwright suites in candidate and explicit native modes.
- [x] 10.4 Run Reference Application clean package, integration, inventory, and candidate plus native Playwright suites.
- [x] 10.5 Run deterministic field build, verify, license, checksum, budget, and npm audit gates.
- [x] 10.6 Run strict CSP and accessibility matrix gates for every adopted family.
- [x] 10.7 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation verification.
- [x] 10.8 Record final gate results and confirm the next change remains the separate Vaadin-default policy flip.
