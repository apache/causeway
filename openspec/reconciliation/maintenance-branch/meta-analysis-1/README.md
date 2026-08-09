# Meta-analysis (pass 1) of the maintenance-branch → main reconciliation opinions

## Purpose

This is a **meta-analysis** — not a fourth clean-room pass. It reconciles the three
independent second/third/fourth-opinion audits of whether the genuine behavioural changes
made on `maintenance-branch` (Causeway v2, worktree `../ecp`) were faithfully forward-ported
into `main` (Causeway v4). Its goal is a single **authoritative** list of the remaining
discrepancies, with each item's status settled by cross-checking the three opinions against
each other and, where they disagree, against the actual source code.

## Inputs

| Opinion | Location | Method | Findings |
|---|---|---|---|
| Second | `../second-opinion/` | Re-derived the 481-commit universe from git; triaged issue groups; fanned out per-capability reviewers; verified against main **code**. | 4 gaps (G1–G4) + 2 divergences (D1–D2) |
| Third | `../third-opinion/` | Eight clean-room subagents over contiguous ~60-commit chronological slices covering all 481 commits; candidates re-challenged against final code, registrations, call sites, tests; one runtime JShell probe. | 11 findings (F1–F11) |
| Fourth | `../fourth-opinion/` | Git-derived universe; 5-agent triage + 6-agent adversarial deep-verify on the command-log subsystem; hand-verification of every non-PRESENT finding. | 12 gaps/divergences (G1–G12) |

All three independently agree on the shared structural facts: merge-base `65d64cd85b7`;
481 commits on `maintenance-branch`; the command-log/replay work was **re-implemented**
on main (largely under CAUSEWAY-3910) with heavy renaming rather than cherry-picked, so
verdicts must be made by symbol/behaviour, not ticket id; and Jakarta namespaces, record
config, JDO-adapter removal, and pure renames are out of scope.

## Method of this meta-analysis

1. Built a cross-reference mapping every finding in every opinion onto a single canonical
   set of discrepancies (`MA-1 … MA-16`), collapsing the different numbering schemes.
2. Classified each canonical item by **agreement level**: unanimous, majority, or
   single-source.
3. For every point where the opinions **contradicted each other** (three material
   disagreements), read the actual code on both worktrees to render a decisive verdict
   rather than averaging the opinions.
4. Also verified the single-source findings (present in only one opinion) so they are not
   accepted or dismissed on one voice alone.

Verification was read-only against both worktrees at the audited heads
(`main` @ `a150e41682d`, `ecp` @ `1683383878939`). No production code was changed.

## Headline result

The forward-port is **substantially faithful** — every capability the first reconciliation
ledger set out to port is present, frequently as a stronger superset, and every omitted
pre-4010 issue group is a backport-from-main or v2-only infra change requiring no action.

But the reconciliation is **not complete**. There are **11 real discrepancies** plus **3
policy/divergence items** and **2 confirmed non-issues**. They cluster in exactly the places
all three audits predicted: the latest maintenance work (**CAUSEWAY-4042**), the **4039**
metamodel tail, and the **4037/4038** replay strands — i.e. work that post-dated the
consolidated maintenance specs the *first* forward-port analysis relied on.

**The four items to treat as first-tier remediation** (all independently confirmed here
against code):

- **MA-1 — mixin domain-event facet isolation** (general metamodel correctness; the single
  highest-value miss; unanimous across all three opinions).
- **MA-2 — `saveForReplay` idempotency** (duplicate replay rows on re-import; unanimous).
- **MA-3 — replay-failure batch continuation** (a failed command halts the whole batch on
  main; unanimous *in substance*, though the fourth opinion wrongly believed it present —
  see below).
- **MA-5 — synthetic collection-navigation replay compatibility** (maintenance-recorded
  collection-nav commands fail to replay on main with "Unknown action"; the third opinion
  had this right, the fourth under-rated it, the second missed it).

The **authoritative consolidated discrepancy table is in [`ledger.md`](ledger.md)**.

## The three inter-opinion disagreements, resolved

The core value of this meta-analysis is settling where the opinions contradicted one
another. All three were resolved by reading the code (evidence in `ledger.md`):

1. **Replay-failure batch continuation (MA-3).** Second and third said main *halts* the
   batch on the first failed command (missing behaviour); fourth said the
   "return-success-in-a-`REQUIRES_NEW`-transaction" behaviour *is* present.
   **Resolution: second and third are correct; the fourth is wrong.** Both branches record
   the failure in a `REQUIRES_NEW` transaction, but only maintenance then calls
   `mapFailureToSuccess(...)` so the returned `Try` is a success and the loop continues.
   Main uses `tryResultBookmark.accept(...)`, which returns the *Failure* unchanged, so the
   identical batch loop stops. The fourth opinion saw the new-transaction error handling and
   stopped there, conflating it with the failure→success mapping that actually drives
   continuation.

2. **Synthetic collection-navigation replay (MA-5).** Third rated this HIGH/ERROR (action-id
   `one_of_` infix mismatch); fourth called the id concern "moot" and treated only the
   argument-binding as a minor gap; second said synthetic navigation was PRESENT.
   **Resolution: the third opinion is correct.** Maintenance uses distinct ids —
   `__causeway_navigate_to_one_of_<collectionId>` for collections vs
   `__causeway_navigate_to_<referenceId>` for scalar references — while main uses a single
   `__causeway_navigate_to_` prefix for both and does an exact lookup that throws "Unknown
   action". A maintenance-recorded collection-navigation command therefore cannot replay on
   main. The fourth opinion's "unique per type" observation answers a different question
   (intra-type collision) and does not neutralise the cross-version prefix change. The
   argument-reconstruction gap (positional on main vs bind-by-id-with-padding on
   maintenance) is also real.

3. **`queryResultsCache` clear in the replay transaction (MA-14).** Fourth flagged it as a
   low gap; third explicitly rejected it as equivalent.
   **Resolution: side with the third opinion — accept as a v4 adaptation, not a gap.** Main
   does not route replay lookups through `QueryResultsCache` (it queries
   `findByInteractionId` directly each time), so there is no stale cache to invalidate. The
   maintenance cache-clear has no functional analogue to port. Flagged as contested-but-low.

## Where each opinion was strongest / weakest

- **Third opinion is the most complete and reliable.** Its full-chronology clean-room method
  surfaced findings no one else did (MA-5 synthetic-nav, MA-11 malformed YAML with runtime
  proof, MA-10 per-command export, the MA-7/MA-8 presentation fidelity strands) and it got
  the two hard disagreements right.
- **Fourth opinion is strongest on granular 4038 replay strands** (MA-5 sub-parts) and did
  careful hand-verification of the consensus items, but made the one material error (MA-3)
  and under-rated MA-5.
- **Second opinion is a sound but narrower pass**: it nailed the two headline gaps and the
  root-cause narrative (4042 + the 4039 tail post-date the specs) but, being closer to the
  consolidated-spec delta, missed the chronologically-discovered items (MA-5, MA-10, MA-11,
  the MA-7 fidelity strands).

## Confidence

Every unanimous and every material-disagreement item was verified by reading both
worktrees' source at the cited file:line. The single-source findings (MA-7, MA-8, MA-10,
MA-11) were likewise independently code-verified here and are not accepted on one opinion
alone. Severity ratings for the narrow 4038 edge cases (MA-5 sub-parts G9/G11) remain
approximate.
