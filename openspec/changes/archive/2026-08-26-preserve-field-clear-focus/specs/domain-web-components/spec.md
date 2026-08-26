## MODIFIED Requirements

### Requirement: Accessible standard interaction presentation
Standard property editors and the standard action prompt SHALL expose labelled controls, associated validation, pending and error announcements, deterministic focus behavior, keyboard operation, and application styling through light-DOM semantic markup.

#### Scenario: Parameterized prompt opens and closes
- **WHEN** a keyboard user opens a parameterized action prompt
- **THEN** focus moves to the first operable prompt control
- **AND** Escape cancels the prompt without invocation
- **AND** focus returns to the originating action affordance

#### Scenario: Invalid property value
- **WHEN** server validation rejects a pending property value
- **THEN** the editor associates the reason with its input and announces it accessibly
- **AND** retains the pending value for correction or cancellation

#### Scenario: Property validation replaces focused controls
- **WHEN** a user tabs from a changed property editor to an owned Clear, Save, or Cancel control and validation-driven rendering replaces that focused control
- **THEN** focus remains on the equivalent newly rendered control, including while an internal adapter upgrades asynchronously
- **AND** subsequent Tab or Shift+Tab navigation continues from that control without restarting at the editor

#### Scenario: Focus leaves the property during validation
- **WHEN** focus genuinely moves beyond the property before validation rendering completes
- **THEN** the property does not restore an obsolete internal focus intent
- **AND** external focus remains unchanged
