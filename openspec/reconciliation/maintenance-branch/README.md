# Maintenance-Branch Reconciliation Programme

## Purpose

This directory is the durable planning home for reconciling Apache Causeway `main` with the capabilities developed on `maintenance-branch`.
It exists outside `openspec/changes/` because this repository permits only one active OpenSpec change at a time.
Small implementation changes can therefore be proposed, implemented, and archived independently while this programme-level analysis remains available.

## Branches and worktrees

- Causeway 4 main worktree: `/Users/danhaywood/repos/github/apache/causeway/main`
- Causeway 2 maintenance worktree: `/Users/danhaywood/repos/github/apache/causeway/ecp`
- Main branch: `main`
- Maintenance branch: `maintenance-branch`

## Reconciliation policy

Comparison is semantic and capability-oriented rather than a raw file merge.
Observable behaviour, public contracts, tests, archived OpenSpec changes, and current maintenance specifications are the primary evidence.

Each capability is classified as one of:

- `Equivalent`
- `Supersede`
- `Adapt`
- `Not applicable`
- `Deferred`
- `Unresolved`

For the commandlog reconciliation, maintenance behaviour is authoritative.
Maintenance behaviour may differ on `main` only where Causeway 4, Jakarta, Spring Boot 4, or the current framework architecture requires an explicit adaptation.

## Dependency order

Core reconciliation follows this subsystem order:

```text
commons
  → api/applib
    → core/config
      → core/metamodel
        → core/runtimeservices
          → core/mmtest and regression tests
```

Commandlog reconciliation then builds on those core contracts before changing persistence adapters or viewer-facing workflows.

## Current inventory result

The inspected core paths contain 34 Causeway issue groups and one unnumbered change group.
Most pre-4010 changes are Causeway 4 capabilities backported into Causeway 2 and should not be ported forward again.
The substantive gap begins with the maintenance evolution of command recording, export, replay, synthetic navigation, result mapping, and manager workflows.

The maintenance worktree contains 23 consolidated specifications under `openspec/specs/`.
Those current specifications are the normative maintenance source for commandlog behaviour.
Archived changes and commits remain supporting evidence for implementation intent and historical sequencing.

## Durable programme artifacts

- `dependency-graph.md` records capability nodes, dependency edges, specification mappings, architectural adaptations, and implementation order.
- `ledger.md` records the current capability classifications and the reconciliation slices completed in the first wave.
- `final-reconciliation-plan.md` is the master plan for **completing** the reconciliation. It freezes the canonical second-wave discrepancy set (MA-1 … MA-16) found by the second/third/fourth opinions and the two meta-analyses, re-anchors every finding to current HEAD (accounting for the PR #3697 / CAUSEWAY-4044 merge that post-dated the audits), records the product decisions taken, and defines the sequence of child changes that finish the programme.
- `second-opinion/`, `third-opinion/`, `fourth-opinion/`, `meta-analysis-1/`, and `meta-analysis-2/` hold the independent audits and their reconciliation.

## Workflow

Only one implementation-oriented OpenSpec change is active at a time.
Each child change must reference this directory for programme context and must state which dependency-graph nodes it covers.
Completed child changes are archived normally.
This programme directory remains in place until the complete maintenance reconciliation has been verified.
