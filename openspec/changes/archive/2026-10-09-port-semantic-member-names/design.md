## Context

Main already separates operation and contextual names under CAUSEWAY-4096. MemberExecutorServiceDefault currently emits `causeway.member.action`, `Action <invoked identifier>` and `causeway.member.id`. CAUSEWAY-4058 supplies separate invoked/domain-facing action identity; main's InteractionHead is a mixee/mixin record, unlike maintenance's managed head hierarchy.

Maintenance's three source commits also modify Wicket and install a custom tracing handler for its older Micrometer baseline. Neither should be copied wholesale. Main has both Boot-managed and framework-supplied agent bridge configurations and supports application-provided telemetry beans.

## Goals / Non-Goals

**Goals:** domain-facing action/property/collection names; fixed meter categories; full canonical search attributes; case-preserving bounded display names; unchanged execution and trace ownership; demonstrated behavior in both modes.

**Non-Goals:** Wicket rendering/preparation/prompts; table detail/budgets; HTTP/root semantic naming; changing JPA or property-update naming; application span service; new applib APIs; duration filtering; modifying automatic agent HTTP/JDBC names.

## Decisions

### Preserve main's operation identity

Keep `causeway.member.action` for ordinary actions instead of maintenance's `causeway.action.invocation`. Add `causeway.property.access` and `causeway.collection.access` only for safely resolved mixed-in associations. This avoids an unnecessary ordinary-action metric migration while introducing useful distinctions. Emit one observation around the existing execution path, never both old and new spans.

### Derive semantic metadata without changing invocation identity

Reuse the CAUSEWAY-4058 domain-facing action identifier helper. Keep physical receiver, invoked identifier, rule state, argument execution, command/DTO publishing and transaction behavior unchanged. For an ActionInvocationFacetForMixedInPropertyOrCollection, resolve its association from the owning specification using MixedInMember.hasMixinAction, then use the association's feature identifier and singular/plural kind. Do not infer kind from a method name such as prop or coll. Unresolved association lookup falls back to normal action instrumentation rather than dropping work or guessing an association.

The full canonical domain identifier uses Identifier.getLogicalIdentityString("#"), including declared action parameter signatures; do not copy maintenance's synthetic `()` suffix. Add exactly the applicable semantic key (`causeway.action.id`, `causeway.property.id` or `causeway.collection.id`). Preserve existing generic `causeway.member.id` metadata for compatibility, documenting that the new semantic keys are authoritative for domain-facing searches. Generic property-update metadata remains unchanged.

### Share core display compaction

Introduce a narrowly scoped core naming formatter for logical member display names. Try the full logical type/member string; above 50 characters retry with a namespace-free logical type, then truncate to 50 characters. Preserve static identifier casing. Display names may collide; full category-specific identity attributes distinguish them. The 50-character display budget ports maintenance's explicit contract and is not a metric-cardinality policy or a new configuration setting. Do not bring unused Wicket region helpers into this step.

### Respect main's handlers and registry ownership

Use standard contextual-name APIs with existing registries. Verify actual uppercase/casing and compaction in exported Boot and agent spans. Main's earlier evidence already showed preserved casing; do not add maintenance's custom tracing handler unless a focused reproduction proves it necessary. Any necessary correction must be limited to these Causeway observations and preserve application/custom registry ownership, scope, parentage, tags and errors; do not replace the shared handler pipeline.

### Keep scope and operator guidance explicit

Root Interaction and HTTP names remain unchanged. Existing inner execution/domain-event/JPA/transaction boundaries remain. Integrate action/mixin association examples and Jaeger searches on full semantic identifiers into the existing M3 how-to and userguide, explaining when only compact names are visible and when association fallback remains action-shaped. Present this as new M3 functionality, without a before/after comparison or migration table. Preserve the existing document flow. Use real existing Petclinic members where available, with fixture evidence for paths the sample does not expose.

## Risks / Trade-offs

- [Risk] Association lookup selects a different contribution → match the actual implementation action via metamodel linkage; cover multiple contributions and missing lookup.
- [Risk] Short display names collide or lose namespace/signature → retain full canonical semantic attributes and test collision/overload cases.
- [Risk] Different tracing integrations alter casing → use exported Boot/agent assertions before considering specialized handling.
- [Risk] Telemetry consumers depend on old names/categories → describe the resulting operation categories and canonical search attributes within the existing guidance, and avoid duplicate legacy spans.
- [Risk] Introducing new observations changes failure/transaction behavior → keep the current observe boundary and verify original exception, cleanup and parentage.

## Migration Plan

No Java application API or configuration migration is required. Update trace-name searches to act/prop/coll and use the new semantic identifier keys for exact searches; dashboards counting mixed-in accesses as actions must select the new property/collection categories. No rollback data migration is needed. Refresh target main and ensure CAUSEWAY-4058 is present before implementation; no remote verification or runtime validation was performed during proposal creation.

## Open Questions

None requiring user input. Actual sample examples and export-handler behavior are implementation verification tasks, not assumptions of maintenance compatibility.
