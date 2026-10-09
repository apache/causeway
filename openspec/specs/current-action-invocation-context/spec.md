# current-action-invocation-context Specification

## Purpose
Defines read-only current-action queries, aggregate rule-checking status, physical and domain-facing invocation identity, and nested execution restoration.
## Requirements
### Requirement: Provider exposes only the current action execution

InteractionProvider SHALL provide a default `currentActionInvocation()` returning the current ActionInvocation when present. It SHALL return empty for no interaction, no current execution or a current non-action execution, without searching prior or ancestor executions. The query SHALL NOT create executions or require observations to be enabled.

#### Scenario: Current action
- **WHEN** the current execution is an action invocation
- **THEN** the provider returns that invocation

#### Scenario: No action or current property edit
- **WHEN** no action execution is current, including a property edit nested within an action
- **THEN** the accessor returns empty rather than the prior or parent action

### Requirement: Current-action matching uses receiver identity and invoked action identity

InteractionProvider SHALL offer predicates for a nonnull target with a full Identifier or logical member name, and corresponding overloads with expected RuleChecking. Predicates SHALL compare receiver identity without calling equals. Full Identifier matching SHALL include the complete identifier; the name-only form SHALL explicitly document that it does not distinguish same-name overloads. Null query arguments SHALL be rejected.

#### Scenario: Equal but distinct receiver
- **WHEN** another receiver is equal to the current receiver but is a different Java instance
- **THEN** current-action matching returns false without invoking receiver equality

#### Scenario: Exact identifier and convenience name
- **WHEN** the same receiver is tested with the current full identifier or its logical member name
- **THEN** the predicates match, while a different full identifier does not match even if its member name is the same

#### Scenario: Expected status and invalid query arguments
- **WHEN** the receiver/action match but the supplied rule status differs, or a required query argument is null
- **THEN** the status predicate returns false for the mismatch and rejects the null query argument

### Requirement: Action invocations expose aggregate rule-checking state compatibly

ActionInvocation SHALL expose RuleChecking CHECKED, SKIPPED and UNKNOWN with isChecked true only for CHECKED. The existing constructor SHALL remain available and default to UNKNOWN. Runtime USER invocations SHALL report CHECKED and FRAMEWORK invocations SHALL report SKIPPED, without changing rule evaluation. Explicit rule status SHALL be nonnull. PASS_THROUGH SHALL retain its existing behavior without creating a new action invocation.

#### Scenario: Checked and skip-rules wrapper calls
- **WHEN** a regular wrapper action and a withSkipRules action execute
- **THEN** their current invocations report CHECKED and SKIPPED respectively, and status-aware predicates distinguish them

#### Scenario: Legacy construction
- **WHEN** a third-party or reconstructed invocation uses the existing constructor
- **THEN** its status is UNKNOWN and isChecked is false

#### Scenario: Standalone unmediated call
- **WHEN** a direct Java or pass-through method call occurs with no existing action execution
- **THEN** it does not gain a current action invocation or a claim of checked rules

### Requirement: Mixin invoked identity remains separate from domain identity

ActionInvocation SHALL retain the physical method receiver and invoked logical identifier. It SHALL additionally expose a nonnull domain-facing logical identifier. For regular actions and existing constructors both identifiers SHALL coincide; runtime mixin invocations SHALL expose the contributed action identifier separately without changing invoked identity or existing publishing/DTO semantics.

#### Scenario: Mixin action queries itself
- **WHEN** a mixin action queries using its mixin instance and invoked implementation identifier or member name
- **THEN** it matches, while the mixed-in domain object does not match as the physical receiver

#### Scenario: Contributed identity
- **WHEN** a mixin contributes an action with a different domain-facing name
- **THEN** its invocation retains the mixin implementation identifier and separately exposes the contributed action identifier

### Requirement: Nested action context is restored without stale state

Queries SHALL reflect the existing interaction execution stack. Nested actions SHALL become current during their execution and restore the previous execution after success or failure. Completed invocations SHALL NOT remain current or leak into another interaction/thread. Direct Java calls SHALL NOT create their own frames; documentation SHALL explain that an existing outer framework invocation can remain visible.

#### Scenario: Nested action succeeds or throws
- **WHEN** an outer action invokes a nested wrapper action that completes or throws
- **THEN** the child is current only during its execution and the outer invocation is restored when control returns

#### Scenario: Interaction completes
- **WHEN** a mediated top-level action completes and a later interaction or another thread queries current action
- **THEN** the completed invocation is not exposed as current

