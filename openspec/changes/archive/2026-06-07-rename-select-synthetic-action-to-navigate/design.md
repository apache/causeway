## Context

Synthetic parented collection actions are generated in `ObjectSpecificationAbstract.ParentedCollectionSelectorActionUtil` when command-log recording support is enabled.
They currently use select-oriented terminology in their deterministic action id prefix, user-facing display name, facet class names, tests, and documentation.
The action behavior is navigation from a parent object to one child object, and command recording identifies the action by its action id in command DTO payloads.

## Goals / Non-Goals

**Goals:**

- Make the generated action contract use navigation terminology by exposing `Navigate To` as the display name.
- Make new command recordings use a deterministic `__causeway_navigate_to_<collectionId>` action id.
- Rename implementation symbols where practical so future command recording navigation enhancements do not inherit select-specific terminology.
- Preserve the existing metamodel behavior other than naming, including action safety, collection association, styling, parameter generation, validation, invocation, command publishing, and empty-collection disabling.

**Non-Goals:**

- Do not add compatibility aliases that register both the old and new synthetic action ids in the metamodel.
- Do not change command export, replay, or remapping formats beyond the action id emitted for newly recorded synthetic navigation commands.
- Do not change matching semantics for scalar or reference filter parameters.
- Do not add new configuration properties for naming or compatibility.

## Decisions

### Rename the canonical action id prefix

The synthetic action factory should replace `__causeway_select_from_` with `__causeway_navigate_to_` as the canonical reserved prefix.
This makes the command DTO action id describe the recorded navigation step rather than the current UI affordance.

Alternative considered: keep the existing action id and only rename the display name.
That would avoid a command DTO break, but it would leave command recordings and replay diagnostics tied to the old select terminology and would not establish a generic namespace for future navigation commands.

Alternative considered: generate both action ids as aliases.
That would soften migration, but it would duplicate metamodel actions or require a replay-only lookup special case and could make ordinary action enumeration ambiguous.

### Rename the user-facing action name to `Navigate To`

The member-name facet installed on the synthetic action should use `Navigate To` instead of `Select`.
This keeps the viewer label aligned with the action's purpose and with the new action id prefix.

Alternative considered: use `Navigate`.
`Navigate To` is slightly longer, but it reads naturally as an action associated with a collection and a target child row.

### Keep the capability name and external concept stable for this change

The OpenSpec capability remains `parented-collection-selector-actions` because the capability still covers the existing generated action feature and its matching/filtering semantics.
Implementation can rename symbols from selector to navigation where the symbol identifies the synthetic action, while utility code that specifically filters or selects a child row can retain selector-oriented helper names if a full rename would obscure behavior.

Alternative considered: introduce a new `parented-collection-navigation-actions` capability and remove the old one.
That would overstate the scope because this change is a rename of an existing requirement rather than a replacement of the feature.

## Risks / Trade-offs

- Existing recorded command DTOs reference the old action id prefix → Users must migrate recordings, re-record commands, or configure replay remapping from `__causeway_select_from_<collectionId>` to `__causeway_navigate_to_<collectionId>`.
- Partial renaming can leave mixed implementation terminology → Tests and review should cover public constants, class names, test names, and user-facing strings most likely to affect future maintainers.
- Downstream code may have assertions against `Select` or `__causeway_select_from_` → The break is intentional and should be highlighted in release notes or migration notes if this branch publishes them.
- Generated action ids remain collection-id-based → This preserves deterministic lookup but still depends on stable collection ids, matching the existing behavior.

## Migration Plan

- Update metamodel generation to emit only the new display name and action id prefix.
- Update tests to assert the new values and to remove old select-oriented expectations.
- For existing recordings, migrate serialized command DTO action ids or add replay remapping data outside this change when old recordings must continue to replay.
- Rollback is possible by restoring the old prefix and display name before release, but recordings created during the interim would then need the inverse migration.

## Open Questions

- Should archive/release notes explicitly include the command DTO action id migration recipe when this change is implemented?
