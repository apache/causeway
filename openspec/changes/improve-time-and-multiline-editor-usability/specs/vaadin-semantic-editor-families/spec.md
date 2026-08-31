## ADDED Requirements

### Requirement: Minute-resolution local-time entry
Each editable qualified Vaadin `LocalTime` or `LocalDateTime` control SHALL present and accept user-entered time at one-minute resolution.
The adapter MUST use the toolkit's supported step contract and MUST NOT introduce a second temporal parser or mutate authoritative state merely by entering edit mode.

#### Scenario: User edits a local time
- **WHEN** a property or action parameter opens an editable qualified `LocalTime` control
- **THEN** the visible entry omits seconds and fractional seconds
- **AND** a newly selected or typed valid value is aligned to the minute before the existing codec validates it

#### Scenario: User edits a local date-time
- **WHEN** a property or action parameter opens an editable qualified `LocalDateTime` control
- **THEN** its integrated time field uses the same one-minute resolution
- **AND** the local date, local time, and absence of browser timezone conversion remain authoritative

#### Scenario: User cancels a precise existing value
- **WHEN** an authoritative value contains accepted seconds or fractional seconds and the user cancels without committing a changed minute value
- **THEN** the authoritative value remains unchanged
- **AND** no rounded or truncated value is submitted

#### Scenario: Value is read-only or uses native fallback
- **WHEN** the value is presented read-only or the Vaadin family is ineligible or disabled
- **THEN** its established precision and native semantic behavior remain unchanged

### Requirement: Accessible local-time clock trigger
Each editable qualified Vaadin time picker SHALL expose its clock trigger as a semantic-field-labelled button in the normal sequential keyboard focus order.
This contract SHALL apply to standalone time pickers and the time child of date-time pickers.
Keyboard or pointer use of the trigger MUST preserve Causeway-owned pending value, validation, commit, cancellation, and focus semantics.

#### Scenario: Keyboard user reaches the clock trigger
- **WHEN** focus is in an editable qualified time input and the user presses Tab
- **THEN** focus moves to the associated clock trigger
- **AND** the trigger's accessible name identifies the time picker and semantic field

#### Scenario: Keyboard user opens the time picker
- **WHEN** the focused clock trigger receives Enter or Space
- **THEN** the existing Vaadin time overlay opens for minute-resolution selection
- **AND** no property commit, action-parameter commit, submission, or cancellation occurs merely because focus moved to or activated the trigger

#### Scenario: Pointer user opens the time picker
- **WHEN** a pointer user activates the visible clock trigger
- **THEN** the existing Vaadin time overlay opens
- **AND** the trigger is not blocked by adapter styling or hidden accessibility state

#### Scenario: Composite date-time order
- **WHEN** a keyboard user traverses an editable qualified date-time picker
- **THEN** the date input, calendar trigger, time input, and clock trigger are reachable in meaningful order
- **AND** reverse Tab traversal returns through the same associated controls

#### Scenario: Time control is not operable
- **WHEN** a qualified time control is read-only or disabled
- **THEN** it does not expose an operable clock trigger in the Tab sequence
- **AND** its existing read-only or disabled semantic state remains authoritative

#### Scenario: Pinned toolkit structure drifts
- **WHEN** the pinned Vaadin time picker no longer exposes the qualified internal clock affordance
- **THEN** browser qualification fails with the missing keyboard and pointer contract identified
- **AND** applications are not required to address Vaadin shadow-DOM structure directly

### Requirement: Single-ring multiline toolkit focus
An editable qualified Vaadin multiline control SHALL display one clear focus indicator rather than combining the application host outline with the toolkit's internal input focus ring.

#### Scenario: Multiline property receives focus
- **WHEN** keyboard focus enters a qualified Vaadin multiline property editor
- **THEN** the toolkit focus ring remains visible
- **AND** no second application-level outline surrounds the complete toolkit host

#### Scenario: Multiline action parameter receives focus
- **WHEN** keyboard focus enters a qualified Vaadin multiline action parameter
- **THEN** it uses the same single-ring presentation as the property editor
- **AND** its clear affordance, resize behavior, validation, and accessible description remain unchanged

#### Scenario: Native textarea receives focus
- **WHEN** native toolkit fallback renders a multiline editor
- **THEN** the application focus-visible outline remains available
- **AND** focus indication is not suppressed by the Vaadin-specific rule
