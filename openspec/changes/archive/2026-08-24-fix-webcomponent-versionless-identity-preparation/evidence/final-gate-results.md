# Final gate results

## Passed

- Foundation Node suite: 137 tests passed with zero failures.
- Web-component foundation and HTMX Maven tests: `BUILD SUCCESS`.
- GraphQL model reactor and tests: `BUILD SUCCESS`.
- Pinned Reference Application versionless metadata and concrete-row integration: `BUILD SUCCESS`.
- Petclinic integration and Playwright reactor: `BUILD SUCCESS`.
- Petclinic Playwright: 4 tests passed with zero failures, errors, or skips.
- Reference Application ordinary clean package, integration tests, inventory tests, and deterministic fixtures: `BUILD SUCCESS`.
- Incremental capability inventory generation retained SHA-256 `75ef904a0d4fbc9c915c74866cdbd503743dab589f7525bbab126baf1eaa024a`, identical to the clean baseline.
- Reference Application Playwright: 9 tests passed with zero failures, errors, or skips.
- Vaadin production-like pilot: 5 scenarios with zero axe, CSP, console, page, external-request, or overflow failures.
- GraphQL model and Reference Application RAT checks: `BUILD SUCCESS`.
- New foundation test source is approved as Apache License 2.0 by RAT.
- Strict OpenSpec validation and `git diff --check`: passed.

## Corrected executable outcomes

`demo.ActionAutoCompletePage.selectTvCharacter` now returns versionless autocomplete candidates, accepts the selected typed identity, closes the prompt, and retains the authoritative route.
`demo.CollectionLayoutPagedPage.children` and `moreChildren` now reach `ready` without requesting `_meta.version` from `demo.CollectionLayoutPagedChildVm`.
The deterministic populated collection renders thirteen semantic object links.
Versioned entity property and collection fixtures continue to request and preserve advertised versions.

## Retained gaps and warning

General union-fragment projection and long opaque composite routes remain separate focused changes.
The standalone foundation RAT goal still reports the same pre-existing `vaadin-reference/README.adoc` and generated `vaadin-reference.js` resources.
Neither resource is introduced or modified by this change, and RAT approves the newly added test file.
