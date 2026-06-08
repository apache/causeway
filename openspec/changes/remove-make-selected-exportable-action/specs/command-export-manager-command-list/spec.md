## ADDED Requirements

### Requirement: Export manager omits obsolete make-selected-exportable action
The export manager MUST NOT expose a `makeSelectedExportable` collection action for the active `commands` collection.
The export manager SHALL rely on current export, exclusion, unexclusion, movement, and row-level exportability behavior instead of the obsolete make-selected-exportable workflow.
Removing the obsolete collection action MUST NOT remove the active `commands` collection or its row-level exportability state.

#### Scenario: Export manager page has no make selected exportable action
- **WHEN** the user views the export manager commands collection
- **THEN** there is no collection action named `makeSelectedExportable`
- **AND** the active `commands` collection remains visible
- **AND** command exportability state remains available on each replayable command when export-manager context supports it
