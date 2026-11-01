# web-component-secman-spring-authentication-bridge Specification

## Purpose

Provide an optional presentation-neutral bridge between local SecMan users, Spring Security authentication, and Causeway user context for explicitly secured web-component hosts.

## Requirements

### Requirement: Optional presentation-neutral SecMan Spring bridge

The project SHALL provide an explicitly imported SecMan-backed Spring username/password authentication bridge for web-component hosts.
The bridge MUST NOT install viewer routes, login pages, logout pages, request caches, security filter chains, or presentation behavior.

#### Scenario: Bridge is imported

- **WHEN** a host imports the shared bridge with Spring security, SecMan integration, and a SecMan password encoder available
- **THEN** it can authenticate local SecMan credentials through a Spring DAO authentication provider
- **AND** no HTMX or Vue dependency or route is introduced

#### Scenario: Bridge is absent

- **WHEN** an application does not import the shared bridge
- **THEN** no user-details service, authentication provider, or bridge-specific principal converter is installed
- **AND** existing bypass, bearer, pre-authentication, Wicket, or application security remains authoritative

### Requirement: SecMan credentials and status determine authentication

The bridge SHALL load a matching SecMan `ApplicationUser` inside a controlled anonymous interaction and SHALL authenticate its encrypted password through the configured SecMan `PasswordEncoder`.
Absent users, wrong passwords, locked users, passwordless users, and internal lookup failures MUST fail closed without disclosing the account condition.

#### Scenario: Valid unlocked user signs in

- **WHEN** an unlocked local user supplies the matching password
- **THEN** Spring authenticates the username without persisting plaintext credentials
- **AND** the resulting principal contains the bounded SecMan profile needed by Causeway

#### Scenario: Credentials are not usable

- **WHEN** the user is absent, locked, passwordless, or supplies an incorrect password
- **THEN** authentication fails with the same public invalid-credentials outcome
- **AND** the response does not distinguish the account condition

#### Scenario: User lookup fails

- **WHEN** SecMan lookup or controlled interaction fails unexpectedly
- **THEN** authentication fails closed
- **AND** no persistence exception, user record, password, role, tenancy, locale, or stack trace reaches the browser

### Requirement: Causeway principal preserves SecMan context

A successful authentication SHALL produce a serializable credentials-erasing principal and SHALL convert it to a Causeway `UserMemento` containing the effective username, roles, tenancy path, language locale, number locale, and time locale captured at authentication time.
The conversion MUST NOT require a second unscoped repository lookup.

#### Scenario: Authenticated user has profile context

- **WHEN** a SecMan user has roles, tenancy, and locale preferences
- **THEN** authenticated Causeway requests receive the corresponding refined `UserMemento`
- **AND** SecMan authorization observes the same username and effective roles

#### Scenario: Authentication credentials are erased

- **WHEN** Spring completes credential validation
- **THEN** plaintext and encrypted password material are absent from the retained session principal
- **AND** serialization and diagnostics expose no credential value

### Requirement: Existing HTMX behavior remains compatible

The shared bridge SHALL preserve the existing HTMX local SecMan authentication outcomes when that integration switches from its provisional local classes.

#### Scenario: HTMX consumes the bridge

- **WHEN** the secured HTMX integration authenticates valid, invalid, absent, locked, or passwordless users through the shared bridge
- **THEN** login outcomes, user context, CSRF behavior, session migration, and logout behavior remain unchanged
- **AND** HTMX-specific route and presentation policy remains outside the bridge
