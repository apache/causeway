# Reproducing the Vaadin reference-widget pilot

## Production frontend closure

From `viewers/webcomponents/foundation/vaadin-reference` run:

[source,shell]
----
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 npm ci --ignore-scripts --no-audit --no-fund
npm run build
npm run verify
npm audit --omit=dev
----

The expected bundle is 191,342 raw bytes, 48,684 gzip bytes, and SHA-256 `40ef3cecd641b14b7212759d45035991d9eb12550c00be64d2d7a786bf8f8a81`.
The closure contains 19 runtime packages and four CSP style hashes.

Do not run `npm run update-policy` for ordinary reproduction.
That command is reserved for an intentionally reviewed dependency or build update.

## Maven packaging

From the repository root run:

[source,shell]
----
mvn -pl viewers/webcomponents/foundation -am package
jar tf viewers/webcomponents/foundation/target/causeway-viewer-webcomponents-foundation-4.0.0-SNAPSHOT.jar \
  | grep vaadin-reference
----

Maven validation runs `verify.mjs` before packaging.
The JAR contains the same-origin candidate assets plus runtime license and third-party metadata.

The maintenance profile reacquires pinned inputs with package lifecycle scripts disabled and rebuilds before packaging:

[source,shell]
----
mvn -pl viewers/webcomponents/foundation -am \
  -Pregenerate-vaadin-reference-assets \
  package
----

## CSP and semantic browser evidence

From `openspec/changes/add-vaadin-reference-widget-pilot/evidence/csp-harness` run:

[source,shell]
----
PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 npm ci --ignore-scripts --no-audit --no-fund
npm run build
npm run matrix
npm run pilot
node audit-production.mjs
----

All browsers are headless.
`matrix` verifies current, exact-hash, element-inline, attribute-inline, nonce, and patched-nonce CSP behavior.
`pilot` exercises actual foundation semantic adapters and the packaged production bundle across desktop, narrow, light, dark, reduced-motion, and forced-color modes.

## Petclinic

Run Petclinic in a background process with a supported JDK:

[source,shell]
----
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -f viewers/webcomponents/pom.xml -Prun-sample-htmx-petclinic
----

Then run:

[source,shell]
----
cd openspec/changes/add-vaadin-reference-widget-pilot/evidence/csp-harness
node petclinic-production-check.mjs
----

For rollback evidence, restart Petclinic with:

[source,shell]
----
CAUSEWAY_VIEWER_WEBCOMPONENTS_HTMX_VAADIN_REFERENCE_WIDGETS=false \
  mvn -f viewers/webcomponents/pom.xml -Prun-sample-htmx-petclinic
----

Then run `node petclinic-rollback-check.mjs`.
Stop each background application after its check.

## Vanilla HTML sample

Start the sample in a background process:

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml -Prun-sample-html
----

Then run `node sample-html-production-check.mjs` from the evidence harness and stop the sample.

## Existing regression suites

[source,shell]
----
mvn -pl viewers/webcomponents/sample-html -am \
  -Dtest=SampleHtmlApplication_IntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test

mvn -pl viewers/webcomponents/sample-htmx-petclinic -am \
  -Pplaywright \
  -Dtest=PetClinicHtmxApplication_IntegTest,PetClinicHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
----

The second command runs Chromium headlessly and also executes foundation and HTMX browser tests through their Maven modules.

## Cleanup

[source,shell]
----
rm -rf viewers/webcomponents/foundation/vaadin-reference/node_modules
rm -rf openspec/changes/add-vaadin-reference-widget-pilot/evidence/csp-harness/node_modules
rm -rf openspec/changes/add-vaadin-reference-widget-pilot/evidence/csp-harness/generated
----

Generated production assets remain checked and verified repository inputs; do not delete `viewers/webcomponents/foundation/vaadin-reference/generated` during ordinary cleanup.
