# Rich GraphQL Object-Interaction Correctness Specification

## Purpose

Define correctness, compatibility, and bounded-error requirements for rich GraphQL polymorphic objects, argument conversion, and property mutation results.

## Requirements

### Requirement: Complete public object identity
The rich GraphQL schema SHALL accept every supported assignable concrete public logical type required to address a generated domain-object input.

#### Scenario: Abstract entity input addresses a concrete entity
- **WHEN** a client supplies an assignable concrete public logical type and identifier to an input declared against an abstract entity type
- **THEN** the server resolves the concrete domain object
- **AND** does not require a Java implementation class name

#### Scenario: Object identity is invalid or unauthorized
- **WHEN** a logical type and identifier pair is invalid, ambiguous, stale, or unauthorized
- **THEN** the server returns a bounded GraphQL error
- **AND** does not disclose domain state or authorization rules

### Requirement: Polymorphic rich object output
The rich GraphQL schema SHALL expose assignable concrete runtime object types when a collection, action result, or other output is declared against an abstract domain-object type.

#### Scenario: Abstract-element collection is read
- **WHEN** a client requests a collection declared with an abstract element type
- **THEN** each row resolves to its concrete rich GraphQL type
- **AND** the client can use `__typename` and standard GraphQL fragments to select concrete members

### Requirement: Consistent declared-type argument conversion
Every rich action negotiation and invocation path SHALL consume values converted according to the declared metamodel parameter types.

#### Scenario: Collection-valued parameters are validated
- **WHEN** a client validates or invokes an action using a collection of domain-object references
- **THEN** validation receives domain objects rather than raw GraphQL maps
- **AND** malformed inputs produce bounded GraphQL errors rather than HTTP 500 assertion failures

#### Scenario: Bulk parameter choices are requested
- **WHEN** a client requests choices for a collection-valued bulk-action parameter
- **THEN** the choices method receives the same converted argument context used by invocation
- **AND** returns typed candidates or an authoritative semantic error

### Requirement: Authoritative property mutation result
A successful rich property mutation SHALL return the mutated domain object and its authoritative post-mutation identity.

#### Scenario: Memento view-model property changes
- **WHEN** a valid mutation changes an editable memento view-model property
- **THEN** the returned property value reflects the submitted change
- **AND** the returned identifier can reload the updated state

#### Scenario: Persistent entity property changes
- **WHEN** a valid mutation changes a persistent entity property
- **THEN** the established persistent bookmark identity remains valid
- **AND** the returned rich object reflects authoritative post-mutation state

### Requirement: Compatibility and bounded exposure
The correctness changes SHALL preserve established successful concrete-object and scalar-interaction documents while adding no duplicate object-member metadata API.

#### Scenario: Existing concrete operation is repeated
- **WHEN** an existing concrete object query or scalar action document is executed after the change
- **THEN** its generated names and semantic result remain compatible
- **AND** any new type exposure is limited to public logical types required by the corrected contract

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
