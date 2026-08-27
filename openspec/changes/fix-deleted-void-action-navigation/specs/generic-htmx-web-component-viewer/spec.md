## ADDED Requirements

### Requirement: Void action route recovery
The viewer SHALL refresh the current object route after a successful void action while the object remains available, and SHALL return to its configured home route when that post-action refresh establishes that the object no longer exists.
The fallback MUST be limited to the current post-action refresh and MUST NOT convert unrelated missing-object navigation into a home redirect.

#### Scenario: Void action retains its target
- **WHEN** a successful void action completes and the current object remains available
- **THEN** the viewer refreshes the current object route
- **AND** presents the updated object state on the same canonical route

#### Scenario: Void action deletes its target
- **WHEN** a successful void action completes and the refreshed current object reports `NOT_FOUND`
- **THEN** the viewer navigates to the configured home route
- **AND** does not leave the deleted object page in a terminal or component-error state

#### Scenario: Missing object is requested independently
- **WHEN** an object route reports `NOT_FOUND` without a current successful void-action refresh
- **THEN** the viewer retains its bounded missing-object state
- **AND** does not redirect to the home route
