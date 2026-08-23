# Reproducing the Reference Application regression baseline

Run all commands from the repository root.
Use JDK 25 for the commands recorded in this baseline.
The copied corpus is entirely local and none of these commands downloads source from the upstream Reference Application repository.

## Verify the copied corpus offline

```bash
mvn -f regressiontests/referenceapp/pom.xml \
  -pl domain -am \
  -Dtest=CorpusIntegrityTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
```

The verifier compares every retained path with `regressiontests/referenceapp/source-manifest.sha256`.
It fails for missing, additional, or changed copied files.

## Targeted compilation and ordinary regression tests

```bash
mvn -f regressiontests/referenceapp/pom.xml clean package
```

This command compiles the viewer-neutral domain, JPA support, and launcher modules and runs provenance, startup, fixture, structural-resource, and inventory tests.
The ordinary package uses a lightweight launcher JAR and does not activate Playwright or Spring Boot repackaging.

```bash
mvn -f regressiontests/pom.xml \
  -pl referenceapp -am \
  -DskipTests \
  package
```

This command verifies integration with the affected regression-test reactor.

## Regenerate and review the capability inventory

```bash
mvn -f regressiontests/referenceapp/pom.xml \
  -Dtest=ReferenceAppCapabilityInventory_IntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dreferenceapp.inventory.update=true \
  test

git diff -- \
  regressiontests/referenceapp/htmx/src/test/resources/referenceapp-capability-inventory.json
```

Inventory regeneration is a review operation rather than an automatic acceptance mechanism.
Unexpected additions, removals, count changes, identifier changes, or classification changes must be explained before the baseline is retained.

## Run the secured Reference Application

```bash
mvn -f regressiontests/referenceapp/htmx/pom.xml \
  -Prun-referenceapp-htmx \
  -Dspring-boot.run.profiles=demo-jpa
```

The generic viewer is available at `http://localhost:8080/htmx`.
The rich GraphQL endpoint is available at `http://localhost:8080/graphql`.
The Wicket comparison viewer is available at `http://localhost:8080/wicket/`.
The deterministic demonstration account is `sven` with password `pass`.

## Run diagnostic startup

```bash
mvn -f regressiontests/referenceapp/htmx/pom.xml \
  -Prun-referenceapp-htmx \
  -Dspring-boot.run.profiles=demo-jpa \
  -Dspring-boot.run.arguments="--logging.level.org.apache.causeway=DEBUG --logging.level.org.eclipse.persistence=INFO"
```

Diagnostic startup deliberately retains the same security and fixture model as the accepted runtime.
A separate security-bypass mode was not required to diagnose this baseline and is therefore not advertised as an acceptance path.

## Run the opt-in headless browser suite

```bash
mvn -f regressiontests/referenceapp/pom.xml \
  -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
```

The profile starts the application on a random port and launches Chromium headlessly by default.
Use `-Dplaywright.headless=false` only for local diagnosis.
A preinstalled Chromium executable can be selected with `-Dplaywright.chromium.executable=/absolute/path/to/chromium`.

## Build the optional executable launcher

```bash
mvn -f regressiontests/referenceapp/pom.xml \
  -Ppackage-referenceapp-htmx \
  -DskipTests \
  package
```

Only this profile runs `spring-boot:repackage` and creates the large executable launcher.
The ordinary reactor package remains lightweight.

## RAT, OpenSpec, and cleanup

```bash
mvn -f regressiontests/referenceapp/pom.xml apache-rat:check
openspec validate add-referenceapp-webcomponent-regression-suite --strict
```

```bash
mvn -f regressiontests/referenceapp/pom.xml clean
rm -rf regressiontests/referenceapp/htmx/playwright-report \
       regressiontests/referenceapp/htmx/test-results
```

The cleanup command removes generated build and browser output without touching the copied source baseline.
