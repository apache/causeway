## Context

Synthetic parented collection selector actions are safe metamodel actions generated at runtime when selector action creation is enabled.
Safe action command publishing relies on normal command publishing facet metadata to decide whether an invocation should create a command log entry.
If the synthetic action does not carry the expected command publishing facet, the action can exist and invoke successfully while still being invisible to the command log and therefore unavailable for export.

## Goals / Non-Goals

**Goals:**

- Ensure synthetic selector actions install command publishing metadata through the same facet model used by normal actions.
- Ensure selector action command logging remains controlled by the existing safe action command publishing configuration.
- Ensure a logged selector action entry can be exported by the existing command export pipeline.
- Add focused coverage that verifies the selector action has command publishing metadata and is exportable once logged.

**Non-Goals:**

- Do not introduce a selector-action-specific command logging path outside the normal action invocation flow.
- Do not make safe action command publishing enabled by default.
- Do not change selector action ids, parameters, validation, or invocation matching behavior.
- Do not change replay result mapping behavior.

## Decisions

- Install or repair the command publishing facet on synthetic selector actions during synthetic action creation.
  This keeps the metamodel contract aligned with normal framework actions and avoids special cases in command logging or export.
  The alternative was to add a command-log special case for selector actions, but that would bypass the facet model and increase the chance of duplicate or inconsistent logging.

- Reuse the existing safe action command publishing configuration to decide whether the facet publishes commands.
  This preserves disabled-by-default behavior and means selector actions follow the same policy as other safe actions.
  The alternative was to add a selector-specific publishing property, but selector action creation is already separately opt-in and the command publishing policy should remain centralized.

- Validate export through the presence of a persisted command log entry rather than a separate export hook.
  The export pipeline should not need to know whether a command came from a synthetic or developer-authored safe action.
  The alternative was to add selector-specific export handling, but that would duplicate the existing safe action export behavior.

## Risks / Trade-offs

- The synthetic action may already attempt to install a command publishing facet, so the defect could be caused by facet creation timing or holder metadata rather than a missing line of code.
  Mitigation: inspect the existing facet factory path and add a test that fails on the current observed absence from command logging or export.

- A metamodel-only test can prove the facet is present but not the full export behavior.
  Mitigation: add the lightest integration-style test available for command log creation or command export when safe action publishing is enabled.

- Command logging behavior depends on both selector action creation and safe action command publishing configuration.
  Mitigation: cover both enabled and disabled safe publishing paths so selector actions do not start logging when the safe publishing opt-in is off.
