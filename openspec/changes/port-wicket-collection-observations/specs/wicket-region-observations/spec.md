## MODIFIED Requirements

### Requirement: Wicket renders expose semantic regions

Causeway SHALL observe domain-object page, fieldset, regular domain property, parented collection, object-form action control and action-prompt rendering with distinct stable operation categories: causeway.wicket.page.render, causeway.wicket.fieldset.render, causeway.wicket.property.render, causeway.wicket.collection.render, causeway.wicket.action.render and causeway.wicket.action.prompt.render. Each rendered region SHALL produce one observation per render when admitted by shared Wicket detail and request-budget policy. Logical table properties and eligible row actions SHALL participate in member admission. Hidden regions, action parameter widgets, associated-parameter action links and presentation-only table widgets SHALL NOT acquire these member-level observations.

#### Scenario: Full object page
- **WHEN** a visible object page renders its domain regions
- **THEN** admitted page and child regions are observed around actual rendering, retaining their natural hierarchy and existing member work

#### Scenario: Ajax component update
- **WHEN** an Ajax request renders only a selected component subtree
- **THEN** the admitted rendered regions join that request's current ancestry without a fabricated page-render parent or duplicate member observations

#### Scenario: Prompt and excluded widgets
- **WHEN** an object action prompt renders parameters and controls
- **THEN** the admitted prompt has one region observation, while parameter widgets do not masquerade as domain property regions

### Requirement: Page preparation has a separate bounded lifecycle

Causeway SHALL observe domain-object page preparation using causeway.wicket.page.prepare, including initial component construction and page/descendant configuration and before-render work where those occur. Each admitted preparation phase SHALL be observed once, SHALL finish before page markup rendering starts and SHALL NOT carry an active scope into another request. Omitted preparation SHALL execute normally without an observation scope. Instrumentation SHALL NOT force otherwise unnecessary model resolution.

#### Scenario: First and subsequent page render
- **WHEN** a new or reused domain-object page prepares to render
- **THEN** its admitted preparation work is enclosed separately from markup rendering, with no duplicate observation from overlapping lifecycle hooks

#### Scenario: Preparation fails
- **WHEN** initial construction or descendant preparation fails
- **THEN** any admitted preparation observation records the failure and closes, preserving the original failure and existing request error handling

### Requirement: Existing observation ownership and viewer behavior remain intact

Wicket regions SHALL use the existing Causeway observation integration under Boot-managed and agent-managed tracing without competing registries, handlers, SDKs or exporters. Disabled or unavailable observations SHALL preserve UI behavior. Existing HTTP, Wicket request, interaction, transaction and domain member observations SHALL retain their lifecycle and ancestry. Shared detail and request-budget admission SHALL affect only Causeway Wicket observations. This capability SHALL NOT rename entry spans, filter non-Wicket spans by duration or instrument other viewers.

#### Scenario: Real exports in both modes
- **WHEN** representative full-page and Ajax work is exported under each tracing owner
- **THEN** semantic Wicket regions preserve casing, full identity, error reporting and natural HTTP/framework/interaction ancestry

#### Scenario: Observation inactive or unavailable
- **WHEN** the observation profile is inactive or telemetry integration/export is unavailable
- **THEN** rendering and error behavior remain unchanged and no competing telemetry infrastructure is created

### Requirement: Operator guidance integrates Wicket regions

The M3 how-to and observability guide SHALL describe Wicket preparation, rendering, prompts and collection/table detail within their existing flow, with sample operations and full-identifier inspection instructions. They SHALL preserve the Boot-first then agent structure, explain startup-bound detail, request budgets and collection summaries using environment overrides with the existing launcher, and distinguish Wicket-specific regions from HTMX and later root naming capabilities. New M3 functionality SHALL be described directly without before/after comparisons.

#### Scenario: Reader verifies the capability
- **WHEN** a reader follows the existing Petclinic telemetry instructions and opens an object, renders a collection or opens an action prompt in Wicket
- **THEN** the guidance explains the expected admitted preparation/render and collection regions, static attributes, numeric summaries, Wicket-only limits and how to select detail and budget without launcher scenario flags

