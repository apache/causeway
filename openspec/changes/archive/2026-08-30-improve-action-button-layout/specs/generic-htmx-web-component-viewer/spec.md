## ADDED Requirements

### Requirement: Application-owned Petclinic object heading actions
The Petclinic owner custom page SHALL compose its object title and object-level actions in an application-owned responsive heading row without changing semantic object-header or action component contracts.

#### Scenario: Owner page renders at wide width
- **WHEN** a Petclinic owner route renders with sufficient inline space
- **THEN** the remove-owner action appears immediately after the object title in the same heading row
- **AND** the page does not place that object-level action in a separate toolbar row below the title

#### Scenario: Owner heading has insufficient width
- **WHEN** the title and object-level action cannot fit on one line
- **THEN** the application heading row wraps in title-before-action document order
- **AND** the title and action remain readable, operable, and free of overlap or horizontal page overflow

#### Scenario: Application owns heading placement
- **WHEN** the owner page composes the title and object-level action
- **THEN** placement is defined by the Petclinic HTML resource and application stylesheet
- **AND** `<cw-object-header>` and `<cw-action>` retain their established framework-neutral rendering and semantic behavior
