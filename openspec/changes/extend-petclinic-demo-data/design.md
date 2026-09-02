## Context

Petclinic currently seeds four owners, five pets, and three upcoming visits.
The HTML resources use page sizes of one or two for selected collections, while pets and upcoming visits are unpaged.
Those choices are useful for narrow tests but make the running sample look sparse and unlike a realistic clinic dashboard.
Existing integration and browser journeys depend on stable identities such as Mary, Basil, Samantha, Helen, and Max, so enrichment must remain additive and deterministic.

## Goals / Non-Goals

**Goals:**

- Seed enough varied owners, pets, historical visits, and upcoming visits to make the dashboard and object pages representative.
- Preserve all established fixture IDs and values required by existing tests and demonstrations.
- Ensure at least one owner has more than five pets, at least one owner has more than ten visits, and the clinic has more than ten upcoming visits so each configured boundary is observable.
- Use page size 5 for owner and pet collections and page size 10 for visit collections.
- Keep both default Vaadin and explicit native browser journeys deterministic.

**Non-Goals:**

- Changing persistence mappings, public domain APIs, viewer-wide paging defaults, or GraphQL window semantics.
- Reproducing a production-scale dataset or introducing random data generation.
- Changing existing fixture identities, mutation targets, or canonical navigation behavior.

## Decisions

### Extend the existing seed graph additively

The existing four owners and their pets remain unchanged, and new owners, pets, and visits are added with stable IDs and clock-relative dates.
This avoids invalidating focused journeys while providing varied species, optional values, past history, and future appointments.
Replacing the seed graph wholesale was rejected because it would create unnecessary churn in unrelated interaction tests.

### Make every relevant collection boundary explicit

The home owner collection and owner pet collection use `paged="5"`.
The home upcoming-visits collection, owner visit-history collection, and nested pet-visits collection use `paged="10"`.
This is preferred over changing component defaults because the desired sizes are sample-specific presentation choices.

### Seed beyond both page boundaries

The deterministic graph includes more than five pets for one owner, more than ten visits for one owner, and more than ten upcoming visits clinic-wide.
This ensures that the larger sizes still exercise real previous/next behavior rather than merely changing markup.

### Preserve semantic test anchors

Tests continue to target stable IDs and names rather than relying on incidental insertion order where possible.
Count assertions are updated where they intentionally qualify the complete seeded dataset, while mutation journeys continue to use the established Mary, Basil, Samantha, Helen, and Max fixtures.

## Risks / Trade-offs

- [Risk] More rows increase browser-test work and may expose order-dependent assumptions. → Preserve established IDs, use deterministic timestamps, and update assertions to select by semantic identity.
- [Risk] Larger page sizes could stop existing pager journeys from crossing a boundary. → Seed beyond the new boundaries and explicitly qualify the first and subsequent ranges.
- [Risk] Clock-relative past and future visits may change rendered dates between runs. → Assert stable identities, reasons, range totals, and relative membership rather than hard-coded calendar dates.
- [Risk] A richer startup graph can make the configuration verbose. → Keep creation grouped by owner and use small helper methods for pets and visits without introducing a separate fixture framework.

## Migration Plan

No persisted production data migration is required because this is an in-memory sample seed.
Apply the additive seed and resource-page updates together, then update integration and browser expectations.
Rollback consists of reverting the seed additions and restoring the previous declarative page sizes.

## Open Questions

None.
