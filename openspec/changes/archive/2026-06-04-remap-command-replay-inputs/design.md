## Context

The command log applib now has a replay mapping SPI that receives recorded-to-actual result bookmark mappings after successful replay.
Replay still executes the imported `CommandDto` as recorded, so target identifiers and action parameter values remain fixed to the source environment at recording time.

When replay happens later or in another environment, those identifiers may no longer point at the intended objects.
The same application service that records result mappings is the natural place to answer replay-time remapping questions before the command is executed.

## Goals / Non-Goals

**Goals:**

- Broaden the replay result mapping SPI into a command replay mapping SPI.
- Ask the SPI for target identifier remapping before executing a replayed command.
- Ask the SPI for reference-valued action parameter remapping before executing a replayed action command.
- Preserve existing recorded-to-actual result mapping notifications after successful replay.
- Keep replay unchanged when no SPI implementation exists or when the SPI does not provide replacements.

**Non-Goals:**

- Do not introduce a persisted mapping table in the command log extension.
- Do not infer mappings automatically from object lookup failures.
- Do not remap property commands unless their DTO representation explicitly exposes parameter-like values through the same supported path.
- Do not mutate the persisted imported `CommandDto` unless that is already required by existing replay execution semantics.

## Decisions

- Rename the SPI from result-specific naming to command replay mapping naming.
  A name such as `CommandReplayMappingListener` better describes both pre-execution remapping and post-execution result notification.
  Alternative considered: add a second SPI, but splitting related mapping behavior would force applications to coordinate two services.

- Use optional list injection for SPI implementations, matching the existing opt-in behavior.
  Replay proceeds unchanged when the list is empty.
  Alternative considered: require a default implementation, but the repository policy is to avoid a no-op implementation for this SPI.

- Apply remapping to a replay copy of the command DTO before calling `CommandExecutorService#executeCommand(...)`.
  This prevents application-provided remapping from changing the stored imported command record and keeps audit data aligned with the original recording.
  Alternative considered: mutate the persisted DTO, but that would make subsequent inspection lose the originally recorded values.

- Ask for target remapping using bookmark-like values derived from each target OID.
  The SPI should be able to return a replacement bookmark or replacement identifier for the same logical type.
  Alternative considered: pass only raw identifier strings, but bookmarks give applications both logical type and identifier context.

- Ask for action parameter remapping for parameters represented as `ParamDto` entries with `type: "reference"` and a populated `reference` OID.
  The SPI should receive enough context to identify the command, action member, parameter name or index, and current reference bookmark, then return an optional replacement reference bookmark.
  Alternative considered: support every `ParamDto` value shape immediately, but the supplied replay use case is specifically object reference remapping and reference DTOs provide bookmark-like type/id data.

- Preserve existing result mapping notification semantics after successful replay.
  The renamed SPI should continue to be notified only when both recorded and actual result bookmarks are present.

## Risks / Trade-offs

- [Risk] Deep copying `CommandDto` can be error-prone → Use existing schema marshalling or utility support where available, and cover remapped replay command content with tests.
- [Risk] Parameter DTOs support many value shapes → Limit the first implementation to reference parameters represented by `ParamDto.reference`, and make no-op behavior explicit for non-reference parameters.
- [Risk] Multiple SPI implementations could return conflicting remaps → Apply implementations in framework-supplied order, passing each replacement to the next implementation.
- [Risk] SPI exceptions could break replay → Treat remapping SPI exceptions consistently with existing result notification policy, preferably logging and continuing with the current value unless implementation reveals a stronger requirement.
