# Rich GraphQL Navigable Breadcrumbs Specification

## Purpose

Define the bounded rich GraphQL metadata contract that projects an object's metamodel-authoritative navigable ancestors for framework-neutral breadcrumb consumers.

## Requirements

### Requirement: Navigable ancestor metadata
Each rich GraphQL object metadata type SHALL expose a nullable `breadcrumbs` field containing the current object's navigable ancestors as shared breadcrumb-entry objects with non-null `logicalTypeName`, `id`, and `title` fields.
The entries MUST be ordered from the root ancestor to the immediate parent and MUST NOT repeat the current object.

#### Scenario: Object has a navigable hierarchy
- **WHEN** the current object's metamodel resolves a parent and that parent resolves another parent
- **THEN** `breadcrumbs` returns both bookmarkable ancestors in root-to-parent order
- **AND** each entry contains its current request-local title and bookmark identity

#### Scenario: Object has no navigable parent
- **WHEN** the current object's metamodel resolves no navigable parent
- **THEN** `breadcrumbs` returns an empty list
- **AND** existing current-object metadata remains unchanged

#### Scenario: Navigable parent is unavailable as a navigation target
- **WHEN** parent traversal reaches null or an object without a bookmark identity
- **THEN** traversal stops before adding an unusable entry
- **AND** no Java class name or arbitrary object string is exposed

### Requirement: Facet-authoritative hierarchy traversal
The rich GraphQL metadata resolver SHALL derive each parent only through the current object's established navigable-parent metamodel semantic, including `@PropertyLayout(navigable = Navigable.PARENT)`, and SHALL NOT infer parents from arbitrary reference properties or persistence relationships.

#### Scenario: Property is the declared navigable parent
- **WHEN** a domain property is marked `Navigable.PARENT`
- **THEN** breadcrumb traversal follows the parent returned by the corresponding navigable-parent facet
- **AND** no duplicate application breadcrumb declaration is required

#### Scenario: Ordinary reference is present
- **WHEN** an object has a reference property that is not its navigable parent
- **THEN** that reference does not appear in the breadcrumb chain merely because it is reachable

### Requirement: Bounded and cycle-safe traversal
Breadcrumb traversal MUST inspect no more than 32 ancestors and MUST detect repeated bookmark identities, including repetition of the current object.
A cycle, depth overflow, or facet failure SHALL produce a bounded safe error on the nullable `breadcrumbs` field rather than an unbounded operation or a misleading truncated chain.

#### Scenario: Hierarchy contains a cycle
- **WHEN** traversal encounters an identity already present in the active chain
- **THEN** the `breadcrumbs` field fails with a bounded cycle diagnostic
- **AND** traversal terminates without returning the cyclic partial chain

#### Scenario: Hierarchy exceeds the bound
- **WHEN** traversal would inspect a thirty-third ancestor
- **THEN** the `breadcrumbs` field fails with a bounded depth diagnostic
- **AND** it exposes neither the overlong chain nor internal exception details

#### Scenario: Parent facet throws
- **WHEN** navigable-parent evaluation throws an application exception
- **THEN** the field reports a bounded safe failure
- **AND** sibling metadata fields can remain available through GraphQL partial-data behavior

### Requirement: Additive metadata contract
The breadcrumb projection SHALL be additive to existing rich object metadata and SHALL use one shared breadcrumb-entry GraphQL type rather than one new entry type per domain type.

#### Scenario: Existing client omits breadcrumbs
- **WHEN** a client executes an established rich object document without selecting `breadcrumbs`
- **THEN** its operation and response shape remain valid
- **AND** no navigable-parent traversal occurs

#### Scenario: Schema contains many domain types
- **WHEN** rich metadata is generated for multiple object types
- **THEN** all breadcrumb fields reuse the same entry type
- **AND** schema growth is bounded independently of the domain-type count
