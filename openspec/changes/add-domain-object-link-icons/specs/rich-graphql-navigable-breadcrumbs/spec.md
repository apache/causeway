## MODIFIED Requirements

### Requirement: Navigable ancestor metadata
Each rich GraphQL object metadata type SHALL expose a nullable `breadcrumbs` field containing the current object's navigable ancestors as shared breadcrumb-entry objects with non-null `logicalTypeName`, `id`, and `title` fields.
When structural metadata responses are enabled, the shared breadcrumb-entry type SHALL additionally expose a nullable `icon` field containing the ancestor's authoritative icon resource URL.
The entries MUST be ordered from the root ancestor to the immediate parent and MUST NOT repeat the current object.

#### Scenario: Object has a navigable hierarchy
- **WHEN** the current object's metamodel resolves a parent and that parent resolves another parent
- **THEN** `breadcrumbs` returns both bookmarkable ancestors in root-to-parent order
- **AND** each entry contains its current request-local title and bookmark identity
- **AND** each entry contains its configured metadata icon resource URL when structural metadata is enabled

#### Scenario: Object has no navigable parent
- **WHEN** the current object's metamodel resolves no navigable parent
- **THEN** `breadcrumbs` returns an empty list
- **AND** existing current-object metadata remains unchanged

#### Scenario: Navigable parent is unavailable as a navigation target
- **WHEN** parent traversal reaches null or an object without a bookmark identity
- **THEN** traversal stops before adding an unusable entry
- **AND** no Java class name or arbitrary object string is exposed

#### Scenario: Structural metadata is forbidden
- **WHEN** GraphQL structural metadata response policy forbids icon resources
- **THEN** the shared breadcrumb-entry type does not advertise `icon`
- **AND** breadcrumb identity, title, ordering, and traversal remain available

#### Scenario: Ancestor icon is unavailable
- **WHEN** a bookmarkable breadcrumb ancestor has no resolvable icon resource URL
- **THEN** its nullable `icon` value is null
- **AND** its navigable identity and title remain available
