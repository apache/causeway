# Vaadin free-core candidate freeze

## Evaluation date and release

The package set was frozen on 2026-08-21 at Vaadin Web Components 25.2.8.
Every direct Vaadin package resolves from the `vaadin/web-components` repository and declares Apache-2.0.
The analysis uses direct browser ES modules and does not install Vaadin Flow.

## Direct package allowlist

- `@vaadin/button@25.2.8`
- `@vaadin/checkbox@25.2.8`
- `@vaadin/combo-box@25.2.8`
- `@vaadin/date-picker@25.2.8`
- `@vaadin/date-time-picker@25.2.8`
- `@vaadin/dialog@25.2.8`
- `@vaadin/grid@25.2.8`
- `@vaadin/multi-select-combo-box@25.2.8`
- `@vaadin/select@25.2.8`
- `@vaadin/tabs@25.2.8`
- `@vaadin/text-area@25.2.8`
- `@vaadin/text-field@25.2.8`
- `@vaadin/time-picker@25.2.8`
- `@vaadin/upload@25.2.8`

The exact integrity values and transitive versions are retained in `harness/package-lock.json` and summarized by `results/asset-verification.json`.
Selective entry points are listed in `harness/vaadin-entry.mjs`.

## Dependency closure

The complete analysis lock currently resolves 64 runtime and development package instances.
Installed package metadata contains 28 Apache-2.0, 30 MIT, 5 BSD-3-Clause, and 1 MPL-2.0 package instance with no unknown license fields.
The MPL-2.0 package is development-only `axe-core`; it is not part of the Vaadin production bundle.
All resolved `@vaadin/*` runtime packages declare Apache-2.0.

The runtime closure also includes Lit and Open WC support packages under permissive licenses.
A production proposal must generate a complete ASF dependency and NOTICE assessment rather than relying only on package metadata.

## Commercial exclusions

The candidate MUST NOT import Vaadin Grid Pro, Rich Text Editor, Charts, Spreadsheet, or any package whose metadata does not establish an approved free-core license.
Documentation examples that use commercial packages are comparison-only.
The ordinary Grid, Combo Box, Multi-Select Combo Box, date and time pickers, Upload, and base controls in this allowlist are the candidate under test.

## Browser and tooling policy

The analysis uses the repository machine's headless Chromium through Playwright 1.61.0.
It records the evaluated browser version with generated evidence.
No supported production browser floor is inferred solely from a successful current-Chromium run; any adoption proposal must state the browser policy for Vaadin 25.

The harness pins esbuild 0.28.2 for selective browser bundling and axe-core 4.10.3 for development-only accessibility evidence.
Browser runtime assets are served locally without CDN access.

## Development-mode and telemetry observation

The Vaadin closure includes `@vaadin/vaadin-development-mode-detector` and `@vaadin/vaadin-usage-statistics` under Apache-2.0.
The selective minified bundle still contains usage-statistics and Flow-detection strings, including the Vaadin usage endpoint, even though minification causes the detector to select production mode and the analysis does not install or initialize Flow.
The browser evidence must confirm that no telemetry or external Vaadin request occurs.
A production proposal must either prove the code is inert in supported builds or exclude it through a documented build configuration.

## Provenance and update mechanics

The package lock records npm integrity hashes for every archive.
The repository URL for the direct component packages is `https://github.com/vaadin/web-components.git`.
Updates require an intentional lock refresh, license and dependency closure comparison, selective bundle measurement, browser regression run, and Maven-packaging verification.

## Initial bundle observation

The first fourteen-component selective bundle is 527,772 raw bytes and 127,143 bytes gzip with SHA-256 `25755ef2396767e927270deeac700d2dc6038eac8c6e4536d05ecbdbe870fdf8`.
This is a broad fixture bundle rather than the recommended initial production slice.
Per-widget entry points and route-lazy chunks must be measured before any adoption recommendation.
