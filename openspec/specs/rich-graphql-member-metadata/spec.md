# Rich GraphQL Member Metadata Specification

## Purpose

Define a narrow additive set of canonical local names, descriptions, and editor-neutral constraints on known rich GraphQL wrappers without duplicating grid, menu, authorization, or viewer policy.

## Requirements

### Requirement: Narrow local metadata on known wrappers
Existing rich GraphQL property, collection, action, and action-parameter wrappers SHALL expose only the additive local metadata assigned to them by this capability.

#### Scenario: Client addresses a known wrapper
- **WHEN** a client requests accepted local metadata beneath a known semantic wrapper
- **THEN** applicable names, descriptions, or editor-neutral constraints are available
- **AND** absent nullable semantics return null

#### Scenario: Client requests an unknown metadata expansion
- **WHEN** a client needs grid structure, menu structure, viewer policy, or metamodel internals
- **THEN** the known wrapper does not expose those concerns through this capability

### Requirement: Independent canonical friendly name and description
Rich property, collection, action, and action-parameter wrappers SHALL expose one non-null `metadata` object containing a non-null `friendlyName` and nullable `description` independently.

#### Scenario: Member has both values
- **WHEN** the canonical metamodel supplies a friendly name and a distinct description
- **THEN** the wrapper returns each translated value independently

#### Scenario: Only a friendly name exists
- **WHEN** the canonical metamodel supplies no description
- **THEN** `friendlyName` remains available
- **AND** `description` is null rather than a copy of the friendly name

#### Scenario: Domain object defines imperative text
- **WHEN** a member can compute a name or description from a domain-object instance
- **THEN** local metadata uses only canonical static facets
- **AND** metadata resolution does not invoke domain-object methods or fetch member values

### Requirement: Bounded standalone editor constraints
The shared metadata object SHALL expose nullable `maxLength`, `pattern`, `patternFlags`, `multiLine`, and `typicalLength` scalar fields from applicable property and action-parameter metamodel facets.

#### Scenario: Constrained value is inspected
- **WHEN** a known property or parameter declares accepted positive local constraints
- **THEN** the applicable scalar values are returned without local reinterpretation
- **AND** `patternFlags` represents Java regular-expression flags
- **AND** server validation remains authoritative

#### Scenario: Constraint is absent or fallback-only
- **WHEN** a constraint is absent, unlimited, non-positive, malformed, or supplied only by the single-line or unlimited fallback
- **THEN** the corresponding metadata field is null
- **AND** no client-facing default is fabricated

#### Scenario: Requiredness is inspected
- **WHEN** a client needs structural nullability or requiredness
- **THEN** it uses the generated GraphQL input type
- **AND** no conflicting required flag is introduced

### Requirement: Established resource file acceptance remains compatible
Resource `fileAccept` metadata SHALL remain in its established property-get and resource action-parameter locations.

#### Scenario: Resource metadata is inspected
- **WHEN** a client requests accepted-file metadata for a Blob or Clob property or resource parameter
- **THEN** the established field and value remain available
- **AND** this capability does not add a duplicate `fileAccept` field

### Requirement: Structural metadata remains in resources
Rich local metadata SHALL NOT duplicate complete grid or menu structure or resource-owned presentation hints.

#### Scenario: Client needs complete page structure
- **WHEN** effective grid structure is available
- **THEN** the client uses the grid resource for rows, columns, tabs, field sets, placement, ordering, icons, CSS, and action positions

#### Scenario: Client needs complete application-menu structure
- **WHEN** effective menu structure is available
- **THEN** the client uses the menu resource for bars, menus, sections, entries, labels, hints, and ordering

### Requirement: Standard member discovery
Member metadata SHALL complement standard introspection rather than introduce a duplicate member list or metamodel API.

#### Scenario: Client discovers type members
- **WHEN** a client needs property, action, and collection identifiers
- **THEN** it uses standard targeted GraphQL introspection
- **AND** no aggregate member-list field is required

### Requirement: Locale-correct request resolution
Canonical friendly names and descriptions SHALL retain Causeway translation behavior for the active request locale without globally caching one locale's result.

#### Scenario: Equivalent requests use different locales
- **WHEN** two authorized requests resolve the same metadata under different supported locales
- **THEN** each response uses the canonical translation for its own locale
- **AND** one response does not contaminate the other

### Requirement: Metadata authorization safety
Local metadata SHALL NOT reveal sensitive values, disabled-reason internals, authorization policy rules, or imperative domain-object results.

#### Scenario: Member is hidden for the current user
- **WHEN** runtime authorization hides a known member wrapper
- **THEN** metadata reveals no sensitive runtime state beyond the established hidden contract
- **AND** static schema identity reveals no more than standard introspection already reveals

### Requirement: Bounded schema growth
The local metadata implementation SHALL add no aggregate catalogue and exactly one shared GraphQL object type for metadata.

#### Scenario: Schema is compared before and after metadata
- **WHEN** generated schema type count, SDL bytes, and startup measurements are compared
- **THEN** metadata introduces one `RichMemberMetadata` type and one metadata field on each known wrapper
- **AND** the representative SDL growth remains below ten percent
- **AND** measured deltas are recorded

### Requirement: Backward-compatible metadata expansion
Metadata additions SHALL preserve established generated names, field descriptions, resource fields, operations, and runtime behavioral fields.

#### Scenario: Existing GraphQL client executes
- **WHEN** a client uses an established document without new metadata fields
- **THEN** its operation and response shape remain valid

### Requirement: Static action Font Awesome metadata
The shared rich member metadata object SHALL expose nullable `cssClassFa` and `cssClassFaPosition` fields for applicable static action Font Awesome facets.
It MUST NOT execute imperative icon logic or expose arbitrary action layout internals.

#### Scenario: Static action icon facet exists
- **WHEN** an action has an accepted static Font Awesome facet
- **THEN** `cssClassFa` returns its canonical bounded quick notation
- **AND** `cssClassFaPosition` returns the canonical `LEFT` or `RIGHT` token

#### Scenario: Static action icon facet is absent
- **WHEN** an action has no static Font Awesome facet
- **THEN** both icon metadata fields return null
- **AND** no default icon or position is fabricated

#### Scenario: Metadata belongs to another wrapper
- **WHEN** property, collection, or action-parameter metadata is requested
- **THEN** action icon metadata fields return null
- **AND** existing names, descriptions, and editor constraints remain unchanged

#### Scenario: Icon behavior is imperative
- **WHEN** icon behavior would require invoking domain code
- **THEN** static icon metadata returns null
- **AND** metadata resolution remains side-effect-free

### Requirement: Static action confirmation metadata
The shared rich member metadata object SHALL expose nullable `areYouSure` metadata derived from canonical action semantics without invoking domain behavior or exposing the complete action-semantics model.

#### Scenario: Action requires confirmation
- **WHEN** an action's canonical semantics have are-you-sure behavior
- **THEN** `areYouSure` returns true

#### Scenario: Action does not require confirmation
- **WHEN** an action's canonical semantics do not have are-you-sure behavior
- **THEN** `areYouSure` returns false
- **AND** no confirmation requirement is inferred from the action's name, icon, description, or mutation placement

#### Scenario: Metadata belongs to another wrapper
- **WHEN** property, collection, or action-parameter metadata is requested
- **THEN** `areYouSure` returns null
- **AND** metadata resolution does not invoke domain-object methods

#### Scenario: Existing client omits confirmation metadata
- **WHEN** an existing GraphQL document does not select `areYouSure`
- **THEN** its operation and response shape remain unchanged
