## ADDED Requirements

### Requirement: Collections expose actual preparation and table phases

Causeway SHALL observe canonical parented collection preparation, table/header/body/footer rendering and participating row preparation/rendering using causeway.wicket.collection.prepare, causeway.wicket.collection.row.prepare, causeway.wicket.collection.table.render, causeway.wicket.collection.table.header.render, causeway.wicket.collection.table.body.render, causeway.wicket.collection.table.footer.render and causeway.wicket.collection.row.render, subject to shared Wicket admission. Preparation SHALL finish before markup. Row preparation SHALL enclose actual population work; row rendering SHALL follow table-body ancestry. Instrumentation SHALL NOT enumerate off-page rows or introduce provider fetches, counts or model resolution. Standalone result/parameter tables and presentation-only widgets SHALL remain excluded.

#### Scenario: Paginated collection
- **WHEN** a parented table prepares and renders a page of rows at admitted detail
- **THEN** collection/table phases and participating rows are observed around their actual callbacks without processing additional rows

#### Scenario: Ajax subtree
- **WHEN** an Ajax request updates a table or row subtree
- **THEN** only participating callbacks are observed under current surviving ancestry without fabricating a page-render parent

### Requirement: Collection members retain static logical identity

Rows SHALL identify their collection and element logical type. Logical property and eligible row-action callbacks SHALL use existing property/action operations and full canonical identifiers, including domain-facing action signatures. Displays SHALL preserve casing and the core 50-character limit. Metadata SHALL NOT contain row indices, bookmarks, instance titles, values or generated component paths. Header, navigation, toggle and decorative widgets SHALL NOT masquerade as logical member observations.

#### Scenario: Same collection on different instances
- **WHEN** equivalent logical rows and member callbacks render for different instances
- **THEN** their descriptors contain the same static identity without instance data

### Requirement: Detail is hierarchical and startup-bound

The configuration SHALL expose causeway.viewer.wicket.observation.detail with NONE, PAGE, REGIONS, ROWS and MEMBERS, default MEMBERS. NONE SHALL admit no Causeway semantic Wicket observations; PAGE SHALL admit page preparation/render and prompts; REGIONS SHALL additionally admit fieldsets, collections and table phases; ROWS SHALL additionally admit rows; MEMBERS SHALL additionally admit logical properties and eligible actions. Invalid/null detail SHALL fail startup. Configuration SHALL use main's immutable binding model.

#### Scenario: Reduced detail
- **WHEN** detail is REGIONS
- **THEN** structural candidates remain eligible while row/member candidates are omitted without changing their work

#### Scenario: Defaults and invalid binding
- **WHEN** observation settings are omitted or an unsupported detail is supplied
- **THEN** omitted settings resolve to MEMBERS and unsupported detail fails startup

### Requirement: One hard budget bounds the whole request

causeway.viewer.wicket.observation.max-spans-per-request SHALL default to 0 for unlimited and reject negative values at startup. A positive value SHALL bound cumulative admitted Causeway semantic Wicket starts across the whole full-page/Ajax request, all collections and preparation/rendering; closures SHALL NOT refund places. ROWS/MEMBERS SHALL NOT consume the structural reserve min(16, max(1, ceil(budget / 10))). PAGE/REGIONS MAY consume it but SHALL NOT exceed the absolute budget. Arithmetic SHALL be overflow-safe and state SHALL reset between requests.

#### Scenario: Multiple collections and completed spans
- **WHEN** a finite-budget request renders multiple collections after earlier observations have closed
- **THEN** their combined starts remain within the original budget with no refund or per-collection reset

#### Scenario: Reserve and smallest budget
- **WHEN** row/member candidates reach the unreserved limit or the budget is 1
- **THEN** row/member admission leaves the reserve available for structural candidates and all starts respect the absolute limit

### Requirement: Omission preserves work and observation ownership

Detail rejection SHALL precede budget rejection. Omitted candidates SHALL open no observation/scope and SHALL execute their underlying callbacks with unchanged error behavior. Emitted descendants SHALL join the nearest surviving active ancestor. Admission SHALL apply only to Causeway semantic Wicket observations, not HTTP, JDBC, JPA, transaction, domain work or other viewers. Both Boot and agent tracing SHALL reuse existing integration without duplicate infrastructure.

#### Scenario: Omitted parent and retained work
- **WHEN** a candidate is rejected while its underlying callback performs instrumented domain or JDBC work
- **THEN** that work still executes and its observations retain the current surviving context

### Requirement: Collection summaries survive omitted detail

Admitted collection preparation/render carriers SHALL report numeric causeway.wicket.collection.row.count, causeway.wicket.collection.row.duration.total.nanos and causeway.wicket.collection.row.duration.max.nanos for their respective participating callbacks. Render carriers SHALL additionally report causeway.wicket.collection.logical-cell.count and causeway.wicket.collection.suppressed.children. Omitted row/member observations SHALL still contribute. Counts SHALL represent callbacks rather than total collection size or physical columns. Failed callbacks SHALL contribute once; timing SHALL be monotonic, nonnegative and overflow-safe. Positive suppression counts SHALL be reported as causeway.wicket.suppressed.detail and causeway.wicket.suppressed.budget on the nearest admitted page/region carrier, counting each candidate under its first rejection reason. Missing carriers SHALL NOT create synthetic spans.

#### Scenario: Collection retained with children omitted
- **WHEN** a collection renders at REGIONS detail or with exhausted child budget
- **THEN** its admitted carrier reports participating row durations, logical-cell counts and omitted-child diagnostics without extra provider work

#### Scenario: Failure and no admitted carrier
- **WHEN** a callback fails or all potential summary carriers are omitted
- **THEN** failure preserves original error semantics and any admitted summary counts it once, while absent carriers produce no synthetic span

### Requirement: Request admission state remains transient and safely cleaned

Budget counters, summary frames and active telemetry SHALL belong to the current request and SHALL NOT survive page serialization or request completion. Cleanup SHALL close outstanding scopes in reverse order before enclosing request/interaction scopes, attempt remaining closures after a closure failure, preserve the original work failure and be idempotent.

#### Scenario: Failed request then reused page
- **WHEN** table work fails and the page is serialized or reused in a later request
- **THEN** the later request starts with fresh counters and no stale telemetry or parent context
