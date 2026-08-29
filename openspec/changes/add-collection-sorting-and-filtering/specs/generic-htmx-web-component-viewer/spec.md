## ADDED Requirements

### Requirement: Selective Petclinic collection sorting and filtering
Petclinic HTML resource pages SHALL demonstrate server-backed collection sorting and filtering on selected collections without changing domain membership, associated actions, or unselected collection behavior.

#### Scenario: Global owner list renders
- **WHEN** the Petclinic home page composes the owner list
- **THEN** its HTML override opts into sortable and filterable collection behavior
- **AND** owner filtering uses an application `CollectionFilterService` over bounded non-sensitive owner tokens

#### Scenario: Owner sorting is exercised
- **WHEN** browser automation changes the owner-name sort direction
- **THEN** the complete filtered owner result is ordered before paging
- **AND** moving between pages does not duplicate, omit, or locally reshuffle owners

#### Scenario: Owner filtering is exercised
- **WHEN** browser automation enters a bounded owner search term
- **THEN** matching owners and filtered paging metadata are returned by the authoritative GraphQL window
- **AND** clearing search restores the unfiltered owner list from offset zero

#### Scenario: Unselected collections render
- **WHEN** Petclinic composes a collection without `sortable` or `filterable`
- **THEN** it retains established paging, ordering, Grid qualification, rows, and associated actions
- **AND** no sorting or filtering control is introduced solely by another collection's configuration
