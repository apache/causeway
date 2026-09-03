## ADDED Requirements

### Requirement: Vue host-owned action policy

The Vue viewer SHALL expose a pre-invocation action policy that receives canonical action identity, a single-claim token, and the current immutable viewer-policy context.
A claimed action MUST NOT proceed to foundation GraphQL validation or invocation, and asynchronous policy failure SHALL reach the configured viewer error policy without creating a successful action result.
The policy SHALL compose with application-owned routing and lifecycle disposal without introducing authentication state into Vue route components.

#### Scenario: Vue action policy claims a request

- **WHEN** the configured Vue action policy claims a service or object action request
- **THEN** ordinary GraphQL dispatch is canceled before invocation
- **AND** the application policy owns any replacement effect

#### Scenario: Vue action policy does not claim a request

- **WHEN** the configured action policy returns without claiming an ordinary action
- **THEN** existing parameter, validation, confirmation, invocation, result, refresh, and navigation behavior continues
- **AND** the action is not duplicated

#### Scenario: Vue action policy fails

- **WHEN** a synchronous or asynchronous action policy throws or rejects
- **THEN** the viewer reports the error through its configured error policy
- **AND** fail-closed actions such as framework Logout do not fall through to GraphQL invocation

### Requirement: Generic Vue framework Logout safety

The generic Vue viewer SHALL classify `causeway.security.LogoutMenu#logout` as a host-owned authentication operation and MUST NOT invoke it through GraphQL by default.
When no application authentication policy claims that operation, the viewer SHALL omit its ordinary menu affordance and SHALL cancel stale or custom requests for the exact identity.
A future authentication integration SHALL own endpoint selection, HTTP method, current anti-forgery evidence, session cleanup, and post-logout navigation.

#### Scenario: Vue application has no authentication integration

- **WHEN** application menus contain the framework Logout action and no Vue host logout capability is configured
- **THEN** the action is absent from ordinary semantic menu controls
- **AND** direct or stale requests cannot reach GraphQL invocation

#### Scenario: Vue application supplies a logout policy

- **WHEN** an application explicitly registers a policy that claims the framework Logout identity
- **THEN** the viewer delegates the operation before GraphQL dispatch
- **AND** the application remains responsible for a secure accessible logout affordance and complete browser-session behavior

#### Scenario: Similar application action is present

- **WHEN** another service exposes a similarly named action or a local-resource result containing `/logout`
- **THEN** the Vue viewer does not suppress or reinterpret it as framework Logout
- **AND** ordinary action or local-resource policy applies

### Requirement: Vue local-resource result navigation

The Vue viewer SHALL interpret a local-resource semantic result through a bounded host policy and a documented application-local resource base.
Its default behavior SHALL perform validated full-document same-origin navigation according to `SAME_WINDOW` or `NEW_WINDOW` and MUST NOT send the target through Vue Router.

#### Scenario: Same-window local resource is returned

- **WHEN** an action returns a valid local-resource result with `SAME_WINDOW`
- **THEN** the Vue host performs full-document navigation to the resolved application-local target
- **AND** the canonical object router does not claim the target

#### Scenario: New-window local resource is returned

- **WHEN** an action returns a valid local-resource result with `NEW_WINDOW`
- **THEN** the Vue host requests a new opener-isolated browsing context for the resolved target
- **AND** the current route generation remains active unless ordinary lifecycle policy later changes it

#### Scenario: Application claims local-resource navigation

- **WHEN** the configured result policy claims a valid local-resource result
- **THEN** default browser navigation does not run
- **AND** the application can apply stricter deployment policy without mutating the canonical result

#### Scenario: Local resource target is unsafe

- **WHEN** a path is malformed, scheme-relative, cross-origin, credential-bearing, outside the configured application-local boundary, or paired with an unknown strategy
- **THEN** the Vue viewer refuses navigation and reports the failure through its error policy
- **AND** it does not repair the value, route it as a domain object, or infer Logout semantics

#### Scenario: Vue application uses a nested deployment context

- **WHEN** the application configures a non-root local-resource base and receives an application-local path
- **THEN** resolution preserves that deployment context exactly once
- **AND** it remains independent of the Vue object-route base path
