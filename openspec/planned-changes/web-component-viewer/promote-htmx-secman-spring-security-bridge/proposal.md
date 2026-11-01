## Why

The initial HTMX local-authentication change is intentionally allowed to copy and adapt SecMan-to-Spring bridging inside an optional viewer-specific integration so it can proceed without changing established security modules.
Once that implementation and its tests prove the contract, retaining viewer-specific copies would duplicate credential lookup, principal conversion, user refinement, account-status handling, and security maintenance that should ultimately be reusable by any Spring-secured Causeway frontend.

## What Changes

- Inventory the HTMX-specific bridge after local authentication is complete and compare it with the Estatio-derived `UserDetailsService`, authentication converters, current Causeway `AuthenticationConverterOfUserDetailsPrincipal`, and SecMan `UserMementoRefinerFromApplicationUser` contracts.
- Define a reusable SecMan-backed Spring username/password authentication integration in the appropriate Causeway security or SecMan integration module rather than in a viewer module.
- Provide controlled interaction and transaction boundaries for SecMan user lookup during Spring authentication.
- Preserve SecMan password encoding, locked-user behavior, roles, tenancy, language, number formatting, time formatting, and any other established `UserMemento` refinement.
- Remove or deprecate the HTMX-specific bridge only after equivalent shared behavior is available and covered by compatibility tests.
- Keep login pages, saved-route policy, shell chrome, CSRF request decoration, session-expiry navigation, and viewer logout presentation owned by the HTMX integration rather than moving viewer concerns into security modules.
- Preserve existing Causeway security defaults and make the shared bridge opt-in so applications that use bypass, Wicket-native authentication, pre-authentication, bearer tokens, or custom Spring providers are unaffected.
- Define compatibility and migration guidance for applications with custom authentication converters or `UserDetailsService` implementations.
- Add framework-level tests for valid, invalid, absent, locked, and refined SecMan users and consumer tests proving the HTMX viewer behaves unchanged after switching to the shared bridge.

## Capabilities

### New Capabilities

- `secman-spring-form-authentication-bridge`: Provides reusable opt-in Spring username/password authentication backed by SecMan and complete Causeway user refinement.

### Modified Capabilities

- `htmx-viewer-local-secman-authentication`: Replace the provisional viewer-specific bridge with the shared integration without changing user-visible authentication behavior.

## Impact

This future refactoring affects Causeway Spring security and SecMan integration modules, optional auto-configuration, authentication conversion and refinement, the provisional HTMX bridge, compatibility tests, and documentation.
It depends on evidence from the completed HTMX-specific implementation and must remain separately scoped from local viewer login delivery.
The draft requires architecture review, complete OpenSpec artifacts, and strict validation before promotion.
