## MODIFIED Requirements

### Requirement: Fallback presentation exposes P2 review metadata only
The manager fallback layout SHALL expose baseline and limit with their state controls and SHALL present all four collections as sequence and replay review surfaces. Manager collection tables SHALL identify commands using interaction id, timestamp, member, replay state, result presence, and known-participants status, with known participants immediately after result presence. The layout SHALL expose E1 sequence-export and replay-import actions. It SHALL expose W1 exclusion and movement actions with `commandsInSequence` and W1 restoration and deletion actions with `excluded`. The layout MUST NOT expose replay-multiple controls, recording background-completion gates, or replay background-completion gates.

#### Scenario: Unified manager layout includes W1 workflows
- **WHEN** the manager is rendered from fallback layout metadata
- **THEN** baseline, limit, all four collections, P1 identification columns, R2 known-participants status, E1 export/import actions, and W1 exclusion, restoration, deletion, and movement actions are visible
- **AND** controls owned by B1/B2 are absent
