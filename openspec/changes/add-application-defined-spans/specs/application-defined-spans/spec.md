## ADDED Requirements

### Requirement: Application-defined span service
Causeway SHALL provide `org.apache.causeway.applib.services.span.ApplicationSpanService` as an injectable applib service that executes synchronous application work within an application-defined observation span without exposing Micrometer or OpenTelemetry types through its public contract.
The service SHALL provide a value-returning `call(String suffix, Callable<T>)` operation and a non-returning `run(String suffix, ThrowingRunnable)` operation.

#### Scenario: Value-returning region completes
- **WHEN** application code calls the service with a valid suffix and a callable that returns a value
- **THEN** Causeway executes the callable exactly once within an application-defined span
- **AND** returns the callable's value unchanged

#### Scenario: Non-returning region completes
- **WHEN** application code calls the service with a valid suffix and a throwing runnable that completes normally
- **THEN** Causeway executes the runnable exactly once within an application-defined span
- **AND** closes and stops the span after the runnable completes

### Requirement: Current logical member identity
Causeway SHALL ensure that declared and contributed mixin action executions expose their domain-facing `Identifier` as the current execution's logical member identifier.
Causeway SHALL resolve the domain-facing logical member identifier for an application-defined span from the current interaction's current execution when one is available.
Causeway SHALL format the canonical identifier as `<logical-type-name>#<member-logical-name>` without action parentheses or parameter type signatures.
Causeway SHALL NOT reconstruct that identity from the callable class, target implementation class, or Java method name.

#### Scenario: Region executes within a declared action
- **WHEN** application code starts a region while a declared action execution is current
- **THEN** the region's contextual name incorporates that action's logical member identifier
- **AND** the full logical member identifier is retained as canonical span metadata

#### Scenario: Region executes within a mixin action
- **WHEN** application code starts a region from a mixin action
- **THEN** the current `ActionInvocation` and the region use the domain-facing mixed-in action identity
- **AND** neither uses the mixin implementation identity ending in `#act`

#### Scenario: Region executes within a nested wrapper invocation
- **WHEN** a wrapper-invoked action starts an application-defined region while its execution is current
- **THEN** the region uses the nested current execution's logical member identifier rather than the outer command's identifier

### Requirement: Stable and contextual naming
Causeway SHALL use `causeway.application.span` as the stable observation name for every application-defined span.
When a logical member identifier is available, Causeway SHALL form the contextual display name from that identifier followed by one plain-space separator and the application-provided suffix.
Causeway SHALL attach the full canonical logical member identifier as `causeway.member.id` and the suffix as `causeway.application.span.suffix`.

#### Scenario: Two regions divide one action
- **WHEN** one action invokes application-defined regions with suffixes `load` and `process`
- **THEN** both observations have stable name `causeway.application.span`
- **AND** their contextual names end with `load` and `process` respectively
- **AND** their canonical member attributes identify the same enclosing action

#### Scenario: No current execution identity is available
- **WHEN** application code starts a region without a current execution logical member identifier
- **THEN** Causeway uses contextual name `app <suffix>`
- **AND** does not attach a `causeway.member.id` attribute
- **AND** still executes the supplied work normally

### Requirement: Suffix-preserving bounded names
Causeway SHALL limit application-defined contextual names to 50 characters and SHALL reject suffixes longer than 46 characters before invoking application work.
Causeway SHALL preserve every accepted application-provided suffix completely and SHALL compact or truncate only the logical-member portion when the combined name exceeds the limit.
Causeway SHALL preserve the full logical member identifier in canonical metadata when its display form is compacted or truncated.

#### Scenario: Full combined name fits
- **WHEN** the full logical member identifier, separator, and suffix total no more than 50 characters
- **THEN** the contextual name contains the full logical member identifier and complete suffix without truncation

#### Scenario: Removing the logical type namespace is sufficient
- **WHEN** the full combined name exceeds 50 characters but the namespace-free logical member identifier and suffix fit
- **THEN** Causeway removes the logical type namespace from the contextual name
- **AND** preserves the complete suffix
- **AND** retains the full logical member identifier in `causeway.member.id`

#### Scenario: Compact logical identity remains too long
- **WHEN** the namespace-free logical member identifier, separator, and suffix still exceed 50 characters
- **THEN** Causeway truncates only the displayed logical-member portion
- **AND** the contextual name is exactly at most 50 characters
- **AND** the complete suffix remains at the end of the contextual name
- **AND** `causeway.member.id` retains the complete logical member identifier

#### Scenario: Suffix exceeds the context-independent limit
- **WHEN** a suffix contains more than 46 characters
- **THEN** the service rejects the suffix with `IllegalArgumentException`
- **AND** does not invoke the supplied callable or runnable

#### Scenario: Longest accepted suffix has no current execution
- **WHEN** a 46-character suffix is supplied without a current execution identity
- **THEN** contextual name `app <suffix>` is exactly 50 characters
- **AND** preserves the complete suffix

#### Scenario: Suffix is absent or blank
- **WHEN** application code supplies a null or blank suffix
- **THEN** the service rejects the suffix before invoking the supplied work

### Requirement: Span parentage and nesting
Causeway SHALL make an application-defined span a child of the observation that is current when the service operation begins.
A nested application-defined span SHALL therefore be a child of its enclosing application-defined span.

#### Scenario: Region executes within an observed action
- **WHEN** an action invocation span is current and application code starts a region
- **THEN** the application-defined span is exported in the same trace beneath the action span

#### Scenario: Application regions are nested
- **WHEN** application code starts one region while another application-defined region is current
- **THEN** the inner region is a child of the outer region
- **AND** the previous current observation is restored when the inner region closes

### Requirement: Failure and cleanup semantics
Causeway SHALL report a throwable from application work to the active observation, close its scope, stop its span, and preserve the original return or throw behavior.
Cleanup SHALL occur exactly once even when application work fails.

#### Scenario: Runtime failure escapes a region
- **WHEN** application work throws a runtime exception or error
- **THEN** the span records that throwable as an observation error
- **AND** Causeway closes and stops the span
- **AND** the original throwable escapes unchanged

#### Scenario: Callable throws a checked exception
- **WHEN** a callable throws a checked exception
- **THEN** Causeway records the exception and lets the original exception escape unchanged and unwrapped using the established `InteractionService` convention
- **AND** the `call` operation does not declare a checked exception or invoke the callable again

### Requirement: Safe inactive behavior
When Causeway observation is inactive, the application-defined span service SHALL execute supplied work without exporting an application-defined span and without requiring an application-owned telemetry registry, SDK, or exporter.
Activation or deactivation of observation SHALL NOT alter the work's result, exception behavior, or invocation count.

#### Scenario: Observation profile is inactive
- **WHEN** application code invokes the service while Causeway uses its no-op observation registry
- **THEN** the supplied work executes exactly once
- **AND** no application-defined span is exported
- **AND** the application does not need conditional tracing code

### Requirement: Bounded static application metadata
The application-defined span API SHALL document that suffixes identify static operations or phases and MUST NOT contain domain-object identifiers, titles, bookmarks, argument values, record values, user identities, tenancy identifiers, or other instance-specific data.

#### Scenario: Application chooses region suffixes
- **WHEN** an application defines suffixes for repeated executions of one action phase
- **THEN** it uses the same bounded suffix for each execution of that phase
- **AND** per-invocation values are absent from the contextual name and span attributes
