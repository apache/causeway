## ADDED Requirements

### Requirement: Existing dynamic observations separate operation and context

Causeway SHALL use fixed operation names for the existing JPA, action invocation, property update and execution-publishing observations. Contextual names and model metadata SHALL use only static entity type, member identity or declared named-query identity. Instance identifiers, bookmarks, object titles, query descriptions and arguments SHALL NOT be introduced into these names or attributes. Execution subscriber counts SHALL NOT be included in names or low-cardinality dimensions. Existing fixed-name instrumentation and invocation semantics SHALL be preserved.

#### Scenario: Different instances and query arguments
- **WHEN** the same JPA operation runs for different bookmarks or query argument values
- **THEN** its operation name remains identical and those values are absent from its Causeway-generated contextual name and attributes

#### Scenario: Different members
- **WHEN** different actions or property updates run
- **THEN** names identify the fixed operation category while contextual metadata identifies the static member without changing the invoked member identity

#### Scenario: Publishing to different subscriber counts
- **WHEN** execution publishing runs with different subscriber counts
- **THEN** its operation and contextual names remain stable and any emitted count is not a low-cardinality dimension

#### Scenario: Missing model metadata
- **WHEN** a safe static identifier is unavailable
- **THEN** the operation remains observable without falling back to object or query descriptions

### Requirement: Identity metadata requires independent opt-in

Causeway SHALL omit username and multitenancy-token attributes by default. It SHALL support independent `causeway.observation.include-user-name` and `causeway.observation.include-multi-tenancy-token` options, both defaulting to false. Explicitly enabled nonempty values SHALL retain their existing attribute keys and high-cardinality classification. Disabled values SHALL NOT be copied into other Causeway-generated names or attributes. Non-identity interaction metadata SHALL remain available.

#### Scenario: Defaults
- **WHEN** an observed interaction has a named user and tenancy token without identity opt-ins
- **THEN** neither identity value is emitted by Causeway interaction tagging

#### Scenario: Independent options
- **WHEN** exactly one identity option is enabled
- **THEN** only its corresponding nonempty value is emitted under the existing key

#### Scenario: Both options and empty values
- **WHEN** both options are enabled
- **THEN** both nonempty values are emitted, and missing or empty values are omitted

### Requirement: JPA duration policy is explicit and validated

Causeway SHALL default `causeway.observation.duration-filtering-enabled` to false and SHALL support `causeway.observation.jpa-duration-threshold`, defaulting to 2 ms. The threshold SHALL accept zero and positive durations and reject malformed or negative values at startup, including when filtering is disabled. The threshold SHALL apply only to the existing JPA duration-filtered boundaries and SHALL NOT change domain outcomes or observation activation.

#### Scenario: Default parent retention
- **WHEN** a short successful JPA observation has an exported JDBC child in the deterministically sampled compatibility fixture with default duration policy
- **THEN** the framework parent remains exported in both supported telemetry configurations and the child's parent ID resolves to it

#### Scenario: Configured and zero thresholds
- **WHEN** Boot-mode filtering is explicitly enabled with a configured threshold
- **THEN** successful observations below it are suppressed, observations equal to or above it are retained, and zero suppresses none by duration

#### Scenario: Invalid duration
- **WHEN** the threshold is negative or malformed
- **THEN** startup reports an actionable configuration failure regardless of whether duration filtering is enabled

#### Scenario: Observation profile inactive
- **WHEN** policy options are configured but the observation profile is inactive
- **THEN** Causeway framework observations remain disabled and application observations remain independent

### Requirement: Telemetry migration and limitations are documented

The observability guide SHALL describe changed operation/contextual names, identity opt-ins, duration defaults and configuration examples. It SHALL distinguish Micrometer operation identity from exported contextual names, state that opt-in duration filtering can leave missing parents, and retain the supported agent requirement to disable filtering. It SHALL NOT imply control over application/agent metadata or externally sampled/exported spans. Migration SHALL NOT introduce duplicate legacy spans.

#### Scenario: Existing deployment migrates
- **WHEN** an operator follows the migration guide
- **THEN** they can update affected queries and explicitly choose identity and supported duration filtering without requiring another SDK or duplicate observations
