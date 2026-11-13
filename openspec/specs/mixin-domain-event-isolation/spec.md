# mixin-domain-event-isolation Specification

## Purpose
TBD - created by archiving change reconcile-mixin-domain-event-isolation. Update Purpose after archive.
## Requirements
### Requirement: Mixed-in members resolve their domain-event type per mixee

The system SHALL resolve the domain-event type of a mixed-in member (action, property, or collection) with respect
to the mixee type to which it is contributed. When a single mixin is contributed to more than one mixee type, and
those mixee types declare different `@DomainObject` domain-event defaults, each mixee's mixed-in member SHALL
dispatch that mixee's own domain-event type. Resolving the event type for one mixee MUST NOT change the event type
resolved for any other mixee or for the mixin's shared faceted method.

An explicitly-specified domain-event type — set on the member itself (e.g. `@Action(domainEvent=…)`) or on the
mixin type — SHALL take precedence and MUST be preserved unchanged for every mixee. Per-mixee resolution SHALL
apply only when the member's domain-event type is still the framework default.

#### Scenario: A shared mixin over two differently-annotated mixees isolates event types

- **GIVEN** a mixin action contributed to two mixee types
- **AND** the first mixee declares an object-level domain-event default and the second declares none
- **WHEN** the metamodel is introspected
- **THEN** the mixed-in action on the first mixee reports the first mixee's object-level domain-event type
- **AND** the mixed-in action on the second mixee reports the framework default domain-event type
- **AND** the result is the same regardless of the order in which the two mixees are processed

#### Scenario: Per-mixee isolation applies to mixed-in properties and collections

- **GIVEN** a mixin property and a mixin collection each contributed to the same two mixee types
- **AND** the two mixee types declare different domain-event defaults
- **WHEN** the metamodel is introspected
- **THEN** each mixee's mixed-in property reports that mixee's property domain-event type
- **AND** each mixee's mixed-in collection reports that mixee's collection domain-event type
- **AND** neither mixee's resolved event type is affected by the other

#### Scenario: An explicitly-specified event type is preserved for every mixee

- **GIVEN** a mixin member whose domain-event type is specified explicitly on the member or on the mixin type
- **AND** that mixin is contributed to multiple mixee types
- **WHEN** the metamodel is introspected
- **THEN** every mixee's mixed-in member reports the explicitly-specified domain-event type
- **AND** no mixee's default domain-event type overrides it

#### Scenario: A single-mixee mixin is unchanged

- **GIVEN** a mixin contributed to exactly one mixee type
- **WHEN** the metamodel is introspected and the mixed-in member is executed
- **THEN** the resolved domain-event type and the dispatched event are exactly as before this change

### Requirement: Mixed-in action execution consults the mixee-specific event holder

When a mixee-specific domain-event type has been resolved for a mixed-in action, the system SHALL dispatch that
mixee's domain-event type when the action is executed, rather than the event type held on the shared mixin action.
When no mixee-specific event type applies, execution SHALL delegate to the shared mixin action as before.

#### Scenario: Execution dispatches the mixee-specific event

- **GIVEN** a mixin action contributed to a mixee type that declares an object-level domain-event default
- **WHEN** the mixed-in action is executed on that mixee
- **THEN** the domain event that is emitted is of the mixee's object-level domain-event type

#### Scenario: Execution without a mixee-specific event uses the shared mixin action

- **GIVEN** a mixin action contributed to a mixee type with no domain-event default
- **WHEN** the mixed-in action is executed on that mixee
- **THEN** execution delegates to the shared mixin action and emits the framework default domain-event type

