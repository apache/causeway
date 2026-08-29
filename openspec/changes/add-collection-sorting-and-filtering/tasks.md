## 1. Rich GraphQL collection criteria

- [ ] 1.1 Add backward-compatible bounded sort and search arguments plus capability metadata to rich collection windows.
- [ ] 1.2 Apply accepted `DataTableInteractive` filtering and single-column sorting before slicing while preserving authorization, configured ordering, and bounded validation.
- [ ] 1.3 Add model and end-to-end GraphQL tests for discovery, sorting, filtering, invalid criteria, counts, paging, and unchanged offset-and-size documents.

## 2. Foundation criteria transport

- [ ] 2.1 Discover criteria arguments and capability fields and include normalized criteria in collection-window operation variables and cache identities.
- [ ] 2.2 Carry current criteria through initial loads, paging, refresh, virtual ranges, broker generations, and stale-contract checks.
- [ ] 2.3 Add operation, context, range, and compatibility tests proving complete-window semantics and safe fallback on older schemas.

## 3. Collection presentation

- [ ] 3.1 Add observed default-off `sortable` and `filterable` attributes with bounded reactive criterion state and offset-zero reload.
- [ ] 3.2 Render accessible native and Grid header sort controls plus one capability-gated bounded quick-search control without exposing toolkit APIs.
- [ ] 3.3 Preserve focus, empty/loading/error presentation, native fallback, paging, associated actions, and responsive transitions under active criteria.
- [ ] 3.4 Update foundation usage documentation and adapter/collection tests for defaults, criteria cycles, filtering, stale work, and unsupported capability behavior.

## 4. Petclinic demonstration

- [ ] 4.1 Add bounded owner filtering tokens through an application `CollectionFilterService`.
- [ ] 4.2 Opt selected Petclinic collections into sorting and filtering while leaving unselected collections unchanged.
- [ ] 4.3 Update Petclinic integration and Playwright coverage for cross-page sorting, filtering, clearing, accessibility, and associated-action continuity.

## 5. Verification

- [ ] 5.1 Run GraphQL model and end-to-end tests, foundation Node and Maven tests, Petclinic integration and default/native browser tests, RAT, compilation or inspections, diff checks, and strict OpenSpec validation.
