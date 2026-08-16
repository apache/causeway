## ADDED Requirements

### Requirement: Discoverable effective menu bars
The rich GraphQL application-entry contract SHALL expose the effective Causeway primary, secondary, and tertiary menu-bar structure through a documented secured representation.

#### Scenario: Application has effective menu bars
- **WHEN** an authorized client requests application-entry metadata
- **THEN** it can distinguish primary, secondary, and tertiary bars
- **AND** recover stable menu, section, and entry ordering

#### Scenario: Application uses generated fallback menus
- **WHEN** no explicit menu-bars layout exists and Causeway generates an effective model
- **THEN** the contract exposes the effective generated model under the same semantics

### Requirement: Canonical service-action references
Every menu entry SHALL resolve to the established rich service-action contract using logical service type and semantic action ID.

#### Scenario: Client selects a menu entry
- **WHEN** the entry references a valid visible service action
- **THEN** the client can address its existing hidden, disabled, parameter, validation, invocation, and result fields
- **AND** no duplicate action execution endpoint is required

#### Scenario: Layout reference is invalid
- **WHEN** a menu entry references a missing or incompatible service action
- **THEN** a bounded diagnostic identifies the invalid reference
- **AND** unrelated valid menu entries remain available

### Requirement: Menu presentation semantics
The application-entry contract SHALL preserve effective labels, descriptions, icons, supported hints, grouping, and ordering without prescribing HTML rendering.

#### Scenario: Menu metadata is localized
- **WHEN** the request context selects a supported locale
- **THEN** effective localized menu presentation is returned according to documented cache semantics

#### Scenario: Client ignores an optional hint
- **WHEN** a client does not implement an icon, CSS, or other optional presentation hint
- **THEN** the menu entry remains semantically invokable

### Requirement: Menu authorization safety
Menu discovery SHALL honor current visibility without disclosing hidden actions or authorization policy rules.

#### Scenario: Service action is hidden
- **WHEN** the current user cannot see a service action
- **THEN** application-entry data does not disclose the hidden action as an available menu entry

#### Scenario: Visibility context changes
- **WHEN** user, role, locale, or another menu-affecting context changes
- **THEN** cached menu data is not reused outside its valid scope

### Requirement: Home-page action discovery
The application-entry contract SHALL identify the configured home-page action and owning service when available.

#### Scenario: Home-page action exists
- **WHEN** the application configures a valid visible home-page action
- **THEN** GraphQL returns its logical service type and semantic action ID
- **AND** invocation reuses the established service-action contract

#### Scenario: Home page is absent or unavailable
- **WHEN** no usable home-page action exists for the current context
- **THEN** GraphQL returns documented absence without inventing navigation behavior

### Requirement: Framework-neutral application entry points
The application-entry contract SHALL NOT prescribe menus, routes, automatic home invocation, authentication screens, or action-result navigation.

#### Scenario: Different clients consume the contract
- **WHEN** web components, HTMX, or another frontend reads application-entry metadata
- **THEN** each can apply its own navigation and presentation policy over the same semantic entries
