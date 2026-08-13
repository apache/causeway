# Planned changes (ready-to-promote drafts)

These are **fully-drafted child changes** for the final reconciliation, held here rather than in
`openspec/changes/` because the repository permits only **one active OpenSpec change at a time**
(see the programme [`README.md`](../README.md) and [`final-reconciliation-plan.md`](../final-reconciliation-plan.md)).

Each subdirectory is a complete change folder (`.openspec.yaml`, `proposal.md`, `design.md`, `tasks.md`,
`specs/…`). When the current active change is archived, promote the next one verbatim:

```
git mv openspec/reconciliation/maintenance-branch/planned-changes/<name> openspec/changes/<name>
openspec validate <name> --strict
```

The OpenSpec CLI only scans `openspec/changes/` and `openspec/specs/`, so nothing here is treated as an active
change or validated until it is promoted.

## Contents

| Order | Draft | Covers | Target capability |
|---|---|---|---|
| slice 2 | `reconcile-synthetic-navigation-replay` | MA-5 | MODIFIED `synthetic-command-navigation` |
| slice 3 | `reconcile-replay-failure-continuation` | MA-3, MA-13 | MODIFIED `command-replay-background-completion` + `replayable-command-projection` |
| slice 4 | `reconcile-replay-import-idempotency` | MA-2 | ADDED to `command-result-metadata` |
| slice 5 | `reconcile-appfeat-reference-data` | MA-4 | MODIFIED `command-export-refdata-marker` |
| slice 6 | `reconcile-replayable-command-presentation` | MA-6, MA-7, MA-8, MA-9 | MODIFIED `replayable-command-projection` |
| slice 7 | `reconcile-per-command-export` | MA-10 | ADDED to `replayable-command-projection` |
| slice 8 | `reconcile-command-yaml-strictness` | MA-11 (+ D-D importer) | ADDED to `command-result-metadata` |
| slice 9 | `reconcile-collection-summary-view-config` | MA-12 (D-B) | NEW `collection-summary-view-config` |
| slice 10 | `reconcile-command-manager-page-limit` | MA-16 (D-C) | MODIFIED `unified-command-manager` |

Slices 1 (`reconcile-mixin-domain-event-isolation`, MA-1) and 2 (`reconcile-synthetic-navigation-replay`, MA-5)
are **archived** (specs synced under `openspec/specs/`; changes under `openspec/changes/archive/`), as is slice 3
(`reconcile-replay-failure-continuation`, MA-3/MA-13), slice 4 (`reconcile-replay-import-idempotency`, MA-2), and
slice 5 (`reconcile-appfeat-reference-data`, MA-4) and slice 6 (`reconcile-replayable-command-presentation`, MA-6/7/8/9) and slice 7 (`reconcile-per-command-export`, MA-10). Slices 8–10 remain drafted here (all recommended at `medium` effort); there is currently **no active change**. The next is slice 8 (`reconcile-command-yaml-strictness`, MA-11).

> **Effort:** every proposal carries a recommended reasoning-effort note. Slices **2**
> (`reconcile-synthetic-navigation-replay`, MA-5) and **3** (`reconcile-replay-failure-continuation`, MA-3) touch
> subtle metamodel/replay semantics and are recommended at **`high`** effort; the remaining slices (4–10) are
> mechanical and well-scoped, and are fine at **`medium`**.

Note: slices 6 and 7 both target `replayable-command-projection`, and slices 4 and 8 both target
`command-result-metadata`. Because only one change is active at a time they are applied sequentially, and each
touches distinct requirements — but when promoting the second of each pair, rebase its delta onto the
already-synced spec.

> **Re-validate on promotion.** These drafts cite current-HEAD (`42ca10925fb`) file:line and were written before
> implementation. Anchors in `core/metamodel` may shift with further CAUSEWAY-4044 immutability work; each draft's
> tasks begin with a re-verification step. Line numbers in the spec deltas' prose are illustrative, not normative.
>
> **Authoring note (for slices 5–10).** `openspec validate --strict` checks for `SHALL`/`MUST` on the **first
> line** of each requirement statement. Keep `SHALL`/`MUST` on the opening line (don't let a leading parenthetical
> push it onto a wrapped second line). Every requirement needs at least one `#### Scenario:` block. All four drafts
> here (and the active slice 1) pass strict validation as written.
