## Context

Command export validation models a replayable sequence as a path from an export root to later command targets and reference parameters.
Known objects are built from command results that precede the command being validated.
For the first command in the sequence, there are no prior results, so the only valid target is an export root domain service.

The current validator already treats domain services as export roots and ordinary domain objects as non-roots.
This change makes the first-command consequence explicit in validation and in the `ReplayableCommand` exportability indicator.

## Goals / Non-Goals

**Goals:**

- Reject export sequences whose first selected action command targets an ordinary domain object.
- Continue accepting first selected action commands whose target is a domain service export root.
- Ensure the row-level `exportable` indicator reports the same result for first commands.
- Preserve existing behavior for later commands whose targets are established by earlier results.

**Non-Goals:**

- Do not change how export roots are identified.
- Do not change YAML export generation.
- Do not change command recording or replay-state transitions.

## Decisions

1. Use the existing known-target validator semantics.

A first command has an empty known-participant set.
Therefore it is exportable only when each target and reference participant is an export root.
This avoids adding special-case export logic while making the expected first-command behavior explicit and tested.

Alternative considered: add a dedicated first-command branch.
That would be more direct, but it risks diverging from the existing known-target graph semantics.

2. Test both action validation and exportability.

The export action and the `ReplayableCommand` property are two user-facing paths into the same rule.
Tests should cover both paths so the export button and table indicator remain aligned.

Alternative considered: test only the lower-level validator.
That would miss integration mistakes in how export-manager context and export roots are supplied.

## Risks / Trade-offs

- Existing manually selected exports that start from a domain object will be rejected → This is the intended constraint because replay cannot reach that object without an earlier finder or navigation result.
- Domain-service detection depends on metamodel lookup → Existing export-root tests already cover this dependency, and this change will add first-command-specific coverage.
