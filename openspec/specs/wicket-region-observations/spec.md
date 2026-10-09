# wicket-region-observations Specification

## Purpose
TBD - created by archiving change port-wicket-region-observations. Update Purpose after archive.
## Requirements
### Requirement: Wicket renders expose semantic regions

Causeway SHALL observe domain-object page, fieldset, regular domain property, parented collection, object-form action control and action-prompt rendering with distinct stable operation categories: causeway.wicket.page.render, causeway.wicket.fieldset.render, causeway.wicket.property.render, causeway.wicket.collection.render, causeway.wicket.action.render and causeway.wicket.action.prompt.render. Each rendered region SHALL produce one observation per render. Hidden regions, action parameter widgets, associated-parameter action links and table-cell regions SHALL NOT acquire these member-level observations.

#### Scenario: Full object page
- **WHEN** a visible object page renders its domain regions
- **THEN** page and child regions are observed around actual rendering, retaining their natural hierarchy and existing member work

#### Scenario: Ajax component update
- **WHEN** an Ajax request renders only a selected component subtree
- **THEN** the rendered regions join that request's current ancestry without a fabricated page-render parent or duplicate member observations

#### Scenario: Prompt and excluded widgets
- **WHEN** an object action prompt renders parameters and controls
- **THEN** the prompt has one region observation, while parameter widgets do not masquerade as domain property regions

### Requirement: Page preparation has a separate bounded lifecycle

Causeway SHALL observe domain-object page preparation using causeway.wicket.page.prepare, including initial component construction and page/descendant configuration and before-render work where those occur. Each preparation phase SHALL be observed once, SHALL finish before page markup rendering starts and SHALL NOT carry an active scope into another request. Instrumentation SHALL NOT force otherwise unnecessary model resolution.

#### Scenario: First and subsequent page render
- **WHEN** a new or reused domain-object page prepares to render
- **THEN** its actual preparation work is enclosed separately from markup rendering, with no duplicate observation from overlapping lifecycle hooks

#### Scenario: Preparation fails
- **WHEN** initial construction or descendant preparation fails
- **THEN** the preparation observation records the failure and closes, preserving the original failure and existing request error handling

### Requirement: Static descriptors preserve full semantic identity

Wicket region descriptors SHALL retain only static logical model identity. Displays SHALL preserve casing and follow the core 50-character compaction policy. Page preparation/render displays SHALL begin with prepare/render, member regions with render and prompts with prompt. Full causeway.object.type and applicable causeway.fieldset.id, causeway.property.id, causeway.collection.id or causeway.action.id SHALL remain untruncated. Contributed action regions SHALL use domain-facing identity including declared parameter signatures. Descriptors SHALL NOT include instance identifiers, bookmarks, titles, argument values or localized labels.

#### Scenario: Contributed action prompt
- **WHEN** a mixin contributes an action whose prompt is rendered
- **THEN** the prompt identifies the domain contribution and its full signature without changing the physical invocation identity

#### Scenario: Long or colliding display
- **WHEN** region identities exceed the display budget or compact to the same text
- **THEN** displays are bounded while full static attributes distinguish the regions

#### Scenario: Default fieldset and instance privacy
- **WHEN** a default fieldset or the same logical region on different objects renders
- **THEN** identifiers use a stable static marker or model identity and contain no instance-specific data

### Requirement: Request cleanup and serialization preserve scope safety

Causeway SHALL own active preparation/render scopes within the current Wicket request. Normal completion SHALL unregister and close each scope once. Skipped callbacks, exception handling and request completion SHALL unwind remaining scopes in reverse opening order before their enclosing interaction/request scopes close. Cleanup SHALL be idempotent, SHALL attempt all outstanding closures even if one fails and SHALL preserve the original work failure. Serialized pages SHALL retain descriptors but SHALL NOT retain live telemetry objects or active request state.

#### Scenario: Child failure followed by another request
- **WHEN** a child render throws and a later request succeeds on the same worker
- **THEN** all outstanding scopes from the failed request are closed and the later trace inherits no stale region or parent

#### Scenario: Cleanup callback failure
- **WHEN** one closure fails during cleanup of multiple open regions
- **THEN** remaining regions are still closed and original request error semantics remain intact

#### Scenario: Page serialization
- **WHEN** a page is serialized during or after an observed lifecycle and subsequently deserialized
- **THEN** its static descriptors survive but active telemetry does not, and a later render starts fresh request-owned observations

### Requirement: Existing observation ownership and viewer behavior remain intact

Wicket regions SHALL use the existing Causeway observation integration under Boot-managed and agent-managed tracing without competing registries, handlers, SDKs or exporters. Disabled or unavailable observations SHALL preserve UI behavior. Existing HTTP, Wicket request, interaction, transaction and domain member observations SHALL retain their lifecycle and ancestry. This capability SHALL NOT rename entry spans or add table detail, filtering, budgets or instrumentation to other viewers.

#### Scenario: Real exports in both modes
- **WHEN** representative full-page and Ajax work is exported under each tracing owner
- **THEN** semantic Wicket regions preserve casing, full identity, error reporting and natural HTTP/framework/interaction ancestry

#### Scenario: Observation inactive or unavailable
- **WHEN** the observation profile is inactive or telemetry integration/export is unavailable
- **THEN** rendering and error behavior remain unchanged and no competing telemetry infrastructure is created

### Requirement: Operator guidance integrates Wicket regions

The M3 how-to and observability guide SHALL describe Wicket preparation, rendering and prompt regions within their existing flow, with sample operations and full-identifier inspection instructions. They SHALL preserve the Boot-first then agent structure, require no new region configuration flag and distinguish Wicket-specific regions from HTMX and later table/root naming capabilities. New M3 functionality SHALL be described directly without before/after comparisons.

#### Scenario: Reader verifies the capability
- **WHEN** a reader follows the existing Petclinic telemetry instructions and opens an object, renders a collection or opens an action prompt in Wicket
- **THEN** the guidance explains the expected preparation/render regions, where to inspect static attributes and which later capabilities are not yet included

