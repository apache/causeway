## ADDED Requirements

### Requirement: Deferred reference-editor focus continuity
A semantic reference editor SHALL preserve a programmatic focus request made while its internal toolkit control is loading and SHALL transfer that focus to the current control after successful upgrade or fallback.
The transfer MUST be generation-safe and MUST NOT focus a disconnected or superseded control.

#### Scenario: Modal opens before reference control upgrade
- **WHEN** a modal action prompt requests initial focus on a reference editor before its internal control is available
- **THEN** the reference host records the focus intent
- **AND** focus enters the installed reference control as soon as the current upgrade completes

#### Scenario: Reference upgrade falls back
- **WHEN** a pending focus request exists and the reference toolkit cannot be loaded
- **THEN** focus transfers to the native fallback control
- **AND** the bounded adapter failure remains locally reported

#### Scenario: Prompt is replaced before upgrade completes
- **WHEN** the reference editor disconnects or a newer generation supersedes its pending upgrade
- **THEN** the obsolete generation does not move focus
- **AND** no stale prompt control becomes active

### Requirement: Reference-first modal Escape cancellation
A modal action prompt whose first applicable parameter is an asynchronously upgraded reference editor SHALL retain the established Escape-cancellation lifecycle once initial focus enters that editor.

#### Scenario: User dismisses a reference-first modal
- **WHEN** the reference dropdown is closed, invocation is not in progress, and the user presses Escape from the focused reference editor
- **THEN** the prompt is cancelled without invocation
- **AND** the originating action regains focus

#### Scenario: Reference dropdown is open
- **WHEN** the user presses Escape while the reference editor's dropdown is open
- **THEN** the editor may consume that press to close its dropdown
- **AND** a subsequent Escape from the closed editor cancels the prompt
