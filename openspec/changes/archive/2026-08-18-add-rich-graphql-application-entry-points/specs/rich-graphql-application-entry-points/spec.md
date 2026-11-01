## ADDED Requirements

### Requirement: Discoverable effective menu-bars resource
The rich GraphQL application-entry contract SHALL expose the authorized effective Causeway menu-bars layout through a documented secured structural resource.

#### Scenario: Application has effective menu bars
- **WHEN** an authorized client requests application-entry metadata
- **THEN** it receives a valid same-origin menu-bars resource reference with media type and format version
- **AND** the resource preserves primary, secondary, tertiary, menu, section, entry, and ordering semantics

#### Scenario: Application uses generated fallback menus
- **WHEN** no explicit menu-bars layout exists and Causeway generates an effective model
- **THEN** the same resource capability exposes the effective generated model

### Requirement: Canonical service-action references
Every menu resource entry SHALL resolve to the established rich service-action contract using public logical service type and semantic action ID.

#### Scenario: Client selects a menu entry
- **WHEN** the entry references a valid visible service action
- **THEN** the client can address its existing hidden, disabled, parameter, validation, invocation, and result fields
- **AND** no duplicate action execution endpoint is required

#### Scenario: Layout reference is invalid
- **WHEN** a menu entry references a missing or incompatible service action
- **THEN** a bounded diagnostic identifies the invalid reference without sensitive data
- **AND** unrelated valid menu structure remains available

### Requirement: Menu presentation semantics remain structural
The effective menu resource SHALL preserve labels, descriptions, icons, supported hints, grouping, and ordering without duplicating the full structure as GraphQL wrapper fields or prescribing HTML rendering.

#### Scenario: Menu metadata is localized
- **WHEN** the request context selects a supported locale
- **THEN** the effective localized menu resource is returned according to documented cache semantics

#### Scenario: Client ignores an optional hint
- **WHEN** a client does not implement an icon, CSS, or another optional presentation hint
- **THEN** the referenced service action remains semantically invokable

### Requirement: Menu authorization safety
Menu discovery SHALL honor current visibility without disclosing hidden values or authorization policy rules.

#### Scenario: Service action is hidden
- **WHEN** the current user cannot see a service action
- **THEN** the effective application-entry representation does not present it as an available menu entry

#### Scenario: Visibility context changes
- **WHEN** user, role, locale, layout generation, or another menu-affecting context changes
- **THEN** effective menu data is generated within the current interaction
- **AND** the resource uses `private, no-store` so it is not reused outside that context

### Requirement: Configured home-page discovery and resolution
The application-entry contract SHALL identify and resolve the domain object returned by `HomePageResolverService` without requiring clients to invent an object identifier.

#### Scenario: Home page is a domain object
- **WHEN** the application configures a valid visible `@HomePage` domain-object type
- **THEN** GraphQL identifies its public logical type
- **AND** resolves the current concrete rich object through the framework home-page behavior

#### Scenario: Resolver does not return a supported domain object
- **WHEN** the configured resolver returns a non-domain, hidden, invalid, or unresolvable value
- **THEN** GraphQL does not advertise an unsupported home kind
- **AND** returns documented absence or a bounded non-disclosing diagnostic

#### Scenario: Home page is absent or unavailable
- **WHEN** no usable home entry exists for the current context
- **THEN** GraphQL returns documented absence or a bounded non-disclosing diagnostic
- **AND** does not invent navigation behavior

### Requirement: Framework-neutral application entry points
The application-entry contract SHALL NOT prescribe HTML menus, routes, automatic home invocation, authentication screens, or action-result navigation.

#### Scenario: Different clients consume the contract
- **WHEN** web components, HTMX, or another frontend reads application-entry metadata
- **THEN** each can apply its own navigation and presentation policy over the same semantic entries
