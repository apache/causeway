> **Execution note:** one `implements` clause plus tests, reusing the existing default classifier; **`medium`
> reasoning effort** is sufficient.

## Why

Child change 5 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancy **MA-4**;
second-opinion G4, third-opinion F8, fourth-opinion G5 — MED, unanimous).

The first reconciliation (R1, `reconcile-command-reference-data`) deliberately scoped the built-in SecMan
reference-data opt-ins to four entity abstractions — `ApplicationUser`, `ApplicationRole`, `ApplicationTenancy`,
`ApplicationPermission` — all of which implement `RefData` on `main` today. Maintenance later (CAUSEWAY-4042
commit `6075f978367`) also marked the permission-feature reference view-model
`ApplicationFeatureChoices.AppFeat` as `RefData`; that later opt-in was not carried over.

On `main`, `AppFeat` implements only `Comparable<AppFeat>, ViewModel`
(`extensions/security/secman/applib/.../feature/api/ApplicationFeatureChoices.java:121-124`). The default
`CommandReplayReferenceDataService` classifies a bookmark as reference data only when its type implements
`RefData`, so a command whose target or reference parameter is an `AppFeat` bookmark (e.g. an `addPermission`
replay) is treated as an **unknown export participant** — excluded from export / `knownParticipants=false` —
unless some other known-participant path happens to establish the same bookmark. (This file is byte-identical
between the audited head and current HEAD.)

## What Changes

- Make `ApplicationFeatureChoices.AppFeat` implement the `RefData` marker (`api/applib/.../domain/RefData.java`),
  so `AppFeat` bookmarks are classified as stable replay reference data by the existing default classifier — no
  new classifier is required.
- No persistence field, schema, or logical-type change (the marker requires no methods).

## Capabilities

### Modified Capabilities

- `command-export-refdata-marker`: the set of built-in SecMan reference-data identities is extended to include
  the permission-feature reference view-model `ApplicationFeatureChoices.AppFeat`, alongside the four existing
  entity abstractions.

## Impact

- Affects `extensions/security/secman/applib` `ApplicationFeatureChoices.AppFeat` only (one `implements` clause).
- Reuses the existing metamodel-only default classifier (no object is loaded to classify the bookmark).
- Requires coverage that an `AppFeat` bookmark is classified as reference data and that a permission-feature
  command (target or reference parameter is an `AppFeat`) is accepted as a known export participant with no prior
  finder.
