## ADDED Requirements

### Requirement: Petclinic bounded visit parameter qualification
The Petclinic owner page SHALL demonstrate declarative action-parameter ranges by booking visits with a future `LocalDate` and an inclusive office-hours `LocalTime` through direct-child `<cw-parameter>` declarations.
The domain action MUST combine accepted date and time values into the existing authoritative visit date-time and retain canonical validation for both constraints.

#### Scenario: Book Visit prompt opens
- **WHEN** browser acceptance opens `bookVisit`
- **THEN** the date editor exposes the resolved `tomorrow` minimum and the time editor exposes `08:00` and `17:00` bounds
- **AND** defaults, parameter order, labels, prompt style, picker accessibility, and cancellation remain authoritative or presentation-driven as before

#### Scenario: Non-future visit date is attempted
- **WHEN** the user enters a date before the resolved future-date minimum
- **THEN** the prompt retains the date and presents a local minimum reason
- **AND** no `bookVisit` GraphQL preparation, validation, or mutation request occurs for the rejected attempt

#### Scenario: Time outside office hours is attempted
- **WHEN** the user enters a well-formed time before `08:00` or after `17:00`
- **THEN** the prompt retains the time and presents the matching local boundary reason
- **AND** no `bookVisit` GraphQL preparation, validation, or mutation request occurs for the rejected attempt

#### Scenario: Visit date and time are corrected
- **WHEN** both pending values lie within their declared ranges
- **THEN** canonical action validation and one `bookVisit` mutation proceed
- **AND** the refreshed visit collection exposes the combined authoritative local date-time

#### Scenario: Native and Vaadin policies are exercised
- **WHEN** Petclinic runs under the default Vaadin and explicit native component-toolkit policies
- **THEN** both date and time controls receive equivalent bounds and local request gating
- **AND** Petclinic markup contains only public Causeway elements and attributes
