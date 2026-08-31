## ADDED Requirements

### Requirement: Shared minute-precision temporal entry
Editable `<cw-property>` and `<cw-parameter>` local-time and local-date-time values SHALL use the same qualified one-minute entry precision when rendered through the Vaadin field adapter.
Causeway codecs and interaction owners MUST remain authoritative for lexical parsing, validation, pending state, cancellation, property mutation, and action invocation.

#### Scenario: Property time is entered
- **WHEN** an editable local-time or local-date-time property uses the qualified toolkit adapter
- **THEN** its time entry and picker choices are limited to minute precision
- **AND** successful save submits the minute-aligned local lexical value through the established property interaction

#### Scenario: Parameter time is entered
- **WHEN** a local-time or local-date-time action parameter uses the qualified toolkit adapter
- **THEN** its time entry and picker choices are limited to minute precision
- **AND** successful invocation submits the minute-aligned local lexical value through the established parameter negotiation path

#### Scenario: Temporal edit is cancelled
- **WHEN** a property or parameter temporal editor is cancelled
- **THEN** its authoritative or prepared value remains unchanged
- **AND** no minute normalization is persisted or invoked solely because the editor was displayed

### Requirement: Semantic time-picker affordance
Qualified property and action-parameter time editors SHALL provide equivalent pointer and keyboard access to their visible clock affordance without exposing Vaadin APIs in application markup.

#### Scenario: Property clock trigger is activated
- **WHEN** a user reaches or clicks an editable property's clock trigger
- **THEN** the time picker opens with an accessible name derived from the property label
- **AND** existing property focus, validation, save, and cancel behavior remains owned by `<cw-property>`

#### Scenario: Parameter clock trigger is activated
- **WHEN** a user reaches or clicks an action parameter's clock trigger
- **THEN** the time picker opens with an accessible name derived from the effective parameter label
- **AND** existing prompt focus containment, validation, submission, and cancellation remain controller-owned
