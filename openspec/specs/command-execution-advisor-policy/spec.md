# Command Execution Advisor Policy Specification

## Purpose

Define configurable interaction-advisor behavior for command DTO action and property execution.

## Requirements

### Requirement: Command execution advisor policy is configurable
The system SHALL provide `causeway.core.runtime-services.command-executor-service.interaction-advisor-policy` with values `CHECK`, `CHECK_BUT_IGNORE`, and `NO_CHECK`.
The system SHALL default the policy to `NO_CHECK` so existing command DTO execution remains compatible when the property is not configured.
The policy SHALL apply to action invocation and property modification performed by `CommandExecutorService`.

#### Scenario: Default execution skips advisor enforcement
- **WHEN** no command-executor interaction-advisor policy is configured
- **THEN** command DTO execution uses `NO_CHECK`

#### Scenario: Check policy is configured
- **WHEN** the interaction-advisor policy is configured as `CHECK`
- **THEN** command DTO execution enforces action and property interaction advisors

### Requirement: Check policy enforces action advisors
When the policy is `CHECK`, action command execution SHALL evaluate visibility, usability, and complete argument-set validity in that order before invocation.
A visibility veto SHALL prevent invocation and raise the framework's standard hidden exception with the action visibility interaction event.
A usability veto SHALL prevent invocation and raise the framework's standard disabled exception with the action usability interaction event.
An argument-set validity veto SHALL prevent invocation and raise the framework's standard invalid exception with the action invocation interaction event.
The system MUST NOT evaluate later advisor phases after an earlier veto.

#### Scenario: Hidden action is rejected
- **GIVEN** command execution advisor policy is `CHECK`
- **WHEN** an action visibility advisor vetoes replay execution
- **THEN** the action is not invoked
- **AND** command execution fails with the standard hidden exception

#### Scenario: Disabled action is rejected
- **GIVEN** command execution advisor policy is `CHECK`
- **AND** action visibility permits execution
- **WHEN** an action usability advisor vetoes replay execution
- **THEN** the action is not invoked
- **AND** command execution fails with the standard disabled exception

#### Scenario: Invalid action arguments are rejected
- **GIVEN** command execution advisor policy is `CHECK`
- **AND** action visibility and usability permit execution
- **WHEN** complete argument-set validation vetoes replay execution
- **THEN** the action is not invoked
- **AND** command execution fails with the standard invalid exception

### Requirement: Check policy enforces property advisors
When the policy is `CHECK`, property command execution SHALL evaluate visibility, usability, and proposed-value validity in that order before mutation.
A visibility veto SHALL prevent mutation and raise the framework's standard hidden exception with the property visibility interaction event.
A usability veto SHALL prevent mutation and raise the framework's standard disabled exception with the property usability interaction event.
A proposed-value validity veto SHALL prevent mutation and raise the framework's standard invalid exception with the property modification interaction event.
The system MUST NOT evaluate later advisor phases after an earlier veto.

#### Scenario: Hidden property is rejected
- **GIVEN** command execution advisor policy is `CHECK`
- **WHEN** a property visibility advisor vetoes execution
- **THEN** the property is not modified
- **AND** command execution fails with the standard hidden exception

#### Scenario: Disabled property is rejected
- **GIVEN** command execution advisor policy is `CHECK`
- **AND** property visibility permits execution
- **WHEN** a property usability advisor vetoes execution
- **THEN** the property is not modified
- **AND** command execution fails with the standard disabled exception

#### Scenario: Invalid property value is rejected
- **GIVEN** command execution advisor policy is `CHECK`
- **AND** property visibility and usability permit execution
- **WHEN** proposed-value validation vetoes execution
- **THEN** the property is not modified
- **AND** command execution fails with the standard invalid exception

### Requirement: Check-but-ignore policy invokes advisors without enforcing vetoes
When the policy is `CHECK_BUT_IGNORE`, action command execution SHALL evaluate visibility, usability, and complete argument-set validity in that order and SHALL invoke the action regardless of veto.
When the policy is `CHECK_BUT_IGNORE`, property command execution SHALL evaluate visibility, usability, and proposed-value validity in that order and SHALL modify the property regardless of veto.
The system SHALL invoke all advisor phases under `CHECK_BUT_IGNORE`, including phases after a veto.

#### Scenario: Action vetoes are observed and ignored
- **GIVEN** command execution advisor policy is `CHECK_BUT_IGNORE`
- **WHEN** action visibility, usability, or argument validation vetoes execution
- **THEN** all three action advisor phases are invoked in order
- **AND** the action is invoked

#### Scenario: Property vetoes are observed and ignored
- **GIVEN** command execution advisor policy is `CHECK_BUT_IGNORE`
- **WHEN** property visibility, usability, or value validation vetoes execution
- **THEN** all three property advisor phases are invoked in order
- **AND** the property is modified

### Requirement: No-check policy bypasses advisors
When the policy is `NO_CHECK`, action command execution MUST NOT evaluate action visibility, usability, or argument validity before invocation.
When the policy is `NO_CHECK`, property command execution MUST NOT evaluate property visibility, usability, or proposed-value validity before mutation.

#### Scenario: Action executes without advisor calls
- **GIVEN** command execution advisor policy is `NO_CHECK`
- **WHEN** an action command DTO is executed
- **THEN** the action is invoked without calling its interaction advisors

#### Scenario: Property changes without advisor calls
- **GIVEN** command execution advisor policy is `NO_CHECK`
- **WHEN** a property command DTO is executed
- **THEN** the property is modified without calling its interaction advisors
