## REMOVED Requirements

### Requirement: JPA duration policy is explicit and validated
**Reason**: JPA-only duration suppression fragments traces and cannot enforce the same decision for independently exported JDBC children or agent-owned export.
**Migration**: Remove `causeway.observation.jpa-duration-threshold` and its environment override. Causeway no longer provides duration-based JPA suppression; use normal sampling/export controls while general filtering is explored.

## ADDED Requirements

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

## MODIFIED Requirements

### Requirement: Telemetry migration and limitations are documented
The observability guide SHALL describe operation/contextual names, automatic identity attributes and the removal of JPA duration filtering. It SHALL distinguish Micrometer operation identity from exported contextual names and explain that normal sampling/export policy still determines available traces. It SHALL NOT present the removed threshold as supported configuration or require agent-specific duration overrides. Migration SHALL NOT introduce duplicate legacy spans.

#### Scenario: Existing deployment migrates
- **WHEN** an operator follows the guide
- **THEN** they can remove obsolete threshold settings and inspect complete eligible JPA/JDBC ancestry without configuring a replacement Causeway filter
