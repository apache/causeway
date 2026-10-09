## Why

Main can identify domain action and association work, but its Wicket traces still lack the page preparation and rendering regions that explain where that work occurs. Forward-port roadmap step 5 now that semantic member naming is available, so operators can inspect page and Ajax work in both supported tracing modes.

## What Changes

- Adapt CAUSEWAY-4059 region observations and the Wicket portions of CAUSEWAY-4062 to main's current components and models.
- Observe domain-object page preparation and rendering, fieldsets, regular object properties, parented collections, object action controls and action prompts.
- Use stable operation categories, bounded semantic display names and full static logical identifiers, reusing the core naming policy.
- Keep observation scopes request-local, safely close them on failure or skipped callbacks, and keep active telemetry out of serialized pages.
- Extend lifecycle and exported-trace tests and integrate examples into the M3 how-to and observability guide.

## Capabilities

### New Capabilities

- `wicket-region-observations`: Semantic Wicket page preparation/render regions and action prompts, with request ownership, privacy, lifecycle safety and operator guidance.

### Modified Capabilities

None. Existing member observations remain unchanged; this introduces enclosing Wicket boundaries.

## Impact

Main's Wicket UI/viewer modules, core contextual naming helper where needed, Wicket lifecycle tests and tracing compatibility fixtures. Primary provenance is CAUSEWAY-4059 (`a8264fd1f9b`), plus Wicket naming/prompt/preparation work from CAUSEWAY-4062 (`a2ae5877c99`, `45e01ff6f42`). No new applib API, configuration switch, SDK or exporter is required.

Table phases/rows/cells, detail levels and budgets remain roadmap step 6; semantic HTTP/entry naming remains step 7. Other viewers, audit/evaluation observations and priming hooks are outside this proposal.
