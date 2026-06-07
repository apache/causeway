## Context

`ReplayableCommandParticipant` currently models participant rows for target, reference parameter, and result bookmarks.
The row already has a `parameterName` property that identifies the recorded command parameter.
It also exposes a `parameter` object-valued property for the resolved actual reference parameter object.
Using both `parameterName` and `parameter` makes the UI and API terminology less clear because one is metadata and the other is the resolved replay argument.

## Goals / Non-Goals

**Goals:**

- Rename the object-valued participant property from `parameter` to `argument`.
- Preserve `parameterName` exactly as the recorded command parameter label.
- Keep participant derivation, bookmark mapping, and object resolution behaviour unchanged.
- Update fallback layout metadata and tests to use the new property name.

**Non-Goals:**

- Do not rename the participant role `PARAMETER`.
- Do not rename `parameterName`.
- Do not change command DTO parsing, replay mapping SPI signatures, or persistence schema.
- Do not introduce compatibility aliases unless implementation constraints require them.

## Decisions

- Use `argument` as the object-valued property name because it describes the runtime value supplied to the replayed command while avoiding collision with `parameterName`.
- Keep the participant role and recorded parameter metadata unchanged because they describe the command signature and existing replay semantics.
- Update layout and column-order metadata in the same change so UI rendering and fallback table ordering stay aligned with the renamed property.
- Treat this as a source/API-breaking rename for consumers of `ReplayableCommandParticipant#parameter`, rather than adding a deprecated duplicate property, because the command log applib participant type is newly introduced and the requested refinement is terminology cleanup.

## Risks / Trade-offs

- Existing custom code or layouts referencing `parameter` will fail until updated → Mark the change as breaking in the proposal and update all in-repository references.
- Display labels may still mention reference parameters through `parameterName` and role `PARAMETER` → Preserve that wording intentionally because it identifies the command parameter, not the resolved argument object.
- Tests could miss layout metadata regressions → Update focused participant tests to assert the new `argument` property and column ordering.
