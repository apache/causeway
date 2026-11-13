## Why

Application actions currently have to inspect an untyped interaction execution and reconstruct receiver/identifier checks to know whether Causeway is executing that exact action and has checked its rules. Forward-port CAUSEWAY-4058 so defensive domain code can use a supported query API, and establish reliable invocation identity for later application-defined spans.

## What Changes

- Add `InteractionProvider.currentActionInvocation()` and receiver/identifier or member-name predicates, including rule-status variants.
- Add aggregate `ActionInvocation.RuleChecking` states CHECKED, SKIPPED and UNKNOWN, preserving the existing constructor with UNKNOWN as its default.
- Record rule status in main's action executor without changing validation or execution behavior.
- Preserve physical receiver and invoked identifier semantics for mixins, with a separate domain-facing identifier incorporating the correction from CAUSEWAY-4065.
- Cover nested success/failure restoration and document direct Java-call limitations.
- Update public API guidance and the M3 telemetry how-to with a short explanation of this supporting capability.

## Capabilities

### New Capabilities

- `current-action-invocation-context`: Read-only current-action queries, explicit aggregate rule status, nested execution behavior and physical/domain-facing mixin identity.

### Modified Capabilities

None. Existing observation boundaries and export behavior remain unchanged.

## Impact

`api/applib` InteractionProvider, ActionInvocation and Execution Javadoc; `core/runtimeservices` MemberExecutorServiceDefault; focused applib tests and `regressiontests/interact` wrapper tests; public documentation and M3 how-to. No new dependencies, configuration, persisted fields or tracing spans.

Provenance: maintenance `6d037db6609`, corrected identity behavior in `f66b8f17a89`. Adapt to main's interaction carrier instead of copying maintenance's interaction implementation. Planning baseline is `41d8db75a06` on CAUSEWAY-4068-v4; refresh main and select a CAUSEWAY-4058 implementation branch before apply.
