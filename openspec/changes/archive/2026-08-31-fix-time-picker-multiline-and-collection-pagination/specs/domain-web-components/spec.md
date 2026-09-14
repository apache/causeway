## MODIFIED Requirements

### Requirement: Shared minute-precision temporal entry
Editable Web Component properties and action parameters backed by qualified Vaadin `LocalTime` or `LocalDateTime` controls SHALL present time without seconds or fractional seconds and SHALL use Vaadin's supported 15-minute dropdown interval.
The behavior SHALL remain consistent across direct property editing, inline prompts, modal prompts, and sidebar prompts.

#### Scenario: Property time enters editing
- **WHEN** a qualified `LocalTime` or `LocalDateTime` property enters editing
- **THEN** its visible time entry omits seconds and fractional seconds
- **AND** its picker offers quarter-hour choices

#### Scenario: Action time parameter is prepared
- **WHEN** a qualified `LocalTime` or `LocalDateTime` action parameter is prepared in any supported prompt style
- **THEN** its visible time entry uses the same 15-minute dropdown interval
- **AND** no default, validation, confirmation, or invocation semantics change

#### Scenario: Precise value remains authoritative without commit
- **WHEN** an accepted authoritative value contains seconds, fractional seconds, or a minute outside a quarter-hour choice and the interaction is only opened or cancelled
- **THEN** the authoritative value retains its accepted precision
- **AND** no rounded value is submitted

## ADDED Requirements

### Requirement: User-visible time-picker keyboard activation
Qualified `<cw-property>` and `<cw-parameter>` time editors SHALL make their labelled clock affordance visibly open the current time-selection overlay from Enter or Space.
Focus transfer and overlay activation MUST preserve Causeway-owned pending values, validation, cancellation, property save, parameter preparation, and action invocation.

#### Scenario: Property clock is keyboard activated
- **WHEN** a user presses Enter or Space on an editable property's current clock trigger
- **THEN** the visible time overlay opens with quarter-hour choices formatted to minutes
- **AND** the property is not saved merely by opening the overlay

#### Scenario: Parameter clock is keyboard activated
- **WHEN** a user presses Enter or Space on an editable action parameter's current clock trigger
- **THEN** the visible time overlay opens with quarter-hour choices within the prompt lifecycle
- **AND** the action is not invoked merely by opening the overlay

### Requirement: Authoritative bounded collection range totals
A bounded collection pager SHALL include the authoritative total count in its live range label whenever a valid total is available.
It MUST retain bounded range wording without a total when count metadata is unavailable and MUST NOT infer a total from loaded rows or navigation state.

#### Scenario: First bounded page has a total
- **WHEN** a collection window reports rows 1 through 10 and authoritative total count 23
- **THEN** the pager announces `Items 1–10 of 23`
- **AND** Next and Previous availability continues to follow authoritative window metadata

#### Scenario: Later bounded page has a total
- **WHEN** a collection window reports rows 21 through 23 and authoritative total count 23
- **THEN** the pager announces `Items 21–23 of 23`
- **AND** the displayed total remains stable across page navigation

#### Scenario: Total count is unavailable
- **WHEN** a bounded collection window has no valid authoritative total
- **THEN** the pager announces only its available current range
- **AND** it does not fabricate an `of` count

#### Scenario: Collection is empty
- **WHEN** the current authoritative window returns no items
- **THEN** the pager retains its established `No items` wording
- **AND** navigation availability remains metadata-driven
