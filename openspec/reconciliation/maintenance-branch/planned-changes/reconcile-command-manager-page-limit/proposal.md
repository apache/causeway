## Why

Child change 10 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancy **MA-16**;
second-opinion D2, fourth-opinion noted — LOW). Implements product decision **D-C**: restore the 320 cap.

`main`'s unified `CommandManager` opens at `DEFAULT_LIMIT = 100` (`CommandManager.java:54`) and validates only
that a user-entered limit is positive — there is **no upper cap** (`HasLimit_changeLimit.validateNewLimit`:
`return newLimit > 0 ? null : "Limit must be positive";`). Maintenance caps a user-entered limit to `[1,320]`
(`MAX_LIMIT = 320`) and opens the standard manager at 320. The current `unified-command-manager` spec explicitly
specifies the un-capped, default-100 policy.

Per D-C, `main` restores the 320 cap. (`CommandManager` and `HasLimit_changeLimit` are byte-identical between the
audited head and current HEAD.)

## What Changes

- Add `MAX_LIMIT = 320` to `CommandManager`.
- Cap the effective page limit at 320 everywhere it is set: `HasLimit_changeLimit.validateNewLimit` accepts only
  `[1,320]`; a memento (or any constructed) limit greater than 320 is capped to 320; a non-positive limit keeps
  using `DEFAULT_LIMIT = 100`.
- Open the **standard** commandlog menu manager at `MAX_LIMIT = 320` (matching maintenance). Retain
  `DEFAULT_LIMIT = 100` as the framework-memento / non-positive fallback. (The legacy `openCommandManager` compat
  shims continue to open at 100.)
- No change to baseline handling, collections, mementos other than the limit bound, or persistence.

## Capabilities

### Modified Capabilities

- `unified-command-manager`: the page limit is bounded to a maximum of 320 (change-limit accepts `[1,320]`, an
  over-limit value is capped to 320, non-positive uses the default 100), and the standard menu launcher opens the
  manager at 320.

## Impact

- Affects commandlog applib `CommandManager` (`MAX_LIMIT`, cap in limit derivation), `HasLimit_changeLimit`
  (validation), and the standard `CommandLogMenu` launcher (opens at 320).
- Deliberate v4 policy alignment with maintenance; the memento fallback of 100 is retained so existing mementos
  and the "non-positive → default" rule are unchanged.
- Requires coverage: change-limit rejects a limit above 320 and accepts 320; a memento limit above 320 is capped
  to 320; a non-positive limit still yields 100; the standard menu opens at 320.

## Open knob

The opening default for the **standard** menu is the one adjustable choice here: this change opens it at 320 to
match maintenance. Keeping the opening default at 100 (only adding the cap) is an equally valid reading of D-C;
the design records 320 as the chosen value and how to switch it to 100 if product prefers.
