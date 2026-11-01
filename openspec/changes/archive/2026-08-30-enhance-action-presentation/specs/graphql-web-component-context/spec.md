## ADDED Requirements

### Requirement: Action presentation metadata continuity
The GraphQL Web Component context SHALL discover available canonical action name, description, static Font Awesome classes, and icon position and preserve them through ordinary component state and action preparation.
Discovery MUST remain compatible with rich schemas that omit the additive icon fields.

#### Scenario: Current schema advertises action presentation metadata
- **WHEN** action discovery introspects the additive metadata fields
- **THEN** the current action state contains bounded canonical name, description, icon classes, and position
- **AND** unrelated metadata and hidden members are not selected

#### Scenario: Older schema omits icon fields
- **WHEN** action discovery introspects a compatible schema without additive Font Awesome metadata
- **THEN** the generated operation omits the unsupported fields
- **AND** naming, description, action preparation, and invocation remain available

#### Scenario: Parameterized action is prepared
- **WHEN** current action preparation succeeds with one or more parameters
- **THEN** the prepared capabilities retain the current canonical action presentation
- **AND** subsequent parameter recomputation preserves it until the prompt generation is retired

#### Scenario: Action generation is superseded
- **WHEN** route, object, service, authorization, refresh, or action generation changes
- **THEN** stale action presentation cannot replace current control or prompt metadata
- **AND** no stale icon, tooltip, name, or description is rendered
