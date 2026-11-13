## Context

The maintenance reconciliation graph identifies C4a result capture and D1 portable result metadata as the next foundations after C1.
Causeway 4 currently records a command result only when the direct action result is persistable, and it ignores bookmarkable view models and single-element result containers.
`CommandDtoUtils` currently supports the Causeway 4 legacy `CommandDto` YAML APIs but has no result-bearing transfer envelope, bookmark DTO, imported-command carrier, or deep-copy operation.
The maintenance implementation is authoritative for observable semantics, while the implementation must retain Causeway 4's current executor construction, Jakarta APIs, `CommandDtoJacksonSupport`, and YAML utility conventions.

## Goals / Non-Goals

**Goals:**

- Capture one bookmarkable direct action result without restricting it to persistent entities.
- Capture the contained bookmarkable object from a framework-supported result container only when the container has exactly one non-empty element.
- Preserve an existing command result and leave ambiguous or non-bookmarkable results unset.
- Add public result-bearing transfer values compatible with maintenance's `command`, `result`, `type`, and `id` shape.
- Add independent deep-copy support for `CommandDto`.
- Serialize result-bearing command values as multi-document YAML without changing existing Causeway 4 `CommandDto` YAML methods.
- Establish contracts and tests that later replay, projection, reachability, and export/import changes can consume.

**Non-Goals:**

- Enabling command publishing for safe actions or property edits.
- Synthesizing or executing navigation actions.
- Importing YAML into commandlog entries or changing command manager workflows.
- Implementing replay input/result mapping.
- Enriching `ReplayableCommand` or introducing the unified `CommandManager`.
- Validating whether later action targets are reachable from earlier results.

## Decisions

### Capture every bookmarkable single result

Result capture will use the adapted result's bookmark as the capability boundary rather than requiring a persistable entity.
This includes bookmarkable view models because recording should preserve available identity metadata while later replayability and reachability policies decide whether that metadata is usable.
Persistable results that do not yet have an identifier will retain the existing transaction-flush behavior before bookmark lookup.

Restricting capture to entities was rejected because it loses maintenance-compatible view-model results and prevents later mapping from observing them.
Introducing a new replay-stability marker at this layer was rejected because no such maintenance contract exists and it would couple neutral recording to downstream replay policy.

### Normalize singleton containers during result capture

A direct result is a candidate as-is.
A framework-packed result container is a candidate only when it contains exactly one non-empty element.
Empty containers, multi-element containers, and singleton elements without bookmarks produce no command result.
The command stores only the contained bookmark and does not preserve the original container shape.

General collection serialization was rejected because the downstream command contract represents one result bookmark and treating one member of a multi-result collection as authoritative would be ambiguous.

### Preserve the first command result

Result capture will not overwrite a result already held by the command.
This preserves existing nested invocation and mixin behavior and matches the maintenance guard against trampling an earlier result.

### Keep transfer values with `CommandDtoUtils`

`CommandExportDto`, `ImportedCommandDto`, and `BookmarkDto` will be public static value types associated with `CommandDtoUtils`, matching the maintenance API location and avoiding a new package-level abstraction before downstream consumers exist.
The transfer types will remain mutable JavaBean-style values so Causeway 4's Jackson/JAXB integration can construct them without custom creators.
`CommandExportDto` will contain an embedded `CommandDto` and nullable `BookmarkDto result`.
`ImportedCommandDto` will contain an embedded `CommandDto` and nullable applib `Bookmark result` for later import consumers.
`BookmarkDto` will convert between bookmark identity and YAML fields `type` and `id` without resolving a domain object.
Unknown YAML properties will be ignored on the export envelope, but the legacy `returnedObject` name will not be introduced as an alias.

Creating a new schema version was rejected because the metadata is an export envelope around the existing command schema rather than a change to `CommandDto` itself.

### Add export YAML without replacing legacy YAML APIs

A dedicated result-bearing export method will emit one `CommandExportDto` per YAML document and omit null fields.
It will reuse Causeway 4's existing `CommandDtoJacksonSupport` and YAML utilities rather than porting maintenance's older Jackson customization internals.
Existing `toYaml`, `toMultiDocYaml`, and `fromYaml` semantics for plain `CommandDto` values will remain unchanged.
Actual replay-import fallback, list rejection, persistence, and manager integration remain E1 work.

### Deep-copy through the established command schema mapping

`CommandDtoUtils.copy(CommandDto)` will create a structurally independent command DTO through the established JAXB command schema mapping and return null for null input.
Manual field copying was rejected because it is brittle as the generated schema evolves.
JSON or YAML copying was rejected because XML/JAXB is already the authoritative full-fidelity mapping for `CommandDto`.

## Risks / Trade-offs

- [Capturing any bookmarkable view model may retain identities that later prove unsuitable for replay] → Keep capture neutral and require later P1/R2 policies to decide replayability and reachability.
- [Container handling depends on Causeway 4's packed managed-object representation] → Test empty, singleton, multiple, and non-bookmarkable cases at the runtime-service seam.
- [New public nested DTOs become compatibility commitments] → Match maintenance names and field shapes and document the additions.
- [Adding YAML support could accidentally alter legacy command YAML] → Add regression tests for the existing methods and introduce a separate export method.
- [JAXB deep copying has per-call allocation cost] → Use it only where callers explicitly request an independent replay/execution DTO, not on every command invocation.

## Migration Plan

The change is additive except for broader result capture on commands that are already being published.
Applications will begin retaining result bookmarks for bookmarkable view models and singleton result containers, while publishing eligibility remains unchanged.
Rollback consists of removing the additive transfer/copy APIs and restoring entity-only result capture; no persistent schema migration is required.

## Open Questions

None.
The programme question about view-model scope is resolved in favor of every bookmarkable single result, with replay stability deferred to downstream policy.
