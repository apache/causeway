## RENAMED Requirements

- FROM: `### Requirement: Identity metadata requires independent opt-in`
- TO: `### Requirement: Interaction identity metadata is automatic`

## MODIFIED Requirements

### Requirement: Interaction identity metadata is automatic

Causeway SHALL always include nonempty username and multitenancy-token values under the existing high-cardinality keys `causeway.user.name` and `causeway.user.multiTenancyToken` on interaction spans. There SHALL be no configuration properties to enable or disable these attributes. Empty values SHALL be omitted and neither value SHALL become a metric label. Other interaction metadata SHALL remain available.

#### Scenario: Defaults
- **WHEN** an observed interaction has a named user and tenancy token
- **THEN** both identity values are emitted without configuration

#### Scenario: Missing values
- **WHEN** an observed interaction has empty or missing identity values
- **THEN** those attributes are omitted without inventing values

### Requirement: Telemetry migration and limitations are documented

The observability guide SHALL describe operation/contextual names, automatic identity attributes, duration defaults and configuration examples. It SHALL distinguish Micrometer operation identity from exported contextual names, explain that enabled duration filtering can leave missing parents, and retain the supported agent requirement to disable filtering. Migration SHALL NOT introduce duplicate legacy spans.

#### Scenario: Existing deployment migrates
- **WHEN** an operator follows the migration guide
- **THEN** they can inspect automatically included identity attributes and choose supported duration filtering without another SDK or duplicate observations

### Requirement: JPA duration policy is explicit and validated

Causeway SHALL expose only `causeway.observation.jpa-duration-threshold` for JPA duration filtering, defaulting to `0ms`. Zero SHALL disable filtering. A positive threshold SHALL mark successful JPA observations strictly below the threshold for discard; failed observations SHALL remain eligible for export. Negative or malformed thresholds SHALL fail startup. No separate duration-filtering-enabled property SHALL exist. Agent-owned export SHALL require a zero threshold because it does not consume Spring's discard predicate.

#### Scenario: Default or zero threshold
- **WHEN** the threshold is zero or unset
- **THEN** short successful JPA spans are retained

#### Scenario: Positive threshold
- **WHEN** the threshold is positive
- **THEN** shorter successful JPA spans are marked for discard while failed spans remain eligible

#### Scenario: Invalid threshold
- **WHEN** the threshold is negative or malformed
- **THEN** startup reports a configuration error
