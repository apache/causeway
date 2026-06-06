## Context

Parented collection selector actions are synthesized from parented collection associations when command-log recording support is enabled.
The current implementation builds child filter parameters from every eligible scalar property on the collection element type, then applies generic exclusions for non-scalar, blob, clob, and technical metadata properties.
Collection rendering already has a column model that determines which element members appear as collection columns and in what order.
The synthetic selector action should mirror that column model so the prompt matches the row-selection surface it records.

## Goals / Non-Goals

**Goals:**

- Build synthetic selector child filter parameters from the associated collection's visible columns rather than from every eligible scalar property on the child type.
- Preserve collection column ordering in the synthetic action parameter order after the mandatory parent parameter.
- Keep the existing scalar, blob, clob, and technical metadata exclusions as a second filter over the collection columns.
- Use only static metamodel association metadata that is safe during startup introspection.
- Preserve startup-safe static layout order by using member `LayoutOrderFacet` sequences where available.

**Non-Goals:**

- Do not change the mandatory disabled parent parameter or its position as the first action parameter.
- Do not add new configuration properties.
- Do not change collection rendering, row selection, or command DTO shape beyond the set and order of child filter parameters.
- Do not make reference, collection, blob, clob, or technical metadata properties eligible merely because they appear as collection columns.

## Decisions

- Derive candidate child filter properties from child associations that are statically visible for `Where.PARENTED_TABLES`.
  This keeps selector prompts aligned with statically rendered collection columns while remaining safe during metamodel startup.
  The alternative was to call `streamAssociationsForColumnRendering`, but that invokes table-column SPIs that can depend on interaction-scoped services and are not safe during application startup.

- Order child filter properties by `LayoutOrderFacet` sequence when present, falling back to member id for deterministic ordering.
  This reuses layout sequence metadata already installed on associations from annotations or XML without normalizing grids during selector synthesis.
  The alternative was to call `GridFacet.getGrid`, but that can recursively trigger selector synthesis while grids are being normalized during startup.

- Apply the existing selector parameter eligibility predicate after resolving the statically column-visible candidates.
  This preserves current safety and size exclusions while allowing static parented-table visibility metadata to control whether scalar properties are candidates.
  The alternative was to trust every rendered column, but rendered columns can include members that should not become equality filter parameters.

- Keep the parent parameter outside the column-derived list.
  The parent parameter represents the selector action target context, not a child column, and existing command export expectations rely on it being present and disabled.
  The alternative was to remove the parent parameter because it is not a collection column, but that would change command DTO structure and parent scoping semantics.

- Add focused metamodel tests around inclusion, omission, and ordering.
  Existing tests already exercise selector parameter generation and are the right place to assert that non-column scalar properties are omitted and column order is preserved.
  The alternative was to rely on viewer tests, but this behavior is synthesized at metamodel level and should be validated without a viewer.

## Risks / Trade-offs

- Runtime column visibility and ordering services can depend on an active interaction, while selector actions are synthesized during metamodel construction.
  Mitigation: do not invoke `TableColumnVisibilityService`, `TableColumnOrderService`, or grid normalization from selector synthesis; use only startup-safe static association visibility and layout-order facets.

- A collection with too few displayed scalar columns may produce an under-specified selector action and increase ambiguous-match validation failures.
  Mitigation: this is consistent with the visible collection shape, and existing validation already rejects ambiguous matches with a clear reason.

- Existing tests or command fixtures may expect parameters for scalar properties that are no longer rendered as collection columns.
  Mitigation: update fixtures and tests to assert the new column-driven contract.
