# Second-Opinion Reconciliation Audit

An independent "second pair of eyes" review of whether the behavioural changes made on
`maintenance-branch` (Causeway v2, worktree `../ecp`) have been faithfully forward-ported
into `main` (Causeway v4).

This analysis was produced afresh. It does **not** reuse the classifications in
`../ledger.md` (the first analysis); instead it re-derives the universe of maintenance
changes from git history and verifies each behaviour against `main`'s **code**, using the
first analysis only as a cross-check. No production code was changed — these documents are
the only output.

## Question being answered

> With respect to changes made on `maintenance-branch`, are they all now faithfully
> forward-ported into `main`, or are there gaps or errors that need filling/fixing?

## Method

1. **Establish the universe.** Merge-base of `main` and `maintenance-branch` is
   `65d64cd85b7` (CAUSEWAY-3883, 2025-04-27). Since then there are **481 commits** on
   `maintenance-branch`, spanning **56 distinct CAUSEWAY issue groups** plus a handful of
   un-numbered commits.
2. **Triage each issue group** from its commit subjects and diffs into:
   - *backport* — a fix that originated on `main`/v4 and was backported *into* v2 (already
     in `main` by origin);
   - *v2-infra* — CI / dependency changes specific to the v2 build line (not applicable);
   - *genuine* — a behavioural change authored on `maintenance-branch` that must exist in
     `main`.
3. **Verify each genuine behaviour against `main`'s code**, comparing observable behaviour
   and public contracts. Stylistic / naming differences and architecture-driven
   adaptations (Jakarta namespaces, immutable record config, Spring Boot 4, deliberate
   removal of the JDO commandlog adapter) are explicitly **out of scope** and are not
   treated as gaps.
4. Deep comparison was fanned out across focused per-capability reviewers; the crux
   findings were then re-verified by hand against both worktrees.

## Headline result

The reconciliation is **largely faithful**. The overwhelming majority of maintenance
behaviours are present in `main`, and in several places `main` is a strict superset
(added null-safety, defensive re-validation, an extra advisor-policy spec). The pre-4010
issue groups not enumerated in the first ledger are all backports-from-main or v2-only
infra and correctly require no forward port.

However, this audit found a **cluster of genuine gaps** and two lesser divergences. They
are itemised in [`ledger.md`](ledger.md).

## Root cause of the gaps

The maintenance work was itself developed with OpenSpec. Its consolidated specs (and its
own `openspec/changes/archive/`) stop at **2026-06-30** (the last was CAUSEWAY-4039). The
first forward-port analysis used those consolidated maintenance specs as its authoritative
source.

**CAUSEWAY-4042** (PR #3678) was developed *after* the specs — 2026-06-30 → 2026-07-13 —
and was therefore **never captured in the consolidated maintenance specs**. Likewise the
final tail of **CAUSEWAY-4039** ("isolate mixin domain event facets", 2026-06-27) is a
general-metamodel fix outside the command-log subsystem the first analysis was scoped to.

Consequently the gaps found here are concentrated in exactly those two places: the latest
maintenance work (4042) and a non-command-log metamodel fix (4039 tail). Everything the
consolidated specs did capture was ported faithfully.
