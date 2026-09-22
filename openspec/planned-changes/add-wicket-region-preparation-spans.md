# Add Wicket Region Preparation Spans

**Roadmap position:** Proposal 2, conditional on evidence from `add-wicket-region-render-spans`.

## Why

Wicket performs component initialization, configuration, visibility, usability, and authorization work before actual markup rendering.
If the first render spans leave substantial page time unattributed, operators need bounded observations that identify which logical region spends time in preparation.

## Proposed Changes

- Add preparation observations for fieldsets, regular entity properties, and entity collections.
- Add an action-evaluation observation around visibility and usability decisions for relevant entity-page actions.
- Keep preparation observations separate from render observations so every duration represents a contiguous operation.
- Reuse the safe static identifiers and exclusion rules established by the region-render capability.
- Preserve full-page and Ajax request trace context and failure cleanup.
- Document how preparation and render spans complement rather than overlap one another.

Candidate stable names are:

```text
causeway.wicket.fieldset.prepare
causeway.wicket.property.prepare
causeway.wicket.collection.prepare
causeway.wicket.action.evaluate
```

## Non-Goals

- Do not stretch one observation from construction through later rendering.
- Do not observe arbitrary Wicket lifecycle callbacks or generic layout components.
- Do not duplicate `causeway.action.invocation` or record action execution.
- Do not add property-value, object-instance, user, tenant, or generated component-path metadata.

## Prerequisites and Evidence Gate

The region-render proposal must be implemented and exercised against representative slow pages first.
Proceed only when traces show material time outside logical render spans and profiling or targeted diagnostics associate that gap with Wicket preparation or consent evaluation.

## Design Questions

- Which callbacks provide contiguous and exception-safe boundaries for each semantic component?
- Should property preparation cover only `configure()` work or also lazy GUI initialization?
- How can repeated Wicket visibility checks be aggregated without producing multiple tiny action spans?
- Which preparation callbacks run before the Causeway root interaction is current, if any?

## Expected Exit Gate

A representative trace distinguishes fieldset, property, collection, and action-evaluation preparation from actual rendering, retains correct parentage, and adds no per-cell or per-row span amplification.
