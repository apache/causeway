## 1. SPI Definition

- [ ] 1.1 Add a public command-log applib SPI for classifying replay reference-data bookmarks.
- [ ] 1.2 Document that implementations must only accept entities whose identities are stable and expected to exist in replay environments.

## 2. Validation Integration

- [ ] 2.1 Inject all registered replay reference-data SPI implementations into the command export validation path.
- [ ] 2.2 Compose implementations with OR semantics so any accepting implementation marks a bookmark as reference data.
- [ ] 2.3 Update known participant validation so targets are known when they are export roots, reference data, or earlier selected results.
- [ ] 2.4 Update reference-parameter validation to use the same reference-data predicate.
- [ ] 2.5 Update exportability calculation to use the same known participant rules as export validation.

## 3. Tests

- [ ] 3.1 Add tests showing a reference-data target is exportable without a prior result.
- [ ] 3.2 Add tests showing a reference-data reference parameter is exportable without a prior result.
- [ ] 3.3 Add tests showing multiple SPI implementations are consulted and any accepting implementation is sufficient.
- [ ] 3.4 Add tests showing ordinary non-reference-data bookmarks still fail when absent from the dotted path.
- [ ] 3.5 Add tests showing no registered SPI implementations preserves existing validation behavior.

## 4. Verification

- [ ] 4.1 Run the relevant command-log applib test suite.
- [ ] 4.2 Run `openspec status --change allow-refdata-replay-arguments` and confirm the change remains apply-ready.
