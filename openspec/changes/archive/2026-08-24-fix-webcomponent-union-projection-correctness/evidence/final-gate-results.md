# Final gate results

## Passed

- Foundation Node suite: 148 tests passed with zero failures.
- Web-component foundation and HTMX Maven tests: `BUILD SUCCESS`.
- GraphQL model reactor, merged-union registry test, and existing model tests: `BUILD SUCCESS`.
- Pinned Reference Application union membership and fragment integration: `BUILD SUCCESS`.
- Petclinic integration and Playwright reactor: `BUILD SUCCESS`.
- Petclinic Playwright: 4 tests passed with zero failures, errors, or skips.
- Reference Application ordinary clean package, integration tests, inventory tests, and deterministic fixtures: `BUILD SUCCESS`.
- Incremental capability inventory retained SHA-256 `75ef904a0d4fbc9c915c74866cdbd503743dab589f7525bbab126baf1eaa024a`, identical to the clean baseline.
- Reference Application Playwright: 9 tests passed with zero failures, errors, or skips.
- Vaadin production-like pilot: 5 scenarios with zero axe, CSP, console, page, external-request, or overflow failures.
- GraphQL model and Reference Application RAT checks: `BUILD SUCCESS`.
- Strict OpenSpec validation and `git diff --check`: passed.

## Corrected executable outcomes

`rich__demo_ValueHolder__gqlv_union` advertises 28 deduplicated concrete types rather than one first-registration member.
`demo.ActionChoicesFromPage.objects` reaches `ready` through bounded typename probing and a concrete `rich__demo_ActionChoicesFromEntity` fragment.
Concrete rows preserve semantic identity, object links, values, and hydrated contexts.
Declared `demo.CollectionTypeOfPage.children` remains a ready concrete collection, while unreadable raw `otherChildren` remains an explicit bounded local error.

## Retained gap and warning

Long opaque composite routes remain the next focused correction.
The standalone foundation RAT goal still reports the same pre-existing `vaadin-reference/README.adoc` and generated `vaadin-reference.js` resources, neither introduced nor modified here.
