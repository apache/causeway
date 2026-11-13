## Context

Current HEAD (identical to the audited head):

```
// extensions/core/commandlog/applib/.../dom/replay/CommandManager.java
:54   public static final int DEFAULT_LIMIT = 100;            // no MAX_LIMIT
:73   @Inject CommandManager(String memento, ReplayContext) { this(State.parseMemento(memento, new State(now, DEFAULT_LIMIT)), …); }

// …/dom/replay/HasLimit_changeLimit.java
:50   validateNewLimit(int newLimit) { return newLimit > 0 ? null : "Limit must be positive"; }   // no upper cap

// …/app/CommandLogMenu.java:97-104
return new CommandManager(baseline, CommandManager.DEFAULT_LIMIT, replayContext);   // opens at 100
```

The `CommandManager_replayOrRetryMultiple.Limit` enum (5,10,20,40,80,160,320,ALL) is unrelated — it bounds the
*replay batch* size, not the manager page limit. Maintenance uses `MAX_LIMIT = 320` for the page limit, opens the
standard manager at 320, and caps `changeLimit` input at 320; its framework-memento fallback is 100 (meta-analysis
2 D2).

The `unified-command-manager` spec currently states "a positive page limit, with a default limit of 100",
"a non-positive limit SHALL use the default limit", and the standard menu "opens `CommandManager` with … page
limit 100".

## Goals / Non-Goals

**Goals:**

- The manager page limit never exceeds 320; change-limit accepts only `[1,320]`; an over-limit value is capped to
  320; a non-positive value uses the default 100.
- The standard menu opens the manager at 320.

**Non-Goals:**

- No change to the memento fallback (`DEFAULT_LIMIT = 100`) for missing/malformed/non-positive components.
- No change to baseline handling, collections, or persistence.
- No change to the `replayOrRetryMultiple` batch-size enum.

## Decisions

### Add `MAX_LIMIT = 320` and cap the effective limit

Introduce `MAX_LIMIT = 320` on `CommandManager`. Cap the limit wherever it is derived: memento/constructed limits
above 320 clamp to 320; non-positive stays at `DEFAULT_LIMIT = 100`. `HasLimit_changeLimit.validateNewLimit`
accepts only `1..320` (reject with a "Limit must be between 1 and 320" message), matching maintenance's
user-input cap.

### Open the standard menu at 320

Change `CommandLogMenu`'s unified-manager action to open at `MAX_LIMIT` (320), matching maintenance. Keep the
legacy `openCommandManager` compat shims at `DEFAULT_LIMIT` (100), consistent with their retained-compat role.

**Adjustable knob:** if product prefers to keep the opening default at 100 and only add the cap, change the
`CommandLogMenu` launcher back to `DEFAULT_LIMIT`; everything else (the 320 cap) is unchanged. This is the single
value in this slice that is a judgement call rather than a straight maintenance port.

## Acceptance evidence

- `validateNewLimit`: rejects 0 and 321, accepts 1 and 320.
- A memento encoding a limit of 500 reconstructs to limit 320; a memento with a non-positive limit reconstructs
  to 100; a valid limit ≤ 320 round-trips unchanged.
- The standard menu action opens the manager with page limit 320.
- Baseline, collections, and mementos (other than the limit bound) are unchanged.
