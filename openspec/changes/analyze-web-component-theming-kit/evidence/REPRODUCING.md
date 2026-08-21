# Reproducing the theming analysis

## Prerequisites

- Node.js 24 or another repository-supported Node version with native test and ES-module support.
- npm with network access for the initial pinned dependency acquisition.
- A Chromium browser supported by Playwright 1.61.0.
- Maven and Java 17 or newer for the real Petclinic integration check.

## Acquire and verify local assets

From `openspec/changes/analyze-web-component-theming-kit/evidence/harness` run:

```shell
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 npm ci --ignore-scripts --no-audit --no-fund
npm run build-webawesome
npm run verify-assets -- --write
npm run measure-assets -- --write
```

`npm ci` verifies the package-lock integrity values and recreates `node_modules` without executing package lifecycle scripts.
`verify-assets.mjs` checks exact direct versions and licenses, records lockfile integrity values, and hashes selected browser assets and license files.
`build-webawesome.mjs` creates pinned selective JavaScript and CSS bundles for the twelve Web Awesome components exercised by the fixture.
`measure-assets.mjs` records raw and gzip sizes for baseline, selective, browser-distribution, and candidate asset groups.

If Chromium is not already installed for Playwright 1.61.0, run:

```shell
npx playwright install chromium
```

Browser installation is a developer prerequisite and is not a production application dependency.

## Serve the harness

Run:

```shell
npm run serve
```

The default URL is `http://127.0.0.1:4173/`.
The built-in server exposes the repository root with path-containment checks and `Cache-Control: no-store` so local candidate and baseline assets can be compared without copying them or relying on a CDN.
Set `PORT` or `HOST` to override the local listener.

## Prototype URLs

- Current Causeway: `/openspec/changes/analyze-web-component-theming-kit/evidence/harness/prototype.html?candidate=baseline`.
- Bootstrap: `/openspec/changes/analyze-web-component-theming-kit/evidence/harness/prototype.html?candidate=bootstrap`.
- Web Awesome: `/openspec/changes/analyze-web-component-theming-kit/evidence/harness/prototype.html?candidate=webawesome`.
- Open Props and native primitives: `/openspec/changes/analyze-web-component-theming-kit/evidence/harness/prototype.html?candidate=openprops`.

Use `theme=light|dark`, `state=all|menu-open|prompt|responsive-nav`, and `motion=normal|reduce` query parameters to select deterministic evidence states.

## Generate browser evidence

Run:

```shell
node capture.mjs --write
```

The script starts the local server, launches Chromium, captures the required screenshot matrix, executes DOM and interaction assertions, records request and timing data, and writes retained evidence beneath `evidence/screenshots` and `evidence/results`.
Set `PLAYWRIGHT_CHROMIUM_EXECUTABLE` when a specific Chromium executable is required.

Run Lighthouse 13.1.0 in desktop and mobile modes for each candidate URL, save each JSON report as `evidence/lighthouse/<candidate>-<mode>/report.json`, and then run:

```shell
npm run summarize-lighthouse
```

The retained summary records category scores and every failed selector and explanation, while the reproducible raw reports can be discarded after summarization.

## Real-viewer integration check

Start the Petclinic application with the existing Maven profile and a supported JDK:

```shell
mvn -f viewers/webcomponents/pom.xml -Prun-sample-htmx-petclinic
```

With the application and harness servers running, execute:

```shell
npm run integrate-petclinic
```

The script records bounded candidate injection checks against `http://localhost:8080/htmx` without changing production source files.
It serves injected assets through Playwright interception under a same-origin analysis path so the real viewer's content security policy remains enforced.
The current production acceptance suite remains:

```shell
mvn -pl viewers/webcomponents/sample-htmx-petclinic -am \
  -Pplaywright \
  -Dtest=PetClinicHtmxApplication_IntegTest,PetClinicHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
```

## Cleanup

Remove acquired and generated local-only files with:

```shell
rm -rf harness/node_modules harness/reports
```

Pinned `package.json`, `package-lock.json`, harness source, retained screenshots, and JSON or Markdown evidence remain version-controlled.
No production Maven module or runtime asset is modified by this analysis harness.
