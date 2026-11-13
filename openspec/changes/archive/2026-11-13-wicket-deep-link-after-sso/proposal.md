## Why

When the Wicket viewer runs behind an external authentication mechanism (Spring Security with OAuth2/OIDC or a pre-authenticating proxy, Keycloak), a user who follows a deep link (for example `/wicket/entity/<logicalType>:<id>` in an email) always lands on the home page instead of the requested object. The same happens when the HTTP session has expired and the user's next request re-authenticates transparently. The requested URL survives the external authentication; it is discarded inside the Wicket viewer.

The cause is the interaction of two pieces of viewer code:

- `WebRequestCycleForCauseway.onBeginRequest` only primes the `AuthenticatedWebSessionForCauseway` from the externally-provided `InteractionContext` when a Wicket `Session` already exists. On the first request of a new session the page's `@AuthorizeInstantiation` check therefore fails, and Wicket throws `RestartResponseAtInterceptPageException` to the sign-in page, recording the requested URL as the original destination.
- On the follow-up request the session exists, is primed and signed in, and the `WicketSignInPage` constructor unconditionally calls `restartAt(PageType.HOME)`, ignoring the recorded original destination.

## What Changes

- `WicketSignInPage`, when constructed for a user who is already signed in, SHALL continue to the original destination recorded by Wicket's intercept mechanism, if any, and only otherwise restart at the home page.
- That continuation SHALL NOT happen when the sign-in page is rendered to report an exception (an `ExceptionModel` is present), so that a page failing during construction cannot cause a redirect loop.
- Interactive (username/password) sign-in behaviour is unchanged.

## Capabilities

### New Capabilities
- `wicket-sign-in-original-destination`: how the Wicket viewer's sign-in page routes an already-authenticated user, preserving the originally requested page after transparent (external) authentication or session expiry.

### Modified Capabilities

(none)

## Impact

- Code: `viewers/wicket/ui` — `WicketSignInPage` (and possibly a small helper on `PageNavigationService` if the continuation is better expressed there).
- Tests: new WicketTester-based test(s) in `viewers/wicket/ui-test`.
- Applications: any Causeway 2.x app using Spring Security (`security/spring`, `extensions/security/spring-oauth2`) or Keycloak with the Wicket viewer benefits without configuration changes. Estatio is the motivating consumer.
- No API, configuration or dependency changes.
