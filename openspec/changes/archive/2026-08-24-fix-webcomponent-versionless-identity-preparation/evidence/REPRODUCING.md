# Reproducing versionless identity evidence

Use Temurin 25 for the copied Reference Application reactor.
Install the local parent and support artifacts when they are not already present, using `-Dmaven.install.skip=false` as documented by the archived regression-suite evidence.

## Foundation tests

```shell
cd viewers/webcomponents/foundation
node --test
```

The focused coverage is in `identity-selection.test.mjs`, `object-context.test.mjs`, `interaction-command.test.mjs`, `collection.test.mjs`, and `graphql-client.test.mjs`.

## Reference Application integration

```shell
mvn -f regressiontests/referenceapp/pom.xml \
  -Dtest=ReferenceAppHtmxApplication_IntegTest#versionlessViewModelsAdvertiseIdentityAndSupportConcreteCollectionRows \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

This test uses separate one-type introspection requests for the versioned entity and two versionless view models, then executes the representative concrete collection-row query.

## Reference Application browser journey

Install the current viewer artifact before starting the copied launcher.

```shell
mvn -f viewers/webcomponents/pom.xml \
  -pl foundation,htmx -am -DskipTests install
mvn -f regressiontests/referenceapp/pom.xml \
  -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest#populatedPagedConfiguredPolymorphicAndRouteReplacedCollectionsRemainVisible \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

The deterministic paged concrete view-model collection must reach `ready` and render navigable object links.
The polymorphic target remains bounded and the composite bookmark retains its separate visible route-gap assertion.

## Full qualification

```shell
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am test
mvn -f viewers/graphql/pom.xml -pl model -am test
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pplaywright test
mvn -f regressiontests/referenceapp/pom.xml clean package
mvn -f regressiontests/referenceapp/pom.xml -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
openspec validate fix-webcomponent-versionless-identity-preparation --strict
git diff --check
```
