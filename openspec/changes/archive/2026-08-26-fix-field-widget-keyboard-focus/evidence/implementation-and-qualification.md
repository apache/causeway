# Implementation and qualification

## Implementation

Qualified optional non-protected field adapters now disable Vaadin's intentionally non-focusable built-in clear control and append a Causeway-owned native button through the public `suffix` slot.
The button is explicitly tabbable, uses a bounded `Clear <label>` accessible name, retains the visible multiplication-sign affordance, and has a stable Causeway class and derived test hook.
Required, disabled, protected, password, checkbox, select, unsupported, and explicit-native controls do not gain the suffix.

Initial upgrade, wrapper value assignment, input, and change events synchronize clear visibility.
Keyboard or pointer activation clears the Vaadin control, publishes the ordinary bubbling input path, hides the now-inapplicable button, and returns focus to the field.
Focus movement between the field and its suffix is treated as internal widget movement and does not prematurely commit an action parameter; departure from the complete widget retains the existing commit signal.

Property interaction rendering now restores Save or Cancel synchronously when the corresponding owned action had focus before markup replacement.
Save remains keyboard-focusable while busy or invalid through `aria-disabled`, and the property click controller suppresses its activation until validation permits saving.
Deferred lazy-editor and selection restoration remains unchanged.

No private toolkit shadow part, second value state, GraphQL behavior, public element name, codec, route, dependency, CSP rule, associated action, or native fallback changed.

## Qualification

- Focused field-widget suite: 9 passed, zero failed.
- Focused interaction suite: 18 passed, zero failed.
- Complete foundation Node suite: 175 passed, zero failed.
- HTMX route-policy Node suite: 5 passed, zero failed.
- Petclinic Java integration suite: passed.
- Focused real-browser editor, clear, Save, and Cancel keyboard sequence: passed.
- Complete Petclinic Vaadin-default Playwright matrix: 4 passed, zero failed.
- Complete Petclinic explicit-native Playwright matrix: 4 passed, zero failed.
- Foundation RAT check: passed.
- Petclinic RAT check: passed.
- Ordinary reactor packaging: passed and produced a 53,891-byte Petclinic jar without `BOOT-INF`.
- Strict OpenSpec validation: passed.
- Git whitespace validation: passed.
