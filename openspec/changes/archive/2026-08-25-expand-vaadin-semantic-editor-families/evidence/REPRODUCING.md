# Reproducing the evidence

Use Temurin JDK 25 for the current reactor qualification.

## Field closure

[source,shell]
----
cd viewers/webcomponents/foundation/vaadin-fields
npm ci --ignore-scripts --no-audit --no-fund
npm run build
npm run verify
npm run audit
node audit.mjs
CAUSEWAY_FIELD_AUDIT_OUTPUT=../../../../openspec/changes/expand-vaadin-semantic-editor-families/evidence/results node audit.mjs
----

`node audit.mjs --write-policy` is only for deliberate reviewed style-hash refresh.
`npm run update-policy` is only for deliberate reviewed bundle-checksum refresh.

## Foundation and HTMX

[source,shell]
----
node --test --test-concurrency=1 viewers/webcomponents/foundation/test
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am test
----

## Petclinic

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pplaywright test
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pplaywright \
  -Dcauseway.viewer.webcomponents.htmx.vaadin-reference-widgets=false \
  -Dcauseway.viewer.webcomponents.htmx.vaadin-field-families= test
----

## Reference Application

Install the current foundation and HTMX artifacts before launching the isolated copied application.

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am install -DskipTests
mvn -f regressiontests/referenceapp/pom.xml clean package
mvn -f regressiontests/referenceapp/pom.xml -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -f regressiontests/referenceapp/pom.xml -Preferenceapp-playwright \
  -Dcauseway.viewer.webcomponents.htmx.vaadin-reference-widgets=false \
  -Dcauseway.viewer.webcomponents.htmx.vaadin-field-families= \
  -Dtest=ReferenceAppHtmxPlaywrightTest -Dsurefire.failIfNoSpecifiedTests=false test
----

## Policy and repository checks

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx apache-rat:check
openspec validate expand-vaadin-semantic-editor-families --strict
openspec validate --specs --strict
git diff --check
----
