# Implementation and qualification

## Implementation

Each semantic menubar now observes bubbling `focusout` at its own component boundary.
When `relatedTarget` remains inside that menubar, the active panel remains open.
When `relatedTarget` is absent or outside the menubar, the existing non-restoring expanded-menu closure synchronizes disclosure `aria-expanded` and controlled-panel `hidden` state.
The handler does not prevent the focus transition, restore focus, dispatch an action, collapse the responsive bar, or install a document-global focus listener.

Foundation coverage verifies internal movement, external movement, absent next focus, unchanged focus counts, synchronized panel state, and unchanged action request counts.
Petclinic acceptance focuses the `listUpcoming` menu action, presses Tab, waits for focus to leave the primary menubar, and requires the Visits panel to close before reopening and invoking the action normally.

## Qualification

- Pre-change focused foundation regression: 5 tests ran, 4 passed, and 1 failed on stale `aria-expanded="true"`.
- Post-change focused foundation regression: 5 passed, zero failed.
- Complete foundation Node suite: 170 passed, zero failed.
- HTMX route-policy Node suite: 5 passed, zero failed.
- Petclinic Java integration suite: passed.
- Petclinic Vaadin-default Playwright matrix: 4 passed, zero failed.
- Petclinic explicit-native Playwright matrix: 4 passed, zero failed.
- Foundation RAT check: passed.
- Petclinic RAT check: passed.
- Ordinary reactor packaging: passed and produced a 53,745-byte Petclinic jar without `BOOT-INF`.
- Strict OpenSpec validation: passed.
- Git whitespace validation: passed.

No GraphQL schema, menu operation, menu resource, action dispatch contract, route, CSP hash, toolkit adapter, dependency, lockfile, or public semantic component vocabulary changed.
