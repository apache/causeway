## Context

Main exposes `Interaction.getCurrentExecution()` through InteractionProvider but lacks typed current-action queries and aggregate rule status. Main creates ActionInvocation in MemberExecutorServiceDefault and executes it through InteractionCarrier; maintenance's runtime used a different interaction layout. InteractionHead already separates owner (domain object) from target (physical receiver).

The maintenance design is recoverable at `473d2c218c4^:openspec/changes/archive/2026-09-22-expose-current-action-invocation-context/`. Implementation `6d037db6609` supplies the API; `f66b8f17a89` corrects a later conflation of invoked and domain-facing identifiers. Include the corrected contract now without porting ApplicationSpanService.

## Goals / Non-Goals

**Goals:** additive public queries, exact identity checks, explicit checked/skipped/unknown status, physical mixin semantics and nested restoration on success/failure. Preserve invocation, publishing, DTO and observation behavior.

**Non-Goals:** rule evaluation changes; per-rule evaluation provenance; viewer-origin classification; intercepting direct Java calls; new telemetry spans; application span service; semantic naming/rendering forward ports.

## Decisions

### Derive read-only queries from the current execution

Add default methods on InteractionProvider so existing implementations remain compatible. `currentActionInvocation()` returns only the current execution when it is an ActionInvocation; do not search ancestors or fall back to prior executions. Implement exact predicates with target identity (`==`) and Identifier equality. Provide member-name convenience and expected RuleChecking overloads. Null query arguments are rejected consistently with maintenance's nonnull contract. A new thread-local action stack would duplicate the existing carrier and risk stale state; reuse main's lifecycle instead.

### Record aggregate status without changing rule processing

Add RuleChecking CHECKED, SKIPPED, UNKNOWN and `isChecked()`. Preserve the four-argument constructor, delegating to UNKNOWN; add explicit-status and separate-identity overloads. In the mediated executor use USER → CHECKED and FRAMEWORK → SKIPPED, consistent with normal wrapper and withSkipRules behavior. PASS_THROUGH returns before invocation creation and creates no action frame. This aggregate state describes the framework rule-checking mode, not an inventory or guarantee that every possible supporting method was evaluated. Verify the mapping against main's execution path before applying it.

### Preserve physical and domain-facing identity separately

Keep `getTarget()` as the actual method receiver and `getLogicalMemberIdentifier()` as the invoked method identifier. A mixin therefore matches `this` and its implementation method (typically act), not the mixed-in object or contributed action name. Add `getDomainFacingLogicalMemberIdentifier()` for the contributed action identity; regular actions and legacy constructors use the invoked identifier for both. Derive the contributed identifier from main's owning action/InteractionHead metadata, never rename the invocation to obtain display metadata. Correct Execution's existing target documentation. This carries the relevant f66b8f17a89 fix without introducing its later span service.

### Verify existing carrier lifecycle

Nested wrapper calls temporarily expose the child and restore the parent after success or failure. Tests must also show no current action after completion, no ancestor action exposed during a property edit, and no state leakage between interactions/threads. Fix carrier lifecycle only if a focused reproduction shows a main defect; do not replace the interaction carrier with maintenance code.

### Document as invocation context, not new instrumentation

Add public API examples showing a defensive check that is skipped only for the matching action with CHECKED status. Document same-name overload ambiguity, UNKNOWN, physical mixin receivers and direct Java-call limitations. Keep the M3 how-to addition brief: this API supports later application-defined spans and creates no telemetry itself; no artificial manual telemetry scenario is required.

## Risks / Trade-offs

- [Risk] Presence is mistaken for proof of validation → examples explicitly require CHECKED and explain aggregate semantics.
- [Risk] Same-name overloads collide → full Identifier overload is the exact form.
- [Risk] Equal domain entities match inadvertently → use identity and test equal-but-distinct receivers without invoking equals.
- [Risk] A direct recursive Java call can observe an outer framework invocation for the same receiver/method → explain that the API reports the current framework execution, not arbitrary Java stack frames.
- [Risk] Changing invoked identifiers breaks publishing or DTOs → retain them, expose domain identity separately and verify mixin regressions.

## Migration Plan

Additive interface default methods and constructor overloads require no application configuration or data migration. Use the main release's @since convention (4.x), not maintenance's 2.x annotations. No remote freshness checks have been made during planning; refresh target main and create/select the implementation branch before applying.

## Open Questions

None requiring user input. Confirm main's contributed-action metadata lookup during implementation and record it in validation evidence.
