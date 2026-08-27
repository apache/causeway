## Context

`<causeway-property>` renders every interaction state through one status-label mapping and always inserts a live status element into its edit grid.
The ordinary `editing` state therefore creates a separate “Editing” row even though the editor and Save/Cancel controls already expose the mode.
Meaningful asynchronous and exceptional states still need their current visible and assistive status output.

## Goals / Non-Goals

**Goals:**

- Omit the redundant status row during stable ordinary editing.
- Preserve the internal `editing` interaction state and all semantic events.
- Preserve visible live statuses for preparing, validating, saving, correction required, and unsupported editing.

**Non-Goals:**

- Change focus restoration, validation, save, cancel, GraphQL, or editor selection behavior.
- Remove meaningful progress or failure announcements.
- Redesign the property editor layout.

## Decisions

The `editing` state maps to no presentation label while retaining its existing state value.
The renderer conditionally inserts the live status element only when the mapped label is non-empty.
This avoids an empty grid child and its associated gap rather than merely hiding the text with CSS.

An alternative was to hide the status row with CSS only in edit mode.
That was rejected because it would retain redundant markup and layout semantics while separating presentation policy from the state mapping.

## Risks / Trade-offs

- [Risk] Removing a live-region message could reduce explicit mode announcement for assistive technology. → The editor receives focus and retains its property label and native controls, while meaningful state transitions continue to use the live region.
- [Risk] Conditional rendering could accidentally remove all statuses. → A focused test asserts omission only for ordinary editing, and the existing interaction suite exercises validation and save transitions.

## Migration Plan

No application migration is required.
Rollback restores the `Editing` label mapping and unconditional status element.

## Open Questions

None.
