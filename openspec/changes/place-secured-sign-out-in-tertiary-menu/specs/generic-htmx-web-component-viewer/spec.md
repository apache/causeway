## MODIFIED Requirements

### Requirement: Generic HTMX framework Logout safety

The generic HTMX viewer SHALL treat `causeway.security.LogoutMenu#logout` as a host-owned authentication operation and MUST NOT invoke it through GraphQL by default.
When no authentication integration claims that operation, the viewer SHALL omit its ordinary menu affordance and SHALL cancel stale or custom action requests for the same exact identity.
An installed authentication integration MAY explicitly retain that exact action in its authoritative semantic menu location, apply a host-owned presentation label, and claim activation before GraphQL.
The integration SHALL remain responsible for presenting an accessible logout affordance, submitting the configured method and current CSRF evidence, clearing the session, and selecting the post-logout destination.

#### Scenario: Generic HTMX viewer has no authentication integration

- **WHEN** application menus contain the framework Logout action but the HTMX host has no registered logout capability
- **THEN** the action is absent from ordinary semantic menu controls
- **AND** the viewer does not imply that a browser session can be ended

#### Scenario: Stale Logout request is published

- **WHEN** stale or custom markup publishes an action request for the exact framework Logout identity without a host logout claim
- **THEN** the HTMX viewer cancels the request before GraphQL validation or invocation
- **AND** it emits a bounded unavailable-operation announcement or diagnostic without a successful result

#### Scenario: Authenticated HTMX integration is active

- **WHEN** an installed HTMX authentication integration explicitly owns exact framework Logout presentation and invocation
- **THEN** the authorized action remains in its authoritative semantic menu location with the host-owned label
- **AND** activation uses the protected host logout flow outside domain action invocation
- **AND** its endpoint, HTTP method, CSRF, session, cookie, and redirect policies remain authoritative

#### Scenario: Similar application action is present

- **WHEN** another service exposes a similarly named action or a local-resource result containing `/logout`
- **THEN** the HTMX viewer does not suppress, relabel, or reinterpret it as framework Logout
- **AND** normal action and local-resource policies apply
