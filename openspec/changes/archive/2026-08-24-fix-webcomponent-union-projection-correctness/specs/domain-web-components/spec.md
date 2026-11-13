## ADDED Requirements

### Requirement: Bounded polymorphic value projection
The component library SHALL derive valid inline-fragment selections for advertised GraphQL interface and union values without selecting concrete fields directly on the abstract type.
It MUST validate fragment membership and nested fields through targeted introspection, MUST bound fragment and introspection cost, and MUST NOT repeat a mutating interaction to discover its result type.

#### Scenario: Small abstract result has described possible types
- **WHEN** an interface or union advertises a bounded set of described concrete object types
- **THEN** the operation selects `__typename` and valid inline fragments for those advertised types
- **AND** each fragment requests only metadata and children valid for its concrete type

#### Scenario: Broad collection union is activated
- **WHEN** an activated side-effect-free collection exposes an abstract row type whose complete possible-type set exceeds the direct expansion limit
- **THEN** the context issues one bounded typename-only probe and describes only distinct observed advertised concrete types
- **AND** reissues the same list or window read once with valid fragments for those types

#### Scenario: Polymorphic rows expose common identity
- **WHEN** concrete fragment results contain `id` and `logicalTypeName`
- **THEN** each row retains its exact typename, metadata, semantic object link, and hydrated row context
- **AND** optional title and version behavior follows the concrete metadata type

#### Scenario: Requested column differs by concrete type
- **WHEN** a declared semantic column is present on some observed concrete types and absent on another
- **THEN** each fragment includes the column only where its wrapper and child selection are advertised
- **AND** the missing cell remains local without invalidating successfully projected rows

#### Scenario: Replay returns an unobserved concrete type
- **WHEN** a collection changes between typename probe and fragment replay and returns an advertised type not included in the replay fragments
- **THEN** that row remains bounded to its available typename projection and is reported locally
- **AND** the context does not enter an unbounded probe loop

#### Scenario: Returned typename is not advertised
- **WHEN** a response typename is not an advertised possible type of the abstract field
- **THEN** the context rejects it as a bounded schema mismatch
- **AND** does not construct or submit a fragment from that value

#### Scenario: Broad mutating action result
- **WHEN** a mutating action returns an abstract type that cannot be projected within the direct fragment bound
- **THEN** the action executes exactly once and retains its successful bounded typename-only outcome
- **AND** the controller does not invent navigable identity or repeat the mutation

#### Scenario: Polymorphic request becomes obsolete
- **WHEN** a probe or fragment replay belongs to a disconnected, cancelled, or superseded collection generation
- **THEN** its remaining work is aborted or discarded
- **AND** it cannot replace the current row state
