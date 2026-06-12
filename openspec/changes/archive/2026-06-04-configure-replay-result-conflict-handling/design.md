## Context

The command log applib provides `CommandReplayMappingListenerDefault` as the conditional default implementation of `CommandReplayMappingListener`.
It records non-identity replay result mappings in memory and uses them to remap later replay inputs.
The current conflict behavior is strict: when a recorded result bookmark already has a remembered actual bookmark and replay reports a different actual bookmark, the default listener throws an exception.
Because result mapping notification runs in the same transaction as replay execution, that exception causes the replayed command to fail and roll back.

Some users want this fail-fast protection, while other replay workflows prefer to keep the first mapping, log the conflict, and continue processing.
The new option should therefore be a property-driven policy on the default listener rather than a change to the SPI contract.

## Goals / Non-Goals

**Goals:**

- Preserve strict conflict rejection as the default behavior.
- Allow applications to configure a lenient conflict policy that logs and continues.
- Keep the existing recorded-to-actual mapping unchanged when a conflict is ignored.
- Keep custom `CommandReplayMappingListener` implementations unaffected.
- Make the property available through the standard `CausewayConfiguration` binding model.

**Non-Goals:**

- Do not change the `CommandReplayMappingListener` SPI methods.
- Do not make the default listener persist mappings.
- Do not add per-command or runtime UI controls for conflict behavior.
- Do not change replay transaction boundaries.

## Decisions

- Add a nested configuration property for default replay mapping listener conflict handling under the command log extension configuration.
  A concrete shape such as `CausewayConfiguration.Extensions.CommandLog.ReplayResultMapping` with `onConflictPolicy` keeps the option near the extension that owns the default listener.
  The external property name should follow existing relaxed binding conventions, for example `causeway.extensions.command-log.replay-result-mapping.on-conflict-policy`.

- Represent behavior with an enum rather than a boolean.
  Suggested values are `THROW_EXCEPTION` and `LOG_AND_CONTINUE`.
  This mirrors existing policy-style configuration such as background command `OnFailurePolicy` and leaves room for additional behaviors without renaming the property.

- Inject `CausewayConfiguration` into `CommandReplayMappingListenerDefault` or its factory and pass the selected policy into the listener.
  This keeps the policy centralized and avoids introducing a separate application bean just to configure the default listener.

- In `THROW_EXCEPTION`, preserve the current behavior exactly.
  The listener throws and keeps the existing mapping unchanged.

- In `LOG_AND_CONTINUE`, log the recorded bookmark, existing actual bookmark, and conflicting actual bookmark at error level, keep the existing mapping unchanged, and return without throwing.
  This lets replay continue while still leaving an operational signal that the replay stream is inconsistent.

## Risks / Trade-offs

- Lenient mode can hide replay data inconsistency if logs are not monitored.
  Mitigation: keep strict mode as the default and log conflicts at error level in lenient mode.

- The property path might be confused with the separate command replay configuration namespace.
  Mitigation: document the property in tests and code comments around the default command log listener.

- Injecting configuration into the default listener factory changes construction in tests.
  Mitigation: update unit tests to cover explicit policy construction and Spring bean creation with default configuration.
