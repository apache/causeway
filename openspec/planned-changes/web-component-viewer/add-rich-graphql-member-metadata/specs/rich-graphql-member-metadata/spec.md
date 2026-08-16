## ADDED Requirements

### Requirement: Structured metadata on known wrappers
Existing rich GraphQL object, service, property, action, parameter, and collection wrappers SHALL expose additive structured metadata justified by the reference-app analysis.

#### Scenario: Client addresses a known member
- **WHEN** a client requests metadata beneath a known semantic member wrapper
- **THEN** applicable constraints, semantics, and optional presentation hints are available as structured fields
- **AND** absent semantics use documented null or default behavior

### Requirement: Independent friendly name and description
Canonical friendly name and description SHALL be independently queryable and SHALL NOT depend on clients interpreting one GraphQL field description as both.

#### Scenario: Member has both values
- **WHEN** Causeway supplies a friendly name and a distinct description
- **THEN** the wrapper returns each value independently

#### Scenario: Only a friendly name exists
- **WHEN** Causeway supplies no description
- **THEN** the canonical friendly name remains available
- **AND** the description does not duplicate it unless documented translation policy requires that result

### Requirement: Property metadata
Rich property metadata SHALL expose confirmed framework-neutral constraints and editing or navigation hints while preserving server validation authority.

#### Scenario: Constrained property is inspected
- **WHEN** a property declares confirmed optionality, length, regular-expression, accepted-file, multiline, typical-length, label-position, or navigation semantics
- **THEN** the applicable values are available as structured metadata
- **AND** a client may use them without bypassing server validation

### Requirement: Action and parameter metadata
Rich action and parameter metadata SHALL expose confirmed framework-neutral semantics and optional invocation or presentation hints.

#### Scenario: Action metadata is inspected
- **WHEN** an action declares confirmed semantics, prompt style, association, position, sequence, icon, CSS, or redirect hints
- **THEN** the applicable metadata is queryable independently from dynamic hidden and disabled state

#### Scenario: Client ignores a hint
- **WHEN** a client does not implement an optional action hint
- **THEN** validation and invocation remain semantically correct

### Requirement: Collection metadata
Rich collection metadata SHALL expose confirmed naming, presentation, configured page-size, ordering, sequence, icon, and CSS semantics where applicable.

#### Scenario: Collection metadata is inspected
- **WHEN** a collection has confirmed layout or ordering facets
- **THEN** the wrapper returns the applicable local metadata
- **AND** complete structural placement remains the responsibility of the grid resource

### Requirement: No duplicate structural metadata API
Member metadata SHALL complement standard introspection and layout resources rather than introduce a duplicate member list or complete layout serialization.

#### Scenario: Client discovers type members
- **WHEN** a client needs property, action, and collection identifiers
- **THEN** it uses standard targeted GraphQL introspection
- **AND** no separate metamodel member-list field is required

#### Scenario: Client needs complete page structure
- **WHEN** complete grid or menu structure is available
- **THEN** the client uses the referenced layout resource
- **AND** wrapper metadata supplies only local semantics

### Requirement: Metadata authorization safety
Structured metadata SHALL NOT reveal hidden members, sensitive values, or authorization policy rules.

#### Scenario: Member is hidden for the current user
- **WHEN** runtime authorization hides a member
- **THEN** metadata responses do not disclose sensitive member state beyond the established hidden contract

### Requirement: Backward-compatible metadata expansion
Metadata additions SHALL preserve established generated names, field descriptions, query and mutation operations, and runtime behavioral fields.

#### Scenario: Existing GraphQL client executes
- **WHEN** a client uses an established document without new metadata fields
- **THEN** its operation and response shape remain valid
