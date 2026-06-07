## Context

ReplayableCommand is a view model over command-log entries used to inspect and replay imported commands.
It currently renders the recorded `CommandDto` as YAML through the `dto` property and exposes replay state, target, member, and timestamp details.
Command export and import already preserve returned object metadata as a `result` element containing bookmark `type` and `id` values.
That metadata is stored on the associated `CommandLogEntry`, but it is not visible directly on ReplayableCommand.

## Goals / Non-Goals

**Goals:**

- Expose recorded result bookmark metadata on ReplayableCommand using the same shape users see in exported YAML.
- Keep the new display read-only and inspection-oriented.
- Preserve the existing command DTO display and replay behavior.
- Cover result-present and result-absent cases with focused tests.

**Non-Goals:**

- Change the command export YAML format.
- Change command replay execution or replay mapping listener behavior.
- Resolve the recorded result bookmark to a live domain object as part of the display.
- Reintroduce or display the legacy `returnedObject` field name.

## Decisions

- Render the existing ReplayableCommand DTO property as a `CommandExportDto` YAML wrapper.
  This keeps command and result metadata together in the same shape used by exported command YAML.
  An alternative was to add a separate result property, but that would split the export wrapper across two UI properties and make inspection less faithful to the exported form.

- Populate the `CommandExportDto` result metadata from `CommandLogEntry#getResult()` using `type` and `id` fields.
  This matches the export wrapper contract while avoiding object resolution.
  An alternative was to display a `Bookmark` string, but that would not match the exported `result` element shape requested by users.

- Omit or render no value when a command has no recorded result bookmark.
  This mirrors exported YAML, where result metadata is absent for commands without a result.
  An alternative was to show an explicit empty YAML block, but that would make absent data look present.

## Risks / Trade-offs

- [Risk] The result is stored on `CommandLogEntry`, while ReplayableCommand caches only selected command data.
  → Mitigation: Read the result through the existing command-log-entry lookup path and invalidate behavior only if necessary.

- [Risk] A result bookmark might not resolve in the current environment after import.
  → Mitigation: Display bookmark metadata only and do not require object lookup.

- [Risk] Table views could become noisy if result metadata is shown in collection tables.
  → Mitigation: Use layout settings consistent with the existing DTO property and hide the detailed result from tables if the implementation chooses an AsciiDoc/YAML rendering.
