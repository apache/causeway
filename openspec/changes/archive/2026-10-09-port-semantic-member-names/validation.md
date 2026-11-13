# Semantic member naming validation

Validated 2026-10-09 on CAUSEWAY-4062-v4, following planning commit `41dfa69342c` and current-action archive `c04587a7bcc`.

## Implementation

Core member display names use act/prop/coll, preserve casing and have a 50-character display budget. Association categories come from the actual mixed-in association linkage, not method spelling; missing linkage falls back to action instrumentation. Full domain-facing category-specific identifiers retain declared parameter signatures. Existing generic member metadata retains physical invocation identity.

The existing invocation body, interaction DTO and command paths remain unchanged. No applib APIs, tracing handler, registry/SDK replacement or configuration flags were added. Wicket naming remains a later roadmap item.

## Checks

Java 25.0.4-tem and Maven 3.9.13; dependencies use Boot 4.2.0-M1, Micrometer 1.18.0-M1, Micrometer Tracing 1.8.0-M1, OpenTelemetry 1.64.0 and Java agent 2.31.1.

- `mvn -o -pl core/config,core/runtimeservices -am -Dtest=CausewayObservationNamingTest,MemberObservationPolicyTest,MemberExecutorServiceDefaultTest,InteractionServiceObservationTest,CausewayObservationAutoConfigurationTest,CausewayAgentObservationAutoConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install`: passed 43 tests. Covers display compaction/collisions, actual association selection, category/fallback metadata, execution behavior, interaction failure cleanup and application-provided registry/tracer ownership.
- `mvn -o -f regressiontests/pom.xml -pl interact -am -Dmaven.source.skip=true -Dmaven.javadoc.skip=true test`: passed 77 executed tests, with 2 existing disabled tests. Includes current-action restoration, nested failures, mixin receiver/domain identity and wrapper execution/DTO behavior.
- `mvn -o -f regressiontests/tracing-compatibility/pom.xml test`: passed 10 tests (8 compatibility and 2 JPA policy tests). Real exported Boot/agent spans cover ordinary/mixed-in actions, property/collection access, unresolved association fallback, canonical action signatures, uppercase, colliding compact names, one member span per invocation, root/framework/JPA/JDBC ancestry, inactive observation, missing exporter and a failed request followed by success on the same worker.
- Both modified AsciiDoc pages converted successfully with Antora's installed Asciidoctor core. Petclinic's logical type `petclinic.PetOwner`, `updateName(String)` and mixed-in visits collection were checked against sample source.
- `git diff --check` and strict OpenSpec validation passed.

Existing standard handlers preserve the names and attributes in both tracing modes; no maintenance-specific handler workaround is required.

## Limits

The export fixture uses controlled metamodel collaborators and actual runtime invocation, JPA observation and SQL work. It verifies declared identifier signatures independently of runtime argument values. This does not replace interactive Petclinic verification; no browser session or full reactor build was performed. Automatic HTTP/JDBC names and Wicket region naming are outside this change.

## Archive checkpoint

After rebase, implementation commit is `5471c51b515`; user documentation edits are committed as `e5b7ea42e11`. The same focused build/test, interaction and exported-trace commands passed again before archive: 43 focused tests, 77 executed interaction tests with 2 existing disabled tests, and 10 tracing/JPA tests. The M3 how-to, migration notes and observability guide rendered successfully; strict change validation also passed.
