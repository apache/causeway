# wicket-observation-volume-control Specification

## Purpose
TBD - created by archiving change bound-wicket-observation-volume. Update Purpose after archive.
## Requirements
### Requirement: Configurable Wicket observation detail

The Wicket viewer SHALL support observation detail levels `NONE`, `PAGE`, `REGIONS`, `ROWS`, and `MEMBERS` with a monotonic hierarchy of semantic categories.
The default level SHALL be `MEMBERS`.

#### Scenario: No Wicket detail

- **WHEN** observation is active and Wicket observation detail is `NONE`
- **THEN** no Causeway Wicket observation is created
- **AND** core interaction, invocation, association-access, Java-agent HTTP, and Java-agent JDBC observations remain unaffected

#### Scenario: Page detail

- **WHEN** detail is `PAGE`
- **THEN** eligible page-preparation, page-render, and action-prompt observations can be created
- **AND** fieldset, collection, table, row, property, and action-button observations are suppressed

#### Scenario: Region detail

- **WHEN** detail is `REGIONS`
- **THEN** page detail plus eligible fieldset, collection-preparation, collection-render, table, table-header, table-body, and table-footer observations can be created
- **AND** row, property, and action-button observations are suppressed

#### Scenario: Row detail

- **WHEN** detail is `ROWS`
- **THEN** region detail plus eligible visible-row preparation and render observations can be created
- **AND** logical property and eligible action-button observations are suppressed

#### Scenario: Member detail

- **WHEN** detail is `MEMBERS`
- **THEN** all otherwise eligible page, region, row, logical-property, and eligible action observations can be created

### Requirement: Hard per-request Wicket observation budget

The Wicket viewer SHALL support `max-spans-per-request`, where `0` means unlimited and any positive value is a hard maximum on Causeway Wicket observations started during one request.
The default SHALL be `0`.
Only Causeway Wicket observations SHALL count against this maximum.

#### Scenario: Unlimited compatibility budget

- **WHEN** `max-spans-per-request` is `0`
- **THEN** no otherwise eligible Wicket observation is suppressed because of a request count

#### Scenario: Finite budget is exhausted

- **WHEN** a positive maximum has been reached during a full-page or Ajax request
- **THEN** no further Causeway Wicket observation is started during that request
- **AND** the total number of Causeway Wicket observations does not exceed the configured maximum

#### Scenario: Automatic and core spans are present

- **WHEN** Java-agent HTTP or JDBC spans and non-Wicket Causeway observations occur during a budgeted request
- **THEN** those spans do not consume the Wicket observation budget
- **AND** they remain governed by their existing instrumentation and sampling

#### Scenario: A new request begins

- **WHEN** a full-page or Ajax request completes and another request begins on the same thread or session
- **THEN** the new request receives a fresh Wicket observation budget
- **AND** no suppression count or active scope leaks from the completed request

### Requirement: Structural observation priority

For a finite budget, the Wicket viewer SHALL reserve bounded capacity for page and region observations before admitting row and member observations.
Structural priority SHALL never allow the total to exceed the configured maximum.

#### Scenario: Row or member detail approaches the reserve

- **WHEN** admitting another row or member observation would consume capacity reserved for structural observations
- **THEN** that row or member observation is suppressed
- **AND** a later eligible page or region observation can consume the reserved capacity while budget remains

#### Scenario: Structural observations exhaust the absolute maximum

- **WHEN** page and region observations have consumed the configured maximum
- **THEN** later structural and detailed observations are suppressed
- **AND** the hard maximum remains satisfied

### Requirement: Deterministic suppression parentage

An otherwise eligible observation whose immediate semantic parent is not emitted SHALL attach to the nearest active emitted Wicket ancestor or to the naturally current non-Wicket context.
The viewer SHALL NOT retain a scope or create a synthetic parent for a suppressed observation.

#### Scenario: Immediate parent is suppressed by detail

- **WHEN** an observation is permitted but its immediate parent category is not emitted
- **THEN** the observation uses the nearest active permitted ancestor
- **AND** its scope closes according to its own lifecycle

#### Scenario: Ajax response has no page-render observation

- **WHEN** an Ajax response renders an eligible logical region without a page-render observation
- **THEN** the region joins the current Ajax request trace through the nearest active context
- **AND** no page observation is fabricated

#### Scenario: Monotonic detail excludes a row

- **WHEN** the configured level excludes row observations
- **THEN** property and row-action descendants are also excluded
- **AND** no member span is emitted without its required semantic detail category

### Requirement: Bounded suppression reporting

The viewer SHALL report fixed-key numeric counts for Wicket observations suppressed by detail and by exhausted budget on the nearest active structural Wicket observation.
It SHALL NOT report suppressed descriptor names, member identities, row identities, or values.

#### Scenario: Detail suppresses observations

- **WHEN** candidate Wicket observations are excluded by the configured detail level while a structural Wicket observation is active
- **THEN** that structural observation reports a bounded detail-suppression count

#### Scenario: Budget suppresses observations

- **WHEN** candidate Wicket observations are denied by the finite request budget while a structural Wicket observation is active
- **THEN** that structural observation reports a bounded budget-suppression count

#### Scenario: No structural Wicket observation is active

- **WHEN** suppression occurs with no active Wicket observation capable of carrying metadata
- **THEN** no synthetic summary observation is created
- **AND** no suppression metadata is attached to automatic or non-Wicket observations

### Requirement: Aggregate visible-row diagnostics

For each emitted parented collection-preparation or collection-render observation, the viewer SHALL record fixed-key aggregate diagnostics from applicable visible-row and logical-cell callbacks without creating additional observations.
Preparation and rendering aggregates SHALL remain separate.

#### Scenario: Visible rows are prepared

- **WHEN** visible-row preparation callbacks execute within an emitted collection-preparation boundary
- **THEN** the collection-preparation observation reports row callback count, total monotonic duration, and maximum monotonic duration
- **AND** the diagnostics contain no row identity or domain value

#### Scenario: Visible rows and logical cells are rendered

- **WHEN** visible-row and logical-cell callbacks execute within an emitted collection-render boundary
- **THEN** the collection-render observation reports row count, row total duration, row maximum duration, and logical-cell count
- **AND** the diagnostics contain no property identity, row identity, or domain value beyond the collection's existing static metadata

#### Scenario: Child observations are suppressed

- **WHEN** row or member callbacks execute but their observations are suppressed by detail or budget
- **THEN** their work still contributes to the applicable emitted collection aggregate
- **AND** the collection observation reports a fixed-key suppressed-child count

#### Scenario: Full child detail is emitted

- **WHEN** row and member observations are admitted
- **THEN** aggregate timing is collected from the framework callbacks rather than reconstructed from exported child spans
- **AND** no duplicate row or cell observation is created for aggregation

#### Scenario: No collection observation is emitted

- **WHEN** detail or budget prevents the applicable collection observation from being emitted
- **THEN** no synthetic aggregate carrier is created
- **AND** temporary aggregate state is discarded safely

### Requirement: Aggregate lifecycle and privacy

Aggregate timing SHALL use a monotonic clock, SHALL close on success and failure, and SHALL remain request-local and non-serialized.
Aggregate and suppression metadata MUST NOT contain total domain collection size, off-page row count, row index, object identity, bookmark, title, value, filter, label, user, tenant, or generated component path.

#### Scenario: Row callback fails

- **WHEN** a timed row callback fails
- **THEN** its timer closes and request-local aggregate state remains consistent
- **AND** the original failure behavior is preserved

#### Scenario: Component state is serialized

- **WHEN** a Wicket component or page containing observation descriptors is serialized
- **THEN** request counters, active observations, scopes, timers, and aggregate accumulators are not serialized
- **AND** only existing static serializable descriptors remain in component state

#### Scenario: Observation is inactive

- **WHEN** the `observation` profile is inactive
- **THEN** Wicket behavior and output remain unchanged
- **AND** no Wicket observation, suppression metadata, or aggregate telemetry is exported

### Requirement: Startup-bound configuration

Wicket observation detail and budget settings SHALL be read through the established Causeway Wicket configuration model and SHALL remain stable for the lifetime of a request.

#### Scenario: Configuration is omitted

- **WHEN** an application supplies no Wicket observation volume configuration
- **THEN** detail defaults to `MEMBERS`
- **AND** `max-spans-per-request` defaults to unlimited

#### Scenario: Configuration changes externally

- **WHEN** an operator changes Wicket observation detail or budget configuration
- **THEN** the documented deployment restart or revision replacement is required before the new values govern requests
- **AND** an in-flight request does not change policy midway through its lifecycle

