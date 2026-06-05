## 1. Metamodel Synthesis

- [ ] 1.1 Identify eligible parented collection associations during `ObjectSpecificationDefault` introspection after associations are created.
- [ ] 1.2 Add a factory or helper that creates deterministic synthetic selector action identifiers and marker metadata for eligible collections.
- [ ] 1.3 Implement synthetic `ObjectAction` construction with safe semantics and integration with existing action enumeration.
- [ ] 1.4 Ensure synthetic selector actions are hidden from ordinary UI action surfaces unless explicitly requested by recording or metamodel tooling.

## 2. Parameter Model

- [ ] 2.1 Add a mandatory parent object parameter typed as the collection owner specification.
- [ ] 2.2 Derive optional selector parameters from eligible scalar properties of the collection element specification.
- [ ] 2.3 Exclude child collections and reference properties from automatic selector parameters.
- [ ] 2.4 Ensure synthetic parameter metadata is serializable into command DTOs through the normal action invocation path.

## 3. Invocation Semantics

- [ ] 3.1 Implement selector invocation by resolving the supplied parent object and reading the parented collection through the existing association facet.
- [ ] 3.2 Filter collection elements using supplied scalar parameter values with deterministic equality semantics.
- [ ] 3.3 Return the single matching child object when exactly one element matches.
- [ ] 3.4 Fail clearly when no elements match or multiple elements match.

## 4. Command Logging, Export, and Replay Integration

- [ ] 4.1 Verify synthetic selector actions participate in existing safe action command publishing only when that publishing path is enabled.
- [ ] 4.2 Verify logged selector action results capture returned object bookmarks through existing result handling.
- [ ] 4.3 Verify exported YAML includes selector command DTOs and returned object metadata without introducing a new YAML shape.
- [ ] 4.4 Verify replay result mapping uses selector action results to remap later command targets and reference parameters.

## 5. Tests and Validation

- [ ] 5.1 Add metamodel tests for synthetic selector action creation, deterministic ids, marker metadata, and action hiding behavior.
- [ ] 5.2 Add parameter tests for mandatory parent parameters, optional scalar child parameters, and excluded non-scalar members.
- [ ] 5.3 Add invocation tests for single match, no match, and ambiguous match outcomes.
- [ ] 5.4 Add command log export and replay integration tests covering a parent-child-grandchild dotted path such as `Lease -> LeaseItem -> LeaseTerm`.
- [ ] 5.5 Run focused Maven tests for the metamodel and command log modules touched by the implementation.
- [ ] 5.6 Run `openspec validate synthesize-parented-collection-actions --strict` and fix any proposal/spec/task validation issues.
