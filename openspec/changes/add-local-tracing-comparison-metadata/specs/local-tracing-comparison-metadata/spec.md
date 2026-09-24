## ADDED Requirements

### Requirement: Shared service identity with comparison role
The local tracing helper SHALL continue to configure `OTEL_SERVICE_NAME` as the logical application identity.
The helper SHALL map a non-empty `OTEL_SERVICE_ROLE` value to the `service.role` resource attribute without changing the service name.
The role value SHALL be treated as low-cardinality deployment metadata, with `baseline` and `candidate` documented as the conventional regression-comparison values.

#### Scenario: Baseline and candidate use one service
- **WHEN** two local processes use the same `OTEL_SERVICE_NAME` and set `OTEL_SERVICE_ROLE` to `baseline` and `candidate` respectively
- **THEN** both processes export under the same `service.name`
- **AND** each process exports its distinct `service.role`

#### Scenario: Role is not configured
- **WHEN** `OTEL_SERVICE_ROLE` is unset or empty
- **THEN** the helper does not synthesize a baseline or candidate role
- **AND** ordinary single-process local tracing remains supported

### Requirement: Git-derived service version with explicit override
The local tracing helper SHALL map a non-empty `OTEL_SERVICE_VERSION` value to the standard `service.version` resource attribute.
When `OTEL_SERVICE_VERSION` is unset or empty, the helper SHALL attempt to default it to the full commit SHA returned by `git rev-parse --verify HEAD` for the current working directory.
The helper SHALL NOT derive the version from Maven project metadata.

#### Scenario: Caller supplies a service version
- **WHEN** the caller sets `OTEL_SERVICE_VERSION` before sourcing the helper
- **THEN** the helper exports that exact value as `service.version`
- **AND** does not replace it with the current Git commit

#### Scenario: Git checkout supplies the default version
- **WHEN** `OTEL_SERVICE_VERSION` is not supplied and the current working directory belongs to a Git checkout
- **THEN** the helper exports the full current commit SHA as `service.version`

#### Scenario: No Git commit is available
- **WHEN** `OTEL_SERVICE_VERSION` is not supplied and the current working directory has no resolvable Git commit
- **THEN** the helper continues enabling local tracing without adding a synthetic `service.version`
- **AND** reports that the version could not be derived

### Requirement: Dirty-worktree provenance warning
When `service.version` is derived from Git, the local tracing helper SHALL detect tracked or untracked worktree changes.
The helper SHALL retain the exact commit SHA as `service.version` and SHALL NOT append a synthetic dirty suffix.

#### Scenario: Git-derived version comes from a dirty worktree
- **WHEN** the helper derives `service.version` from Git and the current checkout contains tracked or untracked changes
- **THEN** the helper warns that the commit SHA does not fully identify the executed worktree
- **AND** `service.version` remains the exact commit SHA

#### Scenario: Explicit version is used in a dirty worktree
- **WHEN** the caller supplies `OTEL_SERVICE_VERSION`
- **THEN** that value remains authoritative
- **AND** Git-derived version selection is not performed

### Requirement: Existing resource attributes are preserved
The local tracing helper SHALL add resolved comparison metadata to `OTEL_RESOURCE_ATTRIBUTES` without discarding unrelated existing resource attributes.
A resolved `OTEL_SERVICE_ROLE` or `OTEL_SERVICE_VERSION` SHALL replace an existing `service.role` or `service.version` entry respectively so that the resulting resource contains one deterministic value for each key.

#### Scenario: Platform metadata already exists
- **WHEN** `OTEL_RESOURCE_ATTRIBUTES` contains cloud, application, environment, or other unrelated metadata
- **THEN** the helper preserves those entries
- **AND** adds the resolved `service.role` and `service.version` entries

#### Scenario: Existing comparison key conflicts
- **WHEN** `OTEL_RESOURCE_ATTRIBUTES` already contains `service.role` or `service.version` and the corresponding helper value is resolved
- **THEN** the helper replaces the existing value for that key
- **AND** does not emit duplicate entries for that key

### Requirement: Resolved comparison identity is operationally visible
The local tracing helper SHALL report the resolved service name and any resolved service role and service version before the application starts.
The tracing operations guide SHALL document how to launch baseline and candidate processes under one service and filter them in Jaeger by role or version.

#### Scenario: Comparison metadata is enabled
- **WHEN** the helper resolves a service role and version
- **THEN** its completion output identifies the service name, role, and version
- **AND** the operator can verify the comparison identity before starting the JVM

#### Scenario: Operator compares two local versions
- **WHEN** an operator follows the documented baseline and candidate workflow
- **THEN** Jaeger presents both variants under one selected service
- **AND** `service.role` and `service.version` can distinguish their traces
