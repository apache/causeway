## Why

The Petclinic sample currently contains too few owners, pets, and visits to look representative or to demonstrate realistic collection paging.
The very small page sizes also make the demo feel test-oriented rather than application-oriented.

## What Changes

- Expand the deterministic Petclinic seed dataset with additional owners, pets, past visits, and upcoming visits while preserving established fixture identities used by existing journeys.
- Ensure the seeded data exceeds the configured pet and visit page sizes so paging is visible and meaningful.
- Configure owner and pet collections with a page size of 5.
- Configure visit-history, nested pet-visit, and upcoming-visit collections with a page size of 10.
- Update integration and browser assertions to qualify the richer data and realistic paging without weakening existing mutation, filtering, sorting, navigation, or row-peek coverage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `generic-htmx-web-component-viewer`: Petclinic demo data and selective collection paging requirements will cover a richer deterministic dataset and realistic page sizes for owner/pet and visit collections.

## Impact

The change affects Petclinic seed-data configuration, Petclinic HTML page resources, focused integration tests, Playwright journeys, and the generic HTMX viewer Petclinic qualification specification.
It does not change public component APIs, GraphQL semantics, persistence schema, or viewer defaults outside the sample application.
