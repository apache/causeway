## ADDED Requirements

### Requirement: Authentication-neutral secured-host integration points

The generic Vue viewer SHALL remain authentication-neutral while allowing an application-owned secured shell to supply an authenticated GraphQL executor, current-user chrome, native logout form, and exact pre-invocation Logout policy.
The viewer MUST NOT fetch credentials or CSRF state, create login or logout endpoints, persist tokens, or infer authentication from route or result paths.

#### Scenario: Secured application binds an executor

- **WHEN** an authenticated Vue application binds a CSRF-decorating executor to its stable `<cw-graphql-client>`
- **THEN** semantic components use that executor without knowing the authentication mechanism
- **AND** generic routing, contexts, interaction results, and lifecycle remain unchanged

#### Scenario: Secured application owns logout chrome

- **WHEN** an application renders current-user and POST logout controls in its stable shell and claims exact framework Logout requests
- **THEN** the host policy can submit its native logout contract before GraphQL invocation
- **AND** the viewer neither manufactures nor duplicates authentication chrome

#### Scenario: Generic application has no authentication context

- **WHEN** the same Vue application runs without an authentication integration
- **THEN** it may omit executor decoration and secured-session chrome
- **AND** the generic viewer introduces no authentication request or endpoint

### Requirement: Dedicated secured Vue launcher

The Vue Petclinic sample SHALL provide a repository-root-relative `run-secured.sh` launcher that selects its secured Maven profile while preserving the environment and argument behavior of `run.sh`.

#### Scenario: Secured Vue script runs

- **WHEN** a maintainer executes `viewers/webcomponents/sample-vue-petclinic/run-secured.sh`
- **THEN** Maven launches the secured Vue Petclinic application and local login flow
- **AND** `JAVA_HOME`, `MVN`, and additional Maven arguments are honored

#### Scenario: Ordinary Vue script runs

- **WHEN** a maintainer executes the existing Vue `run.sh`
- **THEN** the bypass-secured ordinary Vue application remains selected
- **AND** the new secured integration remains opt-in
