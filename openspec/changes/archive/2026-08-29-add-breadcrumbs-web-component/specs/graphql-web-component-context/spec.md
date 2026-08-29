## ADDED Requirements

### Requirement: Semantic breadcrumbs requirement
The object context SHALL accept a semantic breadcrumbs read requirement and SHALL translate it to the supported current-object identity, title, and navigable-ancestor metadata fields without exposing generated GraphQL names to the consumer.

#### Scenario: Breadcrumb consumer joins initial composition
- **WHEN** a breadcrumbs consumer and other domain components register during the same rendering turn
- **THEN** the context coalesces their selections into one initial object operation
- **AND** breadcrumb metadata is returned through the consumer's local requirement state

#### Scenario: Breadcrumb consumer connects later
- **WHEN** breadcrumbs are registered after an object snapshot already exists without their selection
- **THEN** the context loads only the missing metadata selection
- **AND** merges it into the immutable current snapshot

#### Scenario: Breadcrumb consumer disconnects
- **WHEN** the consumer releases its requirement
- **THEN** the requirement is omitted from subsequent complete refresh projections
- **AND** release alone causes no network request

### Requirement: Targeted breadcrumb schema discovery
The GraphQL client SHALL discover the shared breadcrumb-entry object type through targeted introspection only when it is reachable from requested object metadata.

#### Scenario: Metadata supports breadcrumb entries
- **WHEN** an object description exposes `breadcrumbs` as a list of breadcrumb-entry objects
- **THEN** targeted discovery loads that entry type and its supported fields
- **AND** the semantic requirement selects only supported identity and title fields

#### Scenario: Schema lacks breadcrumb support
- **WHEN** the introspected metadata type has no `breadcrumbs` field or no usable entry identity fields
- **THEN** the breadcrumb requirement reports an unsupported local state
- **AND** existing header and member requirements remain usable

### Requirement: Breadcrumb partial-error isolation
The object context SHALL associate a GraphQL error on navigable breadcrumb metadata with the breadcrumbs requirement while preserving successful sibling metadata and member state.

#### Scenario: Ancestor traversal fails
- **WHEN** the object response contains current metadata and member data plus an error at the breadcrumb field path
- **THEN** the breadcrumbs consumer receives a partial-error state
- **AND** successful header, property, action, and collection consumers retain their normal state
