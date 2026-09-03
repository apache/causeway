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
