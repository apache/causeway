## ADDED Requirements

### Requirement: Service selection remains available during the active context lifecycle
The service registry SHALL delegate selection by requested type and qualifiers to the Spring context holder while that holder is available.

#### Scenario: Select services while the context is active
- **WHEN** a caller selects a service type while the Spring context holder is available
- **THEN** the registry returns the services selected by that holder

#### Scenario: Preserve qualifier-aware selection
- **WHEN** a caller selects a service type with qualifiers while the Spring context holder is available
- **THEN** the registry delegates both the type and qualifiers without alteration

### Requirement: Service selection is safe after context closure begins
The service registry SHALL return an empty selection without throwing when its Spring context holder is unavailable.

#### Scenario: Select after the context holder is cleared
- **WHEN** a caller selects a service type after the environment has cleared its Spring context holder
- **THEN** the registry returns an empty selection
- **AND** no null-dereference exception is raised

#### Scenario: Dispose the metamodel after context closure begins
- **WHEN** Spring closes an application context and metamodel disposal performs its layout-service lookup
- **THEN** the unavailable service is treated as an empty selection
- **AND** metamodel disposal completes without a `ServiceRegistryDefault.select(...)` destroy-method exception
