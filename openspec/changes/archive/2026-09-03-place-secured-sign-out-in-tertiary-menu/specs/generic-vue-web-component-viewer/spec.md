## MODIFIED Requirements

### Requirement: Generic Vue framework Logout safety

The generic Vue viewer SHALL classify `causeway.security.LogoutMenu#logout` as a host-owned authentication operation and MUST NOT invoke it through GraphQL by default.
When no application authentication policy claims that operation, the viewer SHALL omit its ordinary menu affordance and SHALL cancel stale or custom requests for the exact identity.
An application MAY explicitly retain that exact action in its authoritative semantic menu location, apply a host-owned presentation label, and claim activation before GraphQL.
The authentication integration SHALL own endpoint selection, HTTP method, current anti-forgery evidence, session cleanup, and post-logout navigation.

#### Scenario: Vue application has no authentication integration

- **WHEN** application menus contain the framework Logout action and no Vue host logout capability is configured
- **THEN** the action is absent from ordinary semantic menu controls
- **AND** direct or stale requests cannot reach GraphQL invocation

#### Scenario: Vue application supplies a logout policy

- **WHEN** an application explicitly registers menu-presentation and action policies for the exact framework Logout identity
- **THEN** the authorized action remains in its authoritative semantic menu location with the host-owned label
- **AND** the viewer delegates activation before GraphQL dispatch
- **AND** the application remains responsible for a secure accessible logout affordance and complete browser-session behavior

#### Scenario: Similar application action is present

- **WHEN** another service exposes a similarly named action or a local-resource result containing `/logout`
- **THEN** the Vue viewer does not suppress, relabel, or reinterpret it as framework Logout
- **AND** ordinary action or local-resource policy applies

### Requirement: Authentication-neutral secured-host integration points

The generic Vue viewer SHALL remain authentication-neutral while allowing an application-owned secured shell to supply an authenticated GraphQL executor, exact menu-action presentation policy, native logout form, and exact pre-invocation Logout policy.
The viewer MUST NOT fetch credentials or CSRF state, create login or logout endpoints, persist tokens, infer authentication from route or result paths, or manufacture separate authentication chrome.

#### Scenario: Secured application binds an executor

- **WHEN** an authenticated Vue application binds a CSRF-decorating executor to its stable `<cw-graphql-client>`
- **THEN** semantic components use that executor without knowing the authentication mechanism
- **AND** generic routing, contexts, interaction results, and lifecycle remain unchanged

#### Scenario: Secured application owns logout through the menu

- **WHEN** an application opts exact framework Logout into semantic menu presentation with a host label, retains a native POST form, and claims exact framework Logout requests
- **THEN** the host policy can submit its native logout contract before GraphQL invocation
- **AND** the viewer neither manufactures nor duplicates visible authentication chrome

#### Scenario: Generic application has no authentication context

- **WHEN** the same Vue application runs without an authentication integration
- **THEN** it may omit executor decoration, menu opt-in, and the native logout form
- **AND** the generic viewer introduces no authentication request, endpoint, or framework Logout affordance
