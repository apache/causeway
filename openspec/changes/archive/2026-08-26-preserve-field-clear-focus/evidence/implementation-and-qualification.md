# Implementation and qualification

## Implementation

The internal field adapter now accepts `focusClear()` before or after toolkit upgrade.
It retains the request while no control exists, then focuses only a connected visible Causeway clear suffix after installation.
An empty or ineligible field expires the adapter request without moving focus, and generation/disconnection checks continue to reject stale adapters.

The property now tracks clear focus independently through local `focusin` and `focusout` events.
Removing the focused suffix during property-owned rendering does not erase the intent.
While consecutive validating and editing renders temporarily leave body focus active, each replacement `<causeway-field-editor>` receives the same clear-focus request.
Focus entering the editor, Save, Cancel, or another owned control clears the intent, as does genuine focus departure beyond the property.

Save/Cancel synchronous restoration and deferred editor selection restoration remain unchanged.
The validating/editing status transition and its brief visual update remain semantically unchanged; only focus continuity is corrected.

## Qualification

- Focused field-widget suite: 10 passed, zero failed.
- Focused interaction suite: 19 passed, zero failed.
- Complete foundation Node suite: 177 passed, zero failed.
- HTMX route-policy Node suite: 5 passed, zero failed.
- Petclinic Java integration suite: passed.
- Focused browser regression holds Clear for 750 ms through validation, then continues to Save: passed.
- Complete Petclinic Vaadin-default Playwright matrix: 4 passed, zero failed.
- Complete Petclinic explicit-native Playwright matrix: 4 passed, zero failed.
- Foundation RAT check: passed.
- Petclinic RAT check: passed.
- Ordinary reactor packaging: passed and produced a 53,891-byte Petclinic jar without `BOOT-INF`.
- Strict OpenSpec validation: passed.
- Git whitespace validation: passed.

No GraphQL, validation timing, status announcement, public element, semantic event, route, CSP, dependency, toolkit asset, protected-value, or native-fallback contract changed.
