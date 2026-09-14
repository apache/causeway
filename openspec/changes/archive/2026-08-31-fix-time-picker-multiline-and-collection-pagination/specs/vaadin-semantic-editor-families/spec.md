## MODIFIED Requirements

### Requirement: Minute-resolution local-time entry
Each editable qualified Vaadin `LocalTime` or `LocalDateTime` control SHALL present time without seconds or fractional seconds and SHALL use the toolkit-supported 15-minute dropdown interval.
The adapter MUST use the public step contract, MUST NOT fabricate private dropdown items, and MUST NOT mutate authoritative state merely by entering edit mode.

#### Scenario: User edits a local time
- **WHEN** a property or action parameter opens an editable qualified `LocalTime` control
- **THEN** the visible entry omits seconds and fractional seconds
- **AND** the picker offers quarter-hour choices through Vaadin's supported overlay

#### Scenario: User edits a local date-time
- **WHEN** a property or action parameter opens an editable qualified `LocalDateTime` control
- **THEN** its integrated time field uses the same 15-minute dropdown interval
- **AND** the local date, local time, and absence of browser timezone conversion remain authoritative

#### Scenario: User cancels a precise existing value
- **WHEN** an authoritative value contains accepted seconds, fractional seconds, or a minute outside a quarter-hour choice and the user cancels without committing a changed picker value
- **THEN** the authoritative value remains unchanged
- **AND** no rounded or truncated value is submitted

#### Scenario: Value is read-only or uses native fallback
- **WHEN** the value is presented read-only or the Vaadin family is ineligible or disabled
- **THEN** its established precision and native semantic behavior remain unchanged

## ADDED Requirements

### Requirement: Visibly presented keyboard time overlay
Enter or Space activation of an editable qualified time-picker clock trigger SHALL synchronously present the current Vaadin time overlay while processing the trusted keyboard event.
The qualification contract MUST verify visible overlay presentation and MUST NOT treat the `opened` property alone as sufficient evidence.

#### Scenario: User opens time choices with Enter
- **WHEN** keyboard focus is on the current clock trigger and the user presses Enter
- **THEN** focus returns to the associated time input and the Vaadin overlay is visibly presented
- **AND** quarter-hour choices formatted to minutes are available without committing or cancelling the Causeway interaction

#### Scenario: User opens time choices with Space
- **WHEN** keyboard focus is on the current clock trigger and the user presses Space
- **THEN** the same visible overlay is presented synchronously
- **AND** page scrolling, action invocation, property save, and prompt submission do not occur

#### Scenario: Trigger generation is obsolete
- **WHEN** a trigger or picker has disconnected or been superseded before activation
- **THEN** it does not open an obsolete overlay or move focus
- **AND** the current editor generation remains authoritative

#### Scenario: Overlay state is not visibly presented
- **WHEN** keyboard activation changes picker state but the pinned Vaadin overlay is not `:popover-open` with visible geometry
- **THEN** browser qualification fails
- **AND** the adapter does not report keyboard time selection as operable

### Requirement: Toolkit-owned multiline boundary
Vaadin multiline editors SHALL exclude their slotted internal textarea from application-native textarea border, padding, sizing, and focus-outline selectors.
The toolkit SHALL remain responsible for the one visible editor boundary and focus ring.

#### Scenario: Vaadin multiline editor receives focus
- **WHEN** a qualified multiline property or action parameter receives keyboard focus
- **THEN** the Vaadin input container presents one visible boundary and focus ring
- **AND** its internal `slot="textarea"` element has no application-native border or outline

#### Scenario: Native multiline fallback receives focus
- **WHEN** toolkit policy or family fallback renders an unslotted native textarea
- **THEN** application-native border, padding, sizing, resizing, and focus indication remain available
- **AND** the Vaadin-specific exclusion does not suppress native usability
