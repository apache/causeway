# Reproducing paged reference autocomplete

Use Temurin 25 for Maven commands.
Install the changed core configuration, GraphQL model, and web-component artifacts before running the separate Reference Application reactor.

## Configuration and GraphQL model

```shell
mvn -pl core/config -am test
mvn -f viewers/graphql/pom.xml -pl model -am test
mvn -f viewers/graphql/pom.xml -pl model -am -DskipTests install
```

The model suite covers configuration validation and existing schema behavior.
The Reference Application integration below proves generated property-independent action-parameter fields, type metadata, all window boundaries, legacy compatibility, ordering, and non-disclosing errors against the real metamodel.

## Foundation

```shell
cd viewers/webcomponents/foundation
node --test
cd ../../..
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am test
```

Focused tests cover targeted window and item discovery, advertised selection, immutable normalization, property, object-action, and service-action execution, dependent arguments, legacy fallback, native search handling, Vaadin later-page callbacks, and stale-filter rejection.

## Reference Application GraphQL

```shell
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am -DskipTests install
mvn -f regressiontests/referenceapp/pom.xml \
  -Dtest=ReferenceAppHtmxApplication_IntegTest#actionReferenceAutocompleteExposesBoundedServerWindowsAndLegacyCompatibility \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

## Reference Application candidate and native browser modes

```shell
mvn -f regressiontests/referenceapp/pom.xml \
  -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest#menusChoicesAutocompleteCancellationAndRouteDisposal \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -f regressiontests/referenceapp/pom.xml \
  -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest#menusChoicesAutocompleteCancellationAndRouteDisposal \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dcauseway.viewer.webcomponents.htmx.vaadin-reference-widgets=false test
```

## Full qualification

```shell
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pplaywright test
mvn -f regressiontests/referenceapp/pom.xml clean package
mvn -f regressiontests/referenceapp/pom.xml -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
openspec validate add-paged-graphql-reference-autocomplete --strict
git diff --check
```
