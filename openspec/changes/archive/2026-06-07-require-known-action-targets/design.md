## Context

Command-log recording support makes safe actions and synthetic navigate-to actions eligible for command logging so exported command streams can replay user journeys.
Replay can only execute such streams reliably when every non-service action target can be reached from a previously recorded step.
Menu service actions provide the roots of the journey because `@DomainService` menu services are globally locatable.
Synthetic navigate-to actions were introduced so navigation through collections and references can produce command results that establish a path to later targets.

The current command log flow records each eligible command independently.
A later action can therefore be persisted even if its target was never produced by an earlier logged command and is not a menu service root.
Blocking such actions during recording is not practical because there is no reliable recording-time boundary that says how far back to search through `CommandLogEntry` history.
The export manager does have a baseline and selected sequence, so export validation can decide whether the selected sequence is self-contained and replayable.

## Goals / Non-Goals

**Goals:**
- Define the rules for whether an exported command target is known in an exportable sequence.
- Validate the selected export sequence before YAML is created.
- Use the export manager baseline as the lower bound for any prior command/result search.
- Keep synthetic navigate-to and other safe action result bookmarks as the mechanism for making later targets known.
- Report the specific command that fails validation.

**Non-Goals:**
- Block additional commands while recording.
- Reconstruct paths for existing invalid command log entries.
- Add a new export YAML field for dotted paths in this change.
- Change command replay result remapping behavior.
- Change behavior for applications that record commands but do not attempt to export them.

## Decisions

### Validate in the export action

The export selected action should validate target knowledge after it has resolved and sorted the selected command log entries, and before it creates the YAML `Clob`.
If validation fails, the action should be disabled or rejected with a clear validation message that identifies the failing command and target.

Alternative considered: validate at command recording completion.
That would reject invalid actions earlier, but it requires guessing how far back in command history to search and could incorrectly block legitimate recordings.

### Use the export baseline as the search boundary

The validator should use the export manager baseline to define the start of the exportable range.
A target can be known from commands selected for export and from prior commands at or after the baseline when those prior commands are part of the same exportable history.
Commands before the baseline should not be considered because they are outside the export manager's current recording/export window.

Alternative considered: search all historical command log entries for a matching result bookmark.
That would make many targets appear known without proving they are reachable within the export being produced.

### Model known targets from roots and results

The validator should treat an action target as known when one of these conditions holds:
- the target is a menu service annotated as a domain service, because menu services are export roots;
- the target bookmark matches the result bookmark of an earlier command in the baseline-bounded exportable sequence;
- the target belongs to a command outside the selected export action set and is therefore not being exported in this operation.

Alternative considered: treat any resolvable bookmark as known.
That would make local object lookup sufficient, but it would not provide a replay path and would defeat the purpose of recording navigation steps.

### Use result metadata as the path edge

No new persistent dotted-path field is required for this change.
The sequence order, export baseline, selected commands, and existing result bookmark metadata provide the graph edges needed by export and replay tooling.
Synthetic navigate-to actions should continue to be ordinary logged safe actions whose result establishes the target for later commands.

Alternative considered: persist an explicit path string on each command log entry.
That would make validation independent of sequence scanning, but it would add a data model concern before the current replay/export contract needs it.

## Risks / Trade-offs

- [Risk] Users can still record commands that cannot be exported on their own.
  → Mitigate by preventing export and explaining which command needs an earlier navigation or finder result.
- [Risk] Determining whether a service target is a root may differ between service objects and command DTO bookmark shape.
  → Mitigate by using the metamodel or specification loader to recognize domain service action targets rather than relying only on bookmark strings.
- [Risk] Baseline-bounded sequence scanning could be expensive for large export ranges.
  → Mitigate by validating only when export is requested and by scanning in export order once.
- [Risk] Nested commands or sibling command ordering may affect what counts as earlier.
  → Mitigate by using existing command log timestamp and interaction ordering consistently with export ordering.

## Migration Plan

No schema migration is expected.
Existing command log entries remain unchanged.
After deployment, applications may continue recording commands as before.
Export will refuse selected command sequences that are not replayable from the export manager baseline.
If needed, rollback by reverting the export validation change.

## Open Questions

- Should validation be implemented only as `validateSelected(...)`, or should `act(...)` also guard against callers that bypass UI validation?
- What command identity should appear in the validation message: interaction id, timestamp, logical member identifier, or all of them?
