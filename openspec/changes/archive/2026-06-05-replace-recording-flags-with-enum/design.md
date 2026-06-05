## Context

The command-log extension currently exposes `safe-action-command-publishing` to enable safe action command logging and `parented-collection-selector-actions-enabled` to create synthetic selector actions for parented collections.
Both properties support the same regression recording use case and are expected to be enabled together.
The project prefers enum-valued configuration over booleans where future modes may be needed.

## Goals / Non-Goals

**Goals:**

- Add `causeway.extensions.command-log.recording-support` as an enum property with values `ENABLED` and `DISABLED`.
- Default `recording-support` to `DISABLED`.
- Use `recording-support=ENABLED` to enable both safe action command publishing and synthetic parented collection selector action creation.
- Remove or stop using the two current boolean configuration properties.
- Update documentation and tests to describe the unified recording support switch.

**Non-Goals:**

- Do not add additional recording support modes in this change.
- Do not change command replay mapping configuration.
- Do not change selector action ids, parameters, matching, validation, invocation, or export behavior except for how the feature is enabled.
- Do not change command publishing behavior for idempotent or non-idempotent actions.

## Decisions

- Use a `RecordingSupport` enum nested under `CausewayConfiguration.Extensions.CommandLog`.
  This keeps the setting close to the command-log features it controls and makes the external property path `causeway.extensions.command-log.recording-support`.
  The alternative was a boolean replacement, but that would not support future recording modes as cleanly.

- Interpret only `RecordingSupport.ENABLED` as enabling recording support behavior.
  This makes the default `DISABLED` equivalent to both former booleans being false.
  The alternative was to preserve independent toggles for advanced users, but the user-facing intent is one recording mode switch.

- Replace call sites rather than layering compatibility helpers around the old boolean getters.
  This ensures new code and tests depend on the enum property and avoids keeping deprecated boolean semantics alive internally.
  The alternative was to retain the booleans as aliases, but that would make the configuration model less clear and require precedence rules.

## Risks / Trade-offs

- Existing applications using the two boolean properties must migrate to the new enum property.
  Mitigation: document the replacement clearly and keep the new default disabled to avoid accidental behavior changes.

- Some users may have enabled safe action command publishing without selector action creation.
  Mitigation: this change intentionally aligns both under recording support because the current work targets command recording and export.

- Tests and documentation may still refer to the old property names.
  Mitigation: search for both old external property names and old Java accessor names during implementation.
