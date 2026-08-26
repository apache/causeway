## MODIFIED Requirements

### Requirement: Rich action parameter negotiation
The standard action prompt SHALL support parameter hidden state, disabled state, defaults, choices, autocomplete, validation, and focus-completion timing when those capabilities are present in the rich schema.
Initial structural preparation SHALL remain GraphQL-authoritative, while parameter validity presentation and dependent-value recomputation SHALL be deferred until focus leaves the actively edited field or the user invokes the action.

#### Scenario: Prompt initializes
- **WHEN** a parameterized action prompt opens
- **THEN** parameters are presented in schema order
- **AND** advertised hidden, disabled, default, and choice semantics are resolved using the current pending preceding arguments
- **AND** untouched parameter validity reasons are not presented as errors

#### Scenario: User edits the focused parameter
- **WHEN** input or change events update the parameter that still owns focus
- **THEN** the controller retains the latest codec-produced pending value
- **AND** does not request parameter-state recomputation or present that parameter's validity reason while editing continues
- **AND** autocomplete search may continue through its independently advertised capability

#### Scenario: Edited parameter loses focus
- **WHEN** focus leaves an edited action parameter for another prompt control
- **THEN** the controller recomputes authoritative parameter state using the current pending argument set
- **AND** presents any mapped validity reason for that completed parameter
- **AND** retains focus on the newly selected control

#### Scenario: Earlier parameter changes
- **WHEN** an earlier parameter value is completed by focus departure
- **THEN** later parameter semantics whose GraphQL fields accept preceding arguments are invalidated
- **AND** recomputed using the current pending argument set

#### Scenario: Invoke occurs before focus departure
- **WHEN** the user invokes an action while the current parameter has not otherwise lost focus
- **THEN** the controller validates the complete latest pending argument set
- **AND** reveals applicable mapped parameter reasons before blocking invalid invocation
- **AND** invokes only when authoritative whole-action validation succeeds

#### Scenario: Parameter autocomplete becomes obsolete
- **WHEN** an autocomplete response belongs to an older search or pending-argument generation
- **THEN** the prompt discards that response without replacing newer suggestions
