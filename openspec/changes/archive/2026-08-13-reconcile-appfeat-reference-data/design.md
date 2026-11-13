## Context

`RefData` is a dependency-neutral applib marker (`api/applib/.../domain/RefData.java`). The default
`CommandReplayReferenceDataService` resolves a bookmark through `SpecificationLoader`, inspects the corresponding
class, and returns `true` when `RefData` is assignable — without loading the object
(`command-export-refdata-marker` spec, "Default classifier recognizes marker implementations using the
metamodel"). The four SecMan entity abstractions already implement `RefData` on `main`
(`ApplicationRole`, `ApplicationTenancy`, `ApplicationUser`, `ApplicationPermission`).

`AppFeat` is the reference view-model used for permission-feature choices (it has a stable `LOGICAL_TYPE_NAME`).
Maintenance marks it `RefData` so that permission-feature commands remain exportable/replayable without a prior
finder. `main` omits the marker.

## Goals / Non-Goals

**Goals:**

- `AppFeat` bookmarks are classified as stable replay reference data by the existing default classifier.
- Permission-feature commands whose target/reference parameter is an `AppFeat` are accepted as known export
  participants.

**Non-Goals:**

- No new classifier, no SecMan-specific `CommandReplayReferenceDataService`.
- No persistence field, schema, or logical-type-name change.
- Not marking any other SecMan or applib type as `RefData` — only `AppFeat`.

## Decisions

### Mark `AppFeat` with the `RefData` marker rather than adding a classifier

Add `RefData` to `AppFeat`'s `implements` clause, mirroring the four existing SecMan opt-ins. This is the minimal,
maintenance-faithful change and needs no new SPI implementation because the default classifier already recognises
marker implementations by metamodel assignability.

Rejected — a SecMan-specific classifier for `AppFeat`: unnecessary given the marker + default classifier already
cover it, and heavier than maintenance's one-line opt-in.

## Acceptance evidence

- A classifier test: a bookmark whose logical type is `AppFeat` is classified as reference data by the default
  `CommandReplayReferenceDataService` without loading the object.
- A reachability test: a command whose target or reference parameter is an `AppFeat` bookmark is a known export
  participant (`knownParticipants=true`) with no prior result establishing that bookmark.
- Confirm the four existing SecMan opt-ins are unaffected.
