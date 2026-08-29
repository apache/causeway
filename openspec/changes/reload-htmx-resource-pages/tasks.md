## 1. Configuration and resource model

- [ ] 1.1 Add the `CACHED` and `RELOAD` resource-page modes to HTMX viewer configuration with `CACHED` as the default and bounded enum binding.
- [ ] 1.2 Refactor private page resource decoding into one reusable bounded validation operation used at startup and during reload.
- [ ] 1.3 Extend resource page definitions to retain either immutable cached HTML or a reloadable resource-backed content provider without adding shared mutable state.

## 2. Viewer wiring and failure behavior

- [ ] 2.1 Wire the configured mode through classpath discovery while preserving startup enumeration, initial validation, duplicate detection, Java factory conflicts, and immutable registry identity.
- [ ] 2.2 Make `RELOAD` definitions reopen and validate only their selected registered resource for each render while leaving factory and generic-page behavior unchanged.
- [ ] 2.3 Preserve bounded safe reload failures without stale-content or generic-layout fallback and without exposing absolute resource paths.

## 3. Automated verification

- [ ] 3.1 Extend loader and page-definition unit tests for cached stability, current-content reload, repeated reload, and all existing content validation bounds.
- [ ] 3.2 Extend registry and renderer tests to prove page additions, deletions, renames, duplicates, and factory conflicts remain startup-bound in both modes.
- [ ] 3.3 Add HTTP integration coverage showing that a changed registered resource appears after refresh in `RELOAD` mode and remains unchanged in default `CACHED` mode.
- [ ] 3.4 Verify invalid configured modes fail binding and invalid reloaded content fails safely without stale or generic output.

## 4. Petclinic developer experience

- [ ] 4.1 Configure the Petclinic sample to opt into `RELOAD` mode and retain the production viewer default as `CACHED`.
- [ ] 4.2 Add Petclinic integration coverage for the configured mode and the edit-build-refresh resource workflow.
- [ ] 4.3 Document IntelliJ resource copying, browser refresh, automatic build, and the restart-required boundary for added, deleted, or renamed pages.

## 5. Validation

- [ ] 5.1 Run focused HTMX viewer unit and integration tests for both resource-page modes.
- [ ] 5.2 Run the Petclinic integration and browser acceptance suites to confirm route, CSP, cache-control, interaction, and page-selection behavior remain stable.
- [ ] 5.3 Run formatting, compilation, OpenSpec validation, and final diff checks.
