# Reproducing the qualification

Use Temurin JDK 25 in this checkout.

## Run Petclinic interactively

From the repository root:

[source,shell]
----
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3-tem"
export PATH="$JAVA_HOME/bin:$PATH"

mvn -f viewers/webcomponents/pom.xml \
  -Prun-sample-htmx-petclinic
----

Open `http://localhost:8080/htmx` for the HTML-authored HTMX pages.
Open `http://localhost:8080/wicket/` for the side-by-side Wicket viewer and sign in with username `sven` and password `pass`.
Stop the application with kbd:[Ctrl+C].

To exercise explicit native editors instead of the default Vaadin adapters, run:

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml \
  -Prun-sample-htmx-petclinic \
  -Dcauseway.viewer.webcomponents.htmx.editor-toolkit=native
----

## Core and sample tests

[source,shell]
----
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.3-tem"
export PATH="$JAVA_HOME/bin:$PATH"

mvn -f viewers/webcomponents/pom.xml \
  -pl htmx,sample-htmx-petclinic -am test

node --test viewers/webcomponents/htmx/test/route-policy.test.mjs
----

## Default and native browser matrices

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml \
  -pl sample-htmx-petclinic -am \
  -Pplaywright test

mvn -f viewers/webcomponents/pom.xml \
  -pl sample-htmx-petclinic -am \
  -Pplaywright \
  -Dcauseway.viewer.webcomponents.htmx.editor-toolkit=native \
  test
----

## Ordinary non-executable packaging

[source,shell]
----
mvn -f viewers/webcomponents/pom.xml \
  -pl sample-htmx-petclinic -am \
  clean package -DskipTests \
  -Dspring-boot.repackage.skip=true

jar tf viewers/webcomponents/sample-htmx-petclinic/target/\
causeway-viewer-webcomponents-sample-htmx-petclinic-4.0.0-SNAPSHOT.jar
----

The listing must contain all four `META-INF/causeway/webcomponents/pages/*.html` resources, all retained layout and column-order resources, and no `BOOT-INF` or `PetClinicHomeFragmentFactory` entry.

## Compatibility, licensing, and specifications

[source,shell]
----
mvn -f viewers/graphql/pom.xml -pl model -am test
mvn -f viewers/webcomponents/htmx/pom.xml apache-rat:check
mvn -f viewers/webcomponents/sample-htmx-petclinic/pom.xml apache-rat:check
openspec validate add-html-authored-htmx-pages --strict
git diff --check
----
