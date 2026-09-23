## 1. Contextual Naming Foundation

- [x] 1.1 Add an internal observation naming helper that produces bounded operation-first contextual names from logical type and member identifiers without using instance data or localized labels.
- [x] 1.2 Add focused unit tests for simple and namespaced logical types, action members, normalization, collisions, and names long enough to encounter the tracing display-name limit.

## 2. Action Invocation Naming

- [x] 2.1 Update `MemberExecutorServiceDefault` to assign the existing action-invocation observation a contextual name derived from the owning action's logical type and member name while retaining the stable observation name and canonical attributes.
- [x] 2.2 Extend the action observation tests to verify the contextual name, stable observation name, canonical action identifier, parentage, success lifecycle, and failure lifecycle.

## 3. Wicket Page and Prompt Rendering

- [x] 3.1 Update the Wicket render descriptor so entity-page observations receive a contextual name derived from the logical object type while existing fine-grained region contextual names remain stable.
- [x] 3.2 Add the `causeway.wicket.action.prompt.render` descriptor with compact contextual naming and canonical `causeway.object.type` and `causeway.action.id` attributes.
- [x] 3.3 Attach the reusable render observation behavior once at the common `ActionParametersPanel` boundary without instrumenting individual parameter fields or presentation-specific prompt shells.
- [x] 3.4 Extend Wicket observation tests to cover page naming, prompt metadata, render lifecycle and parentage, no-op operation, serialization safety, parameter exclusion, and the absence of duplicate prompt observations across supported presentation styles.

## 4. Documentation and Validation

- [x] 4.1 Update `adoc/micrometer-tracing-operations.adoc` with the contextual naming convention, canonical-attribute guidance, 50-character tracing limit, prompt render boundary, privacy rules, and changed example trace tree.
- [x] 4.2 Run the focused core configuration, runtime-services, and Wicket UI observation test suites and record the results.
- [x] 4.3 Validate a representative action invocation, entity-page render, and parameterized action prompt against the Java-agent tracing fixture or local Jaeger, confirming meaningful display names, canonical attributes, correct nesting, and no per-parameter span amplification.

## 5. Logical-Identifier Naming Refinement

- [x] 5.1 Replace lower-hyphen token naming with case-preserving builders for logical types, logical members, and fine-grained render regions.
- [x] 5.2 Implement the 50-character policy: full logical identifier first, namespace-free fallback for type-based names, then deterministic truncation.
- [x] 5.3 Extend naming tests for exact casing, boundary lengths, namespace fallback, final truncation, member-region names, and default fieldset naming.

## 6. Case-Preserving Trace Export and Invocation Identity

- [x] 6.1 Add a Causeway tracing observation handler that preserves bounded contextual names while retaining the default Micrometer tracing lifecycle and tagging behavior.
- [x] 6.2 Configure the dedicated Causeway observation registry to use the case-preserving handler without affecting Java-agent-owned spans.
- [x] 6.3 Resolve action invocations through `IdentifierUtil.logicalMemberIdentifierFor(...)` so mixed-in actions use their domain-facing logical identity in both the contextual name and `causeway.action.id`.
- [x] 6.4 Extend configuration and runtime observation tests for case preservation, 50-character enforcement, declared actions, and mixed-in action identities.

## 7. Wicket Render Name Refinement

- [x] 7.1 Update page and action-prompt contextual names to use full logical identifiers with namespace fallback and truncation.
- [x] 7.2 Give fieldset, property, collection, and action-button observations meaningful `render <region> <id>` contextual names while retaining stable observation names and canonical attributes.
- [x] 7.3 Extend Wicket observation tests for page, prompt, fieldset, default fieldset, property, collection, and action-button display names and limits.

## 8. Refinement Documentation and Validation

- [x] 8.1 Update the tracing operations documentation with the final case-preserving taxonomy, fallback algorithm, examples, and authoritative-attribute guidance.
- [x] 8.2 Update the Java-agent compatibility fixture to assert exact action, page, prompt, and fine-grained render names while automatic HTTP and JDBC naming remains unchanged.
- [x] 8.3 Run the focused naming, configuration, runtime-services, Wicket, and Java-agent compatibility suites and record the refinement results.

## 9. Programming-Model Operation Names

- [x] 9.1 Replace the action contextual-name prefix `invoke` with `act` throughout naming code, tests, documentation, and Java-agent validation.
- [x] 9.2 Resolve invocations implemented by mixed-in property and collection facets back to their domain-facing `ObjectAssociation`.
- [x] 9.3 Emit mixed-in property and collection evaluation as `causeway.property.access` and `causeway.collection.access` with `prop` and `coll` contextual names and the applicable canonical association attribute.
- [x] 9.4 Extend runtime observation tests for declared actions, mixed-in actions, mixed-in properties, mixed-in collections, canonical metadata, and unresolved-association fallback.

## 10. Entity-Page Preparation Observation

- [x] 10.1 Add a `causeway.wicket.page.prepare` descriptor using contextual name `prepare <logical-type-name>` and canonical `causeway.object.type`.
- [x] 10.2 Start preparation before `EntityPage` delegates to `Page.onConfigure()`, keep it active through descendant pre-render preparation, and close it before actual page rendering.
- [x] 10.3 Record preparation failures and provide detach-time cleanup without changing no-op behavior.
- [x] 10.4 Extend Wicket observation tests for preparation/render ordering, association-access parentage, failure cleanup, no-op behavior, and logical-name fallback.

## 11. Documentation and Validation

- [x] 11.1 Update tracing operations documentation and trace-tree examples for `act`, `prop`, `coll`, and `prepare` spans.
- [x] 11.2 Extend the Java-agent compatibility fixture to validate page-preparation parentage and the final action prefix while preserving automatic HTTP and JDBC behavior.
- [x] 11.3 Run the focused core, runtime-services, Wicket, and Java-agent compatibility suites and record the results.
