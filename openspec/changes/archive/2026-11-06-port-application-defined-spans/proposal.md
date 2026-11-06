## Why

Applications need a framework-supported way to divide synchronous business work into meaningful trace regions without depending directly on Micrometer or OpenTelemetry APIs.
Forward-port roadmap step 9 (CAUSEWAY-4065), including maintenance commits `ea32b739d0f`, `5da64c34b57` and `f66b8f17a89`, onto v4's shared Boot/agent integration.

## What Changes

- Add injectable applib `ApplicationSpanService` with value-returning `call`, non-returning `run` and a standalone `NOOP` test helper.
- Add synchronous nested observations with stable operation `causeway.application.span`, bounded suffix-preserving display names and canonical current-member metadata.
- Reuse v4's existing distinction between physical invoked identity and domain-facing contributed action identity; preserve the former and use the latter for application-span names.
- Preserve original checked/runtime exceptions and errors, cleanup, result identity and inactive behavior.
- Add focused and exported-trace tests, usage guidance and roadmap evidence.

## Capabilities

### New Capabilities

- `application-defined-spans`: Public synchronous span service, current-member naming, nesting, validation, privacy and no-op behavior.

### Modified Capabilities

None.
Existing action-invocation identity requirements are preserved, not replaced.

## Impact

Adds public applib and runtime-service packages, module exports and runtime module registration.
Extends the existing observation naming helper without new dependencies, configuration, SDKs or exporters.
The current v4 action context already includes maintenance's final invoked/domain-facing identity fix; no duplicate identity API or executor rewrite is intended.
This branch is stacked on evaluation PR #3898 and can be rebased after that merges.
Application priming remains outside scope.
