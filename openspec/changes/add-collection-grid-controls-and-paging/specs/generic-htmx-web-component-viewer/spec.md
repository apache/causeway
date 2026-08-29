## ADDED Requirements

### Requirement: Selective Petclinic collection paging overrides
Petclinic HTML resource pages SHALL demonstrate declarative paging on collections that can grow materially while leaving smaller or summary collections on established loading behavior.

#### Scenario: Global owner list renders
- **WHEN** the Petclinic home page composes the owner collection
- **THEN** its HTML override declares a bounded `paged` size
- **AND** does not rely on inert `offset` or `size` attributes

#### Scenario: Owner visit history renders
- **WHEN** an owner page composes visit history
- **THEN** that collection declares a bounded `paged` size
- **AND** its associated actions and semantic columns remain unchanged

#### Scenario: Small or summary collection renders
- **WHEN** Petclinic composes an owner's pets or the upcoming-visit summary
- **THEN** the HTML override does not declare `paged`
- **AND** the collection retains established default loading behavior

#### Scenario: Browser navigates a paged collection
- **WHEN** normalized metadata reports another page
- **THEN** the application exposes accessible Causeway previous and next controls and the configured range size
- **AND** navigation does not duplicate rows, associated actions, requests, or page-level headings
