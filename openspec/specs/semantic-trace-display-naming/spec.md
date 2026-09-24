# semantic-trace-display-naming Specification

## Purpose
TBD - created by archiving change add-semantic-trace-display-names. Update Purpose after archive.
## Requirements
### Requirement: Foreground entry span as semantic trace display name
Causeway SHALL use the Java-agent-created foreground HTTP server span's operation name as the portable approximation of a trace display name when a supported semantic candidate is selected.
Causeway SHALL rename that existing span rather than create a replacement root span.

#### Scenario: Supported semantic candidate is selected
- **WHEN** a foreground request produces a selected action, prompt, or entity-view candidate
- **THEN** the exported HTTP server span uses the candidate's bounded semantic display name
- **AND** the trace identity and root-span identity remain those created by the Java agent

#### Scenario: Telemetry backend lists traces by root operation
- **WHEN** a backend uses the root span operation name as the trace-list label
- **THEN** an eligible foreground trace is listed under its selected semantic name
- **AND** Causeway does not claim that OpenTelemetry provides a separate trace-name field

### Requirement: Request-local semantic candidate lifecycle
Causeway SHALL capture the current foreground entry span and install transient nomination state before delegating through the foreground filter.
Causeway SHALL select and apply the candidate after downstream request processing and SHALL clean up nomination state whether processing succeeds or fails.

#### Scenario: Request completes successfully
- **WHEN** downstream request processing nominates one or more candidates and returns normally
- **THEN** Causeway applies the selected candidate before the foreground filter returns
- **AND** removes all request-local nomination state

#### Scenario: Request processing fails
- **WHEN** downstream request processing nominates a candidate and then throws
- **THEN** Causeway applies the selected candidate while preserving the original failure
- **AND** removes all request-local nomination state

#### Scenario: Nested filter scope occurs
- **WHEN** a nested dispatch opens another semantic naming scope on the same thread
- **THEN** the nested scope does not overwrite the enclosing scope's state
- **AND** closing the nested scope restores the enclosing scope

### Requirement: Deterministic candidate selection
Causeway SHALL select candidates in descending priority order of action invocation, action prompt, and entity view.
A higher-priority candidate SHALL replace a lower-priority candidate, and the first candidate at an equal priority SHALL remain selected.

#### Scenario: Action and response page are both observed
- **WHEN** a request invokes a domain action and subsequently renders an entity page or action prompt
- **THEN** the action candidate wins
- **AND** later page or prompt nominations do not replace it

#### Scenario: Prompt and entity view are both observed
- **WHEN** a request presents an action prompt without invoking an action and also renders an entity page
- **THEN** the prompt candidate wins over the view candidate

#### Scenario: Repeated equal-priority nominations occur
- **WHEN** more than one candidate of the selected priority is nominated during a request
- **THEN** the first such candidate remains selected
- **AND** no runtime value is added to distinguish the candidates

### Requirement: Eligible semantic candidates
Causeway SHALL nominate action candidates only for an ordinary domain-facing action that represents the request's top-level interaction command.
Causeway SHALL nominate prompt candidates from an unambiguous rendered action prompt and view candidates from an entity page's logical object type.
Causeway MUST NOT nominate mixed-in association access, nested wrapper work, framework helper invocations, or ambiguous Ajax rendering as the request's primary action or view.

#### Scenario: Top-level action is invoked
- **WHEN** the request executes a top-level declared or mixed-in domain action
- **THEN** Causeway nominates `act <logical-member-identifier>` using the domain-facing action identity

#### Scenario: Calculated association is evaluated
- **WHEN** page preparation evaluates a property or collection implemented by a mixin action
- **THEN** that evaluation does not nominate an action trace name

#### Scenario: Top-level action invokes nested action
- **WHEN** the request's domain action invokes another action through wrapper machinery
- **THEN** the nested invocation does not replace or compete with the top-level action candidate

#### Scenario: Entity action prompt is primary outcome
- **WHEN** a request renders an unambiguous prompt without invoking the action
- **THEN** Causeway nominates `prompt <logical-member-identifier>`

#### Scenario: Entity page is viewed
- **WHEN** an entity page request has no action or prompt candidate
- **THEN** Causeway nominates `view <logical-type-name>`

#### Scenario: Ajax render has no semantic primary outcome
- **WHEN** an Ajax request renders regions without an invoked action or unambiguous prompt
- **THEN** Causeway does not nominate an entity-view candidate solely from those rendered regions

### Requirement: Bounded semantic names and canonical identity
Causeway SHALL preserve declared logical-identifier casing and limit selected semantic names to 50 characters using the existing full-name, namespace-free, and truncation policy.
Causeway SHALL attach the selected bounded name as `causeway.trace.name` and SHALL retain the applicable complete canonical identity as an entry-span attribute.

#### Scenario: Action candidate fits in full
- **WHEN** `act <logical-member-identifier>` does not exceed 50 characters
- **THEN** Causeway preserves the complete logical member identifier in the selected name
- **AND** attaches the complete canonical identifier as `causeway.action.id`

#### Scenario: Action or prompt candidate exceeds the limit
- **WHEN** an action or prompt name using the full logical identifier exceeds 50 characters
- **THEN** Causeway first retries without the logical type namespace and then truncates only if still necessary
- **AND** preserves the complete identity in `causeway.action.id`

#### Scenario: View candidate exceeds the limit
- **WHEN** `view <logical-type-name>` exceeds 50 characters
- **THEN** Causeway first retries with the namespace-free logical type and then truncates only if still necessary
- **AND** preserves the complete logical type in `causeway.object.type`

#### Scenario: Instance-specific context exists
- **WHEN** the selected operation concerns a particular object, user, tenant, or argument set
- **THEN** the entry-span name and Causeway attributes contain no object identity, title, bookmark, value, argument, user identity, tenancy identity, localized label, or arbitrary URL data

### Requirement: Transport telemetry preservation
Renaming a foreground entry span SHALL preserve its standard HTTP method, route, target, status, error, and other transport attributes.
It SHALL also preserve `causeway.execution.mode=foreground`, trace parentage, sampling decisions, and export through the Java agent.

#### Scenario: Entry span is renamed
- **WHEN** Causeway changes an HTTP server span from a transport-facing operation name to a semantic name
- **THEN** all previously recorded standard HTTP and Causeway execution-mode attributes remain available
- **AND** descendants remain in the same trace under the same entry span

#### Scenario: Transport-oriented monitoring is required
- **WHEN** operators need to group renamed requests by route or method
- **THEN** the standard HTTP route and method attributes remain authoritative for that grouping

### Requirement: Safe unchanged behavior
Causeway SHALL leave the agent-provided entry-span operation name unchanged when no supported candidate is selected, no Java agent supplies a recording current span, or the request is outside the supported foreground path.
Causeway SHALL NOT require an application-owned telemetry SDK, sampler, processor, exporter, or registry.

#### Scenario: No candidate is nominated
- **WHEN** a foreground request completes without an eligible action, prompt, or view candidate
- **THEN** the HTTP entry span retains its agent-provided operation name

#### Scenario: Current span is non-recording
- **WHEN** the foreground filter observes no recording entry span
- **THEN** candidate coordination and renaming are harmless no-ops
- **AND** request processing remains unchanged

#### Scenario: Background execution occurs
- **WHEN** a Quartz or other background entry span is classified as background
- **THEN** this capability does not rename that span

### Requirement: Supported-agent evidence gate
Causeway SHALL verify with an exported-span compatibility test that OpenTelemetry Java agent 1.31.0 preserves the final semantic name after the foreground filter returns.
The compatibility test SHALL also verify unchanged HTTP attributes, execution-mode classification, trace identity, and parentage.

#### Scenario: Agent preserves the filter update
- **WHEN** the agent-backed compatibility fixture exports a semantically renamed foreground entry span
- **THEN** the exported operation name equals the selected Causeway name
- **AND** in-process entry-span renaming is supported for the validated baseline

#### Scenario: Agent overwrites the filter update
- **WHEN** the exported operation name does not equal the name applied by the foreground filter
- **THEN** Causeway does not claim in-process semantic renaming as supported
- **AND** emits bounded `causeway.trace.name` and canonical identity attributes for a documented collector transform or supported agent extension

