# configurable-execution-mode-attribute Specification

## Purpose

Defines the shared configurable span attribute key used by Causeway and consuming applications to classify foreground and background execution.

## Requirements

### Requirement: Execution-mode attribute key is configurable
The system SHALL expose the execution-mode span attribute key as `causeway.execution.mode.key` through the typed Causeway configuration API.
The property's default value MUST be `causeway.execution.mode`.

#### Scenario: Application uses the default configuration
- **WHEN** an application does not configure `causeway.execution.mode.key`
- **THEN** the typed configuration reports `causeway.execution.mode` as the execution-mode attribute key

#### Scenario: Application overrides the key
- **WHEN** an application configures `causeway.execution.mode.key` with a non-default value
- **THEN** the typed configuration reports that configured value for use by framework and application instrumentation

### Requirement: Framework classification uses the configured key
The system SHALL write foreground and background execution-mode span attributes using the key reported by `causeway.execution.mode.key`.
The system MUST retain `foreground` and `background` as the corresponding bounded attribute values.

#### Scenario: Foreground request uses an overridden key
- **WHEN** the foreground web filter classifies an HTTP entry span and the application has overridden `causeway.execution.mode.key`
- **THEN** the span receives the value `foreground` under the overridden key

#### Scenario: Background command uses an overridden key
- **WHEN** the command-log Quartz job classifies its entry span and the application has overridden `causeway.execution.mode.key`
- **THEN** the span receives the value `background` under the overridden key

#### Scenario: Existing application keeps the default key
- **WHEN** Causeway classifies a foreground or background span without an execution-mode key override
- **THEN** the span retains the existing `causeway.execution.mode` attribute contract

### Requirement: Consuming applications can share the framework key
A consuming application SHALL be able to read the configured execution-mode key from `CausewayConfiguration` and use it in custom filters or instrumentation without duplicating the default literal.

#### Scenario: Custom application instrumentation reads the key
- **WHEN** consuming application code obtains the execution-mode key from the injected typed configuration
- **THEN** it receives the same key used by Causeway's foreground and background classifiers
