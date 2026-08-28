## 1. Baseline and qualification contract

- [ ] 1.1 Record current read-only property renderer markup, action-button markup, toolkit policy, route requests, CSP hashes, accessible trees, focus behavior, and theme states before implementation.
- [ ] 1.2 Record accepted basic, numeric, local-temporal, and reference closure checksums, compressed sizes, dependencies, licenses, vulnerabilities, style hashes, and zero-request behavior.
- [ ] 1.3 Define the exact read-only eligibility matrix for text, multiline, Boolean, enum, bounded-choice, exact numeric, machine numeric, local date, local time, and local date-time values.
- [ ] 1.4 Record protected, null, reference, resource, LOB, offset, zoned, legacy temporal, custom, collection, unsupported, and application-renderer cases as excluded or authoritative non-standard presentations.
- [ ] 1.5 Define ordinary `<cw-action>` button eligibility and record property interaction, prompt, menu, shell, and other buttons as excluded from this change.
- [ ] 1.6 Set per-family, action-closure, route-level, and aggregate compressed budgets before generating new assets.

## 2. Common component-toolkit policy

- [ ] 2.1 Add bounded `ComponentToolkit` configuration with `vaadin` and `native` external forms and an effective Vaadin default.
- [ ] 2.2 Track explicit `component-toolkit` configuration independently from deprecated `editor-toolkit`, reference-widget, and field-family inputs.
- [ ] 2.3 Resolve explicit component policy before every compatibility input.
- [ ] 2.4 Map explicit deprecated `editor-toolkit` to the complete component policy only when `component-toolkit` is absent.
- [ ] 2.5 Preserve former independent editor-only pilot behavior when both common properties are absent and an older pilot property is explicit.
- [ ] 2.6 Keep read-only fields and action buttons native under old pilot-only compatibility mode.
- [ ] 2.7 Reject invalid component, editor, and pilot policy values with bounded configuration errors.
- [ ] 2.8 Render resolved component policy, compatibility source, reference eligibility, field families, and action eligibility as bounded Causeway-owned shell diagnostics.
- [ ] 2.9 Add exhaustive property tests for default, native, explicit precedence, editor compatibility, pilot subsets, conflicting values, invalid values, and diagnostics.

## 3. Pinned Vaadin action-button packaging

- [ ] 3.1 Create an Apache-licensed `vaadin-actions` build area independent from reference and field closures.
- [ ] 3.2 Pin the accepted Vaadin free-core Button package and build tooling without silently upgrading the currently reviewed Vaadin line.
- [ ] 3.3 Import only `@vaadin/button` and its approved free-core transitive closure from the action entry point.
- [ ] 3.4 Alias Vaadin usage statistics to the existing opt-out module and prohibit lifecycle scripts, telemetry, Flow, Binder, Pro code, CDN content, and external assets.
- [ ] 3.5 Emit a deterministic same-origin ESM action asset with raw, gzip, SHA-256, entry-point, package, license, and exact style-hash metadata.
- [ ] 3.6 Copy complete distributable legal and notice material for the action closure.
- [ ] 3.7 Fail verification on dependency, integrity, license, vulnerability, telemetry, checksum, size, entry-point, style-hash, or generated-resource drift.
- [ ] 3.8 Package the action asset, policy metadata, and legal files through the foundation Maven module.
- [ ] 3.9 Document deterministic build, verification, audit, checksum, budget, and legal reproduction commands.

## 4. Causeway-owned read-only field adapter

- [ ] 4.1 Add a Causeway-owned internal read-only field adapter without adding raw Vaadin APIs to the public component vocabulary.
- [ ] 4.2 Accept normalized non-sensitive value state, family, control kind, accessible-name relationship, description relationship, multiline hint, and test identity.
- [ ] 4.3 Dynamically load only the selected existing field-family closure after an eligible adapter connects.
- [ ] 4.4 Guard import, definition, value assignment, and focus work against disconnect, replacement, policy change, and superseded revisions.
- [ ] 4.5 Map single-line and semantic multiline strings to read-only text controls without duplicate visible labels.
- [ ] 4.6 Map Boolean values to Vaadin Checkbox's genuine read-only state and qualify enum and bounded-choice controls only when they expose no enabled value-changing affordance.
- [ ] 4.7 Map exact numeric values to lexical read-only text fields and machine values to qualified read-only numeric controls without additional coercion.
- [ ] 4.8 Map local date, millisecond local time, and millisecond local date-time to read-only pickers without locale or timezone conversion.
- [ ] 4.9 Preserve Causeway labels, descriptions, disabled-reason tooltips, string alignment hooks, multiline layout, hidden state, errors, and responsive order outside toolkit Shadow DOM.
- [ ] 4.10 Use genuine read-only state rather than disabled state for visible property values and bind one accessible name and description.
- [ ] 4.11 Keep protected, null, reference, resource, LOB, offset, zoned, legacy temporal, custom, collection, unsupported, and unqualified values out of the adapter.
- [ ] 4.12 Emit a bounded Causeway-owned family failure signal without exposing values or toolkit errors.
- [ ] 4.13 Add deterministic adapter tests for every qualified and excluded family, asynchronous upgrade, reconnect, stale revision, accessibility relationships, and module failure.

## 5. Property renderer and interaction integration

- [ ] 5.1 Add toolkit-aware read-only selection only after application renderer precedence and established standard renderer selection are known.
- [ ] 5.2 Preserve application-specific renderers and all established object-link, null, LOB, resource, custom, and unsupported renderers unchanged.
- [ ] 5.3 Mount and update the read-only adapter from `<cw-property>` without serializing protected or unsafe descriptor data into markup.
- [ ] 5.4 Rerender the authoritative native value immediately when policy, eligibility, family status, or adapter loading requires fallback.
- [ ] 5.5 Preserve property visibility, usability, descriptions, disabled reasons, member-scoped errors, null state, live status, and semantic events across adapter rendering.
- [ ] 5.6 Coordinate view and edit family decisions while retaining separate read-only and editor state machines.
- [ ] 5.7 Preserve pending values, codecs, validation, save, cancellation, authoritative refresh, and focus intent across view-to-edit and edit-to-view transitions.
- [ ] 5.8 Prevent stale read-only or editor work from replacing post-save, post-cancel, failure, hidden, disconnected, or newer-generation presentation.
- [ ] 5.9 Add foundation tests for application precedence, every view family, exclusions, disabled and described fields, responsive multiline layout, transitions, authoritative reconciliation, and native parity.

## 6. Causeway-owned ordinary action adapter

- [ ] 6.1 Add an internal action-control adapter that lazy-loads the action closure and remains subordinate to `<cw-action>` state.
- [ ] 6.2 Map label, description, disabled state, disabled reason, test identity, theme, accessible name, and visible focus into Vaadin Button presentation.
- [ ] 6.3 Translate keyboard and pointer activation into the existing `<cw-action>` activation path exactly once.
- [ ] 6.4 Keep GraphQL invocation, parameter prompting, validation, result handling, navigation policy, and focus restoration in the existing Causeway interaction controller.
- [ ] 6.5 Ensure hidden or disconnected actions cannot be restored by late module or definition work.
- [ ] 6.6 On module or definition failure, disable only the action adapter and rerender all ordinary actions as established native buttons.
- [ ] 6.7 Preserve associated-action order, node identity, owner composition, and independent visibility and usability semantics.
- [ ] 6.8 Leave property edit, save, cancel, clear, prompt, menu, and shell controls on their existing native implementations.
- [ ] 6.9 Add adapter and `<cw-action>` tests for enabled, disabled, hidden, associated, object, service, parameterized, failure, stale, disconnect, focus, and duplicate-activation cases.

## 7. HTMX CSP and route-lazy delivery

- [ ] 7.1 Serve the action closure and policy metadata as same-origin packaged resources.
- [ ] 7.2 Extend default generated CSP to the deterministic deduplicated union of reference, field-family, and action-button style hashes.
- [ ] 7.3 Generate explicit native CSP without any Vaadin hash or closure eligibility.
- [ ] 7.4 Generate editor compatibility CSP from the complete policy and pilot compatibility CSP from only its former editor subset.
- [ ] 7.5 Retain `style-src-attr 'none'`, same-origin scripts and connections, and no `unsafe-inline` or external source in every mode.
- [ ] 7.6 Request a field closure when the first eligible read-only presentation or editor from that family connects and reuse it for later states.
- [ ] 7.7 Request the action closure only when the first visible eligible ordinary action connects.
- [ ] 7.8 Assert unaffected and native routes request no Vaadin closure and route readiness never waits for an unused family.
- [ ] 7.9 Add controller, properties, validator, shell, CSP, asset, route-fragment, policy-precedence, lazy-request, and failure-isolation tests.

## 8. Themes samples and documentation

- [ ] 8.1 Map documented `--causeway-*` variables into internal read-only fields and action buttons without requiring application-facing Vaadin selectors.
- [ ] 8.2 Verify labels, descriptions, disabled reasons, read-only values, action states, and focus in baseline and cohesive themes.
- [ ] 8.3 Extend the vanilla sample with deterministic eligible, excluded, disabled, described, multiline, action, failure, and native-policy presentations.
- [ ] 8.4 Update Petclinic default pages and fixtures to demonstrate Vaadin read-only fields and ordinary actions without raw Vaadin markup.
- [ ] 8.5 Document `component-toolkit`, default behavior, native rollback, precedence, deprecated properties, eligibility, exclusions, route loading, closures, budgets, CSP, fallback, and dependency-update obligations.
- [ ] 8.6 Document that protected read-only values, non-field renderers, property interaction controls, prompts, Grid, Menu Bar, Flow, Binder, Pro, telemetry, CDN content, and raw Vaadin APIs remain excluded.
- [ ] 8.7 Add source audits that prevent raw application-facing Vaadin APIs and accidental broadening to excluded controls or values.

## 9. Petclinic and Reference Application qualification

- [ ] 9.1 Add default and explicit-native Petclinic assertions for read-only field families, ordinary actions, view/edit transitions, disabled reasons, custom pages, routing, and requested assets.
- [ ] 9.2 Select deterministic retained Reference Application read-only targets for every qualified family and every important excluded classification.
- [ ] 9.3 Select deterministic enabled, disabled, hidden, object, service, parameterless, parameterized, and associated ordinary action targets.
- [ ] 9.4 Add default and native read-only field journeys comparing values, labels, descriptions, disabled reasons, layout, accessibility, and authoritative outcomes.
- [ ] 9.5 Add default and native action journeys comparing request counts, preparation, validation, invocation, results, route identity, and focus restoration.
- [ ] 9.6 Add view/edit/save/cancel/failure transitions and assert no stale editor, stale value, duplicate control, or focus loss.
- [ ] 9.7 Inject field and action closure failures before and after connection and verify bounded independent native recovery.
- [ ] 9.8 Replace routes during field and action upgrade and verify disconnected work cannot alter the current route.
- [ ] 9.9 Exercise component, editor, pilot, conflicting, default, and native policy profiles and verify diagnostics, hashes, closures, and precedence.
- [ ] 9.10 Verify clean and incremental capability inventory generation remain byte-identical unless intentional presentation evidence requires a reviewed baseline update.

## 10. Security accessibility and performance evidence

- [ ] 10.1 Measure field, action, route-level, and aggregate raw and gzip assets against accepted budgets.
- [ ] 10.2 Run production npm audit and retain reviewed vulnerability results for every packaged closure.
- [ ] 10.3 Capture and verify candidate-originated style hashes across representative read-only and action states.
- [ ] 10.4 Run enforcing exact-hash CSP with `style-src-attr 'none'` across default, native, editor compatibility, and pilot subset modes.
- [ ] 10.5 Run axe, keyboard, accessible-name, description, disabled-reason, visible-focus, responsive, theme, reduced-motion, and forced-colors checks.
- [ ] 10.6 Assert zero unexpected console errors, page errors, external requests, stale state, duplicate controls, overlay leaks, clipping, and page overflow.
- [ ] 10.7 Verify protected values remain absent from markup, properties, events, errors, diagnostics, operation summaries, screenshots, and route evidence.
- [ ] 10.8 Verify no Flow, Binder, Pro, Grid, Menu Bar, telemetry, CDN, server-side Vaadin runtime, or unapproved control enters a production closure.
- [ ] 10.9 Retain checksums, legal manifests, policy reports, accessibility output, request evidence, screenshots, and reproduction commands.

## 11. Final qualification

- [ ] 11.1 Run complete foundation Node and web-component Maven suites.
- [ ] 11.2 Run HTMX configuration, shell, CSP, route, security, and packaging suites.
- [ ] 11.3 Run deterministic field and action build, checksum, license, budget, telemetry, and npm audit gates.
- [ ] 11.4 Run complete vanilla sample and Petclinic integration and Playwright suites in default and explicit-native modes.
- [ ] 11.5 Run Reference Application clean package, integration, inventory, and default plus native Playwright suites.
- [ ] 11.6 Run enforcing strict-CSP, accessibility, keyboard, responsive, theme, reduced-motion, forced-colors, route-isolation, and failure matrices.
- [ ] 11.7 Run applicable RAT checks, production-isolation review, JavaScript syntax, strict OpenSpec validation, and `git diff --check`.
- [ ] 11.8 Record final gate results, resolved compatibility behavior, supported rollback, accepted budgets, and remaining exclusions before implementation is considered complete.
