## ADDED Requirements

### Requirement: Complete incremental union registration
Rich GraphQL SHALL preserve the union of concrete possible types discovered by every registration of the same generated interface or union name during metamodel construction.
It MUST deduplicate possible types by GraphQL name and MUST retain one deterministic generated abstract type and resolver registration.

#### Scenario: Later registration discovers another concrete type
- **WHEN** a generated union name is registered after an existing registration with one or more additional concrete object types
- **THEN** the schema registry replaces the stored union with merged possible-type membership
- **AND** introspection advertises both the earlier and later concrete types

#### Scenario: Registration repeats an existing concrete type
- **WHEN** another registration contributes a concrete type name already present on the generated union
- **THEN** the stored union contains that type exactly once
- **AND** schema construction remains deterministic

#### Scenario: Runtime resolver returns a merged concrete type
- **WHEN** the union resolver identifies a concrete type contributed by a later registration
- **THEN** GraphQL validates that type against the completed union membership
- **AND** a client can select it through an advertised inline fragment
