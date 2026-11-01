## Why

Main's member observations expose generic action names and mixin implementation identities. Operators should see the domain action, property or collection being accessed, with stable metric categories and complete identifiers for searching traces.

## What Changes

- Give action spans contextual names `act <logical-type>#<member>`, using contributed domain identity for mixins while preserving main's `causeway.member.action` operation name.
- Recognize mixed-in property/collection implementations and instrument them as `causeway.property.access` / `causeway.collection.access`, with `prop` / `coll` contextual names.
- Add full canonical domain identifiers as `causeway.action.id`, `causeway.property.id` or `causeway.collection.id`, retaining parameter signatures for actions.
- Port compact naming: retain full logical names when they fit, shorten namespaces when needed, then bound display names to 50 characters. Preserve declared casing and complete identity attributes.
- **BREAKING (telemetry only):** contextual member names change, and resolved mixed-in association accesses move from action meters to their own categories. Avoid duplicate legacy spans.
- Extend real Boot/agent trace fixtures and merge examples of the new semantic names and identifier searches into the existing M3 how-to content; no before/after comparison is needed.

## Capabilities

### New Capabilities

- `semantic-member-span-naming`: Domain-facing member display names, category-specific canonical attributes, mixed-in association recognition and safe display compaction.

### Modified Capabilities

None. The existing observation-policy contract remains intact: stable operation categories, static metadata and preserved invocation semantics.

## Impact

Core configuration naming helper; metamodel ActionExecutor association lookup; runtime MemberExecutorServiceDefault observation selection; core and exported-trace tests; M3 how-to and observability guide. No applib API additions, configuration flags, new SDK/exporter or dependency pins.

Sources: maintenance `a2ae5877c99`, `d5ecd49a1f3`, `45e01ff6f42`. Extract only core naming and tests; Wicket preparation/prompts/regions remain roadmap step 5, collection rendering detail step 6 and entry trace naming step 7. Planning baseline: local `c04587a7bcc` on CAUSEWAY-4058; refresh main and select a CAUSEWAY-4062 implementation branch before apply.
