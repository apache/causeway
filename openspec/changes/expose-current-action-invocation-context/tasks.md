## 1. Public interaction API

- [x] 1.1 Add the optional current-action accessor and exact target-plus-`Identifier` predicate as default methods on `InteractionProvider`, with API documentation that distinguishes invocation from rule enforcement.
- [x] 1.2 Add the target-plus-logical-member-name convenience predicate, using target identity and the current action's logical member name.
- [x] 1.3 Add focused applib unit tests for absent interactions, non-action executions, matching actions, mismatched identifiers, and equal-but-non-identical targets.
- [x] 1.4 Add checked, skipped, and unknown rule-checking status to `ActionInvocation` while retaining its existing constructor.
- [x] 1.5 Add status-aware exact-current-action predicate overloads and focused unit coverage.

## 2. Runtime invocation coverage

- [x] 2.1 Add regression coverage proving that the API reports a metamodel-mediated top-level action only while its body is executing.
- [x] 2.2 Add wrapper regression coverage proving that a nested action becomes current and that its parent is restored after nested completion.
- [x] 2.3 Add wrapper regression coverage proving that both normal and skip-rule invocations are reported as framework-managed actions.
- [x] 2.4 Add regression coverage proving that a plain Java call made without a matching action execution is not reported as current.
- [x] 2.5 Add regression coverage proving that normal wrapper invocation reports checked rules and `withSkipRules()` reports skipped rules.

## 3. Verification

- [x] 3.1 Run the affected applib, interaction, runtime-services, and wrapper regression test suites.
- [x] 3.2 Review generated API documentation and compatibility checks to confirm that the additive default methods preserve maintenance-branch compatibility.
