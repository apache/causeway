## ADDED Requirements

### Requirement: Accessible local-date calendar trigger
Each editable qualified Vaadin date picker SHALL expose its calendar trigger as a field-labelled button in the normal sequential keyboard focus order.
Keyboard use of the trigger MUST preserve Causeway-owned pending value, validation, commit, cancellation, and focus semantics.

#### Scenario: Keyboard user reaches the calendar trigger
- **WHEN** focus is in an editable qualified date input and the user presses Tab
- **THEN** focus moves to the associated calendar trigger
- **AND** the trigger's accessible name identifies the calendar and semantic field

#### Scenario: Keyboard user opens the calendar
- **WHEN** the focused calendar trigger receives Enter or Space
- **THEN** the existing Vaadin calendar overlay opens for date selection
- **AND** no property commit, action-parameter commit, submission, or cancellation occurs merely because focus moved to or activated the trigger

#### Scenario: Date control is not operable
- **WHEN** a qualified date control is read-only or disabled
- **THEN** it does not expose an operable calendar trigger in the Tab sequence
- **AND** its existing read-only or disabled semantic state remains authoritative

#### Scenario: Pinned toolkit structure drifts
- **WHEN** the pinned Vaadin date picker no longer exposes the qualified internal calendar affordance
- **THEN** browser qualification fails with the missing keyboard contract identified
- **AND** applications are not required to address Vaadin shadow-DOM structure directly
