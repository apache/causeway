## MODIFIED Requirements

### Requirement: Transient application menu dismissal
The viewer's semantic application menus SHALL close an expanded menu panel after an enabled action is selected, when focus leaves its owning menubar, and when the user dismisses it with Escape or outside activation.
Disclosure accessibility state, panel visibility, action dispatch, and focus behavior MUST remain synchronized during dismissal.

#### Scenario: Enabled menu action is selected
- **WHEN** the user activates an enabled action in an expanded application menu by pointer or keyboard
- **THEN** the containing menu disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** the selected semantic action request is dispatched exactly once
- **AND** the resulting prompt, result, or route transition continues according to its existing focus policy

#### Scenario: Focus remains inside the menubar
- **WHEN** keyboard or scripted focus moves between disclosures or actions within one semantic menubar
- **THEN** the active expanded menu panel remains open
- **AND** its disclosure and controlled-panel accessibility state remain synchronized

#### Scenario: Focus leaves the menubar
- **WHEN** Tab, Shift+Tab, pointer focus, or scripted focus moves from an expanded application menu to a target outside its owning semantic menubar
- **THEN** the active disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** focus remains on the newly selected external target
- **AND** no service action is requested

#### Scenario: Expanded menu is dismissed with Escape
- **WHEN** focus is within an expanded application menu and the user presses Escape
- **THEN** the active menu disclosure changes to collapsed and its controlled panel becomes hidden
- **AND** focus returns to that menu's disclosure control
- **AND** no service action is requested

#### Scenario: Menu dismissal is scoped
- **WHEN** an expanded menu is dismissed after selection, focus departure, outside activation, or Escape
- **THEN** only the transient menu panel is closed
- **AND** the surrounding menu bar and stable application shell remain operable
