## ADDED Requirements

### Requirement: Export selected defaults to exportable commands
The export manager SHALL default the `exportSelected` action's selected commands to active commands whose exportability property is `true`.
The export manager MUST NOT default commands whose exportability property is `false` or `null` into the `exportSelected` selected commands.
The `exportSelected` action choices SHALL remain the active `commands` collection so users can override the default selection.
Default selection MUST NOT change command replay state.

#### Scenario: Export action selects exportable active commands by default
- **GIVEN** an export manager baseline is set
- **AND** command `A` in the active commands collection has exportability `true`
- **AND** command `B` in the active commands collection has exportability `false`
- **WHEN** the system provides defaults for the `exportSelected` selected commands
- **THEN** command `A` is selected by default
- **AND** command `B` is not selected by default

#### Scenario: Export action choices still include active commands
- **GIVEN** an export manager baseline is set
- **AND** command `A` is in the active commands collection
- **AND** command `B` is in the active commands collection
- **WHEN** the system provides choices for the `exportSelected` selected commands
- **THEN** command `A` is available as a choice
- **AND** command `B` is available as a choice

#### Scenario: Default selection does not mark commands exported
- **GIVEN** an export manager baseline is set
- **AND** command `A` in the active commands collection has replay state `UNDEFINED`
- **AND** command `A` has exportability `true`
- **WHEN** the system provides defaults for the `exportSelected` selected commands
- **THEN** command `A` still has replay state `UNDEFINED`
