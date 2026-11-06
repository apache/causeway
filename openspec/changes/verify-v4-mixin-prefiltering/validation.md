# Validation: existing v4 mixin applicability

## Scope and provenance

Maintenance CAUSEWAY-4061 commit `680ac40ef52` and its `prefilter-mixins-before-full-introspection` artifacts were reviewed.
The user explicitly approved a tests-only verification rather than a redundant runtime port.
Planning checkpoint `3bde25d3817` is on `CAUSEWAY-4061-v4`, following the separate CAUSEWAY-4060 PR #3914 work.

V4's existing SpecificationLoaderDefault bootstraps mixins before domain types and builds MixinSpecStreamerEager during that phase.
The eager index uses facet mixeeType and complete Java superclass/interface traversal; the standard MixinFacetImpl.isMixinFor uses Class.isAssignableFrom.
MixedInMemberFactory selects those candidates before either action or association member streaming.
MixinSpecStreamerOnTheFly is deprecated and its only constructor reference is commented out.
The maintenance applicableMixinFacet helper and its diagnostic caller-filter change therefore have no corresponding new helper to port into v4.

This does not assert that unrelated mixins are never fully introspected globally.
Their independent eager bootstrap is intentional and retained; the verified absence of work concerns additional rejected-candidate loading/member access caused by per-target discovery.

## Environment and commands

Java 25.0.4-tem and Maven 3.9.13 were used with the inherited Boot 4.2.0-RC1 baseline.
The worktree is not open in IntelliJ, so the requested IDE build was unavailable; Maven compiled the affected tests instead.

```bash
export JAVA_HOME="$HOME/.sdkman/candidates/java/25.0.4-tem"
export PATH="$JAVA_HOME/bin:$PATH"
mvn -B -pl core/config,core/metamodel -am \
  -Dtest=MixinApplicabilityTest,CausewayBeanTypeRegistryMixinOrderTest,_IntrospectionDiagnosticsTest,SpecificationLoaderDiagnosticsTest,PrimingRegistryDefaultTest,PrimingRegistryDefaultObservationTest,ActionExecutorPrimingTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true install
mvn -B -f regressiontests/pom.xml -pl domainmodel -am \
  -Dmaven.source.skip=true -Dmaven.javadoc.skip=true test
openspec validate verify-v4-mixin-prefiltering --strict
git diff --check
git diff --name-only 3bde25d3817 -- "**/src/main/java/**"
```

## Focused coverage

Seven new tests exercise the actual eager streamer and MixedInMemberFactory with controlled registry/loader/facet objects.
They cover exact, superclass and ordinary interface candidates, empty/unrelated/missing-facet cases, both discovery paths, index reuse, managed-service Object-mixin action exclusion and ineligible targets.
Bootstrap loader interactions are separated from subsequent discovery assertions.
Accepted candidates' member streams are accessed, rejected candidates' streams are not, and per-target discovery/selection issues no further request to the controlled loader.
Registry ordering itself remains covered by the preceding change's actual-registry tests.
Initial test-fixture compilation required wildcard-safe Class stubs and a mocked registry because its constructor is package-private; no production visibility or code change was needed.

The combined focused gate passed 44 tests: two configuration tests and forty-two metamodel tests, without failures or skips.
This includes the seven new tests and the existing diagnostics, registry and priming regressions.

## Real bootstrap coverage

The full domain-model reactor passed all 121 tests without failures or skips.
The new isolated real Spring context verifies base-class properties/collections and ordinary-interface actions on a subtype, using actual framework mixin facets.
It verifies tag/children/alpha identifiers and String/Child element types, synthetic collection navigation and explicit zulu-before-alpha sequence opposing class-name traversal.
Unrelated action/property/collection counterparts are positively verified on their own mixee and excluded from the subtype; the base without the interface does not acquire the interface action.
Existing repeated cyclic recording-navigation bootstrap and all good/bad-domain, serialization, editing, annotation and general metamodel regressions remain passing.

## Documentation and scope checks

The diagnostic guide now explains the eager bootstrap/index/discovery distinction without claiming that registered unrelated mixins are globally skipped.
It rendered without warnings using Asciidoctor 2.0.26 core/data from cached AsciidoctorJ 3.0.1 to `target/mixin-applicability-diagnostics.html`.
Strict change validation and whitespace checks passed.
The production-Java diff relative to the planning checkpoint is empty, and no dependency/configuration change was introduced.
No new runtime performance claim is made.

## Limits

Interaction tests use controlled metadata and empty member streams to isolate the discovery boundary; actual contribution creation is covered by the real bootstrap test.
Coverage establishes standard v4 facet/type-hierarchy behavior, not arbitrary replacement facet implementations or the retired on-the-fly loader.
No full repository reactor, browser session, production application reproduction or complete Antora site build was performed.
