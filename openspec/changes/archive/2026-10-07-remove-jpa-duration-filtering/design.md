## Context

The completed classification/correlation change is archived as `2026-10-07-classify-and-correlate-observations`. The JPA facet wraps every generated observation using a configurable duration threshold. At completion, the wrapper marks short successful spans for discard; independently exported descendants cannot be retracted. The agent does not consume the Spring predicate, resulting in different policies between trace owners.

## Goals / Non-Goals

**Goals:** remove duration-based suppression and its configuration/API, retain JPA ancestry and diagnostic spans, simplify documentation and launcher wiring, and record future exploration.

**Non-goals:** implement general filtering, alter sampling/export ownership, rename spans, change user metadata, or remove explicit transaction discard behavior.

## Decisions

- Delete the duration wrapper, threshold property and `CausewayObservationPolicy` record, which has no remaining fields. Reduce integration construction to the selected registry and remove configuration binding. JPA uses the regular observation provider.
- Keep `DiscardedSpanExportingPredicate` and explicit discard helpers because transaction instrumentation deliberately discards unused transaction observations. This independent behavior is outside the removed feature.
- Remove duration-wrapper unit tests. Replace JPA/export filtering assertions with retention, failure and ancestry assertions, including a legacy threshold property which no longer influences instrumentation. Keep real Boot/agent and JDBC export tests and metadata checks.
- Remove environment threshold overrides, filtering walkthroughs and agent restrictions from the launcher/guides. Document removal and retain a short explanation of normal sampling/export limits.
- Defer a general policy to the backlog. Duration is known at completion; parent/child consistency, failed descendants, buffering bounds, late spans and both trace owners must be addressed before shipping a replacement.

## Risks / Trade-offs

- More spans for users who enabled filtering → normal sampling/export options remain available; no performance/storage saving is promised by this change.
- Removal of public policy/wrapper API → compile affected modules and update all in-repository consumers; these milestone APIs/configuration are removed intentionally.
- Existing property files may retain obsolete settings → explain removal; Boot's normal handling of unbound properties applies and no new deprecated binding is introduced.

## Migration Plan

Remove the JPA threshold property/environment override and use standard observations in manually constructed integrations. No database or exporter migration. Reverse this code change to restore the previous narrow policy, if necessary; general filtering remains an exploration item.
