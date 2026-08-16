## ADDED Requirements

### Requirement: Valid same-origin resource references
Every enabled rich GraphQL metadata or value resource reference SHALL resolve against the application's current origin and configured GraphQL path without introducing a network authority.

#### Scenario: Resource is published under a root deployment
- **WHEN** GraphQL returns an enabled resource reference
- **THEN** a standards-compliant browser resolves it to the current application origin
- **AND** the path does not begin with a protocol-relative authority

#### Scenario: Resource is published under a prefixed deployment
- **WHEN** the application uses a servlet context, reverse-proxy prefix, or non-default GraphQL endpoint path
- **THEN** the returned reference retains the effective deployment path
- **AND** encoded object identity is not double encoded

### Requirement: Independent resource-category policy
The GraphQL viewer SHALL apply independent policy to structural metadata resources and domain value-content resources.

#### Scenario: Structural metadata is enabled and value content is forbidden
- **WHEN** a client requests an authorized effective grid and an authorized Blob link
- **THEN** the grid capability is available
- **AND** the Blob content capability is absent or null according to the documented schema shape

#### Scenario: Resource category is forbidden
- **WHEN** policy forbids a resource category
- **THEN** GraphQL does not publish a usable object-bearing reference for that category
- **AND** direct endpoint attempts remain forbidden

### Requirement: Resource dereference authorization
Every resource dereference SHALL authenticate the request and re-evaluate target identity, member visibility, authorization, and category policy.

#### Scenario: Authorized resource is fetched
- **WHEN** the current user may access the target, member, and enabled category
- **THEN** the endpoint returns the declared resource with appropriate media and cache metadata

#### Scenario: Hidden or unauthorized resource is fetched
- **WHEN** the target or member is hidden, absent, stale, or unauthorized
- **THEN** the endpoint returns a bounded non-disclosing failure
- **AND** no resource content or authorization rule appears in the response, logs, or diagnostics

### Requirement: Compatible policy migration
The corrected resource contract SHALL provide documented migration from the existing global resource response-type setting.

#### Scenario: Existing global setting is present
- **WHEN** an application has not configured the new resource categories independently
- **THEN** the viewer applies the documented temporary compatibility mapping
- **AND** emits at most a bounded startup diagnostic without sensitive values
