## ADDED Requirements

### Requirement: Current action invocation access

The interaction provider SHALL expose the current execution as an optional action invocation when, and only when, the current execution is an action invocation.

#### Scenario: Action body is executing

- **WHEN** an action body is executing through Causeway's metamodel invocation machinery
- **THEN** the current action invocation is present
- **AND** it exposes the same target, logical member identifier, and arguments recorded by the interaction execution graph

#### Scenario: No interaction is active

- **WHEN** no interaction is active on the calling thread
- **THEN** the current action invocation is empty

#### Scenario: Current execution is not an action

- **WHEN** an interaction is active but its current execution is a property edit or another non-action execution
- **THEN** the current action invocation is empty

### Requirement: Exact current-action matching

The interaction provider SHALL determine whether a supplied target and action identity describe the current action invocation.

#### Scenario: Target and full identifier match

- **WHEN** the supplied target is the identical object recorded by the current action invocation
- **AND** the supplied action identifier equals the current action's logical member identifier
- **THEN** exact current-action matching returns true

#### Scenario: Equal but non-identical target

- **WHEN** the supplied target is equal to but not identical to the target recorded by the current action invocation
- **THEN** current-action matching returns false

#### Scenario: Different action is current

- **WHEN** the supplied target matches but the supplied action identity does not identify the current action
- **THEN** current-action matching returns false

#### Scenario: Logical member name convenience matching

- **WHEN** the supplied target is identical to the current action target
- **AND** the supplied logical member name equals the current action identifier's logical member name
- **THEN** logical-member-name current-action matching returns true

#### Scenario: Mixin action matching

- **WHEN** a mixin action body supplies its own transient mixin instance as the target
- **AND** it supplies `act` as the logical member name
- **THEN** logical-member-name current-action matching returns true
- **AND** the mixed-in domain object is not treated as the current action target

### Requirement: Nested action semantics

The current action invocation API SHALL follow the existing interaction execution stack for nested framework-managed action calls.

#### Scenario: Nested wrapped action is executing

- **WHEN** one action invokes another action through `WrapperFactory`
- **AND** the nested action body is executing
- **THEN** the nested action is reported as current
- **AND** the parent action is not reported as current

#### Scenario: Nested wrapped action completes

- **WHEN** a nested framework-managed action completes and control returns to its parent action
- **THEN** the parent action is again reported as current

### Requirement: Rule-checking status

Each action invocation SHALL report whether Causeway checked rules, skipped rules, or cannot determine the status.

#### Scenario: Wrapper invocation checks rules

- **WHEN** an action is executing through a normal `WrapperFactory` invocation
- **THEN** that action is reported as current
- **AND** its rule-checking status is `CHECKED`

#### Scenario: Wrapper invocation skips rules

- **WHEN** an action is executing through a `WrapperFactory` invocation configured to skip rule validation
- **THEN** that action is reported as current
- **AND** its rule-checking status is `SKIPPED`

#### Scenario: Rule-checking status was not supplied

- **WHEN** an action invocation is reconstructed or created through the compatibility constructor without rule-checking information
- **THEN** its rule-checking status is `UNKNOWN`

#### Scenario: Matching includes rule-checking status

- **WHEN** the supplied target and action identity match the current action
- **AND** the expected rule-checking status matches the current invocation
- **THEN** status-aware current-action matching returns true

#### Scenario: Matching rule-checking status differs

- **WHEN** the supplied target and action identity match the current action
- **BUT** the expected rule-checking status differs from the current invocation
- **THEN** status-aware current-action matching returns false

#### Scenario: Plain Java invocation

- **WHEN** an action method is called directly without a matching metamodel-managed action execution
- **THEN** that action is not reported as current
