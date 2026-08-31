## ADDED Requirements

### Requirement: Canonical action prompt-style metadata
The shared rich member metadata object SHALL expose nullable `promptStyle` metadata derived from the resolved canonical action prompt style without invoking domain behavior.
The value SHALL use the canonical `PromptStyle` enum name so clients can apply presentation-specific normalization.

#### Scenario: Action has a resolved prompt style
- **WHEN** metadata is requested for an action whose resolved prompt style is `DIALOG_SIDEBAR`, `DIALOG_MODAL`, `INLINE`, or another canonical `PromptStyle` value
- **THEN** `promptStyle` returns that enum name
- **AND** metadata resolution does not invoke the action or its supporting methods

#### Scenario: Metadata belongs to another wrapper
- **WHEN** property, collection, or action-parameter metadata is requested
- **THEN** `promptStyle` returns null
- **AND** existing names, descriptions, icons, confirmation metadata, and editor constraints remain unchanged

#### Scenario: Existing client omits prompt-style metadata
- **WHEN** an existing GraphQL document does not select `promptStyle`
- **THEN** its operation and response shape remain unchanged
