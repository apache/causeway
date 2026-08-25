# Reproducing the qualification

Use Temurin JDK 25 for the Reference Application commands in this checkout.

## Foundation, HTMX, and vanilla sample

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx,sample-html -am test
----

## Petclinic default and native modes

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pplaywright test
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pplaywright \
  -Dcauseway.viewer.webcomponents.htmx.editor-toolkit=native test
----

## Reference Application

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am install -DskipTests
mvn -f regressiontests/referenceapp/pom.xml clean package
mvn -f regressiontests/referenceapp/pom.xml -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -f regressiontests/referenceapp/pom.xml -Preferenceapp-playwright \
  -Dcauseway.viewer.webcomponents.htmx.editor-toolkit=native \
  -Dtest=ReferenceAppHtmxPlaywrightTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -f regressiontests/referenceapp/pom.xml \
  -Dtest=ReferenceAppCapabilityInventory_IntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
sha256sum regressiontests/referenceapp/htmx/src/test/resources/referenceapp-capability-inventory.json
----

## Deterministic closure and browser policy gates

[source,shell]
----
cd viewers/webcomponents/foundation/vaadin-reference
npm run verify
npm run audit

cd ../vaadin-fields
npm run verify
npm run audit
node audit.mjs

cd ../../../../openspec/changes/archive/2026-08-22-add-vaadin-reference-widget-pilot/evidence/csp-harness
node run-pilot.mjs
----

## RAT and OpenSpec

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml -pl foundation apache-rat:check
mvn -f viewers/webcomponents/pom.xml -pl htmx apache-rat:check
mvn -f viewers/webcomponents/pom.xml -pl sample-html apache-rat:check
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic apache-rat:check
mvn -f regressiontests/referenceapp/pom.xml apache-rat:check
openspec validate make-vaadin-default-for-webcomponent-viewer --strict
git diff --check
----
