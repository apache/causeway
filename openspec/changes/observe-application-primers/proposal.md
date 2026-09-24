## Why

Action and view primers deliberately perform preloading work that can trigger significant JDBC activity, but that work currently appears directly beneath the enclosing action or request and cannot be distinguished from surrounding framework work.
A semantic observation around every invoked primer will expose its duration and automatic JDBC descendants while preserving the existing priming lifecycle and privacy boundaries.

## What Changes

- Create one Causeway observation for every matching `ActionPrimer` callback that is actually invoked.
- Create one Causeway observation for every matching `ViewPrimer` callback that is actually invoked.
- Use stable observation names, bounded domain-facing contextual names, and complete canonical action or object-type attributes.
- Preserve natural action, interaction, HTTP, and Wicket parentage without creating observations when no primer matches.
- Record callback failures and propagate them unchanged while preserving inactive-observation behavior.
- Keep primer targets, arguments, values, bookmarks, users, tenants, implementation class names, registration order, and other instance data out of observation names and attributes.
- Preserve Java-agent ownership of JDBC spans, propagation, sampling, and export.

## Capabilities

### New Capabilities

- `application-primer-observation`: Defines per-callback semantic observations for action and view primers, including naming, parentage, lifecycle, failure handling, and privacy.

### Modified Capabilities

None.

## Impact

- Priming dispatch in `core/metamodel`, principally `PrimingRegistryDefault`.
- Existing action and Wicket view priming tests, plus focused observation and Java-agent compatibility coverage.
- Tracing operations documentation and trace hierarchy guidance.
- No public priming API signature change, no new dependency, and no change to primer matching or execution order.
