# Wicket collection observations validation

Validated on 2026-10-10 on `CAUSEWAY-4059-v4`, following proposal commit `d13809e0251` and Wicket region archive `aa9c7e80db7`.

## Main adaptations

- Immutable Wicket configuration records expose hierarchical detail (default MEMBERS) and max-spans-per-request (default 0/unlimited). Invalid detail and negative budgets fail startup; generated Spring metadata contains both properties and defaults.
- A request-local coordinator admits all semantic Wicket preparation/render boundaries cumulatively, with no refund and an overflow-safe structural reserve. The enclosing Apache Wicket Request Cycle and other observation categories remain outside admission. Omitted callbacks open no scopes, execute normally and contribute collection summaries.
- ParentedCollectionPanel encloses descendant before-render preparation; initial construction remains in page preparation/current ancestry. Main's relocated DataRow-based CausewayAjaxDataTable exposes table/header/body/footer and row callbacks. Its existing index-based reuse strategy is wrapped rather than replaced; pagination/provider calls remain unchanged.
- Logical parented property cells and eligible action controls participate. Table actions retain their original Where/visibility semantics and are admitted only when rendered inside a table with canonical parented collection identity. Standalone tables, parameters, titles and decorative widgets remain excluded.
- Serializable descriptors hold static identifiers only. Tracker/coordinator cleanup attempts every outstanding closure in reverse order, preserves work failures, and clears transient request counters, frames and scopes. Collection summaries count actual participating callbacks with monotonic timing and saturating arithmetic.
- Both tracing owners reuse existing production integration. No tracing SDK, exporter, applib API, entry-span naming or general span filtering is added. M3/how-to guidance preserves the existing Boot-first/agent flow and uses environment overrides with the existing launcher.

## Commands and evidence

Java 25.0.4-tem, Maven 3.9.13, Wicket 10.11.0 and the existing Boot/Micrometer/OTel dependency management. Commands use the configured Java 25 JAVA_HOME and local Maven cache.

```sh
mvn -o -pl core/config \
  -Dtest=CausewayConfiguration_WicketObservation_Test,CausewayObservationNamingTest test

mvn -o -pl viewers/wicket/ui,viewers/wicket/ui-test,viewers/wicket/viewer \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install

mvn -o -f regressiontests/tracing-compatibility/pom.xml test
```

Configuration/default/invalid binding and naming tests passed all eight tests.

The Wicket build/install passed 107 executed tests, with six existing skips (93 UI tests including three skips; 20 viewer tests including three skips). New coverage includes all detail levels, smallest/unlimited/largest budgets, structural reservation, no refunds, multiple collections, request reset, omitted-parent context, collection summaries, failure/closure-error cleanup, real table full-page/Ajax callbacks, unchanged pagination/provider iteration, index-based row reuse and table-action eligibility. Existing preparation/render serialization and request cleanup regressions remain passing.

The complete tracing/JPA export suite passed all 14 tests.

The exported fixture uses actual main table population, rendering and Ajax callbacks with controlled provider/column collaborators. It exports collection/table/row/member boundaries through production Boot and agent configuration, checks full static identities, reduced-detail summaries, row/body ancestry, finite budgets and unchanged domain/JPA/JDBC work. Deliberate rendering failure is followed by a successful request on the same worker. Budget assertions group by framework interactions because the agent also instruments synthetic servlet boundaries inside WicketTester requests.

Both modified AsciiDoc pages rendered with the installed Asciidoctor core. Strict OpenSpec validation and git diff --check passed.

## Limits

The table fixtures exercise the actual CausewayAjaxDataTable, its row reuse and production observation helpers, with controlled data providers/columns and standard Wicket toolbars. They do not render a complete live Petclinic DomainObjectPage with persistence-backed collections and all Causeway toolbars. Policy tests verify real PropertyModel/ActionModel metadata; exported column fixtures use controlled logical member components. Existing WicketTester failure handling does not establish end-to-end servlet error-page ancestry. No interactive browser verification or full repository reactor test run was performed.

## Archive checkpoint

Implementation committed as `b8383bfb1c2`. Fresh pre-archive validation passed the core configuration/Wicket build/install with 47 focused tests, all fourteen tracing/JPA export tests, both documentation renders, strict change validation and git diff --check. The earlier full Wicket run passed 107 executed tests with six existing skips. Seven collection requirements were added and four region requirements updated, retaining the existing static identity and cleanup requirements.
