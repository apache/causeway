## Context

The command replay mapping SPI currently supports replay input remapping through separate callbacks for command targets and reference-valued action parameters.
Both callbacks ultimately ask the same question: whether a recorded bookmark should be replaced before replay execution.
The reference-parameter callback also exposes parameter name and index, but that metadata is not required for the intended mapping use case and makes implementations more complex.

## Goals / Non-Goals

**Goals:**

- Simplify `CommandReplayMappingListener` so target and reference-parameter remapping both call one `remap(CommandLogEntry, Bookmark)` method.
- Preserve existing replay behavior for target remapping, reference-parameter remapping, stored DTO immutability, and result mapping notification.
- Keep the listener resilient by continuing to treat remapping exceptions as no-op mappings.
- Update tests and OpenSpec coverage to describe the simplified SPI contract.

**Non-Goals:**

- Do not add a discriminator for whether a bookmark came from a target or reference parameter.
- Do not expose parameter name, parameter index, or DTO internals through the remapping SPI.
- Do not change YAML import/export formats or command DTO structures.
- Do not change the result mapping notification method.

## Decisions

- Use a single `default Optional<Bookmark> remap(CommandLogEntry commandLogEntry, Bookmark recordedBookmark)` method on `CommandReplayMappingListener`.
This keeps the SPI focused on bookmark replacement and avoids leaking replay DTO details to implementations.
The alternative was to keep separate methods or add a context enum, but both preserve distinctions that are not needed for the current use case.

- Call the same listener method from both target and reference-parameter traversal paths.
This preserves the existing implementation structure while removing duplicated listener callback code.
The alternative was to merge traversal into one generic DTO visitor, but that would create a larger refactor than needed for an SPI simplification.

- Keep `onReplayResultMapped(...)` unchanged.
The requested simplification only concerns replay input remapping, and result notification still needs both recorded and actual result bookmarks.
The alternative was to rename or reshape result notification for symmetry, but that would add unrelated API churn.

- Treat this as a breaking API change for the recently introduced SPI.
Existing implementors must rename their remapping override and drop parameter-specific arguments.
The alternative was to leave deprecated bridge methods, but the SPI was introduced recently and the goal is to remove noise rather than preserve it.

## Risks / Trade-offs

- Existing listener implementations that override `remapTarget(...)` or `remapReferenceParameter(...)` will no longer compile.
Mitigation: document the breaking change in the proposal and update tests to show the replacement override.

- Implementations cannot make remapping decisions based on parameter name or index.
Mitigation: this is intentional because that information is not required for the desired SPI use case.

- A single method means an implementation cannot distinguish targets from reference parameters through the SPI signature alone.
Mitigation: callers that need different behavior can use the recorded bookmark and command log entry context, but no dedicated source discriminator will be introduced.
