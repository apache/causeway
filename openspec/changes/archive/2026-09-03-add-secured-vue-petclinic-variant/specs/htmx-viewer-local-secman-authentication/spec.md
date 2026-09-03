## ADDED Requirements

### Requirement: HTMX authentication consumes the shared bridge

The HTMX local SecMan integration SHALL consume the presentation-neutral web-component SecMan Spring bridge for credential lookup, principal state, Causeway user conversion, and DAO authentication-provider construction.
It MUST retain ownership of HTMX paths, login presentation, request caching, CSRF transport, session-expiry handling, shell chrome, logout, and security-chain scope.

#### Scenario: Secured HTMX starts after bridge extraction

- **WHEN** the HTMX SecMan integration is imported
- **THEN** the shared bridge supplies equivalent credential and principal behavior
- **AND** existing HTMX routes, login, CSRF, user context, session, and logout outcomes remain unchanged

#### Scenario: Vue security is absent

- **WHEN** an application installs only the HTMX SecMan integration
- **THEN** no Vue route, asset, authentication context, policy, or security chain is introduced
- **AND** the shared bridge remains presentation-neutral

### Requirement: Dedicated secured HTMX launcher

The HTMX Petclinic sample SHALL provide a repository-root-relative `run-secured.sh` launcher that selects the established secured Maven profile while retaining the environment and argument behavior of `run.sh`.

#### Scenario: Secured HTMX script runs

- **WHEN** a maintainer executes `viewers/webcomponents/sample-htmx-petclinic/run-secured.sh`
- **THEN** Maven launches `PetClinicHtmxSecuredApplication` through the secured profile
- **AND** `JAVA_HOME`, `MVN`, and additional Maven arguments are honored

#### Scenario: Ordinary HTMX script runs

- **WHEN** a maintainer executes the existing `run.sh`
- **THEN** the ordinary HTMX and Wicket comparison runtime remains selected
- **AND** no local SecMan form integration is enabled by the new launcher

## MODIFIED Requirements

### Requirement: Existing security implementations remain unchanged

The HTMX integration MUST consume the optional presentation-neutral web-component SecMan Spring bridge and MUST NOT modify existing Causeway core security, Spring security, SecMan, Wicket, or OAuth implementation code.
The shared bridge SHALL remain isolated from HTMX routes and presentation while making credential and principal behavior reusable by another explicitly installed web-component host.

#### Scenario: Shared bridge is consumed

- **WHEN** local HTMX authentication is installed
- **THEN** shared Causeway security modules retain their existing defaults and implementation behavior
- **AND** credential lookup and principal conversion come from the optional presentation-neutral bridge
- **AND** HTMX-specific security policy remains isolated in the HTMX integration

#### Scenario: Existing application does not opt in

- **WHEN** an application upgrades without importing an optional viewer authentication integration
- **THEN** no new security bean, filter chain, login route, logout route, or SecMan lookup is activated

### Requirement: Authenticated principal preserves SecMan user context

A successful local login SHALL establish a Causeway `UserMemento` for the authenticated username with the effective SecMan roles, tenancy path, language, number locale, and time locale available at authentication time.
The shared converter MUST operate without an unscoped repository access and MUST erase credential material according to Spring Security lifecycle behavior.

#### Scenario: User has roles and profile preferences

- **WHEN** a local user with SecMan roles, tenancy, and locale preferences signs in
- **THEN** authenticated HTMX and GraphQL requests execute with the corresponding refined `UserMemento`
- **AND** SecMan authorization sees the same effective username and roles

#### Scenario: Authentication completes

- **WHEN** Spring finishes validating the local credentials
- **THEN** raw credentials are absent from the session principal and authentication diagnostics
- **AND** retained browser-session state contains no plaintext password
