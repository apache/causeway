# Packaging, licensing, and supply-chain assessment

## Frozen closure

The lockfile contains 64 production, development, and optional package instances.
The installed production closure contains 33 instances:

- 26 Apache-2.0.
- 5 BSD-3-Clause.
- 2 MIT.

All 26 resolved `@vaadin/*` runtime packages declare Apache-2.0.
The only MPL-2.0 package is development-only axe-core and is not included in the generated browser bundle.
No package has an unknown license field in the retained metadata report.

## Free-core gate

The direct allowlist contains fourteen Vaadin 25.2.8 packages and each declares Apache-2.0.
Grid Pro, Rich Text Editor, Charts, Spreadsheet, and other commercial or ambiguously licensed components are absent from the lock and bundle.
The ordinary Grid, Combo Box, Multi-Select Combo Box, date and time pickers, Upload, and supporting controls provide enough behavior for the evaluation without Pro features.

A production proposal still requires the repository's formal ASF dependency, source redistribution, NOTICE, and binary licensing process rather than relying only on npm metadata.

## Integrity and vulnerabilities

`package-lock.json` records npm integrity hashes for every archive.
`verify.mjs` checks direct versions, Vaadin runtime licenses, prohibited packages, generated checksums, and unknown license fields.
`npm audit --omit=dev --json` reported zero info, low, moderate, high, or critical vulnerabilities for the frozen closure.

Updates require an explicit lock refresh, dependency and license diff, vulnerability review, selective rebuild, checksum review, headless regression suite, CSP integration check, and Maven package verification.

## Maven packaging proof

The analysis JAR built successfully with Maven and contains:

- One broad candidate asset.
- Seven route-split entry or shared-chunk assets.
- Fourteen direct-package license files.

The JAR contains 320,248 bytes and requires no browser runtime CDN.
Generated assets live beneath `META-INF/resources/causeway-vaadin-analysis`; licenses live beneath `META-INF/licenses/vaadin-analysis`.
The analysis module is not part of the repository reactor and does not affect production artifacts.

A supported build should invoke pinned npm acquisition and selective generation from Maven, verify generated hashes, and package one chosen delivery strategy rather than both analysis variants.

## Development-mode and usage-statistics code

Vaadin's runtime closure includes Apache-2.0 development-mode detection and usage-statistics modules.
The minified browser run selected production behavior and made no external request, but the broad bundle retains the usage endpoint and Flow-detection strings.
This does not violate the offline runtime check, but production governance should exclude or prove inert that code and add a regression assertion for external requests.

## CSP release implication

Same-origin Maven packaging does not by itself make Vaadin compatible with the current viewer CSP.
Four component-originated inline-style attempts were blocked by `style-src 'self'` in the real Petclinic check.
A release proposal must include a security-reviewed CSP design and test it before the candidate can pass production integration.

## Assessment

Pinned offline Maven delivery is feasible and the free-core license closure passes the analysis gate.
The remaining supply-chain work is conventional but larger than the current no-npm production model.
The strict-CSP policy, telemetry code, and frontend build become explicit release concerns rather than application runtime dependencies.
