## Context

CAUSEWAY-4096 follows foundation PR #3814 (`74d44c060ce`). The foundation fixed lifecycle and registry ownership without changing metadata policy. `JpaEntityFacet` still embeds bookmarks/query descriptions in names and wraps every operation with a 2 ms threshold. Runtime `ia/_Observation` unconditionally emits username and multitenancy token as high-cardinality attributes. High-cardinality classification does not prevent export. Member execution embeds feature identifiers in observation names; execution publishing embeds subscriber counts.

The foundation fixture demonstrates that Boot can apply the discard filter, whereas the documented agent configuration retains these spans and requires filtering disabled. Dropping a framework parent at stop cannot retract independently exported JDBC children. Main's tracing handler already preserves contextual naming without maintenance's custom handler.

## Goals / Non-Goals

**Goals:** establish a reusable naming and metadata contract; apply it to current dynamic names; expose coherent configuration; make default traces retain short framework parents; document compatibility and validate both supported telemetry owners.

**Non-Goals:** new instrumentation boundaries, semantic mixin classification, trace-root renaming, correlation IDs, Wicket detail/budgets, custom SDK/exporter/sampler, collector policies, or sanitising observations emitted by application code, JDBC instrumentation or external agents.

## Decisions

### Stable operation names and bounded context

Separate Micrometer operation names (including meter identity) from contextual names used by tracing. Use the following mapping for existing dynamic sites. Leave existing fixed names unchanged in this ticket; broad cosmetic renaming would create extra migration cost.

| Current name pattern | Operation name | Context allowed in contextual name / attributes |
|---|---|---|
| Fetch by Bookmark (...) | `causeway.jpa.fetch-by-bookmark` | Entity type only |
| Fetch all Instances (query description) for all-instances query | `causeway.jpa.fetch-all` | Entity type only |
| Fetch all Instances (query description) for named query | `causeway.jpa.named-query` | Entity type and declared query name |
| Persist / Refresh / Remove / Detach (type=...) | `causeway.jpa.persist`, `.refresh`, `.remove`, `.detach` | Entity type only |
| Action Invocation (feature identifier) | `causeway.member.action` | Existing static member identity |
| Property Update (feature identifier) | `causeway.member.property-update` | Existing static member identity |
| Execution Publishing (subscribers=N) | `causeway.execution.publish` | Subscriber count as numeric data, not a name or low-cardinality dimension |

Use static contextual labels such as `Persist <type>` and `Action <member>`; do not include bookmarks, object titles, query descriptions, argument values or arbitrary object `toString()` output in names or new metadata. Use `causeway.entity.type`, `causeway.member.id` and `causeway.query.name` for bounded model metadata, and `causeway.execution.subscriber-count` as high-cardinality observation data. Model metadata must come from metamodel/type/declared query identifiers. Omit unavailable metadata rather than synthesising it from runtime values. Preserve existing fixed module/bean attributes and initiated-by semantics.

Do not rewrite an action's physical invocation identifier to change its display. Later CAUSEWAY-4062 work will distinguish mixed-in property/collection semantics. Avoid a custom tracing handler or exporter to enforce policy; construct observations correctly at their existing call sites. Merely moving an unsafe name into a contextual name or high-cardinality attribute would still export it and is rejected.

### Cohesive, validated configuration

Add a typed observation-policy configuration using the repository's configuration conventions and pass the resolved policy into integration/tagging consumers. Bind at startup, not repeatedly through Environment at each call. Keep registry selection and lifecycle ownership from the foundation. Preserve existing programmatic integration constructors where practical and make their default policy consistent with auto-configuration.

| Property | Proposed default | Meaning |
|---|---|---|
| `causeway.observation.include-user-name` | `false` | Emit existing `causeway.user.name` attribute |
| `causeway.observation.include-multi-tenancy-token` | `false` | Emit existing `causeway.user.multiTenancyToken` attribute |
| `causeway.observation.duration-filtering-enabled` | `false` | Existing switch, now explicit opt-in |
| `causeway.observation.jpa-duration-threshold` | `2ms` | Nonnegative JPA threshold when filtering is enabled |

Validate malformed/negative durations at configuration binding even when filtering is disabled. Zero retains all nonnegative-duration operations. Keep username and tenancy opt-ins independent; omit absent/empty values even when enabled. Retain impersonation boolean, locale and existing non-identity metadata. This replaces a loosely explained boolean with named policy and documentation without redesigning registry ownership.

### Retention by default; filtering remains lossy opt-in

Disable duration filtering by default in both modes. Keep the wrapper's fluent/stop/error guarantees when enabled. A successful duration strictly below the configured threshold is discardable in the supported Boot setup; threshold equality and failures are retained. Apply this threshold to the existing JPA boundaries only.

Document that explicit filtering can yield missing exported parents. Do not claim ancestor-aware suppression or buffer entire traces: that would require a separate exporter/sampling design. The agent configuration continues to require filtering disabled because Spring's discard filter does not govern the agent exporter. General sampler/export failures remain outside any completeness guarantee. Validate ancestry with deterministic sampling and export in the existing fixture.

Retaining the default 2 ms filter was considered but preserves unexplained holes in otherwise successful traces. Removing filtering entirely would discard an existing volume-control option. The proposal instead makes that trade-off explicit.

## Risks / Trade-offs

- Changed operation/contextual names and missing identity defaults → publish a migration table and test both Micrometer context names and actual exported names/tags.
- More short JPA spans by default → document volume impact and the opt-in lossy filter; later collection budgets remain separate.
- Accidental disclosure through fallback descriptions → sentinel-value tests across names and attributes, including named-query arguments and bookmarks.
- Static identifiers can still be numerous → restrict dimensions to application-model identifiers and keep runtime values out of meter dimensions.
- Different exporter behavior → exercise Boot and agent fixture modes; do not assert that a Spring filter governs all exporters.

## Migration Plan

Update code, configuration metadata and observability guide together. Existing explicit `duration-filtering-enabled=false` remains valid. Deployments needing former filtering set it to `true` in the supported Boot configuration; identity emission requires each opt-in. Dashboards must migrate the affected names; no duplicate legacy observations will be emitted. Rollback requires reverting this implementation and restoring prior dashboard/configuration assumptions, without domain data migration.

## Open Questions

No blocking scope questions. Implementation must verify the exact metamodel accessor for declared named-query/type identity and the repository's typed configuration binding conventions before coding; unsafe fallback values are not permitted. Default changes and mapping above are proposed review decisions, not claims that implementation already exists.
