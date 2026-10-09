# Validation

2026-10-09 on CAUSEWAY-4058, planning checkpoint `9a250065cfa` (based on local `41d8db75a06`). Java 25.0.4, Maven 3.9.13, main Boot 4.2.0-M1. Implementation is recorded in the checkpoint commit containing this validation evidence.

## Implementation adaptation

Main uses InteractionHead as a mixee/mixin pair, rather than maintenance's ActionInteractionHead subclass. Domain-facing identity is constructed using the owning domain specification's logical type, the programming model's existing mixin naming strategy and the invoked identifier's parameter signature. Unit tests cover custom naming and parameter retention; a real wrapper regression compares the result to the contributed action's metamodel identifier. The invoked identifier remains unchanged.

Wrapper rule controls map normal execution to USER and skip-rules to FRAMEWORK in DomainObjectInvocationHandler. These become CHECKED and SKIPPED; PASS_THROUGH still returns before ActionInvocation creation. Legacy constructors report UNKNOWN. No rule-processing or carrier lifecycle changes were needed.

## Checks

Focused reactor command:

```bash
JAVA_HOME=/Users/danhaywood/.sdkman/candidates/java/25.0.4-tem \
  mvn -o -pl api/applib,core/runtimeservices -am \
  -Dtest=InteractionProviderTest,MemberExecutorServiceDefaultTest,InteractionServiceObservationTest \
  -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
```

Passed 29 tests: 10 provider tests, 12 executor tests and 7 observation lifecycle tests. Checks include empty and non-action execution, identity without equals, full signatures versus names, null arguments, checked/skipped/unknown state and separate invoked/domain-facing identifiers.

Interact regression command:

```bash
JAVA_HOME=/Users/danhaywood/.sdkman/candidates/java/25.0.4-tem \
  mvn -o -f regressiontests/pom.xml -pl interact -am \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true test
```

Passed: 79 reported, 77 executed, 2 existing disabled tests, no failures/errors. Includes nine WrapperInteraction_1_IntegTest tests: real normal/skip-rules wrapper invocations, regular/mixin identity, nested child success/failure, property-edit exclusion, restoration, thread isolation, top-level failure cleanup, later interaction and direct Java calls. Existing action/property/wrapper and publishing-related interaction regressions also passed. Expected exceptions in the failure fixture are logged by the existing carrier.

Rendered all three changed AsciiDoc pages with the locally installed Asciidoctor: InteractionProvider reference, ActionInvocation reference and M3 otel-howto. Strict OpenSpec change validation and git diff whitespace validation passed.

## Limits

This additive invocation API emits no new spans and requires no observation profile. No interactive browser or external exporter verification applies. Direct Java calls do not introduce framework execution frames and can still see an enclosing invocation. RuleChecking is aggregate mode information, not individual rule provenance. This implementation is based on the local preceding feature branch; remote main was not fetched or merged during apply.
