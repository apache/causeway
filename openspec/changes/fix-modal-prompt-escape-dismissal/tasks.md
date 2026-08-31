## 1. Reference focus lifecycle

- [ ] 1.1 Preserve pending focus intent and options while a reference editor's toolkit control is loading.
- [ ] 1.2 Transfer pending focus to the current upgraded or fallback control without allowing stale generations to move focus.
- [ ] 1.3 Add foundation regression tests for deferred toolkit focus, fallback focus, and disconnected-generation safety.

## 2. Modal Escape regression

- [ ] 2.1 Extend Petclinic Playwright coverage to dismiss the reference-first `removePet` modal with Escape.
- [ ] 2.2 Verify cancellation performs no mutation, restores originating-action focus, and permits a clean reopen.

## 3. Validation

- [ ] 3.1 Run foundation Node and Maven tests, Petclinic Playwright tests, and strict OpenSpec validation; resolve regressions.
