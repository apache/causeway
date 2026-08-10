## 1. Re-verify anchors on current HEAD

- [ ] 1.1 Confirm `ReplayableCommand` still returns the failed `Try` after recording the failure, and that
      `CommandLogEntry.saveAnalysis` does not populate the exception on the failure branch.
- [ ] 1.2 Confirm the bounded/next replay managers stop on `isFailure()`.

## 2. Handled-failure recording

- [ ] 2.1 In the replay failure branch, record the analysis in a `REQUIRES_NEW` transaction: set `FAILED` state,
      failure reason, and the exception (`setException`).
- [ ] 2.2 Classify the recorded reason with a typed prefix (`Hidden:` / `Disabled:` / `Invalid:`), mapping hidden
      and disabled advisor exceptions consistently with maintenance.

## 3. Map failure to success

- [ ] 3.1 After recording, map the handled failure to a successful replay outcome so the returned result is not a
      failure.
- [ ] 3.2 Ensure the failure record is committed independently of the (now successful) outer replay outcome.

## 4. Batch continuation

- [ ] 4.1 Ensure bounded, multiple, and next replay continue past a recorded failure to the next eligible
      command; retain the B2 stop-on-newly-pending-background behaviour and the requested-bound / collection
      exhaustion stop conditions.
- [ ] 4.2 Leave replay eligibility and the replay-state boundary unchanged.

## 5. Tests

- [ ] 5.1 Bounded-replay integration test: a failing command is recorded (state + reason + exception + typed
      prefix) and the batch continues to the next command; pre-failure states unchanged.
- [ ] 5.2 Unit test: single-command replay outcome after a handled failure is a success while the failure record
      is committed.
- [ ] 5.3 Typed-prefix test for hidden / disabled / invalid outcomes.
- [ ] 5.4 Confirm the B2 background-pending pause/continue scenarios still pass.

## 6. Verification

- [ ] 6.1 Run focused commandlog applib and JPA replay tests plus the affected reactor under JDK 21, and strict
      OpenSpec validation.
- [ ] 6.2 Confirm no schema, persistence, configuration, eligibility, or background-gate change.
