## MODIFIED Requirements

### Requirement: Object-home and semantic result policy
The viewer SHALL translate the established object-home entry and semantic result events through replaceable viewer policy rather than changing component contracts.
Every newly installed landing fragment SHALL apply that object-home policy while preventing concurrent resolution for the same landing fragment.

#### Scenario: Object home is available
- **WHEN** targeted application-entry discovery returns a valid public object home
- **THEN** default home policy routes to its canonical object route
- **AND** no home service-action descriptor is inferred

#### Scenario: Landing fragment is installed after shell startup
- **WHEN** brand navigation or another canonical route transition installs a landing fragment after the stable shell has started
- **THEN** the viewer resolves the GraphQL-authoritative object home for that new landing fragment
- **AND** replaces the landing history entry with the canonical object route
- **AND** concurrent lifecycle callbacks for the same landing fragment issue no duplicate home-resolution work

#### Scenario: Home is unavailable
- **WHEN** the object home is absent, hidden, invalid, unsupported, or partially failing
- **THEN** the shell renders a bounded accessible landing state
- **AND** menus and explicit canonical routes remain usable

#### Scenario: Object result is published
- **WHEN** an interaction publishes a semantic object result without an application override
- **THEN** default policy requests its canonical route

#### Scenario: Non-object result is published
- **WHEN** an interaction publishes scalar, collection, or void semantics without an application override
- **THEN** default policy presents or announces that result in the documented shell region
- **AND** void completion refreshes the current object context only when one exists

#### Scenario: Application overrides result handling
- **WHEN** an application registers a scoped handler for a semantic result kind
- **THEN** that handler receives the semantic result and public target detail
- **AND** component interaction, validation, and mutation behavior remain unchanged
