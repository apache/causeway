# Final gate results

## Passed implementation gates

- HTMX resource loader accepts strict literal UTF-8 pages from exploded directories and jars.
- Invalid name, empty content, malformed UTF-8, NUL content, unreadable resource, discovery failure, 256-KiB overflow, 512-page overflow, and every duplicate-source combination are rejected deterministically.
- Existing Java fragment factories remain compatible and mixed resource/factory duplicates fail startup.
- An absent exact registration retains `<causeway-object editable>` and effective-grid fallback.
- Route identity remains escaped outside literal resource content and every page retains one context and one interaction controller.
- Private page URLs return HTTP 404 rather than exposing raw resources.
- Petclinic packages exactly four HTML-authored pages and no `PetClinicHomeFragmentFactory` class.
- All retained menu, grid, and column-order resources remain byte-identical to the planning baseline.
- Ordinary packaging with Spring Boot repackage explicitly skipped produces a 53,852-byte jar containing all page and fallback resources and no `BOOT-INF` entry.

## Passed automated gates

- Foundation Node suite: 163 passed, zero failed.
- HTMX Node suite: 5 passed, zero failed.
- HTMX Java suite: 44 passed, zero failed.
- Petclinic integration suite: 5 passed, zero failed.
- GraphQL model compatibility suite: 31 passed, zero failed.
- Petclinic Vaadin-default Playwright matrix: 4 passed, zero failed.
- Petclinic explicit-native Playwright matrix: 4 passed, zero failed.
- HTMX RAT check: passed.
- Petclinic RAT check: passed.
- Strict OpenSpec validation: passed.
- Git whitespace validation: passed.

## Browser and policy gates

Both browser matrices cover all four resource pages, direct routes, home replacement, object links, history back and forward, focus restoration, responsive layout, menus, property updates, validation, action prompts, cancellation, collections, object results, scalar results, and navigation.
The suites fail on unsuccessful GraphQL responses, CSP violations surfaced to the page, console errors, page errors, failed requests, horizontal overflow, lost focus, duplicate requests, or protected-value disclosure.

Vaadin-default delivery remains route-lazy by family.
Native mode uses the identical HTML resources while making no Vaadin request and receiving no Vaadin CSP hash.
Both modes retain `style-src-attr 'none'` and never add `unsafe-inline`.

## IDE validation note

The optional IntelliJ incremental build could not select among multiple open IDE projects because the MCP build tool exposes no project-path parameter.
The Maven reactors, Java suites, Node suites, integration suites, browser matrices, packaging checks, and RAT checks all passed, so this tooling limitation is not a release blocker.
