# Reproducing action dispatch qualification

Use JDK 25 and the repository Maven wrapper or Maven installation configured for this checkout.
The Reference Application modules intentionally skip installation by default, so local standalone launch preparation must explicitly override that policy.

```shell
mvn -f regressiontests/pom.xml -N -Dmaven.install.skip=false install
mvn -f regressiontests/referenceapp/pom.xml -N -Dmaven.install.skip=false install
mvn -f regressiontests/referenceapp/pom.xml -DskipTests -Dmaven.install.skip=false install
```

Run the foundation operation-shape matrix:

```shell
node --test viewers/webcomponents/foundation/test/action-dispatch.test.mjs
node --test viewers/webcomponents/foundation/test/application-menu-client.test.mjs
```

Run the complete foundation suite:

```shell
cd viewers/webcomponents/foundation
node --test
```

Run the focused Reference Application browser journey:

```shell
mvn -f viewers/webcomponents/pom.xml -pl foundation,htmx -am -DskipTests install
mvn -f regressiontests/referenceapp/pom.xml \
    -Preferenceapp-playwright \
    -Dtest=ReferenceAppHtmxPlaywrightTest#menusChoicesAutocompleteCancellationAndRouteDisposal \
    -Dsurefire.failIfNoSpecifiedTests=false \
    test
```

The first service action must navigate to `demo.ActionChoices` without opening an error prompt.
The parameterized `selectTvCharacter` mutation must accept one advertised choice, close its prompt, and retain the authoritative route.
The shell and unrelated routes must continue to make zero Vaadin candidate-bundle requests.

For targeted introspection, start the application with `run-referenceapp-htmx`, wait until a small `__type` query succeeds, and inspect only the named service, object, action-wrapper, result, and mutation types listed in `operation-shape-matrix.md`.
Broad schema introspection is neither required nor used by the viewer.
