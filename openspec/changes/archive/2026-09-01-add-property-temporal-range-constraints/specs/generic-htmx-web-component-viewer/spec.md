## ADDED Requirements

### Requirement: Petclinic bounded property-date qualification
The Petclinic owner page SHALL demonstrate declarative temporal range constraints on its editable `lastVisit` property through the public `<cw-property>` contract.

#### Scenario: Owner last-visit editor opens
- **WHEN** browser acceptance edits the owner's `lastVisit` `LocalDate` property
- **THEN** the qualified or native control exposes the authored absolute minimum and resolved `today` maximum
- **AND** localized British date presentation retains the same ISO boundaries and current value

#### Scenario: Future last-visit date is attempted
- **WHEN** the user enters a valid local date after the resolved maximum
- **THEN** the pending value remains available with a local range error
- **AND** no GraphQL property validation or mutation occurs

#### Scenario: In-range last-visit date is entered
- **WHEN** the user corrects the pending date to the closed admissible interval
- **THEN** canonical property validation and save proceed normally
- **AND** the authoritative owner refresh displays the accepted date

#### Scenario: Interaction is cancelled
- **WHEN** the bounded date editor is opened and cancelled without save
- **THEN** the authoritative date, focus restoration, picker accessibility, and local temporal precision remain unchanged

#### Scenario: Both toolkit policies run
- **WHEN** Petclinic runs with Vaadin-default and explicit native field policy
- **THEN** the same authored `min` and `max` declaration constrains both editors
- **AND** no raw Vaadin element or API is required in Petclinic markup
