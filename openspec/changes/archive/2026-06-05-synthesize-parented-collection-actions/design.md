## Context

`ObjectSpecificationDefault` builds the metamodel for entity types by creating associations from association faceted methods and actions from action faceted methods during full introspection.
Parented collections already carry enough metadata to know the parent type, collection id, element type, and element scalar properties, but selecting an element from a rendered collection does not currently pass through `ObjectAction` invocation.
Command recording already knows how to record logged safe action invocations.
The missing piece is an invokable, safe metamodel member that represents parent-to-child collection navigation.

## Goals / Non-Goals

**Goals:**

- Generate synthetic actions for parented collections during metamodel introspection only when explicitly enabled by configuration.
- Make each synthetic action invokable by command recording code using the normal `ObjectAction` path.
- Use a mandatory parent reference parameter and optional scalar filter parameters to identify a child object within the parented collection.
- Return a single child object from the synthetic selector invocation.
- Keep ordinary collection rendering and row-click navigation unchanged when the feature is disabled.

**Non-Goals:**

- Do not replace collection rendering or require end users to use synthetic actions during normal browsing.
- Do not add command export or replay result mapping behavior for synthetic selector action results.
- Do not infer arbitrary business identity for non-scalar associations or plural child matches beyond deterministic selection rules.

## Decisions

### Gate synthetic action creation behind configuration

Create synthetic selector actions only when the disabled-by-default `causeway.extensions.command-log.parented-collection-selector-actions-enabled` configuration property is enabled.
This keeps ordinary applications from seeing additional metamodel members unless they opt in for command recording or replay tooling.
When enabled, create synthetic selector actions in `ObjectSpecificationDefault` after `replaceAssociations(createAssociations())` has populated declared associations and before the final action list is installed.
This keeps collection-derived actions close to the metamodel data that defines them and avoids requiring domain developers to add mixins or explicit finder methods.
The action stream should concatenate declared actions with synthetic actions derived from eligible parented `OneToManyAssociation`s.

Alternative considered: generate mixin classes or require application-level finder methods.
That would make command recording possible, but it would push framework replay infrastructure concerns into domain applications and would be inconsistent across apps.

### Represent synthetic selectors as internal safe ObjectActions

Each selector should be an `ObjectAction` with safe semantics and an invocation facet that reads the parented collection from the supplied parent object and returns the matching element.
The action id should be deterministic and namespaced to avoid colliding with developer actions, for example using a reserved prefix plus the collection id.
The action should carry marker metadata that lets viewers and metamodel export distinguish synthetic recording actions from user-authored actions when the feature is enabled.

Alternative considered: bypass `ObjectAction` and inject command log entries directly from collection row clicks.
That would record navigation but would not provide a normal command target and parameter model.

### Parameterize by parent plus optional scalar child values

The first parameter should be mandatory and typed as the parent specification.
Additional parameters should be optional and derived from scalar properties of the child element specification that are suitable for value equality filtering.
Synthetic selectors should ignore child collections and reference properties as automatic parameters to avoid deep traversal, unstable object identity, and ambiguous command DTO parameter mapping.

Alternative considered: use a single child bookmark parameter.
That would not model the dotted parent-to-child navigation path through the parented collection.

### Return one selected child or fail deterministically

Invocation should resolve the parent object, access the collection through the existing association facet, filter by supplied scalar values, and return exactly one child when possible.
If no child matches or multiple children match, invocation should fail with a clear validation or execution error instead of choosing an arbitrary row.
This makes exported recordings stable and forces the recorder to supply enough scalar values for replay.

Alternative considered: return a list of matching children.
Returning a list would lose the single dotted path step.

### Integrate through existing safe action command publishing

Synthetic selector actions should be safe actions and should be command logged only when the existing safe action command publishing configuration enables safe action logging or when an equivalent explicit recording path invokes them.
This keeps the new behavior additive and avoids special cases in command export and replay.

Alternative considered: always command-publish synthetic selector actions.
That would surprise applications that are not recording replay scripts and would bypass the disabled-by-default policy introduced for safe actions.

## Risks / Trade-offs

- Synthetic action enumeration could affect viewers or tooling that assume every action comes from application code.
  Mitigation: only create synthetic selector actions when the explicit configuration property is enabled, and add marker metadata so tooling can identify them.
- Scalar filters may not uniquely identify every collection element.
  Mitigation: require exact single-match results and document that recordings must supply enough scalar values.
- Creating parameters from every scalar child property can produce large action signatures.
  Mitigation: start with eligible scalar properties and allow later refinement through annotations or configuration if needed.
- The metamodel currently builds actions from `FacetedMethod`s, so synthetic actions may need a small internal action/facet construction path.
  Mitigation: isolate synthetic action creation behind a factory and reuse existing facets where practical.
