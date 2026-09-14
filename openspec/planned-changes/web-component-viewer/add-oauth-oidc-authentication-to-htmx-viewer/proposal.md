## Why

After local SecMan authentication establishes the HTMX viewer's session, login-page, CSRF, user-chrome, expiry, and logout boundaries, applications should be able to authenticate through standard OAuth 2.0 and OpenID Connect providers such as Microsoft Entra ID or Google without redesigning the viewer shell.
Provider integration is deferred because redirect flows, delegated-user provisioning, provider logout, configuration, and deterministic testing are materially more complex than local form login.

## What Changes

- Extend the authenticated HTMX login page with configuration-driven OAuth/OIDC provider choices while retaining local SecMan login when hybrid mode is enabled.
- Use Spring Security OAuth client support and the existing Causeway Spring OAuth principal conversion and SecMan delegated-user integration where their current contracts are sufficient.
- Converge successful local and OAuth/OIDC authentication on the same secure browser session, HTMX shell, CSRF behavior, saved-route restoration, session-expiry handling, and host-owned logout contract.
- Initially resolve delegated users through one explicitly configured claim such as `preferred_username`, UPN, or email, with `preferred_username` as the first reviewed default.
- Do not add `(issuer, subject)` persistence in this change; stable delegated identity mapping remains a separate planned SecMan enhancement.
- Define missing-user, locked-user, auto-created-user, claim-absence, claim-conflict, provider-error, cancelled-login, expired-state, and callback-failure outcomes without exposing provider tokens or sensitive claims.
- Define application-only logout separately from optional provider logout and support provider-specific capabilities without making one provider's endpoint shape the generic contract.
- Keep access and refresh tokens server-side and prevent HTMX components, browser storage, URLs, diagnostics, and semantic events from exposing them.
- Provide provider-neutral tests using controlled OIDC fixtures or a mock authorization server, plus configuration examples for Microsoft Entra ID and Google that do not require live-provider credentials in the ordinary build.
- Preserve the local-only deployment mode and require no OAuth dependencies or provider configuration when OAuth/OIDC support is absent.

## Capabilities

### New Capabilities

- `htmx-viewer-oauth-oidc-authentication`: Adds optional OAuth/OIDC and hybrid login to the established authenticated HTMX viewer session contract.

### Modified Capabilities

- `htmx-viewer-local-secman-authentication`: Extend the login surface and session policy to support configured delegated providers without weakening local-only mode.

## Impact

This future change affects the optional HTMX security integration, Spring OAuth client configuration, login and logout presentation, delegated SecMan lookup or auto-creation, provider callback and error handling, tests, and security documentation.
It depends on completion of local SecMan authentication and intentionally continues claim-based delegated lookup until the separate stable-identity proposal is implemented.
The draft requires current provider API review, a deterministic test strategy, complete OpenSpec artifacts, and strict validation before promotion.
