## 1. Contextual Naming Foundation

- [ ] 1.1 Add an internal observation naming helper that produces bounded operation-first contextual names from logical type and member identifiers without using instance data or localized labels.
- [ ] 1.2 Add focused unit tests for simple and namespaced logical types, action members, normalization, collisions, and names long enough to encounter the tracing display-name limit.

## 2. Action Invocation Naming

- [ ] 2.1 Update `MemberExecutorServiceDefault` to assign the existing action-invocation observation a contextual name derived from the owning action's logical type and member name while retaining the stable observation name and canonical attributes.
- [ ] 2.2 Extend the action observation tests to verify the contextual name, stable observation name, canonical action identifier, parentage, success lifecycle, and failure lifecycle.

## 3. Wicket Page and Prompt Rendering

- [ ] 3.1 Update the Wicket render descriptor so entity-page observations receive a contextual name derived from the logical object type while existing fine-grained region contextual names remain stable.
- [ ] 3.2 Add the `causeway.wicket.action.prompt.render` descriptor with compact contextual naming and canonical `causeway.object.type` and `causeway.action.id` attributes.
- [ ] 3.3 Attach the reusable render observation behavior once at the common `ActionParametersPanel` boundary without instrumenting individual parameter fields or presentation-specific prompt shells.
- [ ] 3.4 Extend Wicket observation tests to cover page naming, prompt metadata, render lifecycle and parentage, no-op operation, serialization safety, parameter exclusion, and the absence of duplicate prompt observations across supported presentation styles.

## 4. Documentation and Validation

- [ ] 4.1 Update `adoc/micrometer-tracing-operations.adoc` with the contextual naming convention, canonical-attribute guidance, 50-character tracing limit, prompt render boundary, privacy rules, and changed example trace tree.
- [ ] 4.2 Run the focused core configuration, runtime-services, and Wicket UI observation test suites and record the results.
- [ ] 4.3 Validate a representative action invocation, entity-page render, and parameterized action prompt against the Java-agent tracing fixture or local Jaeger, confirming meaningful display names, canonical attributes, correct nesting, and no per-parameter span amplification.
