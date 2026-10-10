## MODIFIED Requirements

### Requirement: Existing observation ownership and viewer behavior remain intact

Wicket regions SHALL use the existing Causeway observation integration under Boot-managed and agent-managed tracing without competing registries, handlers, SDKs or exporters. Disabled or unavailable observations SHALL preserve UI behavior. Existing HTTP, Wicket request, interaction, transaction and domain member observations SHALL retain their lifecycle and ancestry. Shared detail and request-budget admission SHALL affect only Causeway semantic Wicket observations. Wicket page/prompt descriptors SHALL support semantic trace display nominations independently of region admission. This capability SHALL NOT filter non-Wicket spans by duration or instrument other viewers.

#### Scenario: Real exports in both modes
- **WHEN** representative full-page and Ajax work is exported under each tracing owner
- **THEN** semantic Wicket regions preserve casing, full identity, error reporting and natural HTTP/framework/interaction ancestry

#### Scenario: Observation inactive or unavailable
- **WHEN** the observation profile is inactive or telemetry integration/export is unavailable
- **THEN** rendering and error behavior remain unchanged and no competing telemetry infrastructure is created

### Requirement: Operator guidance integrates Wicket regions

The M3 how-to and observability guide SHALL describe Wicket preparation, rendering, prompts and collection/table detail within their existing flow, with sample operations and full-identifier inspection instructions. They SHALL preserve the Boot-first then agent structure, explain startup-bound detail, request budgets and collection summaries using environment overrides with the existing launcher, and distinguish Wicket-specific regions from HTMX and explain semantic entry naming separately from region displays. New M3 functionality SHALL be described directly without before/after comparisons.

#### Scenario: Reader verifies the capability
- **WHEN** a reader follows the existing Petclinic telemetry instructions and opens an object, renders a collection or opens an action prompt in Wicket
- **THEN** the guidance explains the expected admitted preparation/render and collection regions, static attributes, numeric summaries, Wicket-only limits and how to select detail and budget without launcher scenario flags
