# Production viewer isolation verification

## Source and configuration boundary

All implementation files are confined to `regressiontests/referenceapp/`, the `regressiontests/pom.xml` aggregator entry, and this OpenSpec change.
`git diff --name-only` reports no path beneath `viewers/`.
It also reports no root `pom.xml`, BOM, production configuration, production route, or production browser-asset change.
The Reference Application modules are install- and deploy-skipped non-release regression modules.
No production module depends on them.

## Production package verification

The unchanged production modules package successfully with:

```bash
mvn -f viewers/webcomponents/pom.xml \
  -pl foundation,htmx \
  -am \
  -DskipTests \
  package
```

The final run completed with `BUILD SUCCESS` under JDK 25.
Because no production source, POM, resource, configuration, route, or browser asset is modified, the production dependency graph and package inputs are unchanged.

## Regression launcher package boundary

The ordinary regression package creates these lightweight launcher artifacts:

| Artifact | Bytes |
| --- | ---: |
| `causeway-regressiontests-referenceapp-domain-4.0.0-SNAPSHOT.jar` | 4,898,739 |
| `causeway-regressiontests-referenceapp-support-jpa-4.0.0-SNAPSHOT.jar` | 38,033 |
| `causeway-regressiontests-referenceapp-htmx-4.0.0-SNAPSHOT.jar` | 23,909 |

The inherited Spring Boot `repackage` execution is disabled in ordinary packaging.
The opt-in `package-referenceapp-htmx` profile was separately verified and produced a 333,618,156-byte executable launcher while retaining the 23,909-byte original JAR.
A subsequent ordinary clean package restored the 23,909-byte lightweight JAR and produced no `.jar.original` file.

## Runtime isolation

The regression-only `/htmx`, `/graphql`, and `/wicket/` routes exist solely in `ReferenceAppHtmxApplication` under `regressiontests/referenceapp/htmx`.
No production default route or viewer property changes.
The regression launcher enables Vaadin reference widgets only in its own `application.properties` and therefore does not change the production default-selection policy.
The copied corpus adds no CDN, external browser asset, Flow runtime, telemetry, or production dependency.
