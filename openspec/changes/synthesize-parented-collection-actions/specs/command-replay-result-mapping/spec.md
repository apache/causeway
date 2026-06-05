## ADDED Requirements

### Requirement: Replay result mapping includes synthetic selector action results
The system SHALL notify the command replay mapping SPI after successful replay of a synthetic parented collection selector action when both recorded and actual result bookmarks are available.
The system SHALL use that mapping to remap later replay command targets and reference parameters in the same way as other logged action results.
The system MUST NOT notify the mapping SPI when replay fails or when either result bookmark is unavailable.

#### Scenario: Replayed selector action maps recorded child to actual child
- **GIVEN** an imported command entry records a synthetic selector action result bookmark for a child object
- **AND** replaying that selector action returns a bookmarkable child object
- **WHEN** replay of the selector action succeeds
- **THEN** the system notifies the command replay mapping SPI with the recorded and actual child bookmarks

#### Scenario: Later replay command target is remapped from selector result
- **GIVEN** a replayed synthetic selector action mapped recorded child bookmark `child:A` to actual child bookmark `child:B`
- **WHEN** a later replay command targets recorded bookmark `child:A`
- **THEN** replay invokes the later command on actual bookmark `child:B`

#### Scenario: Later replay reference parameter is remapped from selector result
- **GIVEN** a replayed synthetic selector action mapped recorded child bookmark `child:A` to actual child bookmark `child:B`
- **WHEN** a later replay command has a reference parameter recorded as `child:A`
- **THEN** replay supplies actual bookmark `child:B` for that reference parameter

#### Scenario: Selector replay result mapping is skipped when unavailable
- **GIVEN** an imported synthetic selector action command entry lacks a recorded returned object bookmark
- **WHEN** replay of that selector action succeeds
- **THEN** the system does not notify the command replay mapping SPI for that selector action result
