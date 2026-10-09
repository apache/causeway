## 1. Public invocation API

- [x] 1.1 Add InteractionProvider default typed accessor and identity/identifier/member-name predicates, including expected-status variants and documented null/overload semantics.
- [x] 1.2 Add RuleChecking, compatible constructors and separate domain-facing identifier to ActionInvocation; correct Execution receiver Javadoc and use main release @since annotations.
- [x] 1.3 Add applib tests for absent/non-action execution, identity without equals, full identifier versus name, null arguments, statuses, legacy defaults and regular/mixin identifier separation.

## 2. Main runtime integration

- [x] 2.1 Verify main USER/FRAMEWORK/PASS_THROUGH rule paths, record CHECKED/SKIPPED in MemberExecutorServiceDefault and derive domain-facing action identity without changing the invoked identifier.
- [x] 2.2 Extend wrapper integration regressions for normal/skip-rules actions and mixin physical receiver/contributed identifier behavior, preserving existing publishing/DTO results.
- [x] 2.3 Verify nested success/failure and property-edit restoration, cleanup after completion and thread/interaction isolation; make only reproduced lifecycle fixes if necessary.

## 3. Documentation and validation

- [x] 3.1 Update InteractionProvider/ActionInvocation public guidance with exact checked-action examples, UNKNOWN, overloads and direct Java-call limitations.
- [x] 3.2 Add a concise supporting-capability explanation to the M3 otel-howto, making clear that the context API adds no spans and supports the later application-span forward port.
- [x] 3.3 Run focused applib/runtime tests and applicable interact regression profile; render changed documentation and record commands, versions, results and remaining limitations in validation.md.
