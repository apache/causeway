## ADDED Requirements

### Requirement: Faithful Petclinic owner deletion policy
The Petclinic sample SHALL retain the pinned original application's observable owner-deletion policy while demonstrating standard are-you-sure confirmation through the HTMX Web Components viewer.

#### Scenario: Owner has related visits
- **WHEN** a Petclinic owner has one or more related visits
- **THEN** Delete is disabled with the reason `This owner has N visits` using the authoritative count
- **AND** the viewer does not offer confirmation or attempt deletion

#### Scenario: Owner has no related visits
- **WHEN** a Petclinic owner has no related visits
- **THEN** Delete remains enabled with canonical are-you-sure metadata
- **AND** its description does not claim that related visits will be deleted

#### Scenario: Eligible deletion is cancelled
- **WHEN** a user activates Delete for an eligible disposable owner and declines confirmation
- **THEN** the owner remains available on its canonical route
- **AND** no delete mutation is issued

#### Scenario: Eligible deletion is confirmed
- **WHEN** a user activates Delete for an eligible disposable owner and explicitly confirms
- **THEN** the action completes through the established GraphQL mutation and void-result policy
- **AND** the deleted object route returns to the configured home route after post-action refresh establishes that the owner no longer exists

### Requirement: Petclinic destructive-action regression coverage
Petclinic integration and opt-in browser acceptance SHALL verify canonical confirmation metadata, visit-based disable behavior, cancellation, successful confirmed deletion, focus, routing, and error monitoring.

#### Scenario: Rich GraphQL delete state is queried
- **WHEN** integration coverage reads Delete for a fixture owner with visits and an eligible owner without visits
- **THEN** both expose `areYouSure` as true
- **AND** only the fixture owner exposes the visit-count disabled reason

#### Scenario: Browser exercises destructive deletion
- **WHEN** browser automation cancels one eligible deletion and confirms another
- **THEN** focus and route outcomes follow the standard interaction and void-action recovery contracts
- **AND** no unexpected console, page, resource, GraphQL, or referential-integrity failure occurs
