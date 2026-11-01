## ADDED Requirements

### Requirement: Canonical are-you-sure action confirmation
The standard interaction controller SHALL require explicit accessible confirmation before invoking an action whose canonical rich GraphQL metadata reports `areYouSure` as true.
HTML-authored component presentation MUST NOT suppress, fabricate, or override that canonical requirement.

#### Scenario: Parameterless action requires confirmation
- **WHEN** an enabled parameterless action with `areYouSure` metadata is activated
- **THEN** the controller presents confirmation before invoking the action
- **AND** the action is invoked only after the user explicitly confirms

#### Scenario: Parameterized action requires confirmation
- **WHEN** an action with `areYouSure` metadata has parameters
- **AND** the user submits values that pass authoritative parameter preparation and whole-action validation
- **THEN** the controller presents confirmation with the validated pending values retained
- **AND** invokes only after the user explicitly confirms

#### Scenario: Parameterized confirmation is declined
- **WHEN** a user cancels or presses Escape from confirmation for a parameterized action
- **THEN** the controller returns to the parameter prompt with pending values intact
- **AND** does not invoke the action

#### Scenario: Parameterless confirmation is declined
- **WHEN** a user cancels or presses Escape from confirmation for a parameterless action
- **THEN** the controller closes the prompt without invocation
- **AND** restores focus to the originating action affordance

#### Scenario: Ordinary action is activated
- **WHEN** an enabled action's canonical metadata reports `areYouSure` as false or the compatible schema does not expose the field
- **THEN** the controller retains the established parameterless or parameterized interaction flow
- **AND** does not infer confirmation from presentation or operation placement

#### Scenario: Confirmation is presented accessibly
- **WHEN** an action enters confirmation state
- **THEN** one controller-owned modal dialog identifies the effective action name and destructive decision
- **AND** exposes keyboard-operable Confirm and Cancel controls with deterministic focus and stable automation hooks

#### Scenario: Confirmation is activated repeatedly
- **WHEN** the user confirms an action and invocation begins
- **THEN** confirmation controls are gated against repeated activation
- **AND** the controller invokes the action at most once for that prompt generation

#### Scenario: Action is disabled
- **WHEN** authoritative action state supplies a disabled reason
- **THEN** activation remains unavailable and the reason remains accessible
- **AND** no confirmation prompt opens
