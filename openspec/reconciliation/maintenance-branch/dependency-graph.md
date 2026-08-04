# Command Recording and Replay Dependency Graph

## Scope

This graph decomposes the 23 consolidated maintenance specifications into independently implementable capability nodes.
The specifications cannot safely be used as implementation units because several contain both foundational requirements and downstream integration requirements.

## Existing main baseline

Causeway `main` already provides:

- command and interaction lifecycle abstractions;
- `CommandDto` schema support;
- `CommandDtoUtils` with legacy multi-document YAML support;
- `CommandExecutorService` and `CommandExecutorServiceDefault`;
- commandlog entries and repositories;
- `ReplayableCommand`;
- separate `CommandExportManager` and `CommandReplayManager` implementations.

Maintenance supersedes the two-manager model with a unified `CommandManager`.
The manager redesign is therefore a semantic replacement rather than a set of additive mixins.

## Capability nodes

| ID | Capability | Principal modules | Dependencies |
|---|---|---|---|
| C1 | Recording-support configuration, suppression marker, and pause/resume contracts | `api/applib`, `core/config`, `core/runtimeservices`, commandlog subscriber | Existing command publishing |
| C2 | Recording-aware command publishing for safe actions and property edits | `core/metamodel` | C1 |
| C3 | Synthetic parented-collection selector and scalar-reference navigation actions | `core/metamodel` | C1, C2 |
| C4a | Scalar, view-model, and singleton-list command-result capture | `core/runtimeservices` | Existing command lifecycle |
| C4b | Synthetic navigation argument reconstruction and execution | `core/runtimeservices` | C3 |
| D1 | Result-bearing command export DTOs, bookmark metadata, DTO copying, and YAML compatibility | `api/applib`, commandlog applib | C4a for complete behaviour |
| D2 | Interaction-advisor policy during command execution | `core/config`, `core/runtimeservices` | Existing command executor |
| M1 | Replay mapping SPI, replay-input remapping, and result notification | commandlog applib | D1, D2 |
| M2 | Built-in in-memory replay mapping listener | commandlog applib | M1 |
| M3 | Persistent replay result mappings and listener | commandlog applib, commandlog persistence JPA/JDO | M1 |
| P1 | Rich `ReplayableCommand` projection, result presence, participants, mappings, and navigation | commandlog applib | D1, with M1 for actual mappings |
| P2 | Unified baseline-bounded `CommandManager` and command collections | commandlog applib | P1 |
| R1 | Reference-data marker, application SPI, and default classifier | `api/applib`, commandlog applib | Metamodel availability |
| R2 | Known-participant and reachability validation | commandlog applib | P2, D1, R1 |
| E1 | Validated command YAML export and compatible replay import | `api/applib`, commandlog applib | D1, P2, R2 |
| W1 | Command exclusion, restoration, deletion, movement, and retimestamping | commandlog applib and repositories | C1, P2 |
| B1 | Recording background-completion gate | commandlog applib | C1 and background-command querying |
| B2 | Replay background-completion gate | commandlog applib | P2 and background-command querying |

## Graph

```text
Existing main command and commandlog baseline
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
 C1 Recording policy       C4a Result capture
        │                       │
        ▼                       ▼
 C2 Publishing policy      D1 Export/result DTO contract
        │                       │
        ▼                  ┌────┴─────────────┐
 C3 Synthetic actions      ▼                  ▼
        │             M1 Mapping SPI     P1 Replayable projection
        ▼                  │                  │
 C4b Runtime support   ┌────┴────┐             ▼
                      ▼         ▼         P2 Unified manager
                     M2        M3              │
                                               ├──────────┐
                                               ▼          ▼
                                      R2 Reachability    B2 Replay gate
                                               ▲
                                      ┌────────┴────────┐
                                      │                 │
                                 R1 Reference data     D1 Results
                                               │
                                               ▼
                                      E1 Export/import
                                               │
                                               ▼
                                      W1 Manager workflows

C1 Recording policy ───────────────────────────────► B1 Recording gate
D2 Interaction policy ─────────────────────────────► M1 Replay execution
```

## Specification mapping

### Core recording foundation

| Maintenance specification | Capability nodes |
|---|---|
| `command-recording-suppression` | C1 |
| `property-edit-recording` | C1, C2 |
| `safe-action-command-publishing` | C1, C2, C3, C4a, D1, R2 |
| `parented-collection-selector-actions` | C1, C2, C3, C4b |

### Result metadata and replay mapping

| Maintenance specification | Capability nodes |
|---|---|
| `command-export-yaml-result` | C4a, D1, P1, R2, E1 |
| `command-replay-result-mapping` | D1, M1, P1 |
| `default-command-replay-mapping-listener` | M1, M2 |
| `persistent-command-replay-mapping-listener` | M1, M3 |

### Replayable command read model

| Maintenance specification | Capability nodes |
|---|---|
| `replayable-command-eligibility` | P1, P2 |
| `replayable-command-result-presence` | P1 |
| `replayable-command-participant-availability` | P1 |
| `replayable-command-remappings` | M1, P1 |
| `replayable-command-navigation` | P1, P2 |
| `replayable-command-actions` | P1, P2, B2 |
| `replayable-command-exportability` | P1, P2, R2 |

### Export validation and management

| Maintenance specification | Capability nodes |
|---|---|
| `command-export-refdata-marker` | R1 |
| `command-export-reference-data-participants` | R1 |
| `command-export-known-targets` | D1, P2, R1, R2 |
| `command-export-manager-command-list` | P1, P2, R2, E1 |
| `command-export-command-exclusion` | C1, P2, W1 |
| `command-export-command-reordering` | P2, W1 |

### Background coordination

| Maintenance specification | Capability nodes |
|---|---|
| `command-recording-background-completion` | B1 |
| `command-replay-background-completion` | B2 |

## Apparent specification cycles

### Result YAML

`command-export-yaml-result` combines foundational DTOs, import compatibility, runtime result capture, safe-action export, singleton-list capture, replayable-command display, and reachability validation.
It must be implemented as separate D1, C4a, P1, R2, and E1 slices rather than as one change.

### Safe-action publishing

`safe-action-command-publishing` spans recording configuration, metamodel publishing policy, generated actions, runtime result capture, and known-target validation.
It must be implemented incrementally through C1, C2, C3, C4a, D1, and R2.

## Causeway 4 adaptations

Maintenance configuration uses mutable nested classes.
Causeway 4 configuration uses immutable record-based configuration and requires new fields, defaults, and documentation to follow that model.

Maintenance `CommandExecutorServiceDefault` is a conventional class.
The Causeway 4 implementation is a record and must receive behaviour without reverting its current construction model.

Maintenance extends `ObjectSpecificationAbstract`, which is not present in the same architecture on `main`.
Synthetic action installation must use the current Causeway 4 metamodel post-processing and specification implementation structure.

Causeway 4 uses Jakarta APIs and Spring Boot 4.
All persistence, validation, dependency injection, and lifecycle adaptations must use the current namespaces and extension wiring.

## Topological implementation order

1. C1: recording policy and suppression.
2. C4a and D1: result capture and portable result metadata.
3. C2: recording-aware safe-action and property-edit publishing.
4. C3 and C4b: synthetic actions and runtime execution support.
5. D2, M1, and M2: replay execution policy and in-memory mapping.
6. M3: persistent mapping adapters.
7. P1: replayable command projection.
8. P2: unified command manager.
9. R1 and R2: reference data and known-participant validation.
10. E1: validated export and compatible import.
11. W1: exclusion, restoration, deletion, and reordering workflows.
12. B1 and B2: recording and replay background-completion gates.

## Planned change boundaries

The first change is `reconcile-command-recording-core-policy` and covers C1 only.

Subsequent proposed changes should remain independently restartable and should normally follow these boundaries:

1. `reconcile-command-result-metadata` for C4a and D1.
2. `reconcile-recording-aware-publishing` for C2.
3. `reconcile-synthetic-command-navigation` for C3 and C4b.
4. `reconcile-command-replay-mapping` for D2, M1, and M2.
5. `reconcile-persistent-replay-mapping` for M3.
6. `reconcile-replayable-command-projection` for P1.
7. `reconcile-unified-command-manager` for P2.
8. `reconcile-command-reference-data` for R1.
9. `reconcile-command-export-reachability` for R2.
10. `reconcile-command-export-import` for E1.
11. `reconcile-command-manager-workflows` for W1.
12. `reconcile-command-background-gates` for B1 and B2.
