## Why

Applications need selected synchronous preloading hooks to populate the current ORM persistence context before expensive actions and entity-page preparation.
Forward-port roadmap step 10a (CAUSEWAY-4066), maintenance commit `19d2a36d2f9`, without including primer observations from step 10b.

## What Changes

- Add application-facing registrar, registry, action/view callbacks and immutable positional action arguments.
- Validate exact metamodel class/action registrations after metamodel loading and freeze the lookup registry before requests.
- Invoke ordinary action primers after executing-event argument adjustment and before domain invocation, using mixee/domain-facing identity and preserving pass-through/pseudo-getter behavior.
- Invoke authorized Wicket domain-object page primers before expensive preparation, at most once per page per request, including later full/Ajax requests.
- Add focused API, registry, action lifecycle and Wicket request tests plus usage documentation.

## Capabilities

### New Capabilities

- `application-priming-hooks`: Validated startup registration and synchronous action/entity-view priming with immutable arguments, exact matching and transparent failures.

### Modified Capabilities

None.
Existing action context, authorization and observation boundaries are preserved.

## Impact

Adds applib and metamodel service packages/exports and explicit metamodel module registration.
Adapts maintenance's mutable action executor to v4's record-based `ActionExecutor` and its `InteractionHead.owner()`/separate domain-facing invocation identity.
Adapts maintenance's entity page hook to v4's `DomainObjectPage`, including initial authorized preparation and its existing refresh lifecycle.
No production dependency, telemetry configuration, schema, automatic bulk-loading policy or asynchronous execution is introduced.
The metamodel module adds managed JUnit/Mockito dependencies in test scope for its new focused tests.
The branch is stacked on application-span PR #3911 and can be rebased after that merges.
