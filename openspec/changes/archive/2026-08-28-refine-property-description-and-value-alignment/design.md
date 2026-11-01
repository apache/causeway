## Context

A disabled property currently renders a dedicated circular information indicator as a third grid item and attaches the disabled-reason tooltip to that indicator.
The property label already identifies the affected member and can own the same bounded tooltip without inserting another visual control between label and value.
The read-only output does not explicitly express string alignment, so surrounding application or browser styles can place textual values at the logical end while editors remain start aligned.

## Goals / Non-Goals

**Goals:**

- Remove the standalone disabled-reason information indicator.
- Attach the bounded disabled-reason tooltip to the property label and make that label keyboard focusable only when it owns the tooltip.
- Preserve accessible description references for both the label trigger and property output.
- Preserve an independent non-redundant property-description title and description content.
- Mark GraphQL `String` output explicitly and align it to the logical start in baseline and cohesive-theme styles.

**Non-Goals:**

- Change whether a property is disabled or reveal reasons that authorization omitted.
- Remove ordinary property descriptions or change multiline description placement.
- Impose a new alignment policy on numeric, temporal, boolean, enum, reference, or custom-rendered values.
- Change property editing, value formatting, or GraphQL behavior.

## Decisions

Derive one label class and attribute set from property presentation state.
When a bounded disabled reason exists, add a tooltip-owner class, `tabindex="0"`, `data-tooltip`, and `aria-describedby` to the existing label.
Retain the visually hidden disabled-reason span as the accessible description source, but remove the visible standalone information indicator.
Keep the ordinary property description in its existing markup and retain its `title` on the label so disabled reason and descriptive metadata remain distinct.

Reuse the current CSS-only tooltip behavior on the label-owned tooltip class.
The label becomes `position: relative` and reveals the tooltip on hover or `:focus-visible`, preserving pointer and keyboard access without a key modifier.

Use the introspected GraphQL named type to add `causeway-property-value-string` to the output only for `String` properties.
Apply `text-align: start` to that class in baseline and cohesive-theme styles.
A dedicated class avoids broadening the rule to numeric and other scalar presentations and gives application themes a stable semantic hook.

Keeping the circular indicator and merely moving it closer to the label was rejected because it would retain the redundant control and grid disruption.
Using only the native `title` attribute for disabled reasons was rejected because it is not reliably keyboard-discoverable and cannot retain the current bounded styled tooltip.
Applying start alignment to every property value was rejected because numeric and application-specific renderers may intentionally use another alignment.

## Risks / Trade-offs

- [Risk] Making a label focusable adds a tab stop. → Add it only when a disabled reason is available and the label owns useful tooltip content.
- [Risk] A label can have both a description title and disabled tooltip. → Keep the disabled reason in the styled `data-tooltip` and the ordinary description in its existing title and accessible description IDs.
- [Risk] GraphQL custom string-like scalars remain unaffected. → Scope the first change to the standard `String` type requested and leave custom scalar alignment application-controlled.
- [Risk] Removing the indicator could reduce visual discoverability. → Use a help cursor and retain hover and focus tooltip behavior on the semantically named label.

## Migration Plan

No application migration is required.
Rollback restores the dedicated indicator markup and removes the semantic string-value class and alignment rule.

## Open Questions

None.
