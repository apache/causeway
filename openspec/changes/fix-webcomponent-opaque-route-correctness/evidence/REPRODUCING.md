# Reproducing opaque-route correctness

Use Temurin 25 for Maven commands.
Install current foundation and HTMX artifacts before running the separate Reference Application reactor.

## Route codecs and controller

```shell
cd viewers/webcomponents/htmx
node --test
cd ../../..
mvn -f viewers/webcomponents/pom.xml -pl htmx -am \
  -Dtest=HtmxRouteCodecTest,HtmxViewerControllerTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

The tests cover shared canonical vectors, long opaque segments, exact boundaries, percent-encoded expansion, Unicode, reserved punctuation, malformed values, context paths, full pages, fragments, history restoration, and non-disclosing invalid routes.

## Reference Application integration

```shell
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am -DskipTests install
mvn -f regressiontests/referenceapp/pom.xml \
  -Dtest=ReferenceAppHtmxApplication_IntegTest#compositeViewModelIdentityRoundTripsThroughTheBoundedCanonicalRoute \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

The test obtains the authoritative identifier through GraphQL, records only safe lengths, round-trips it through `HtmxRouteCodec`, directly loads the canonical route, and reconstructs the same GraphQL identity and composite value.

## Reference Application browser

```shell
mvn -f regressiontests/referenceapp/pom.xml \
  -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest#scalarTemporalUrlProtectedAndCustomValueFamiliesRemainTyped \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

The journey dispatches the public semantic navigation event, waits for `demo.CompositeValuesPage`, compares the exact context identifier, verifies composite content, traverses back and forward, and retains malformed-route rejection.

## Full qualification

```shell
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am test
mvn -f viewers/webcomponents/pom.xml -pl sample-htmx-petclinic -am -Pplaywright test
mvn -f regressiontests/referenceapp/pom.xml clean package
mvn -f regressiontests/referenceapp/pom.xml -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
openspec validate fix-webcomponent-opaque-route-correctness --strict
git diff --check
```
