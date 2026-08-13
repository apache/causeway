## 1. Re-verify on current HEAD

- [ ] 1.1 Confirm `saveForReplay` still creates and persists unconditionally and that `findByInteractionId` is
      available on the repository.

## 2. Idempotent saveForReplay

- [ ] 2.1 Guard `saveForReplay(CommandDto)` on `findByInteractionId(commandToReplay.getInteractionId())`; return
      the existing entry unchanged when present.
- [ ] 2.2 Otherwise create, initialise (`ReplayState.PENDING`, null parent interaction id, foreground
      execute-in), and persist as today.

## 3. Tests

- [ ] 3.1 Repository/integration test: importing the same canonical result-bearing stream twice returns the
      existing entries, creates no duplicate rows, and does not fail persistence.
- [ ] 3.2 Same for a legacy multi-document stream via the legacy importer.
- [ ] 3.3 First-time import test confirming unchanged create/init behaviour.

## 4. Verification

- [ ] 4.1 Run focused commandlog applib and Causeway 4 JPA import tests plus the affected reactor under JDK 21,
      and strict OpenSpec validation.
- [ ] 4.2 Confirm no new query, schema, persistence-adapter, JDO, or import-caller change.
