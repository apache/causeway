# Final gate results

## Passed semantic and lifecycle gates

- Only direct action children are marked as associated declarations.
- Arbitrary descendant actions continue their independent semantic lifecycle without an association marker.
- Parser-late insertion preserves source order and creates no duplicate action node or requirement.
- Property loading, hidden, disabled, ready, editing, validating, cancellation, reconciliation, error, and toolkit fallback update only the stable primary region.
- Collection metadata, hidden, disabled, inactive, loading, empty, table, paging, error, refresh, disconnect, and reconnect update only the stable primary region.
- Interleaved collection columns and actions produce exactly the declared row fields and action order.
- Hidden or disabled owners do not override independently visible or enabled actions.
- Hidden or disabled actions retain their established GraphQL-authoritative behavior.
- Action button events bubble through nested owners to the existing interaction controller exactly once.
- Route disconnection and reconnection release and register each owner and action requirement exactly once per connection.

## Passed automated gates

- Foundation Node suite: 170 passed, zero failed.
- HTMX route-policy Node suite: 5 passed, zero failed.
- Petclinic integration suite: 5 passed, zero failed.
- GraphQL model compatibility suite: 31 passed, zero failed.
- Petclinic Vaadin-default Playwright matrix: 4 passed, zero failed.
- Petclinic explicit-native Playwright matrix: 4 passed, zero failed.
- Foundation RAT check: passed.
- Petclinic RAT check: passed.
- Strict OpenSpec validation: passed.
- Git whitespace validation: passed.

## Browser, accessibility, and request gates

Both browser matrices verify direct property and collection nesting, ordered action children, wide and 390-pixel responsive presentation, no horizontal overflow, keyboard focus restoration, disabled reasons, prompt defaults, validation, cancellation, scalar and object outcomes, navigation, history, collection refresh, CSP, console output, and failed requests.
Existing theme coverage retains light, dark, reduced-motion, and application-variable behavior because the new structural rules use current-color-independent layout and inherited `--causeway-*` values.

Captured GraphQL POST bodies prove exactly one mutation for each successful `updateName`, `addPet`, `bookVisit`, and `removePet` activation.
Validation and cancellation produce no successful mutation.
Owner rerendering does not add action requirement requests.

Vaadin-default mode retains independently route-lazy qualified editor families.
Explicit native mode uses the identical nested HTML, emits no Vaadin asset request, and receives no Vaadin CSP hash.
Both modes retain `style-src-attr 'none'` and add no `unsafe-inline` policy.

## Packaging and isolation gates

Ordinary packaging with Spring Boot repackage skipped produces a 53,745-byte Petclinic jar without `BOOT-INF`.
The foundation jar contains `META-INF/resources/causeway-webcomponents/member-composition.mjs`.
No frontend package installation, JavaScript bundling, CDN retrieval, or executable repackaging is required.

No GraphQL, dispatch, codec, autocomplete, collection-window, route, Vaadin closure, CSP hash, dependency, lockfile, menu, grid, fixture, or retained layout resource changed.

## IDE validation note

The optional IntelliJ incremental build could not select among multiple open IDE projects because the MCP build tool exposes no project-path parameter.
The Maven reactors, Node suites, Java suites, integration suites, browser matrices, packaging checks, compatibility suite, and RAT checks all passed, so this tooling limitation is not a release blocker.
