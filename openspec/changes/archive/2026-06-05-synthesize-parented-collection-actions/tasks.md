## 1. Metamodel Synthesis

- [x] 1.1 Identify eligible parented collection associations during `ObjectSpecificationDefault` introspection after associations are created.
- [x] 1.2 Add a factory or helper that creates deterministic synthetic selector action identifiers and marker metadata for eligible collections.
- [x] 1.3 Implement synthetic `ObjectAction` construction with safe semantics and integration with existing action enumeration.
- [x] 1.4 Add a disabled-by-default configuration property and create synthetic selector actions only when that property is enabled.

## 2. Parameter Model

- [x] 2.1 Add a mandatory parent object parameter typed as the collection owner specification.
- [x] 2.2 Derive optional selector parameters from eligible scalar properties of the collection element specification.
- [x] 2.3 Exclude child collections and reference properties from automatic selector parameters.
- [x] 2.4 Ensure synthetic parameter metadata is serializable into command DTOs through the normal action invocation path.

## 3. Invocation Semantics

- [x] 3.1 Implement selector invocation by resolving the supplied parent object and reading the parented collection through the existing association facet.
- [x] 3.2 Filter collection elements using supplied scalar parameter values with deterministic equality semantics.
- [x] 3.3 Return the single matching child object when exactly one element matches.
- [x] 3.4 Fail clearly when no elements match or multiple elements match.

## 4. Command Logging Integration

- [x] 4.1 Verify synthetic selector actions participate in existing safe action command publishing only when that publishing path is enabled.

## 5. Tests and Validation

- [x] 5.1 Add metamodel tests for config-gated synthetic selector action creation, deterministic ids, and marker metadata.
- [x] 5.2 Add parameter tests for mandatory parent parameters, optional scalar child parameters, and excluded non-scalar members.
- [x] 5.3 Add invocation tests for single match, no match, and ambiguous match outcomes.
- [x] 5.4 Run focused Maven tests for the metamodel module touched by the implementation.
- [x] 5.5 Run `openspec validate synthesize-parented-collection-actions --strict` and fix any proposal/spec/task validation issues.
