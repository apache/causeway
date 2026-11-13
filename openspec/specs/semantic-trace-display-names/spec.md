# semantic-trace-display-names Specification

## Purpose
TBD - created by archiving change port-semantic-trace-display-names. Update Purpose after archive.
## Requirements
### Requirement: Foreground entry naming uses existing tracing ownership

With Causeway observation active, semantic naming SHALL capture the existing foreground HTTP entry through the shared tracing bridge and update that entry before it ends under Boot-managed and agent-managed tracing. It SHALL NOT create spans, rename children or background entries, change stable observation operation categories, or introduce tracing infrastructure. Missing tracing context SHALL be a safe no-op.

#### Scenario: Either tracing owner
- **WHEN** a supported semantic outcome occurs in an observed synchronous HTTP request under either owner
- **THEN** the exported entry reflects that outcome while HTTP attributes, execution classification, trace/span identity and child parentage remain intact

#### Scenario: Inactive or absent tracing
- **WHEN** Causeway observation is inactive or there is no supported current tracing context
- **THEN** application behavior remains intact without semantic entry mutation or new telemetry infrastructure

### Requirement: Candidates have deterministic bounded static identity

Causeway SHALL select action over prompt over view, with the first candidate winning equal-priority ties. Displays SHALL use act, prompt or view prefixes and existing case-preserving 50-character compaction. The selected display SHALL be tagged as causeway.trace.name. Actions/prompts SHALL retain full canonical causeway.action.id, including declared parameter signatures, and views SHALL retain full causeway.object.type. Display names SHALL omit action signatures and instance data; nominations SHALL use only static logical identity. Requests with no candidate SHALL leave the supplied entry name unchanged.

#### Scenario: Priority and ties
- **WHEN** view, prompt and multiple eligible actions nominate in one request
- **THEN** the first eligible action supplies the entry display and canonical action attribute

#### Scenario: Compaction and privacy
- **WHEN** a long contributed action identity nominates for different object instances
- **THEN** its display is bounded and case-preserving, its full canonical identity remains available and no values, bookmarks, titles or instance identifiers appear

#### Scenario: No supported outcome
- **WHEN** a static-resource or other request has no semantic candidate
- **THEN** its entry keeps the tracing owner's supplied name and acquires no selected-outcome attributes

### Requirement: Trusted nominations identify the request outcome

A real action SHALL nominate only when its domain-facing identity matches the current top-level interaction command and it is not a mixed-in association access. Contributed actions SHALL preserve domain-facing identity without rewriting physical invocation identifiers. Calculated property/collection access, unmatched nested actions and helper work SHALL NOT take over entry naming. Wicket full object-page rendering SHALL nominate view; an enclosing action-prompt render SHALL nominate prompt. Ajax-only region rendering SHALL NOT fabricate a view nomination. Wicket nominations SHALL remain independent of detail and budget admission and SHALL NOT force model resolution.

#### Scenario: Top-level contributed action
- **WHEN** a contributed action executes with matching command identity and invokes unmatched nested/helper work
- **THEN** its domain-facing action supplies the entry name and physical invocation identity remains unchanged

#### Scenario: Calculated association access
- **WHEN** rendering performs mixed-in property or collection access
- **THEN** that access does not nominate an action outcome

#### Scenario: Suppressed page or prompt region
- **WHEN** a supported Wicket page or prompt renders with NONE detail or an exhausted region budget
- **THEN** its nomination still selects the applicable entry name without creating an otherwise suppressed region span

#### Scenario: Ajax subtree
- **WHEN** Ajax renders an arbitrary property or collection subtree without an action or enclosing prompt outcome
- **THEN** no full-page view nomination is fabricated

### Requirement: Naming state is bounded by request lifetime

Naming scopes SHALL isolate nested requests, close idempotently in reverse order and discard state on success or failure. Cleanup SHALL preserve original processing failures and prevent stale nominations on reused workers. Serialized Wicket pages SHALL retain no live naming context. Error/async redispatch SHALL NOT retarget unrelated current child spans; cross-thread continuation nominations are outside the synchronous scope.

#### Scenario: Failed request followed by success
- **WHEN** request processing or naming cleanup fails and another request runs on the same worker
- **THEN** the original failure is preserved and later entry naming contains no stale state or ancestry

#### Scenario: Nested scopes
- **WHEN** nested naming scopes close normally and close is repeated
- **THEN** each outcome applies to its captured entry once and the enclosing nomination state is restored

### Requirement: Operators can verify semantic names

The M3 how-to and observability guide SHALL integrate action, prompt and view naming into their existing flow with Petclinic operations, priority, full-identifier inspection and no-candidate behavior. They SHALL preserve Boot-first then agent structure, describe detail/budget independence and Wicket-only page/prompt nominations, and explain HTTP attributes for route grouping. New M3 capability SHALL be described directly without before/after comparisons or additional launcher scenario flags.

#### Scenario: Reader inspects outcomes
- **WHEN** a reader follows the existing telemetry setup and views an object, opens a prompt and invokes an action
- **THEN** the guidance identifies expected entry displays and static attributes under both owners and distinguishes them from child region/member displays

