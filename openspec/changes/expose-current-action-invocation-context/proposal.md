## Why

Domain action implementations sometimes retain defensive rule checks for callers that invoke their Java methods directly, but those checks duplicate visibility, usability, and validity evaluation when Causeway invokes the same action through its metamodel.
Causeway already tracks the current `ActionInvocation`, so exposing a supported way to identify the exact currently executing action lets domain code avoid duplicate queries and other expensive rule evaluation without mistaking an unrelated outer action for the current call.

## What Changes

- Add an application-facing API for determining whether a specified action on a specified target is the current framework-managed action invocation.
- Match the target and logical member identifier, rather than merely reporting that some interaction or execution is active.
- Define framework-managed invocation to include viewer, REST, and `WrapperFactory` execution, including wrappers configured to skip rule validation, while excluding plain Java calls and direct calls made from another executing action.
- Preserve the existing interaction and execution APIs and add the capability without breaking existing callers.
- Add regression coverage for top-level, nested, wrapped, skip-rules, and direct invocation paths.

## Capabilities

### New Capabilities

- `current-action-invocation-context`: Allows application code to test whether an exact target action is currently executing through Causeway's metamodel invocation machinery.

### Modified Capabilities

None.

## Impact

- Public API in the applib interaction service area.
- Runtime interaction/execution implementation and, if needed, metamodel identifier resolution for mixin actions.
- Wrapper and headless regression tests that exercise nested execution semantics.
- No new external dependencies and no persistence-adapter-specific behavior.
