# Reproducing the CSP gate

## Prerequisites

- Node.js 24 or another repository-supported Node release.
- npm access for the initial pinned acquisition.
- Google Chrome at the documented path or `PLAYWRIGHT_CHROMIUM_EXECUTABLE`.
- Maven and a supported JDK only for the optional real Petclinic journey.

All browser runs are headless.

## Isolation matrix

From `evidence/csp-harness` run:

```shell
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 npm ci --ignore-scripts --no-audit --no-fund
npm run build
npm run matrix
```

The server applies the exact viewer policy by response header and serves all fixture, script, stylesheet, and candidate resources from one temporary local origin.
The fixture has no inline script or fixture-owned inline style.

The run creates:

- `evidence/results/csp-matrix.json` with per-case violation events, console output, style hashes, source stacks, CSSOM mutations, overlays, focus, overflow, and requests.
- `evidence/results/csp-matrix.md` with the policy summary.

Expected results are 60 `style-src-elem` violations under the exact current policy and zero violations across 24 cases under the four-hash policy.
Diagnostic element-inline and patched-nonce policies pass, while attribute-inline and unmodified-nonce policies fail.

## Real Petclinic journey

Start Petclinic in a background process with a supported JDK:

```shell
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -f viewers/webcomponents/pom.xml -Prun-sample-htmx-petclinic
```

In another terminal run:

```shell
cd openspec/changes/add-vaadin-reference-widget-pilot/evidence/csp-harness
node petclinic-check.mjs
```

The script intercepts only the disposable browser context's fetched document response to add the four exact style hashes and serves the selective candidate bundle through a same-origin Playwright route.
It changes no production source or running server resource.

The expected result is zero CSP violation, console error, page error, external request, Flow runtime, or page overflow while route and menu readiness remain intact.
Stop the background Petclinic process after the check.

## Clean up

```shell
rm -rf node_modules generated
```

The manifest, integrity lock, source, retained JSON, and Markdown are sufficient to reproduce the gate.
