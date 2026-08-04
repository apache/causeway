## ADDED Requirements

### Requirement: Recording support enables safe-action command publishing
When `causeway.extensions.command-log.recording-support` is `ENABLED`, the system SHALL enable command publishing for action invocations with safe semantics through the normal action command-publishing facet model.
The system SHALL apply this recording-aware eligibility to unannotated safe actions and safe actions configured as `AS_CONFIGURED`, including when the ordinary global action policy is `NONE` or ignores safe actions.
The system MUST NOT use action result shape or bookmark availability to decide publishing eligibility.
When recording support is `DISABLED`, safe actions MUST retain existing command-publishing behavior.

#### Scenario: Unannotated safe action is eligible during recording
- **GIVEN** recording support is `ENABLED`
- **AND** a safe action has no explicit command-publishing setting
- **WHEN** the metamodel evaluates command-publishing metadata for the action
- **THEN** the action reports command publishing enabled

#### Scenario: Global action policy does not exclude a recorded safe action
- **GIVEN** recording support is `ENABLED`
- **AND** the global action command-publishing policy is `NONE` or ignores safe actions
- **AND** a safe action is unannotated or configured as `AS_CONFIGURED`
- **WHEN** the metamodel evaluates command-publishing metadata for the action
- **THEN** the action reports command publishing enabled

#### Scenario: Safe action remains unchanged when recording is disabled
- **GIVEN** recording support is `DISABLED`
- **AND** a safe action is not otherwise command-published
- **WHEN** a user invokes the action
- **THEN** the invocation is not command-published solely because it has safe semantics

#### Scenario: Result shape does not determine safe-action eligibility
- **GIVEN** recording support is `ENABLED`
- **AND** two otherwise equivalent safe actions return different result shapes or bookmark availability
- **WHEN** the metamodel evaluates command-publishing metadata for both actions
- **THEN** both actions have the same recording-aware publishing eligibility

### Requirement: Safe-action opt-outs and state-changing policy remain authoritative
An explicit `@Action(commandPublishing = Publishing.DISABLED)` setting SHALL remain a local opt-out for a safe action while recording support is enabled.
Recording-aware safe-action publishing MUST NOT alter command-publishing eligibility for idempotent or non-idempotent actions.
Existing explicit enablement and command DTO processor behavior SHALL remain authoritative and MUST NOT create a second publication path.

#### Scenario: Explicitly disabled safe action remains disabled
- **GIVEN** recording support is `ENABLED`
- **AND** a safe action is explicitly configured with command publishing `DISABLED`
- **WHEN** the metamodel evaluates command-publishing metadata for the action
- **THEN** the action reports command publishing disabled

#### Scenario: State-changing action retains existing policy
- **GIVEN** recording support is `ENABLED`
- **AND** an idempotent or non-idempotent action is not otherwise command-published
- **WHEN** the metamodel evaluates command-publishing metadata for the action
- **THEN** recording support does not make that action command-published solely because recording is enabled

#### Scenario: Explicitly published safe action is not duplicated
- **GIVEN** recording support is `ENABLED`
- **AND** a safe action is already explicitly command-published or uses a command DTO processor
- **WHEN** a user invokes the action once
- **THEN** the normal command-publishing flow publishes at most one command for that invocation

### Requirement: Recording support enables property-edit command publishing
When recording support is `ENABLED`, the system SHALL enable command publishing for ordinary property edits through the normal property command-publishing facet model.
Recording-aware property publishing SHALL apply to unannotated, `AS_CONFIGURED`, explicitly enabled, and explicitly disabled properties regardless of the ordinary global property publishing policy.
When recording support is `DISABLED`, properties MUST retain their existing annotation and global-policy behavior.
The system MUST install one authoritative property command-publishing facet and MUST NOT add a second property-edit publication path.

#### Scenario: Unannotated property edit is eligible during recording
- **GIVEN** recording support is `ENABLED`
- **AND** a property has no explicit command-publishing setting
- **WHEN** the metamodel evaluates command-publishing metadata for the property
- **THEN** the property reports command publishing enabled

#### Scenario: Explicitly disabled property is eligible during recording
- **GIVEN** recording support is `ENABLED`
- **AND** a property is explicitly configured with command publishing `DISABLED`
- **WHEN** the metamodel evaluates command-publishing metadata for the property
- **THEN** the property reports command publishing enabled for replay completeness

#### Scenario: Property policy remains unchanged when recording is disabled
- **GIVEN** recording support is `DISABLED`
- **AND** a property is not otherwise command-published
- **WHEN** a user edits the property
- **THEN** the edit is not command-published solely because it is a property edit

#### Scenario: Already-published property edit is not duplicated
- **GIVEN** recording support is `ENABLED`
- **AND** a property is already explicitly or globally command-published
- **WHEN** a user edits the property once
- **THEN** the normal command-publishing flow publishes at most one command for that edit

### Requirement: Recording-aware publication preserves command-recording suppression
Recording-aware action and property facets SHALL use the existing command preparation and publication lifecycle.
An interaction whose target or member owner implements `CommandRecordingSuppressed` MUST remain excluded by the C1 suppression boundary even when recording-aware facet metadata reports publishing enabled.
The system MUST NOT persist recording-aware interactions through a commandlog-specific fallback when normal command preparation suppresses them.

#### Scenario: Marked safe-action target remains suppressed
- **GIVEN** recording support is `ENABLED`
- **AND** a safe action is recording-aware eligible
- **AND** the interaction target or action owner implements `CommandRecordingSuppressed`
- **WHEN** the action is invoked
- **THEN** command preparation does not publish the invocation

#### Scenario: Marked property target remains suppressed
- **GIVEN** recording support is `ENABLED`
- **AND** a property edit is recording-aware eligible
- **AND** the interaction target or property owner implements `CommandRecordingSuppressed`
- **WHEN** the property is edited
- **THEN** command preparation does not publish the edit

#### Scenario: Eligible unmarked interaction uses normal lifecycle
- **GIVEN** recording support is `ENABLED`
- **AND** an unmarked safe action or property edit is recording-aware eligible
- **WHEN** the interaction executes
- **THEN** command readiness, start, and completion use the existing command-publishing lifecycle
- **AND** no recording-specific publication lifecycle is created
