## Purpose

Defines stable observation operation names, safe contextual metadata, automatic interaction identity, and duration-independent JPA span retention.
## Requirements
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

### Requirement: Telemetry migration and limitations are documented
The observability guide SHALL describe operation/contextual names, automatic identity attributes and the removal of JPA duration filtering. It SHALL distinguish Micrometer operation identity from exported contextual names and explain that normal sampling/export policy still determines available traces. It SHALL NOT present the removed threshold as supported configuration or require agent-specific duration overrides. Migration SHALL NOT introduce duplicate legacy spans.

#### Scenario: Existing deployment migrates
- **WHEN** an operator follows the guide
- **THEN** they can remove obsolete threshold settings and inspect complete eligible JPA/JDBC ancestry without configuring a replacement Causeway filter

### Requirement: Interaction identity metadata is automatic

Causeway SHALL always include nonempty username and multitenancy-token values under the existing high-cardinality keys `causeway.user.name` and `causeway.user.multiTenancyToken` on interaction spans. There SHALL be no configuration properties to enable or disable these attributes. Empty values SHALL be omitted and neither value SHALL become a metric label. Other interaction metadata SHALL remain available.

#### Scenario: Defaults
- **WHEN** an observed interaction has a named user and tenancy token
- **THEN** both identity values are emitted without configuration

#### Scenario: Missing values
- **WHEN** an observed interaction has empty or missing identity values
- **THEN** those attributes are omitted without inventing values

### Requirement: JPA observations are independent of duration
Causeway SHALL retain successful and failed JPA observations regardless of their duration, subject to ordinary sampling and export policies. It SHALL NOT expose a JPA duration threshold or automatically mark these observations for discard by duration. Existing operation/contextual names, metadata and ancestry SHALL remain intact in Boot-managed and agent-managed tracing. Explicit discard behavior outside duration filtering SHALL be preserved.

#### Scenario: Short successful operation
- **WHEN** a short JPA operation completes successfully under either supported tracing owner
- **THEN** its JPA span remains eligible for export with its children and parentage intact

#### Scenario: Failed operation
- **WHEN** a JPA operation fails
- **THEN** its error and observation lifecycle remain intact regardless of duration

#### Scenario: Obsolete configuration
- **WHEN** an application still supplies the removed JPA threshold property
- **THEN** it has no effect on Causeway's JPA observation retention and is absent from configuration metadata

