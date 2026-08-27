## Why

Session-based authentication for the HTMX Web Components viewer requires CSRF protection for GraphQL mutations, logout, and other state-changing browser requests.
`CausewayModuleSecuritySpring` already preserves its current default behavior while allowing applications to retain Spring CSRF filters through `causeway.security.spring.allow-csrf-filters`, but Wicket compatibility with that mode has not been established.
Applications should eventually be able to run Wicket and the HTMX viewer together under one CSRF-safe Spring Security configuration rather than choosing between viewer coexistence and browser-request protection.

## What Changes

- Preserve the existing `CausewayModuleSecuritySpring` default and its opt-in `allow-csrf-filters` configuration contract.
- Establish current evidence for Wicket behavior when Spring CSRF filters remain enabled, including ordinary forms, Ajax actions, file operations, sign-in, and logout.
- Define one server-owned CSRF token contract that Wicket and the HTMX viewer can use without weakening protection for either viewer.
- Make Wicket submit valid CSRF evidence for protected state-changing requests where current integration does not already do so.
- Keep stateless bearer-token API chains independently configurable so they do not acquire an unnecessary browser CSRF contract.
- Add coexistence regression coverage proving that Wicket and HTMX can share an authenticated Spring session while CSRF protection remains enabled.
- Document migration and diagnostics for applications that previously relied on globally disabled Spring CSRF filters.

## Capabilities

### New Capabilities

- `csrf-safe-viewer-coexistence`: Defines CSRF-safe Spring Security integration for co-located Wicket and HTMX browser viewers.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Require the authenticated HTMX host to publish and submit the configured CSRF token without changing the framework-wide default.

## Impact

This future change affects Spring Security integration, Wicket request and Ajax submission, the HTMX shell and GraphQL executor, logout handling, configuration diagnostics, browser regression suites, and security documentation.
It must not silently change existing applications from CSRF-disabled to CSRF-enabled behavior.
The draft requires fresh investigation and complete OpenSpec artifacts before promotion.
