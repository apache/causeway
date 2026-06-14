## Context

Command-log recording support already uses the normal command publishing facet model to make extra interactions available to the command-log subscriber.
Safe action and synthetic navigation recording have explicit recording-support behavior, but property edits still depend on the property command-publishing facet installed from `@Property(commandPublishing)` or from the global property command-publishing policy.
When recording support is enabled, that can leave an edited property without a persisted `CommandLogEntry`, which makes the recorded replay sequence incomplete.

The implementation should be a minimal, targeted extension of the existing metamodel/facet flow.
It should avoid introducing a second command logging path or a persistence-specific workaround.

## Goals / Non-Goals

**Goals:**
- Treat command-log recording support configured for the application metamodel as sufficient to enable command publishing for ordinary property edits.
- Continue to route property edit command logging through the existing command publishing and command-log subscriber pipeline.
- Keep explicit suppression via the command recording suppression marker authoritative.
- Preserve disabled-recording behavior outside recording-support application contexts.
- Override property-level `@Property(commandPublishing = DISABLED)` while recording support is enabled so replay recordings remain complete.
- Add focused metamodel coverage plus JDO/JPA command-log regression coverage for enabled recording, disabled recording, and explicit publishing.

**Non-Goals:**
- Do not change command DTO structure, replay DTO semantics, or command-log persistence schemas.
- Do not create duplicate command log entries for property edits that are already command-published.
- Do not broaden recording support to helper objects or targets that implement the suppression marker.
- Do not change action command-publishing policies except where shared tests or helper naming need adjustment.

## Decisions

1. Install recording-support property publishing through the property command publishing facet model.
When `causeway.extensions.command-log.recording-support` is `ENABLED` while the metamodel is being built, property command publishing facet creation returns a property-specific `EnabledByCommandLogProperty` facet.
That facet reports command publishing enabled and bypasses the normal property annotation and property command-publishing policy branches for that application context.
This keeps `CommandPublishingFacet.isPublishingEnabled(...)` authoritative for the normal command dispatch path.
Alternative considered: bypass `CommandPublishingFacet#isPublishingEnabled` in the property edit execution path.
That would be more invasive and risks diverging command DTO creation from normal command publishing.

2. Treat recording-support enablement as an early property facet selection, not a duplicate publisher.
If recording support is enabled for the application context, property facet creation installs a single enabled recording-support facet, including for properties that are explicitly command-published or explicitly disabled.
The same interaction should still yield one command log entry because it continues through the existing command publishing and subscriber flow.
If recording support is disabled, property edits keep following annotation and global property policy behavior.
Alternative considered: set the global property command-publishing policy to `ALL` when recording support is enabled.
That would be simple but would blur configuration semantics and could affect introspection or unrelated policy consumers.

3. Preserve explicit recording suppression after facet enablement.
The suppression marker should continue to prevent command-log persistence for recording-support-only interactions on command export/replay helper objects and marked domain targets.
If an interaction is explicitly command-published for another reason, the existing command-log pause/suppression rules should determine persistence without adding special cases here.
Alternative considered: prevent the facet from being installed on marked targets.
That would require target-type knowledge during more metamodel construction paths and could miss contributed-property cases.

4. Cover the behavior at both metamodel and integration boundaries where practical.
A low-level metamodel test verifies that recording support enables property command publishing metadata, including for explicitly disabled properties, and that disabled recording preserves the default.
A base command-log integration test verifies that a property without command-publishing annotation is not logged when recording support is disabled.
Separate JDO and JPA recording-support integration tests start application contexts with `causeway.extensions.command-log.recording-support=ENABLED` and verify that property edits persist exactly one `CommandLogEntry`.
Alternative considered: only add integration coverage.
That would prove user behavior but make future facet regressions harder to diagnose.

## Risks / Trade-offs

- [Risk] Recording support overrides an explicit `@Property(commandPublishing = DISABLED)` in a surprising way.
  → Mitigation: make the spec explicit and test the chosen behavior so users know recording mode is intentionally stronger for replay completeness.
- [Risk] Adding a fallback facet could duplicate command logging for properties already configured for command publishing.
  → Mitigation: keep a single installed facet per property and add a regression assertion for one persisted entry.
- [Risk] Suppression could be applied too early or too late for contributed properties or mixins.
  → Mitigation: preserve the existing suppression mechanism and add at least one suppression regression around a property edit target.
- [Risk] Existing tests may assume recording support only affects safe actions.
  → Mitigation: update expectations only where the tested scenario enables recording support and edits properties.

## Migration Plan

No data migration is required.
Applications that enable command-log recording support before metamodel construction will start recording ordinary property edits as part of the command sequence.
Applications that require property edits on a type to be excluded from recording should use the existing command recording suppression marker.
Rollback is to disable command-log recording support for the application context or revert the facet behavior change.

## Open Questions

None.
The implementation treats recording support as authoritative for property edit replay completeness, including when a property explicitly disables command publishing, unless the target opts out with the command recording suppression marker.
