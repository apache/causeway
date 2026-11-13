## 1. Replay YAML Decoding

- [x] 1.1 Add a dedicated `CommandDtoUtils.fromYamlForReplay` path that rejects list roots, prefers non-empty `CommandExportDto` multi-document streams, falls back to legacy `CommandDto` multi-document streams, and preserves parse failures.
- [x] 1.2 Convert decoded export envelopes into imported-command carriers with optional portable result bookmarks while ignoring absent commands and the legacy `returnedObject` field.
- [x] 1.3 Add focused API tests and fixtures for wrapped results, absent and unresolved results, legacy multi-document fallback, ordering, empty or malformed documents, list-root rejection, and unchanged plain YAML APIs.

## 2. Export Envelope Remapping

- [x] 2.1 Extend `ResultRemappingService` to produce independent `CommandExportDto` copies whose targets, reference parameters, and result bookmarks use existing ordered mapping lookup semantics.
- [x] 2.2 Test mapped and unmapped envelope identities, listener failure followed by a later mapping, null result handling, disabled remapping, and preservation of source DTOs and bookmarks.

## 3. Unified Manager Export

- [x] 3.1 Add and register the unified-manager `exportSequence` action, deriving the ordered implicit sequence from R2-known commands and disabling it when that sequence is empty.
- [x] 3.2 Generate result-bearing multi-document YAML with filename defaults, optional sanitized timestamp suffixing, and optional copied-envelope remapping without changing replay state or manager state.
- [x] 3.3 Add focused manager export tests for implicit inclusion and omission, ordering, result metadata, filename behavior, remapping, empty-sequence disablement, and observational state boundaries.

## 4. Unified Manager Import

- [x] 4.1 Add and register the unified-manager `importCommands` action using the strict replay decoder and repository `saveForReplay` semantics.
- [x] 4.2 Attach optional imported result bookmarks without object resolution and return a manager moved to the oldest usable imported timestamp only when requested, retaining the current limit.
- [x] 4.3 Add focused import tests for canonical and legacy streams, persisted results, unresolved bookmarks, repeated import, default and disabled baseline movement, null timestamps, empty input, and list-root rejection.

## 5. Presentation, Compatibility, and Verification

- [x] 5.1 Expose E1 export/import actions in unified-manager fallback layout and add presentation tests while keeping W1 workflow, replay-gate, and B1/B2 background-gate controls absent.
- [x] 5.2 Add compatibility guards for legacy manager logical types, bookmarks, direct construction, plain YAML APIs, existing replay states and mementos, unchanged persistence schemas, and the absent commandlog JDO adapter.
- [x] 5.3 Run focused API and commandlog applib tests, affected module Maven verification under JDK 21, strict OpenSpec validation, and repository whitespace checks.
