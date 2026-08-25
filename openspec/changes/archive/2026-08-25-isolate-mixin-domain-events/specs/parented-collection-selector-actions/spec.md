## ADDED Requirements

### Requirement: Synthetic navigation post-processing isolates mixed-in domain-event facets

When command-log recording support synthesizes navigation actions for parented collections or scalar references, the post-processing sweep SHALL keep generated navigation actions domain-event neutral.
Generated navigation actions MUST NOT expose an owner type's action domain-event default.
Generated navigation actions MUST retain their safe semantics and command publishing metadata when recording support is enabled.
Synthetic navigation action synthesis MUST NOT install or update action domain-event facets on unrelated pre-existing mixed-in actions.
Synthetic navigation action synthesis MUST NOT cause a mixee-specific action domain-event type discovered while processing one domain type to appear on a non-navigation action contributed to another domain type.
When a mixed-in action's effective action domain-event type depends on the current mixee type, the system SHALL keep that effective event type isolated to the mixed-in member for that mixee.
The system MUST NOT mutate the shared mixin method's action domain-event facet with a concrete mixee's type-level domain-event default.

#### Scenario: Parented collection navigation action is domain-event neutral

- **GIVEN** an entity type has a parented collection of child entities
- **AND** command-log recording support is `ENABLED`
- **AND** the entity type declares a type-level action domain-event default
- **WHEN** the framework synthesizes the parented collection navigation action
- **THEN** the generated navigation action does not expose the entity type's action domain-event default
- **AND** the generated navigation action remains safe and command-published for recording support

#### Scenario: Scalar reference navigation action is domain-event neutral

- **GIVEN** an entity type has a scalar reference to another entity
- **AND** command-log recording support is `ENABLED`
- **AND** the entity type declares a type-level action domain-event default
- **WHEN** the framework synthesizes the scalar reference navigation action
- **THEN** the generated navigation action does not expose the entity type's action domain-event default
- **AND** the generated navigation action remains safe and command-published for recording support

#### Scenario: Generic mixed-in action keeps mixee-specific event type isolated

- **GIVEN** command-log recording support is `ENABLED`
- **AND** a generic mixed-in action is contributed to two unrelated domain types
- **AND** only the first domain type declares a type-level action domain-event default
- **WHEN** the framework runs metamodel post-processing for both domain types
- **THEN** the mixed-in action on the first domain type may use that first domain type's action domain-event default
- **AND** the mixed-in action on the second domain type does not expose the first domain type's action domain-event default

#### Scenario: Navigation synthesis does not contaminate unrelated generic mixins

- **GIVEN** command-log recording support is `ENABLED`
- **AND** one domain type declares a type-level action domain-event default
- **AND** an unrelated generic mixed-in action is contributed to multiple domain types
- **WHEN** the framework synthesizes generated navigation actions for eligible one-to-many collection associations and one-to-one reference associations
- **THEN** the unrelated generic mixed-in action does not receive an action domain-event facet whose event type came from a different mixee type
- **AND** generated navigation actions are still available for eligible one-to-many collection associations and one-to-one reference associations

#### Scenario: Application-authored explicit mixed-in domain-event semantics are preserved

- **GIVEN** an application-authored mixed-in action explicitly declares an action domain-event type
- **AND** command-log recording support is `ENABLED`
- **WHEN** the framework runs synthetic navigation action post-processing
- **THEN** synthetic navigation action processing does not replace the application-authored mixed-in action's explicit domain-event type
- **AND** synthetic navigation action processing does not add a navigation-specific domain-event marker to the application-authored mixed-in action
