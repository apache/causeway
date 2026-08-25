# Reproducing the qualification

Use Temurin JDK 25 in this checkout.

## Foundation and HTMX suites

[source,shell]
----
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3-tem"
export PATH="$JAVA_HOME/bin:$PATH"

(cd viewers/webcomponents/foundation && node --test)

mvn -f viewers/webcomponents/pom.xml \
  -pl sample-htmx-petclinic -am test
----

The Maven run executes the complete foundation Node suite and HTMX route-policy suite before the Java integration tests.

## Default and native browser matrices

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml \
  -pl sample-htmx-petclinic -am \
  -Pplaywright \
  -Dtest=PetClinicHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  test

mvn -f viewers/webcomponents/pom.xml \
  -pl sample-htmx-petclinic -am \
  -Pplaywright \
  -Dtest=PetClinicHtmxPlaywrightTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dcauseway.viewer.webcomponents.htmx.editor-toolkit=native \
  test
----

## Packaging, compatibility, and licensing

[source,shell]
----
mvn -f viewers/graphql/pom.xml -pl model -am test
mvn -f viewers/webcomponents/foundation/pom.xml apache-rat:check
mvn -f viewers/webcomponents/sample-htmx-petclinic/pom.xml apache-rat:check

mvn -f viewers/webcomponents/pom.xml \
  -pl sample-htmx-petclinic -am \
  clean package -DskipTests \
  -Dspring-boot.repackage.skip=true

jar tf viewers/webcomponents/foundation/target/\
causeway-viewer-webcomponents-foundation-4.0.0-SNAPSHOT.jar \
  | grep member-composition.mjs

openspec validate add-declarative-associated-action-composition --strict
git diff --check
----

The foundation artifact must contain `META-INF/resources/causeway-webcomponents/member-composition.mjs`.
The ordinary sample artifact must contain no `BOOT-INF` directory.
