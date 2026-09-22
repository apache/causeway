## Context

Causeway represents each metamodel-mediated action execution as an `ActionInvocation` in the current `Interaction` execution graph.
`MemberExecutorServiceDefault` creates and pushes that invocation immediately around execution of the action body, and nested `WrapperFactory` calls temporarily make their child invocation current.
Application code can currently reach this information only by obtaining the current interaction, reading its untyped current execution, testing its runtime type, and comparing the target and identifier itself.

The motivating use case is a domain action that keeps defensive checks for direct Java callers but wants to avoid repeating expensive `disable`, `validate`, and parameter-validation methods after the framework has already mediated the call.
The API must distinguish the exact current action from an unrelated outer action and must not describe invocation as rule validation, because `WrapperFactory` can intentionally skip rules.

## Goals / Non-Goals

**Goals:**

- Expose the current action invocation directly through the application-facing interaction provider API.
- Provide a convenient exact-current-action predicate based on target identity and logical action identity.
- Preserve correct behavior for nested wrapper invocations and restoration of their parent execution.
- Keep the change source-compatible, binary-compatible, and independent of viewers and persistence adapters.

**Non-Goals:**

- Report which visibility, usability, or validity rules were evaluated.
- Distinguish whether an invocation originated from a viewer, REST endpoint, wrapper, fixture, or another framework entry point.
- Resolve a mixin Java class to its contributed action identifier.
- Make direct Java method calls pass through the Causeway metamodel.

## Decisions

### Add default methods to `InteractionProvider`

Add a default accessor returning `Optional<ActionInvocation>` for the current execution when it is an action invocation.
Add default predicates that compare a supplied target and action identity with that current invocation.
Placing the methods on `InteractionProvider` makes them available consistently from `InteractionService` and other existing interaction-provider surfaces without requiring implementation changes.
Default methods preserve compatibility for third-party implementations.

An alternative was to add the methods only to `InteractionService`.
That would unnecessarily hide a read-only interaction query from other implementations of the existing provider abstraction.

### Expose both exact and ergonomic matching

The primary predicate will accept the target and full `Identifier`, providing unambiguous matching including parameter types.
A convenience overload will accept the target and logical member name for domain code that naturally knows its action name but does not already hold an `Identifier`.
The convenience overload will compare `Identifier.getMemberLogicalName()` after establishing that the current execution is an `ActionInvocation` for the supplied target.

An alternative was an API accepting a mixin class.
That would couple the low-level interaction API to metamodel lookup and mixin naming conventions, while the execution already exposes the canonical identifier needed by callers requiring exact matching.

### Compare targets by identity

The predicates will compare the current invocation target using Java identity rather than `equals`.
The framework records the actual target POJO for the invocation, and identity avoids entity equality methods, persistence access, or accidental equality with another instance representing the same persistent row.

### Describe framework-managed execution, not successful rule validation

The API will report an action while its action body is current in the interaction execution graph.
It will therefore also report invocations made with `WrapperFactory` controls that skip rule validation.
This is intentional because those calls are still explicitly framework-managed, and claiming that rules were checked would be incorrect.

An alternative was to expose a `rulesWereChecked` flag.
The execution model does not currently retain the wrapper control that selected rule enforcement, and that additional state is unnecessary for identifying direct versus metamodel-mediated invocation.

## Risks / Trade-offs

- [Risk] The logical-member-name overload cannot distinguish hypothetical overloaded actions sharing the same logical name on the same target. → Document the limitation and provide the full-`Identifier` overload for exact matching.
- [Risk] Callers may interpret current action presence as proof that rules were enforced. → Name and document the API in terms of invocation only, and explicitly cover skip-rules behavior.
- [Risk] Equality-based tests could trigger domain behavior or persistence access. → Require and test target identity matching.
- [Risk] Nested calls could accidentally report the parent action. → Derive the result exclusively from `Interaction.getCurrentExecution()` and add nested invocation regression coverage.

## Migration Plan

No migration is required because the change adds default methods to an existing public interface.
Existing applications continue to compile and run unchanged.
Rollback consists of removing the new methods and their tests before release; no persisted data or configuration is affected.

## Open Questions

- Confirm the final names of the accessor and predicates against existing applib naming conventions during implementation.
- Confirm whether the logical-member-name convenience overload is desirable in the maintenance branch or whether the initial API should expose only the optional accessor and exact `Identifier` predicate.
