## Context

Rich GraphQL action wrappers already expose canonical static presentation metadata, but they do not expose the metamodel's are-you-sure semantic.
The Web Components action element therefore cannot tell the standard interaction controller that an otherwise invokable action needs confirmation.
The controller currently prepares parameterless and parameterized actions, validates complete parameter values, and invokes immediately.
Petclinic's parameterless `delete` action is marked `NON_IDEMPOTENT_ARE_YOU_SURE`, making it a deterministic acceptance example, but its current fixture-only disable rule masks the pinned original application's visit-based veto.

## Goals / Non-Goals

**Goals:**

- Expose canonical are-you-sure semantics without invoking domain behavior or broadening GraphQL metadata into a metamodel API.
- Insert confirmation after authoritative preparation and validation but before invocation.
- Preserve pending parameter values when a user declines a parameterized action's confirmation.
- Preserve action-source focus when a parameterless confirmation is cancelled.
- Restore Petclinic's original rule that an owner with visits cannot be deleted.

**Non-Goals:**

- Custom confirmation text, browser-native `window.confirm`, or application-authored confirmation policy.
- Confirmation for ordinary non-idempotent, idempotent, or safe actions.
- Cascading deletion of visits to make owner deletion succeed.
- Changes to server-side authorization, validation, invocation placement, or result policy.

## Decisions

### Expose a nullable `areYouSure` Boolean in shared rich member metadata

`RichMemberMetadata` will add `areYouSure` as a nullable Boolean.
For an `ObjectAction`, it reflects `action.getSemantics().isAreYouSure()`; for property, collection, and parameter wrappers it is null.
A Boolean communicates the exact client decision while avoiding coupling clients to all `SemanticsOf` enum values.
The field remains side-effect-free and backward compatible because GraphQL responses include it only when selected.

Alternative considered: expose the complete semantics enum or string.
That would reveal more metamodel vocabulary than this viewer needs and force clients to interpret enum evolution.

### Treat confirmation as authoritative action presentation, not HTML configuration

Object and service action state reads will select `metadata.areYouSure` when the schema advertises it.
`<cw-action>` will carry only a strict `true` value into the semantic action request and standard controller.
No public attribute will enable or disable confirmation, so page-authored presentation cannot weaken canonical destructive semantics.
Older compatible schemas that lack the field continue with existing behavior.

Alternative considered: infer confirmation from mutation placement or action names.
Neither reliably represents Causeway's explicit semantics.

### Add a `confirming` interaction state before invocation

After parameterless preparation, an are-you-sure action enters `confirming` instead of invoking.
After a parameterized action passes parameter preparation and whole-action validation, it enters the same state instead of invoking.
Confirmation invokes once using the retained action id, context, values, source, and generation.
Declining a parameterized confirmation returns to editing with values intact; declining a parameterless confirmation closes the prompt and restores source focus.
Escape follows the same decline behavior, while stale generations and repeated activation remain gated.

Alternative considered: call `window.confirm`.
A controller-owned light-DOM dialog provides stable accessibility, styling, automation, focus, and lifecycle behavior without blocking the browser event loop.

### Use a separate confirmation presentation within the existing prompt owner

The interaction controller will render an accessible modal confirmation with the effective action name, concise irreversible-action wording, and explicit Confirm and Cancel controls.
The confirm control receives a destructive semantic class and stable test id.
The existing controller remains the sole owner of action prompts and invocation state.

Alternative considered: nest a second dialog over the parameter prompt.
Replacing the prompt presentation avoids stacked modal focus and accessibility ambiguity while retained state allows returning to parameters.

### Restore Petclinic's visit-count veto through member support

`PetOwner.disableDelete()` will query `VisitRepository.findByPetOwner(this)` and return the pinned original reason `This owner has N visits` when the count is nonzero.
Owners without visits remain eligible, so their delete action demonstrates confirmation and can remove the owner and cascade-owned pets without referential-integrity failure.
This reproduces the original observable policy without importing obsolete application event infrastructure.
The action description will no longer claim that visits are deleted.

Alternative considered: subscribe to the delete domain event exactly as the pinned application did.
The current compact sample can express the same authoritative disable semantic with conventional member support and less ported infrastructure.
Cascade-deleting visits was rejected because the selected policy is the original veto behavior.

## Risks / Trade-offs

- [Risk] A compatible older GraphQL schema lacks `areYouSure`, so confirmation cannot be inferred safely. → Select the field only after introspection and preserve existing compatibility behavior when absent.
- [Risk] Re-rendering between parameter and confirmation views can lose values or focus. → Retain one immutable prompt state, test decline and confirmation paths, and restore focus according to parameterized versus parameterless origin.
- [Risk] Double activation could invoke twice. → Gate confirm while invoking and retain generation checks around invocation.
- [Risk] Petclinic fixture owners all have visits, leaving no successful delete example. → Browser acceptance creates or selects a disposable owner without visits before testing confirmation and successful deletion.

## Migration Plan

1. Add and test the optional rich GraphQL metadata field.
2. Consume the field conditionally in object and service action state reads.
3. Add confirmation state, markup, behavior, styling, and foundation tests.
4. Replace Petclinic's fixture-specific disable reason with the visit-count veto and update its description and tests.
5. Exercise disabled fixture deletion and confirmed disposable deletion in integration and browser verification.

Rollback removes the optional field selection and confirmation state while leaving existing GraphQL documents compatible.

## Open Questions

None.
