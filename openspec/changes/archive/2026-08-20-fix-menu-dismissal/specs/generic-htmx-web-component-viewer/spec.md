## ADDED Requirements

### Requirement: Transient application menu dismissal
The viewer's semantic application menus SHALL close an expanded menu panel after an enabled action is selected and SHALL allow the user to dismiss an expanded menu with Escape.
Disclosure accessibility state, panel visibility, action dispatch, and focus behavior MUST remain synchronized during dismissal.

#### Scenario: Enabled menu action is selected
- **WHEN** the user activates an enabled action in an expanded application menu by pointer or keyboard
- **THEN** the containing menu disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** the selected semantic action request is dispatched exactly once
- **AND** the resulting prompt, result, or route transition continues according to its existing focus policy

#### Scenario: Expanded menu is dismissed with Escape
- **WHEN** focus is within an expanded application menu and the user presses Escape
- **THEN** the active menu disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** focus returns to that menu's disclosure control
- **AND** no service action is requested

#### Scenario: Menu dismissal is scoped
- **WHEN** an expanded menu is dismissed after selection or with Escape
- **THEN** only the transient menu panel is closed
- **AND** the surrounding menu bar and stable application shell remain operable
