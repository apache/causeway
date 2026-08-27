## 1. Viewer Routing

- [ ] 1.1 Track the current void-action route refresh in the HTMX shell without affecting unrelated navigation.
- [ ] 1.2 Redirect a matching post-action `NOT_FOUND` object state to the configured home route while preserving the successful result.
- [ ] 1.3 Clear post-action recovery state when the target remains available or a different terminal condition occurs.

## 2. Regression Coverage

- [ ] 2.1 Update the Petclinic destructive Playwright journey to assert home navigation after `PetOwner#delete`.
- [ ] 2.2 Run focused HTMX viewer and Petclinic validation checks.
