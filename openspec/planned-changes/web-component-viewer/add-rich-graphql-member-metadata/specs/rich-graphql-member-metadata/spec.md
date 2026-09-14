## ADDED Requirements

### Requirement: Narrow local metadata on known wrappers
Existing rich GraphQL wrappers SHALL expose only additive local metadata justified by matrix entries `REF-METADATA-01` and `REF-METADATA-02`.

#### Scenario: Client addresses a known wrapper
- **WHEN** a client requests accepted local metadata beneath a known semantic wrapper
- **THEN** applicable names, descriptions, or editor-neutral constraints are available
- **AND** absent semantics use documented null or default behavior

### Requirement: Independent friendly name and description
Canonical friendly name and description SHALL be independently queryable and SHALL NOT require clients to interpret one GraphQL field description as both.

#### Scenario: Member has both values
- **WHEN** Causeway supplies a friendly name and a distinct description
- **THEN** the wrapper returns each value independently

#### Scenario: Only a friendly name exists
- **WHEN** Causeway supplies no description
- **THEN** the canonical friendly name remains available
- **AND** the description remains absent unless documented localization produces a distinct value

### Requirement: Standalone editor constraints
Rich property and parameter metadata SHALL expose the accepted maximum-length, regular-expression, accepted-file, multiline, and typical-length semantics when present.

#### Scenario: Constrained value is inspected
- **WHEN** a known property or parameter declares an accepted local constraint
- **THEN** the applicable value is available as structured metadata
- **AND** server validation remains authoritative

#### Scenario: Requiredness is inspected
- **WHEN** a client needs structural nullability or requiredness
- **THEN** it uses the generated GraphQL input type
- **AND** no conflicting required flag is introduced

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

### Requirement: Metadata authorization safety
Local metadata SHALL NOT reveal sensitive values, disabled-reason internals, or authorization policy rules.

#### Scenario: Member is hidden for the current user
- **WHEN** runtime authorization hides a member
- **THEN** metadata reveals no sensitive runtime state beyond the established hidden contract
- **AND** static schema identity reveals no more than standard introspection already reveals

### Requirement: Backward-compatible metadata expansion
Metadata additions SHALL preserve established generated names, field descriptions, operations, and runtime behavioral fields.

#### Scenario: Existing GraphQL client executes
- **WHEN** a client uses an established document without new metadata fields
- **THEN** its operation and response shape remain valid
