# Reproducing input value semantics

Run commands from the repository root with JDK 25.

## Foundation codecs and interactions

```bash
cd viewers/webcomponents/foundation
node --test
```

The suite covers exact numeric grammar and boundaries, nullable Boolean, local and offset temporal precision, daylight-saving overlap, protected-value redaction, URL policy, custom codec registration, property reconciliation, action defaults, cancellation, and stale work.

## GraphQL marshallers

```bash
mvn -f viewers/graphql/pom.xml \
  -pl model -am \
  -Dtest=ScalarMarshallerBuiltInInputTest,ScalarMarshallerInputCapabilityTest,GraphQLValueScalarsTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
```

## GraphQL resource and protected-value policy

```bash
mvn -f viewers/graphql/pom.xml \
  -pl test -am \
  -Dtest=ResourceValuePolicy_IntegTest,ResourcePolicy_IntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
```

## Reference Application focused contract

```bash
mvn -f regressiontests/referenceapp/pom.xml \
  -Dtest=ReferenceAppHtmxApplication_IntegTest#exactDecimalPropertyMutationUsesAdvertisedStringAndRestoresFixture+nullableBooleanAndOffsetAndZonedTemporalActionsRoundTrip \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
```

## Inventory review

```bash
mvn -f regressiontests/referenceapp/pom.xml \
  -Dtest=ReferenceAppCapabilityInventoryTest,ReferenceAppCapabilityInventory_IntegTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dreferenceapp.inventory.update=true \
  test

git diff -- regressiontests/referenceapp/htmx/src/test/resources/referenceapp-capability-inventory.json
```

The accepted inventory contains 4,286 items, including 3,421 supported items and no remaining value-semantics `VIEWER_DEFECT` classification.

## Browser qualification

```bash
mvn -f regressiontests/referenceapp/pom.xml \
  -Preferenceapp-playwright \
  -Dtest=ReferenceAppHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test
```

```bash
mvn -f viewers/webcomponents/sample-htmx-petclinic/pom.xml \
  -Pplaywright \
  test
```

The full final gate also runs the existing Vaadin production CSP and rollback harness without changing its pinned bundle or CSP hashes.
