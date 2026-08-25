## 1. Baseline and policy evidence

- [x] 1.1 Record the accepted reference and field closure checksums, gzip budgets, CSP hashes, licenses, and vulnerability status.
- [x] 1.2 Record current native defaults, pilot properties, shell attributes, and candidate sample overrides.
- [x] 1.3 Record the default, explicit-native, and deprecated compatibility policy matrix.
- [x] 1.4 Confirm the Reference Application inventory baseline and existing browser qualification counts before policy changes.

## 2. Common HTMX toolkit policy

- [x] 2.1 Add a bounded `EditorToolkit` configuration value with `vaadin` and `native` external forms.
- [x] 2.2 Default the effective common policy to Vaadin while tracking whether its setter was explicitly invoked.
- [x] 2.3 Retain deprecated reference-widget and field-family accessors with explicit-configuration tracking.
- [x] 2.4 Resolve an explicitly configured common policy ahead of all deprecated values.
- [x] 2.5 Enter complete old-policy compatibility mode when deprecated configuration is explicit and the common property is absent.
- [x] 2.6 Resolve default Vaadin policy to references plus basic, numeric, and local-temporal families.
- [x] 2.7 Resolve native policy to no Vaadin reference or field family.
- [x] 2.8 Reject invalid common and deprecated values with bounded configuration errors.
- [x] 2.9 Add unit tests for defaults, native rollback, explicit precedence, legacy true and false, legacy subsets, and invalid values.
- [x] 2.10 Document the deprecated-property compatibility period and deterministic precedence table.

## 3. Foundation default selection

- [x] 3.1 Change reference adapter configuration to default enabled while retaining explicit disablement.
- [x] 3.2 Change field adapter configuration to default all qualified families while retaining an empty native configuration.
- [x] 3.3 Consume explicit resolved shell reference and field policy values without duplicating server precedence logic.
- [x] 3.4 Keep existing JavaScript configuration functions usable for custom hosts, tests, injected modules, and native diagnostics.
- [x] 3.5 Preserve registry precedence, codec qualification, exact numeric lexical handling, temporal precision limits, and unsupported-shape fallback.
- [x] 3.6 Preserve document-scoped reference failure and family-scoped field failure without broadening another family.
- [x] 3.7 Add unit tests for foundation defaults, explicit native policy, resolved shell values, lazy imports, and failure fallback.
- [x] 3.8 Assert protected values remain absent under default selection and explicit native rollback.

## 4. Shell attributes, CSP, and route-lazy delivery

- [x] 4.1 Render the resolved common toolkit policy as bounded shell diagnostics.
- [x] 4.2 Render explicit resolved reference and field-family values for foundation initialization.
- [x] 4.3 Generate default CSP from the complete reviewed exact-hash union.
- [x] 4.4 Generate native CSP without any Vaadin style hash.
- [x] 4.5 Generate deprecated compatibility CSP from only the resolved old-policy subset.
- [x] 4.6 Retain `style-src-attr 'none'`, same-origin scripts and connections, and no `unsafe-inline` source in every mode.
- [x] 4.7 Assert common policy precedence cannot create mixed shell attributes or broadened hashes.
- [x] 4.8 Assert landing, read-only, and otherwise unaffected routes request zero toolkit closures under the default policy.
- [x] 4.9 Add controller, shell, CSP, route-fragment, and packaged-asset tests for all policy modes.

## 5. Samples, support policy, and migration documentation

- [x] 5.1 Remove Vaadin candidate opt-ins from Petclinic and the Reference Application so ordinary configuration exercises the default.
- [x] 5.2 Update explicit-native test launches to use only `editor-toolkit=native`.
- [x] 5.3 Update the vanilla sample to demonstrate default Causeway-owned adapters and documented native configuration.
- [x] 5.4 Replace pilot and candidate production wording with supported-default and native-rollback terminology.
- [x] 5.5 Document the common property, default behavior, precedence, deprecated mapping, and immediate rollback command.
- [x] 5.6 Document route-lazy delivery, family qualification, failure fallback, exact-hash CSP, and dependency-update obligations.
- [x] 5.7 Document that Grid, uploads, unsupported temporal shapes, Flow, Binder, Pro components, telemetry, CDN assets, and raw Vaadin APIs remain excluded.
- [x] 5.8 Retain the separately packaged reference and field closure identities, budgets, legal manifests, and verification commands.

## 6. Default and native regression qualification

- [x] 6.1 Update Petclinic assertions so no override selects Vaadin references and field families.
- [x] 6.2 Run Petclinic property, object-action, service-action, reference, cancellation, focus, route, and accessibility journeys in default mode.
- [x] 6.3 Run the equivalent Petclinic suite with the common native policy and assert zero Vaadin requests.
- [x] 6.4 Update Reference Application assertions and target metadata from candidate to default terminology.
- [x] 6.5 Run Reference Application reference, basic, numeric, temporal, protected, action, lifecycle, and route-isolation journeys in default mode.
- [x] 6.6 Run the equivalent Reference Application suite with the common native policy and compare authoritative GraphQL outcomes.
- [x] 6.7 Inject reference and field closure failures under default selection and verify bounded native recovery and family independence.
- [x] 6.8 Verify clean and incremental capability inventory generation remain byte-identical.

## 7. Release gates and evidence

- [x] 7.1 Run foundation Node and web-component Maven suites.
- [x] 7.2 Run HTMX configuration, shell, CSP, route, and packaging suites.
- [x] 7.3 Run deterministic reference and field build, checksum, license, budget, telemetry, and npm audit gates.
- [x] 7.4 Run enforcing strict-CSP, axe, keyboard, focus, responsive, theme, reduced-motion, and forced-colors matrices.
- [x] 7.5 Assert zero unexpected console errors, page errors, external requests, overflow failures, stale state, or overlay leaks.
- [x] 7.6 Run Petclinic and Reference Application default plus explicit-native release suites and ordinary clean Maven packaging.
- [x] 7.7 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation review.
- [x] 7.8 Record final gate results, resolved migration policy, supported release obligations, and remaining explicit exclusions.
