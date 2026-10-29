## 1. Reproduce

- [ ] 1.1 Add a WicketTester-based test in `viewers/wicket/ui-test` (alongside `EntityPagePrimingTest`) that simulates an externally-authenticated user with no Wicket session requesting a bookmarkable protected page, and asserts the rendered page; confirm it currently ends on the home page
- [ ] 1.2 Add a test for the direct case: signed-in session + recorded `InterceptData` → constructing `WicketSignInPage` currently restarts at HOME

## 2. Fix

- [ ] 2.1 In `WicketSignInPage`'s signed-in branch, call `continueToOriginalDestination()` before `pageNavigationService.restartAt(PageType.HOME)`, unless an `ExceptionModel` is present or `clearOriginalDestination` is configured
- [ ] 2.2 Make the tests from 1.1 and 1.2 pass

## 3. Guard behaviour

- [ ] 3.1 Test: signed-in user visiting the sign-in page with no original destination → HOME
- [ ] 3.2 Test: `clear-original-destination=true` → HOME
- [ ] 3.3 Test: sign-in page constructed with an `ExceptionModel` and recorded original destination → no redirect to the original destination (no loop)
- [ ] 3.4 Run the existing `viewers/wicket` module tests (ui, ui-test, viewer) and confirm no regressions in interactive sign-in

## 4. Verify in a consuming application

- [ ] 4.1 Build the framework (`mvn clean install -Drevision=2.2.0-SNAPSHOT ...` for affected modules) and verify manually with a Spring Security–based app (e.g. Estatio in `local` form-login mode): open `/wicket/entity/<logicalType>:<id>` in a fresh browser session → lands on the entity page
- [ ] 4.2 Verify session expiry case (shorten `server.servlet.session.timeout`, wait, reload a bookmarkable URL → same page)
