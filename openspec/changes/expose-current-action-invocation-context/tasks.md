## 1. Public interaction API

- [ ] 1.1 Add the optional current-action accessor and exact target-plus-`Identifier` predicate as default methods on `InteractionProvider`, with API documentation that distinguishes invocation from rule enforcement.
- [ ] 1.2 Add the target-plus-logical-member-name convenience predicate, using target identity and the current action's logical member name.
- [ ] 1.3 Add focused applib unit tests for absent interactions, non-action executions, matching actions, mismatched identifiers, and equal-but-non-identical targets.

## 2. Runtime invocation coverage

- [ ] 2.1 Add regression coverage proving that the API reports a metamodel-mediated top-level action only while its body is executing.
- [ ] 2.2 Add wrapper regression coverage proving that a nested action becomes current and that its parent is restored after nested completion.
- [ ] 2.3 Add wrapper regression coverage proving that both normal and skip-rule invocations are reported as framework-managed actions.
- [ ] 2.4 Add regression coverage proving that a plain Java call made without a matching action execution is not reported as current.

## 3. Verification

- [ ] 3.1 Run the affected applib, interaction, runtime-services, and wrapper regression test suites.
- [ ] 3.2 Review generated API documentation and compatibility checks to confirm that the additive default methods preserve maintenance-branch compatibility.
