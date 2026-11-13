# Final gate results

## Passed

- Core configuration reactor and autocomplete validation tests: `BUILD SUCCESS`.
- GraphQL model reactor, concurrent registry test, and existing model tests: `BUILD SUCCESS`.
- Foundation Node suite: 154 tests passed with zero failures.
- Web-component foundation and HTMX Maven reactor: `BUILD SUCCESS`.
- Focused Reference Application window and legacy GraphQL integration: `BUILD SUCCESS`.
- Petclinic candidate Playwright: 4 tests passed with zero failures, errors, or skips.
- Petclinic explicit native Playwright: 4 tests passed with zero failures, errors, or skips.
- Reference Application ordinary clean package, integration tests, inventory tests, and deterministic fixtures: `BUILD SUCCESS`.
- Reference Application candidate Playwright: 9 tests passed with zero failures, errors, or skips.
- Reference Application focused explicit native autocomplete journey: passed.
- Clean and incremental capability inventory retained SHA-256 `75ef904a0d4fbc9c915c74866cdbd503743dab589f7525bbab126baf1eaa024a` byte-for-byte.
- Vaadin production-like pilot: 5 scenarios with zero axe, CSP, console, page, external-request, or overflow failures.
- Core configuration, GraphQL model, HTMX, and Reference Application RAT checks: `BUILD SUCCESS`.
- Strict OpenSpec validation, `git diff --check`, and production-isolation review: passed.

## Corrected executable outcomes

Property and action-parameter wrappers retain legacy autocomplete and advertise bounded response windows.
The Reference Application seven-result filter returns a five-item first page and two-item later page with accurate count and continuation metadata.
The unchanged legacy list exactly matches concatenated window order.
The internal Vaadin data provider selects an item absent from page zero and submits it through the existing action contract.
Native mode obtains the first page, reports additional matches honestly, selects an authoritative reference, and submits without sending raw search text as an object input.
Invalid windows are rejected without exposing the search value.

## Retained limits and next step

Each page request materializes one authoritative application autocomplete result before slicing; persistence query pushdown, cursors, and cross-request snapshots remain outside the public metamodel contract.
The standalone foundation RAT goal still reports the same pre-existing `vaadin-reference/README.adoc` and generated `vaadin-reference.js` resources, neither introduced nor modified here.
The next recommended change is `expand-vaadin-semantic-editor-families`.
