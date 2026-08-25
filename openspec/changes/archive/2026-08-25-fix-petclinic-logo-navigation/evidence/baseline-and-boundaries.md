# Baseline and authority boundaries

## Reproduced defect

The focused `PetClinicHtmxPlaywrightTest.routesHomeObjectsHistoryCollectionsAndResponsiveLayout` regression failed before implementation after clicking `.causeway-shell-brand` from the Visit route.
The server returned the `/htmx` landing fragment, but the route remained on the generic welcome card because module-startup `resolveHome()` was not re-entered after the HTMX swap.
With the corrected regression wait and the pre-change JavaScript restored, the baseline Maven invocation exited 1 after a 30-second timeout waiting for the Petclinic home object route.

## Preserved boundaries

- Application-entry GraphQL remains authoritative for home identity and availability.
- The brand remains an ordinary origin-local canonical route link to `/htmx`.
- The root controller continues to return a bounded landing fragment rather than embedding Petclinic identity.
- `homeObjectIdentity`, canonical route encoding, replacement history, policy overrides, and unavailable-home messages retain their existing contracts.
- Public Causeway components, menus, action dispatch, object contexts, page resources, CSP, toolkit selection, and native full-page fallback remain unchanged.
- No protected value is introduced into markup, events, errors, diagnostics, or route evidence.
