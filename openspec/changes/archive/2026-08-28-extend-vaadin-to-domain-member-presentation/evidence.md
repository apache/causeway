# Implementation evidence

## Pre-implementation baseline

The implementation baseline is planning commit `b31705c5b43`.
Read-only properties rendered the selected Causeway value renderer directly inside one `<output class="causeway-property-value">`, with Causeway-owned label and description relationships and no Vaadin view control.
Ordinary ready actions rendered one native `<button type="button">` directly inside `.causeway-action`, with the existing description, disabled reason, semantic activation event, focus behavior, and GraphQL interaction path.
Property edit, save, cancel, clear, action prompt, menu, and shell controls were native and remain outside this change.
The master policy was `causeway.viewer.webcomponents.htmx.editor-toolkit=vaadin|native`, while the older reference-widget and field-family pilot properties retained independent compatibility behavior.
Reference and editor field closures were requested only after a connected eligible adapter selected them, and unaffected routes requested no Vaadin closure.
Generated CSP used same-origin scripts and connections, exact candidate-originated style hashes, `style-src-attr 'none'`, and no `unsafe-inline` or external source.
Existing closure browser audits recorded zero CSP violations, unexpected external requests, page errors, console errors, axe violations, and page overflow, with focus and keyboard behavior covered by the retained field, reference, property, action, Petclinic, and Reference Application tests.

## Accepted read-only eligibility

| Value family | Qualified read-only control | Qualification boundary |
|---|---|---|
| Standard single-line `String`, `ID`, `UUID`, `Locale`, `Char`, and URL | Vaadin Text Field | Non-null standard scalar renderer only. |
| Semantic multiline `String` | Vaadin Text Area | The Causeway `multiline` hint remains authoritative. |
| Boolean | Vaadin Checkbox | Genuine read-only state and no value-changing keyboard or pointer behavior are required. |
| Enum and bounded choice | Vaadin Select | Genuine read-only state and no enabled picker affordance are required. |
| `Long`, `BigInteger`, and `BigDecimal` | Vaadin Text Field | The original lexical value is preserved without numeric coercion. |
| `Int`, `Short`, `Byte`, `Float`, and `Double` | Vaadin Integer Field or Number Field | The existing machine-numeric codec remains authoritative. |
| `LocalDate` | Vaadin Date Picker | No locale or timezone conversion is permitted. |
| `LocalTime` | Vaadin Time Picker | Fractional precision must not exceed milliseconds. |
| `LocalDateTime` | Vaadin Date Time Picker | Fractional precision must not exceed milliseconds and no timezone conversion is permitted. |

Protected values, nulls, references, resources, LOBs, offset or zoned temporals, legacy temporal types, collections, unsupported values, custom presentations, and application-selected renderers remain excluded or authoritative non-standard presentations.
No excluded or protected value is approximated with a disabled generic field.
Only ordinary ready `<cw-action>` controls are eligible for Vaadin Button presentation.
Property interaction controls, action-prompt controls, menu controls, shell controls, hidden actions, and other non-ordinary buttons remain excluded.

## Accepted closure and delivery policy

The accepted reference closure checksum is `40ef3cecd641b14b7212759d45035991d9eb12550c00be64d2d7a786bf8f8a81`, with 191342 raw bytes and 48684 gzip bytes.
The accepted basic field checksum is `879c4162cc0957c59d3f76f7c8b15ad7d4c7aca4763242a0100b5f487e6b9398`, with 163339 raw bytes and 42429 gzip bytes.
The accepted numeric field checksum is `c225135710434681739ea8fd6987130af692d1f2bb1be97a45cfd64a36e723ef`, with 96131 raw bytes and 25677 gzip bytes.
The accepted local-temporal field checksum is `05866b47c4fbba2fd56a71c94046c31cc13cb78d23c27b7b66cbce0de645325f`, with 235777 raw bytes and 61051 gzip bytes.
The accepted action checksum is `1073fc9a396e5216e2701c5513ce3e55b44796f60e44bab08b89b6195970f5cb`, with 64770 raw bytes and 19862 gzip bytes.
The accepted action entry point is only `@vaadin/button`, pinned to Vaadin `25.2.8` together with its reviewed Vaadin closure pins.
The complete accepted package versions, integrity values, repositories, license identifiers, copied license files, entry points, and style hashes are recorded in each closure's generated `THIRD-PARTY.json` and `csp-policy.json` manifests.
The accepted production vulnerability count is zero for the reference, field, and action closures under `npm audit --omit=dev`.

The reference gzip budget is 66560 bytes.
The basic field gzip budget is 81920 bytes.
The numeric field gzip budget is 66560 bytes.
The local-temporal field gzip budget is 102400 bytes.
The aggregate field-family gzip budget is 204800 bytes.
The action gzip budget is 65536 bytes.
An unaffected or explicit-native route has a zero-byte Vaadin closure budget.
Each route's budget is the sum of only the reference, field-family, and action budgets selected by connected eligible adapters.
The all-closure aggregate gzip budget is 336896 bytes, while the accepted all-closure measurement is 197703 bytes.

The accepted exact style hashes remain the deterministic union recorded by the reference, field-family, and action policy manifests.
The action closure contributes `sha256-xGEkK13KcZJdGhZfeIjuH6IWVGTHtjs/IqUVa8T0XXw=`, which is deduplicated when another selected closure already requires it.
No CSP hash permission causes a network request by itself.

## Final qualification results

The complete foundation Node suite passed with 203 tests and zero failures.
The foundation, generic HTMX, optional HTMX SecMan security, vanilla sample, and Petclinic Maven suites passed under Java 21.
Targeted shell, configuration, CSP, route, security, packaging, and vanilla-sample resource tests passed, including same-origin action-asset and action-policy retrieval.
The default and explicit-native Petclinic Playwright profiles each passed all four retained journeys.
The default and explicit-native Reference Application Playwright profiles each passed all nine retained journeys.
The Reference Application HTMX integration, capability inventory, and package-without-tests gates passed against current reactor artifacts.
Clean and incremental capability inventory generation retained SHA-256 `75ef904a0d4fbc9c915c74866cdbd503743dab589f7525bbab126baf1eaa024a` byte-identically.

The reference, basic, numeric, local-temporal, and action closures rebuilt to their accepted checksums and remained within every individual and aggregate gzip budget.
Production `npm audit --omit=dev` reported zero vulnerabilities for the reference, field, and action closures.
Closure verification rejected prohibited package and runtime markers and confirmed no Flow, Binder, Pro, Grid, Menu Bar, telemetry, CDN, or external-resource dependency entered production assets.
Strict exact-hash browser audits recorded zero CSP violations, axe violations, console errors, page errors, external requests, overlay leaks, or page overflow.
The audits exercised read-only mutation resistance, closed overlays, labels, descriptions, disabled state, keyboard and pointer behavior, visible focus, responsive layouts, dark theme, reduced motion, and forced colors.
Protected-value unit, integration, and browser assertions retained write-only values outside adapter markup, semantic events, errors, operation evidence, diagnostics, routes, and screenshots.

Machine-readable browser reports and value-free screenshots are retained under `evidence/fields/` and `evidence/actions/` beside this document.
Generated `THIRD-PARTY.json` and `csp-policy*.json` files retain the legal, dependency, checksum, entry-point, and exact-style-hash evidence for each closure.
Applicable RAT checks passed independently for foundation, HTMX, HTMX SecMan, vanilla sample, Petclinic, and Reference Application HTMX modules.
JavaScript syntax checks passed for 83 production and test files.
Strict OpenSpec validation and `git diff --check` passed.

The principal reproduction commands are:

[source,shell]
----
(cd viewers/webcomponents/foundation && node --test --test-concurrency=1)
(cd viewers/webcomponents/foundation/vaadin-reference && npm run build && npm run verify && npm audit --omit=dev)
(cd viewers/webcomponents/foundation/vaadin-fields && npm run build && npm run verify && npm audit --omit=dev && node audit.mjs)
(cd viewers/webcomponents/foundation/vaadin-actions && npm run verify)
mvn -pl viewers/webcomponents/foundation,viewers/webcomponents/htmx,viewers/webcomponents/htmx-security-secman,viewers/webcomponents/sample-html,viewers/webcomponents/sample-htmx-petclinic -am test
mvn -Pplaywright -Dtest=PetClinicHtmxPlaywrightTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -Preferenceapp-playwright -Dtest=ReferenceAppHtmxApplication_IntegTest,ReferenceAppCapabilityInventory_IntegTest,ReferenceAppHtmxPlaywrightTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -Preferenceapp-playwright -Dtest=ReferenceAppHtmxPlaywrightTest -Dsurefire.failIfNoSpecifiedTests=false -Dcauseway.viewer.webcomponents.htmx.component-toolkit=native test
openspec validate extend-vaadin-to-domain-member-presentation --type change --strict --no-interactive
git diff --check
----
