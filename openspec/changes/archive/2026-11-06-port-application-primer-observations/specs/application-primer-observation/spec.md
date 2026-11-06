## ADDED Requirements

### Requirement: Individual action-primer observations

With active observation, each invoked action primer SHALL run within exactly one `causeway.priming.action` observation.
Multiple matches SHALL produce sequential sibling observations in existing registration order, not an aggregate parent.
The observation SHALL close before the domain action method begins.

#### Scenario: One or several matching action callbacks

- **WHEN** an ordinary or contributed action invokes registered primers
- **THEN** each callback runs once inside its own action-primer observation in the existing order
- **AND** no aggregate priming observation is created

#### Scenario: No action callback is invoked

- **WHEN** no key matches or existing pass-through/association exclusions prevent priming
- **THEN** no action-primer observation is created and existing action behavior remains unchanged

### Requirement: Individual request-local view-primer observations

With active observation, each invoked view primer SHALL run within exactly one `causeway.priming.view` observation that closes before page preparation.
Existing authorization and once-per-page/request admission SHALL remain unchanged, and no aggregate parent SHALL be created.

#### Scenario: Authorized view callbacks

- **WHEN** one or several view primers match an admitted authorized page request
- **THEN** each callback has its own sequential sibling observation before page preparation

#### Scenario: No view callback is invoked

- **WHEN** the root is unauthorized/unmatched or request admission excludes re-entry
- **THEN** no view-primer observation is created

### Requirement: Bounded domain-facing names and private canonical metadata

Contextual names SHALL be `prime action <logical-type>#<local-action>` or `prime view <logical-type>`, preserving case and using existing namespace fallback/truncation within 50 characters.
Both kinds SHALL retain complete `causeway.object.type`; action primers SHALL also retain the complete domain-facing `causeway.action.id`, including resolved metamodel parameter signatures.
Only static canonical and framework bean/module metadata SHALL be added, never callback implementation/lambda names, sequence/counts, target identities, arguments, values, bookmarks, titles, users, tenants, transaction/interaction IDs or component paths.

#### Scenario: Ordinary or contributed action metadata

- **WHEN** an action-primer observation is created
- **THEN** its display omits signatures and its canonical attributes retain the complete registered domain-facing type/action, not the mixin implementation identity

#### Scenario: Long logical names and repeated siblings

- **WHEN** long logical names or several callbacks for one key are observed
- **THEN** contextual names remain bounded and canonical identifiers remain complete
- **AND** siblings use identical semantic names/metadata without implementation or ordering labels

### Requirement: Natural parentage and JDBC ownership

Primer observations SHALL inherit current action, request, interaction or externally supplied context without fabricating action, page-preparation or aggregate parents.
Synchronous JDBC work SHALL remain beneath the primer while retaining existing Boot/agent instrumentation ownership.

#### Scenario: Action callback executes SQL

- **WHEN** a primer performs synchronous SQL within an observed action
- **THEN** its observation inherits the action context and the SQL spans descend from that primer without duplicate instrumentation

#### Scenario: View callback precedes rendering

- **WHEN** a primer performs synchronous work before page preparation
- **THEN** it inherits current request/interaction context and is a sibling of later preparation/render work, not its child

### Requirement: Failure transparency and cleanup

A callback runtime exception or error SHALL be recorded and rethrown unchanged after its observation closes and restores the previous scope.
Existing fail-fast behavior SHALL prevent later callbacks and normal action/page work from proceeding.
Secondary telemetry errors MUST NOT replace the callback's primary failure.

#### Scenario: Callback failure

- **WHEN** an observed action/view primer fails
- **THEN** its identical failure propagates after error recording and cleanup without retry or suppression
- **AND** later matching callbacks do not run and subsequent requests inherit no stale primer scope

#### Scenario: Reporting or cleanup also fails

- **WHEN** telemetry error reporting or cleanup fails while a callback failure is propagating
- **THEN** the callback failure remains primary with secondary failures suppressed

### Requirement: Inactive and absent integration behavior

Absent/no-op observation integration SHALL preserve matching, order, invocation counts, results and original failures without creating primer observations or tracing infrastructure.
No primer spans SHALL be exported without an exporting pipeline.

#### Scenario: Inactive or missing integration

- **WHEN** registered callbacks run with the integration absent or no-op
- **THEN** each admitted callback executes exactly once with unchanged fail-fast behavior and no primer observation

#### Scenario: Exporter absent

- **WHEN** callbacks execute with no exporting tracing pipeline
- **THEN** work and failure cleanup still complete normally without exported primer spans
