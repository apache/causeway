## ADDED Requirements

### Requirement: Generic HTMX framework Logout safety

The generic HTMX viewer SHALL treat `causeway.security.LogoutMenu#logout` as a host-owned authentication operation and MUST NOT invoke it through GraphQL by default.
When no authentication integration claims that operation, the viewer SHALL omit its ordinary menu affordance and SHALL cancel stale or custom action requests for the same exact identity.
An installed authentication integration SHALL remain responsible for presenting an accessible logout control, submitting the configured method and current CSRF evidence, clearing the session, and selecting the post-logout destination.

#### Scenario: Generic HTMX viewer has no authentication integration

- **WHEN** application menus contain the framework Logout action but the HTMX host has no registered logout capability
- **THEN** the action is absent from ordinary semantic menu controls
- **AND** the viewer does not imply that a browser session can be ended

#### Scenario: Stale Logout request is published

- **WHEN** stale or custom markup publishes an action request for the exact framework Logout identity without a host logout claim
- **THEN** the HTMX viewer cancels the request before GraphQL validation or invocation
- **AND** it emits a bounded unavailable-operation announcement or diagnostic without a successful result

#### Scenario: Authenticated HTMX integration is active

- **WHEN** an installed HTMX authentication integration claims Logout and supplies its host-owned control
- **THEN** the existing protected logout flow remains available outside domain action invocation
- **AND** its endpoint, HTTP method, CSRF, session, cookie, and redirect policies remain authoritative

#### Scenario: Similar application action is present

- **WHEN** another service exposes a similarly named action or a local-resource result containing `/logout`
- **THEN** the HTMX viewer does not suppress or reinterpret it as framework Logout
- **AND** normal action and local-resource policies apply

### Requirement: HTMX local-resource result navigation

The HTMX viewer SHALL resolve a local-resource semantic result against the current application context and SHALL perform full-document navigation according to its supported opening strategy.
It MUST validate the resolved target as same-origin and application-local before navigation and MUST NOT interpret any target path as an authentication operation.

#### Scenario: Same-window local resource is returned

- **WHEN** an action returns a valid local-resource result with `SAME_WINDOW`
- **THEN** the HTMX host navigates the current browsing context to the context-aware local target
- **AND** it does not issue an HTMX fragment request or canonical object-route transition

#### Scenario: New-window local resource is returned

- **WHEN** an action returns a valid local-resource result with `NEW_WINDOW`
- **THEN** the HTMX host requests a new opener-isolated browsing context for the local target
- **AND** the current object route remains unchanged

#### Scenario: Local resource target is unsafe

- **WHEN** a result path is malformed, scheme-relative, cross-origin, credential-bearing, outside the configured application-local boundary, or paired with an unknown strategy
- **THEN** the HTMX host refuses navigation and reports a bounded result-policy error
- **AND** no target value is repaired, rewritten to another origin, or treated as Logout

#### Scenario: Application uses a servlet context path

- **WHEN** the HTMX application is deployed beneath a non-root servlet context and returns an application-local resource path
- **THEN** resolution preserves the authoritative servlet context exactly once
- **AND** the viewer does not confuse the HTMX route base with the application context
