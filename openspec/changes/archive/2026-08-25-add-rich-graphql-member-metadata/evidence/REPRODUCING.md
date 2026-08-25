# Reproducing the qualification

Use Temurin JDK 25 for these commands in this checkout.

## Core metamodel and focused model tests

[source,shell]
----
mvn -f core/pom.xml -pl metamodel -am test
mvn -f core/pom.xml -pl metamodel -am install -DskipTests
mvn -f viewers/graphql/pom.xml -pl model -am test
----

## Metadata and compatibility integration tests

[source,shell]
----
mvn -f viewers/graphql/pom.xml -pl test -am \
  -Dtest=MemberMetadata_IntegTest,CollectionWindow_IntegTest,CollectionWindowConfiguration_IntegTest,Department_IntegTest,Staff_IntegTest,Staff_2_IntegTest,ApplicationEntry_IntegTest,ResourcePolicy_IntegTest,ResourceValuePolicy_IntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false test

mvn -f viewers/graphql/pom.xml -pl test -am \
  '-Dtest=*IntegTest,!Calculator_IntegTest,!EditableMementoViewModel_IntegTest' \
  -Dsurefire.failIfNoSpecifiedTests=false test
----

The two exclusions isolate pre-existing baseline failures unrelated to metadata.
`Calculator_IntegTest` expects a trailing `.000` in one offset timestamp while the current JDK formatter omits it.
`EditableMementoViewModel_IntegTest` currently reports an existing memento input-conversion error.
All other GraphQL integration tests pass together.

## Generated schema

[source,shell]
----
mvn -f viewers/graphql/pom.xml -pl test -am \
  -Dtest=PrintSchemaIntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
sha256sum viewers/graphql/test/src/test/resources/schema.gql
----

The accepted schema contains 783 object types, exactly one `RichMemberMetadata` definition, 341 wrapper metadata fields, 307,984 bytes, and SHA-256 `fae5f09107277d5cc1276f14f948d1a1eeb97be2f8c32445214df0be20e7f2ef`.

## Reference Application

[source,shell]
----
mvn -f viewers/graphql/pom.xml -pl viewer -am install -DskipTests
mvn -f regressiontests/referenceapp/pom.xml test
sha256sum regressiontests/referenceapp/htmx/src/test/resources/referenceapp-capability-inventory.json
----

The expected inventory SHA-256 is `75ef904a0d4fbc9c915c74866cdbd503743dab589f7525bbab126baf1eaa024a`.

## RAT and specification checks

[source,shell]
----
mvn -f core/pom.xml -pl metamodel apache-rat:check
mvn -f viewers/graphql/pom.xml -pl model apache-rat:check
mvn -f viewers/graphql/pom.xml -pl test apache-rat:check
mvn -f regressiontests/referenceapp/pom.xml apache-rat:check
openspec validate add-rich-graphql-member-metadata --strict
git diff --check
----
