## Context

With external authentication (Spring Security `oauth2Login()`, a pre-authenticating proxy feeding `RequestHeaderAuthenticationFilter`, or Keycloak), `SpringSecurityFilter` establishes the `InteractionContext` before Wicket runs. The Wicket session is then "signed in" lazily: `WebRequestCycleForCauseway.onBeginRequest` primes `AuthenticatedWebSessionForCauseway` and `getInteractionContext()` calls `signIn(true)`. This only happens when `Session.exists()`.

Request sequence today for a deep link with no existing session (verified by code reading at `871f4502`):

```
#1 GET /wicket/entity/X        (already authenticated by Spring)
   onBeginRequest: no Session → return (not primed, not signed in)
   EntityPage @AuthorizeInstantiation → getRoles() == null → unauthorized
   → RestartResponseAtInterceptPageException(SIGN_IN)  [stores InterceptData{url=/wicket/entity/X}]
   → 302 /wicket/signin

#2 GET /wicket/signin
   onBeginRequest: Session exists → primed → signIn(true)
   WicketSignInPage(): isSignedIn() → restartAt(HOME)     ← InterceptData ignored
   → HOME
```

An expired HTTP session (default 30 minutes) loses both the Spring security context and the Wicket session, so the next request follows the same path.

Constraints: Causeway 2.x is in maintenance and being decommissioned; prefer minimal-risk, targeted fixes (see `AGENTS.md`). Wicket is 9.17.0.

## Goals / Non-Goals

**Goals:**
- An externally-authenticated user following a bookmarkable deep link lands on that page.
- After session expiry with transparent re-authentication, a full-page request to a bookmarkable URL lands on that page.
- No redirect loops; interactive sign-in and explicit visits to the sign-in page behave as before.

**Non-Goals:**
- Removing the intermediate redirect via the sign-in page (priming the Wicket session on the very first request). This would be a cleaner fix but changes session creation for every security integration (Shiro, Keycloak, Spring, bypass); left as a possible follow-up.
- Preserving page-instance state (`?n` page versions, unsaved dialog state, Ajax listener requests) across session expiry. Non-bookmarkable `/wicket/page?n` URLs cannot be recovered once the session is gone.
- Changing Spring Security's saved-request handling or anything outside the Wicket viewer.

## Decisions

### D1: Continue to original destination in `WicketSignInPage` (chosen)

In the constructor branch where the session is already signed in, call `continueToOriginalDestination()` before `pageNavigationService.restartAt(PageType.HOME)`. In Wicket 9, `Component#continueToOriginalDestination()` throws `NonResettingRestartException` to the stored URL when `InterceptData` is present, and returns normally (no-op) otherwise, so the existing HOME fallback is retained.

Alternatives considered:
- *Prime the Wicket session on the first request* (`onBeginRequest` creating/binding a session when an external `InteractionContext` is present, or lazily signing in from `getRoles()`): removes the bounce, but alters session creation semantics across all security integrations and is harder to reason about for resource/Ajax requests. Rejected for this change (see Non-Goals).
- *Fix in each application* (custom `PageType.SIGN_IN` page class): works, but leaves the defect in the framework for every external-auth app.

### D2: Do not continue when an `ExceptionModel` is present

`PageAbstract` catches construction failures, invalidates the session and throws `RestartResponseAtInterceptPageException(signInPage)` with the exception exposed via `EXCEPTION` thread-local; `WebRequestCycleForCauseway.errorPageFor` likewise renders the sign-in page with an `ExceptionModel` when not signed in. For externally-authenticated users `invalidate()` is a no-op and the session will be signed in again, so continuing to the original destination would re-request the failing page indefinitely. In that case keep the current behaviour (restart at HOME / render the exception).

### D3: Respect `clearOriginalDestination` setting

`causeway.viewer.wicket.clear-original-destination` already disables continuation for interactive sign-in. Honour it here too, for consistency: when set, behave as today (HOME).

## Risks / Trade-offs

- [Stale `InterceptData` sends the user somewhere unexpected] → Wicket clears intercept data once the original URL is reached, and `RestartResponseAtInterceptPageException`'s mapper clears it on navigation elsewhere; only an already-signed-in visit to the sign-in page consumes it.
- [Original destination is a page that fails without throwing during construction (e.g. a bookmark of a deleted object)] → Rendered as the usual error/recognized-exception page rather than HOME; this is the same outcome as following the link with an existing session.
- [Loop via exception path] → mitigated by D2, and covered by a test.
- [Still one extra redirect via `/wicket/signin`] → accepted (see Non-Goals).

## Migration Plan

No configuration or API change. Applications pick it up on upgrading the framework build. Rollback is reverting the commit.

## Open Questions

- Should `PageNavigationService` gain a method such as `continueToOriginalDestinationElseRestartAt(PageType)` for reuse, or is the change best kept local to `WicketSignInPage`? Default: keep it local.
