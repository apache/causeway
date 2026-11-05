# Wicket region observations validation

Validated on 2026-10-09, branch `CAUSEWAY-4059-v4`. Planning commit `cfce3c3b33d` follows main merge `a073787d229` (semantic member naming).

## Main adaptations

- Maintenance EntityPage maps to main DomainObjectPage, which builds its tree during initialization. Preparation starts after the already-required object/visibility lookup supplies static type metadata; it encloses layout/component construction, configuration and descendant before-render work, and closes before markup rendering. Reused pages start a fresh configuration phase; Ajax subtree updates do not manufacture a page-render parent.
- Maintenance scalar/collection components map to AttributePanel/PropertyModel and ParentedCollectionPanel. Regular object properties and object-form action controls are included; table/parameter widgets and associated-parameter links are excluded. Prompt instrumentation is attached to main's three-argument ActionParametersPanel constructor.
- Serializable descriptors retain only static strings/region kind. Active closures and request references are transient. Preparation and render scopes share request-local LIFO ownership; callback, exception, detach and end-of-request paths are idempotent. Unwinding continues after a failed closure and preserves the original work failure.
- Main's existing commons ObservationClosure is reused. No new registry, tracing handler, SDK/exporter, applib API, user flag or launcher scenario is added. The Wicket UI module exports its observation helpers and declares its existing Micrometer dependency on the module path; Wicket tester is a BOM-managed test dependency.

## Build and test checks

Java 25.0.4-tem, Maven 3.9.13, Wicket 10.11.0, Boot 4.2.0-M1, Micrometer 1.18.0-M1, Micrometer Tracing 1.8.0-M1, OTel 1.64.0 and agent 2.31.1.

Commands use the configured Java 25 JAVA_HOME and the existing local Maven cache:

```sh
mvn -o -pl viewers/wicket/ui-test,viewers/wicket/viewer -am   -Dtest=WicketRenderObservationTest,ActionPromptObservationTest,TelemetryRequestCycleTest,CausewayObservationNamingTest   -Dsurefire.failIfNoSpecifiedTests=false   -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install

mvn -o -pl viewers/wicket/ui-test,viewers/wicket/viewer   -Dmaven.source.skip=true -Dmaven.javadoc.skip=true test

mvn -o -f regressiontests/tracing-compatibility/pom.xml test
```

The focused build/install passed, including five naming tests. The final full Wicket UI/viewer run passed 93 executed tests with six existing skips (79 UI tests including 3 skips; 20 viewer tests including 3 skips). Its new coverage includes 21 region/lifecycle tests, one real prompt-panel attachment test and three request-cycle tests, including production end-of-request cleanup order.

The complete exported-trace/JPA suite passed 12 tests. Real Wicket component initialization, full rendering and Ajax callbacks exercise production observation helpers, RequestCycle2, TelemetryStartHandler and WebRequestCycleForCauseway end cleanup. HTTP/framework/interaction/region/member/JPA/JDBC ancestry, casing, full signatures, one region per render, errors, failure-followed-by-success, inactive profiles and absent exporters are checked. Boot's standard receiver handler is bound to the fixture's real JDK HTTP request with SERVER kind; agent mode owns automatic JDK HTTP/JDBC instrumentation. No fixture-owned tracing SDK or handler is installed.

Both modified AsciiDoc pages rendered successfully using Antora's installed Asciidoctor core. Petclinic's logical owner type, name property, identity fieldset, visits contribution and updateName(String) prompt were checked against source. Guidance is integrated into the existing Boot-first/agent flow without comparison tables or configuration flags.

Strict OpenSpec validation and `git diff --check` passed.

## Limits

Lifecycle tests use lightweight Wicket components/pages and the production helpers; exported fixtures use controlled business/model collaborators and actual framework/member/JPA/SQL work. They characterize the hooks used by main's DomainObjectPage but do not constitute an end-to-end render of that page with a live Petclinic persistence context. No interactive browser verification or full reactor test run was performed. Table internals/detail/budgets, root display naming and rendering instrumentation for other viewers remain separate roadmap items.

## Archive checkpoint

Implementation committed as `895782abf6d`. Fresh pre-archive checks passed the focused build/install (30 tests: 5 naming, 22 UI/region/prompt and 3 request-cycle), full Wicket UI/viewer tests (93 executed, 6 existing skips), all 12 exported-trace/JPA tests and rendering of both documentation pages. Strict change validation passed; six requirements were synced during archive.
