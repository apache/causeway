# Reproducing the Vaadin Web Component evaluation

## Prerequisites

- Node.js 24 or another repository-supported Node release.
- npm network access for the initial pinned acquisition.
- Google Chrome or Chromium compatible with Playwright 1.61.0.
- Maven and JDK 17 or newer for packaging and the optional real Petclinic check.

All browser commands are headless and do not open a visible browser window.

## Acquire, build, and verify

From `openspec/changes/evaluate-vaadin-web-components-for-graphql-viewer/evidence/harness` run:

```shell
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 npm ci --ignore-scripts --no-audit --no-fund
npm run build
npm run verify
npm run measure
npm run audit-security
```

`npm ci` verifies the committed integrity lock without executing package lifecycle scripts.
`build.mjs` creates the broad analysis bundle, route-split reference, Grid, and field entries, shared chunks, metafile, and direct-package license copies.
`verify.mjs` checks exact Vaadin versions, Apache-2.0 runtime licensing, commercial-package exclusions, unknown licenses, and generated checksums.
`measure.mjs` records current-viewer assets, installed sources, broad and split bundles, individual files, and recursive entry closures.

## Headless browser evidence

Run:

```shell
npm run capture
```

The script starts the local server in the background for the duration of the run, launches headless Chromium, executes keyboard and programmatic journeys, runs axe-core, captures screenshots, records five timing runs, and then stops its server.
Set `PLAYWRIGHT_CHROMIUM_EXECUTABLE` when Chrome is not installed at the default macOS path.

The retained output is:

- `evidence/results/browser-evidence.json` and `.md`.
- Six screenshots beneath `evidence/screenshots`.

## Maven packaging proof

Run:

```shell
mvn -f ../packaging/pom.xml clean package
node verify-packaging.mjs --write
```

The verification checks one broad asset, route-split assets, fourteen direct-package licenses, JAR size, and absence of a runtime CDN requirement.

## Real Petclinic integration

Start Petclinic with a supported JDK in a background terminal or process:

```shell
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -f viewers/webcomponents/pom.xml -Prun-sample-htmx-petclinic
```

From the harness directory run:

```shell
npm run integrate-petclinic
```

The integration is headless.
It intercepts only same-origin `/__analysis/` requests in a disposable Playwright context, injects the selective bundle and probe, verifies route and menu behavior, captures one screenshot, classifies the expected current-CSP style failures, and modifies no server or production file.

The current CSP incompatibility is retained evidence rather than a reason to weaken the running sample policy.

## Production acceptance baseline

No production source is changed by the analysis.
The existing checks remain:

```shell
mvn -pl viewers/webcomponents/sample-htmx-petclinic -am \
  -Pplaywright \
  -Dtest=PetClinicHtmxApplication_IntegTest,PetClinicHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
```

## Cleanup

Local acquired and transient files can be removed with:

```shell
rm -rf node_modules reports ../packaging/target
```

The package manifest, package lock, harness source, generated selective assets, licenses, screenshots, JSON, Markdown, and Maven proof remain reproducible archive evidence.
