## ADDED Requirements

### Requirement: Already-signed-in user continues to original destination

When the Wicket viewer's sign-in page is constructed for a session that is already signed in, and Wicket has recorded an original destination (because an earlier request for an `@AuthorizeInstantiation`-protected page was intercepted), the viewer SHALL redirect the user to that original destination instead of the home page.

#### Scenario: Deep link with transparent external authentication
- **WHEN** a user with no existing Wicket session requests a bookmarkable page URL such as `/wicket/entity/<logicalType>:<id>`, and the request is already authenticated by an external mechanism (e.g. Spring Security)
- **THEN** after any intermediate redirects the user SHALL be shown that entity page, not the home page

#### Scenario: Request after session expiry with transparent re-authentication
- **WHEN** a user's HTTP session has expired and their next full-page request is for a bookmarkable page URL that is transparently re-authenticated
- **THEN** the user SHALL be shown the requested page, not the home page

#### Scenario: Intercepted then signed in
- **WHEN** a request for a protected page was intercepted to the sign-in page, and on the sign-in request the session is signed in
- **THEN** the sign-in page SHALL redirect to the intercepted URL

### Requirement: Home page fallback without original destination

When the sign-in page is constructed for a session that is already signed in and no original destination is recorded, the viewer SHALL restart at the home page, as before.

#### Scenario: Signed-in user visits sign-in page directly
- **WHEN** a signed-in user navigates directly to the sign-in page with no prior intercepted request
- **THEN** the user SHALL be redirected to the home page

#### Scenario: Original destination clearing is configured
- **WHEN** `causeway.viewer.wicket.clear-original-destination` is `true` and a signed-in user reaches the sign-in page with an original destination recorded
- **THEN** the user SHALL be redirected to the home page

### Requirement: No continuation when reporting an exception

When the sign-in page is constructed with an exception model (the page is reporting a failure, such as a page that failed during construction), the viewer SHALL NOT continue to the original destination, so that a failing page cannot cause a redirect loop.

#### Scenario: Original destination fails during construction
- **WHEN** an externally-authenticated user requests a page whose construction throws, causing the sign-in page to be rendered with an exception model
- **THEN** the viewer SHALL NOT redirect back to the failing page, and SHALL behave as it did before this change

### Requirement: Interactive sign-in unchanged

Interactive (username/password) sign-in SHALL continue to the original destination, or the home page, exactly as before.

#### Scenario: Form sign-in after intercept
- **WHEN** a user who is not signed in requests a protected page, is shown the sign-in form, and signs in successfully
- **THEN** the user SHALL be redirected to the originally requested page
