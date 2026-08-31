## Context

The standard interaction controller currently owns one prompt lifecycle and always renders parameter entry as a fixed modal-looking `<dialog>` in the controller's light DOM.
Causeway's metamodel already resolves `ObjectAction.getPromptStyle()` from annotations, layout XML, configuration, action shape, and viewer defaults, but rich GraphQL does not expose that value.
Authored `<cw-action>` currently supports name and nested parameter presentation overrides but has no prompt placement override.
Property-associated actions can be represented either as direct children of an authored `<cw-property>` or as effective-grid action groups structurally associated with a generated property.

## Goals / Non-Goals

**Goals:**

- Carry canonical prompt style from rich GraphQL through object and service action presentation.
- Add a presentation-only `prompt-style` HTML override with deterministic precedence and safe normalization.
- Present one unchanged interaction lifecycle inline, in a movable modal, or in a vertical sidebar.
- Preserve accessibility, focus, cancellation, validation, confirmation, invocation, stale-response, and at-most-once guarantees.
- Support both authored and effective-grid property associations for inline placement.

**Non-Goals:**

- Add new `PromptStyle` enum constants or change metamodel resolution.
- Let HTML change parameters, action semantics, confirmation policy, or invocation.
- Introduce a router, application callback, frontend framework, or third-party dialog library.
- Make sidebar prompts resizable or persist modal positions between invocations.
- Convert parameterless direct invocation into a presentation-only parameter prompt.

## Decisions

### Expose the resolved canonical enum name as nullable shared metadata

`RichMemberMetadata.promptStyle` will be a nullable string.
For an `ObjectAction`, it will return `ObjectAction.getPromptStyle().name()` when available; for other member wrappers it will return null.
Existing clients remain compatible because GraphQL fields are opt-in, and Web Components clients will select the field only when schema introspection advertises it.

Alternative considered: expose a new GraphQL enum.
A shared nullable string follows the existing metadata shape, avoids coupling the schema to every metamodel enum constant, and permits older schemas and clients to interoperate.

### Normalize to three effective Web Components styles

The client will use `INLINE`, `DIALOG_MODAL`, or `DIALOG_SIDEBAR` internally.
Canonical `DIALOG` and unknown values will safely resolve to `DIALOG_MODAL`; canonical `INLINE_AS_IF_EDIT` will resolve to `INLINE`; `AS_CONFIGURED` and `NOT_SPECIFIED` will resolve to `DIALOG_MODAL` if they reach the client.
The authored `prompt-style` attribute will be case-insensitive after trimming and hyphen-to-underscore normalization, but only the three effective values are accepted as overrides.
An absent or invalid authored value will defer to canonical metadata rather than suppress it.

Alternative considered: support every metamodel enum value in HTML.
The authored contract is intentionally narrower because configuration resolution and as-if-edit semantics belong to the authoritative metamodel, not page markup.

### Treat prompt style as presentation with HTML-over-metadata precedence

`<cw-action prompt-style="DIALOG_SIDEBAR">` will override canonical prompt placement for that component instance.
The effective style will travel in the existing action presentation detail and will not alter action identity, parameter metadata, preparation, validation, confirmation, or invocation.
Object actions, service actions, and application-menu actions will otherwise use canonical metadata when selected.

Alternative considered: let the interaction controller inspect the originating element's attribute directly.
Carrying normalized presentation preserves the existing separation between semantic action sources and controller behavior and also works for menu actions that have no `<cw-action>` host.

### Portal inline prompts into the associated member composition

The interaction controller will continue to own state and rendering decisions, but it may place the active prompt surface into a temporary light-DOM portal at the associated property composition.
For a directly nested action, the property primary presentation and associated action controls will be hidden while the portal is active.
For an effective-grid association, the generated property and its associated-action group will be hidden while the portal occupies their composition wrapper.
The original nodes and hidden states will be retained and restored on cancellation, successful completion, style transition, disconnection, or replacement by a newer generation.

If `INLINE` has no resolvable property association, the effective presentation will fall back to `DIALOG_MODAL`.
This prevents a prompt from disappearing or replacing an unrelated member.

Alternative considered: copy parameter state into `<cw-property>` or absolutely position controller content over it.
A reversible portal keeps one interaction owner and real light-DOM controls while preserving layout flow and event semantics.

### Use style-specific accessible prompt surfaces

Inline prompts will use a labelled non-modal region and will not trap Tab navigation.
Modal prompts will use the established labelled modal-dialog semantics, backdrop, Escape cancellation, focus containment, and restoration.
The modal heading will act as a pointer drag handle; movement will be clamped to the viewport and reset for each prompt.
Sidebar prompts will use labelled modal-dialog semantics in a fixed vertical panel at the inline end of the viewport, with the same backdrop, Escape, focus containment, and restoration as modal prompts.
Responsive styling will keep modal and sidebar surfaces within the visual viewport.

Alternative considered: native `showModal()` and an external draggable-dialog package.
The current controller uses declarative light-DOM rendering, and bounded pointer movement can be implemented without a dependency while retaining deterministic tests and CSP compatibility.

### Keep destructive confirmation modal

When canonical `areYouSure` enters `CONFIRMING`, the controller will close any inline or sidebar parameter surface and render the existing alert-dialog confirmation as `DIALOG_MODAL`.
Parameterized cancellation from confirmation will return to the same parameter values and original effective prompt style.
This preserves the established safety contract and prevents HTML presentation from weakening canonical confirmation.

## Risks / Trade-offs

- [Risk] Portal cleanup leaves a property or action hidden. → Record original hidden states, centralize restoration, and test cancellation, success, failure, style transition, and disconnect paths.
- [Risk] Prompt events no longer reach controller listeners after portal placement. → Bind the same controller-owned delegated handlers to each active portal and remove them during cleanup.
- [Risk] Re-rendering replaces focused controls. → Query and restore focus through the active prompt surface rather than assuming the controller host.
- [Risk] Dragging moves a modal beyond reach. → Start dragging only from the heading, clamp coordinates to the viewport, and reset position on every new prompt.
- [Risk] Old GraphQL schemas reject new selections. → Gate `promptStyle` selections on introspected metadata fields and retain modal fallback.
- [Risk] Generic metamodel styles differ from the three Web Components styles. → Document and unit-test the explicit normalization table.

## Migration Plan

1. Add and test rich GraphQL prompt-style metadata.
2. Extend introspection-aware object and service selections and presentation normalization.
3. Add the `<cw-action>` reflected attribute/property and carry effective style in semantic requests.
4. Add style-specific prompt surfaces, reversible inline portals, modal dragging, sidebar styling, and active-surface event/focus handling.
5. Update documentation, fixtures, unit coverage, and Petclinic declarations and Playwright acceptance.
6. Run focused GraphQL, foundation, Petclinic integration, and browser checks before archive.

Rollback removes the optional metadata selection and attribute handling and returns all parameter prompts to the existing modal rendering.

## Open Questions

None.
