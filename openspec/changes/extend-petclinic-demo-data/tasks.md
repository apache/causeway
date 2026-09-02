## 1. Deterministic demo data

- [ ] 1.1 Extend the existing Petclinic seed graph additively with stable owners and varied pets while preserving established fixture identities and values.
- [ ] 1.2 Add deterministic past and future visits with stable IDs and clock-relative dates so owner history and clinic-wide upcoming visits exceed their page boundaries.
- [ ] 1.3 Add integration assertions for deterministic totals, representative sparse and populated owners, preserved fixtures, and idempotent startup behavior.

## 2. Realistic collection paging

- [ ] 2.1 Configure home owner and owner pet collections with page size 5.
- [ ] 2.2 Configure upcoming, owner-history, and nested pet-visit collections with page size 10.
- [ ] 2.3 Update resource and integration assertions for the explicit page-size policy.

## 3. Browser qualification

- [ ] 3.1 Update stable browser expectations affected by the richer seed graph without relying on incidental insertion order.
- [ ] 3.2 Qualify owner, pet, visit-history, upcoming-visit, sorting, filtering, navigation, mutation, compact sizing, and row-peek journeys under the new page boundaries.
- [ ] 3.3 Run default Vaadin and supported native Playwright suites and retain clean console, network, focus, overflow, and lifecycle behavior.

## 4. Regression and archive readiness

- [ ] 4.1 Run focused and full Petclinic integration tests with Java 21 and perform the applicable IDE build/inspection checks.
- [ ] 4.2 Run strict OpenSpec validation and `git diff --check`.
