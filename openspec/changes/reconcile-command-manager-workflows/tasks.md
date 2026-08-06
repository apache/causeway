## 1. Workflow Membership and Mutation Support

- [x] 1.1 Add reusable unified-manager workflow support that snapshots current sequence or excluded rows, compares selections by interaction id, preserves manager order, and resolves backing entries only after whole-selection validation.
- [x] 1.2 Add focused tests for null, empty, stale, foreign, duplicate, and reconstructed view-model selections, proving invalid selections cause no partial mutation.

## 2. Exclusion and Restoration

- [x] 2.1 Add and register `excludeCommands` on `commandsInSequence`, including recording-support disablement/direct guards, current-sequence choices, and default selection of commands with unknown participants.
- [x] 2.2 Add and register `unexcludeCommands` on `excluded`, with current-excluded choices, all non-`EXCLUDED` replay-state choices, recording-support UI disablement, and destination/membership validation.
- [x] 2.3 Test exclusion and restoration state transitions, retained entries, refreshed manager collections, unknown-participant defaults, all-or-nothing validation, and unchanged baseline/limit/memento.

## 3. Excluded Command Deletion

- [x] 3.1 Add and register danger-styled `deleteCommands` on `excluded`, validating the entire current excluded selection before removing backing entries through the existing persistence service.
- [x] 3.2 Test deletion choices and disablement, successful multi-delete, rejection of empty, active, stale, pending, failed, recorded, and replayed selections, and preservation of independent replay-result mappings.

## 4. Deterministic Command Movement

- [x] 4.1 Add movement support that derives selected manager order, validates an unselected active target, and calculates target-plus-one-second timestamps with preserved qualifying gaps or the deterministic one-second minimum.
- [x] 4.2 Update each moved command-log timestamp and embedded command DTO timestamp together while leaving target and unselected entries, replay states, results, membership, and manager state unchanged.
- [x] 4.3 Add and register the single bidirectional `moveCommands` action on `commandsInSequence`, including recording-support guards, selected/target choices, direct validation, and a false-by-default `squashTimings` parameter.
- [x] 4.4 Test moves earlier and later, target-choice filtering, reordered input selection, equal/reversed/sub-second gaps, preserved positive gaps, squash timing, invalid targets, atomic rejection, and E1/R2 observation of the refreshed order.

## 5. JPA Persistence and Compatibility

- [x] 5.1 Add JPA integration coverage that commits and reloads exclusion/restoration, excluded-entry deletion, and matching moved entry/DTO timestamps through established queries.
- [x] 5.2 Confirm no ordering column, schema migration, named query, repository mutation API, JDO-specific source, or commandlog JDO adapter is introduced.
- [x] 5.3 Preserve legacy manager logical types, timestamp-only mementos, bookmarks, action registrations, replay states, E1 YAML behavior, and existing row-action compatibility with focused guards.

## 6. Presentation and Verification

- [x] 6.1 Expose exclusion and movement with `commandsInSequence` and restoration and deletion with `excluded` in the unified-manager fallback layout.
- [x] 6.2 Add presentation tests for W1 action association, styling, prototyping and publishing metadata, while keeping direction-specific movement, replay-multiple, and B1/B2 background-gate controls absent.
- [x] 6.3 Run focused commandlog applib and JPA tests, affected commandlog reactor Maven verification under JDK 21, strict OpenSpec validation, persistence-scope checks, and repository whitespace checks.
