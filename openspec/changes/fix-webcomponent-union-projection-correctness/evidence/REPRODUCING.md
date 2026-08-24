# Reproducing union projection evidence

Use Temurin 25 for the GraphQL and copied Reference Application reactors.
Install current GraphQL model and web-component artifacts before running the separate Reference Application reactor.

## GraphQL model

```shell
mvn -f viewers/graphql/pom.xml -pl model -am test
mvn -f viewers/graphql/pom.xml -pl model -am -DskipTests install
```

`GraphQLTypeRegistryUnionTest` proves possible-type merging and deduplication by generated union name.

## Foundation

```shell
cd viewers/webcomponents/foundation
node --test
```

Focused selection, interaction, and collection coverage verifies inline-fragment rendering, validation, direct expansion, broad probe and replay, mixed concrete rows, missing columns, unadvertised typenames, changed replay types, windows, caching, cancellation, and hydration.

## Reference Application integration

```shell
mvn -f regressiontests/referenceapp/pom.xml \
  -Dtest=ReferenceAppHtmxApplication_IntegTest#polymorphicCollectionsCompleteUnionMembershipAndAcceptFragments \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

The test records the pinned 28-member `ValueHolder` union, valid typename and concrete-fragment operations, invalid direct metadata selection, and concrete `typeOf` behavior.

## Reference Application browser

```shell
mvn -f viewers/webcomponents/pom.xml \
  -pl foundation,htmx -am -DskipTests install
mvn -f regressiontests/referenceapp/pom.xml \
  -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest#populatedPagedConfiguredPolymorphicAndRouteReplacedCollectionsRemainVisible \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

The `ActionChoicesFromPage.objects` collection must reach `ready`, expose concrete object links, and retain a fragment selection for `rich__demo_ActionChoicesFromEntity`.
The declared `CollectionTypeOfPage.children` collection remains concrete and ready, while unreadable `otherChildren` remains a bounded local error.

## Full qualification

```shell
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am test
mvn -f viewers/graphql/pom.xml -pl model -am test
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pplaywright test
mvn -f regressiontests/referenceapp/pom.xml clean package
mvn -f regressiontests/referenceapp/pom.xml -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
openspec validate fix-webcomponent-union-projection-correctness --strict
git diff --check
```
