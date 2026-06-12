## Context

Synthetic parented collection selector actions are framework-generated safe actions that support command recording for parent-to-child collection navigation.
The current implementation uses a single reserved action id prefix constant in `ObjectSpecificationAbstract` and appends the parented collection id to produce deterministic action ids.
The requested change is intentionally small, but it changes a framework-visible identifier that can appear in command export, replay, and tests.

## Goals / Non-Goals

**Goals:**

- Rename the reserved selector action id prefix to `__causeway_select_from_`.
- Preserve deterministic action ids by keeping the collection id suffix unchanged.
- Keep the user-facing action name `Select` unchanged.
- Keep selector action synthesis, parameter behavior, invocation behavior, and command publishing behavior unchanged.
- Update tests and OpenSpec requirements so the new reserved id is covered explicitly.

**Non-Goals:**

- Do not add compatibility aliases for the old `__causeway_select_` prefix.
- Do not change command DTO shape beyond the synthetic action id value.
- Do not rename the synthetic action display name.
- Do not revisit selector eligibility, parameter filtering, or ordering rules.

## Decisions

- Replace the prefix constant in the selector synthesis utility rather than deriving the new id at call sites.
  This keeps all generated selector action ids consistent and avoids duplicated string construction.
  The alternative was to post-process ids in export or replay, but that would leave the metamodel with the less readable id and create split behavior.
- Treat the prefix rename as a breaking identifier change.
  Existing stored command recordings that reference `__causeway_select_...` will not match the renamed synthetic action without data migration or regeneration.
  The alternative was to support both prefixes, but compatibility aliases would weaken the deterministic one-action-per-collection model and add unnecessary complexity for a readability improvement.
- Keep the suffix as the associated collection id.
  This preserves the existing deterministic mapping from a parented collection to its selector action.
  The alternative was to include parent type or other metadata, but that is outside the requested rename and would broaden the compatibility impact.

## Risks / Trade-offs

- [Risk] Existing command recordings or tests may reference the old id prefix.
  Mitigation: document the rename as breaking in the proposal and update repository tests that assert the generated id.
- [Risk] The new prefix may require updates in downstream replay fixtures or exported examples.
  Mitigation: search for the old prefix across source, tests, and OpenSpec artifacts during implementation.
- [Risk] A partial update could leave code and specs describing different prefixes.
  Mitigation: validate the OpenSpec change and run selector action tests after implementation.
