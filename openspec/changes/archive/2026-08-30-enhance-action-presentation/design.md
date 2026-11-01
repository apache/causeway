## Context

The ordinary action component already discovers hidden, disabled, and canonical description state, but its visible label falls back directly from `label` to a humanized member ID and its description is only hidden explanatory text.
Parameterized prompts independently humanize the action ID, so authored or canonical names and descriptions are lost at the dialog boundary.
Application menus already parse effective-menu names, descriptions, and Font Awesome hints, but disabled reasons replace rather than compose with descriptions and icon hints currently create only empty placeholders.
Rich member metadata has a shared additive metadata object, but it does not expose action Font Awesome facets or their position.

The change crosses GraphQL schema generation, browser metadata discovery, ordinary and Vaadin action controls, effective-menu parsing, prompt state, shared assets, and sample coverage.
Action presentation must remain escaped, bounded, authorization-safe, toolkit-neutral, and backward-compatible with older rich GraphQL schemas.

## Goals / Non-Goals

**Goals:**

- Let HTML authors override an action name with `named` while retaining `label` compatibility.
- Preserve canonical action names and descriptions through buttons, menus, and parameterized prompts.
- Compose description and disabled reason as separate tooltip sections.
- Render static `@ActionLayout(cssClassFa, cssClassFaPosition)` metadata with pinned same-origin Font Awesome assets.
- Keep native and Vaadin-backed action and menu presentations semantically equivalent.
- Demonstrate representative behavior in Petclinic without changing every action.

**Non-Goals:**

- Do not expose imperative icon methods, arbitrary HTML, action CSS classes, or other metamodel internals through rich metadata.
- Do not add application-authored icon overrides to `<cw-action>`.
- Do not change action execution, authorization, validation, result navigation, or refresh semantics.
- Do not redesign prompts or add configurable description presentation modes for actions.
- Do not load fonts, styles, or icons from a CDN.

## Decisions

### Action naming uses established authored precedence

`<cw-action>` will observe and reflect `named` in addition to compatible `label`.
The effective name precedence will be `named`, then `label`, then canonical `metadata.friendlyName`, then a humanized action ID.
This matches collection naming vocabulary while preserving existing pages that use `label`.

An alternative was to replace `label` outright, but that would break compatible application markup without improving semantics.

### Rich metadata exposes only static action icon data

The shared rich member metadata type will add nullable `cssClassFa` and `cssClassFaPosition` fields.
Only action wrappers with a static Font Awesome facet will populate them; other wrappers, absent facets, and imperative icon facets will return null.
The class value will use Causeway's canonical quick notation and the position will use the canonical `LEFT` or `RIGHT` token.

This keeps the schema additive and avoids exposing executable icon behavior or broad layout metadata.

### Browser discovery remains tolerant of older schemas

Object and service action discovery will request the new metadata fields only when introspection advertises them.
Normalized action presentation will carry bounded name, description, icon classes, and position into ordinary component state and interaction capabilities.
Parameterized prompt state will copy that presentation once preparation succeeds, and subsequent recomputation will preserve it.
Older schemas will continue to render names and prompts using established fallbacks without issuing invalid selections.

### Tooltips use one bounded composed presentation

Action descriptions and disabled reasons will be independently bounded and escaped, then composed with a blank-line section separator.
Ordinary action controls and native and Vaadin menu items will expose the composed tooltip while retaining hidden `aria-describedby` sections for assistive technology.
A disabled reason will never replace an available description.
Disabled ordinary controls will retain a focusable explanatory wrapper so keyboard users can reach the tooltip without making the action invokable.

Using only a native `title` attribute was rejected because it is not reliably keyboard-accessible and cannot provide consistent section styling.

### Parameterized prompts use the prepared action presentation

When an action has parameters, the dialog heading will use the same effective action name as the invoking control or menu item.
A non-duplicate canonical description will render immediately below the heading using the quiet description styling already established for properties and collections.
The description will participate in the dialog's accessible description alongside any current error.
Parameterless actions will continue directly to invocation without opening a dialog.

### Font Awesome rendering is same-origin and token-bounded

The HTMX shell will load the pinned Font Awesome WebJar stylesheet from the same origin before application styles.
Browser rendering will accept only bounded whitespace-separated CSS class tokens matching the Font Awesome token vocabulary and will add the required base class.
Icons will be decorative, `aria-hidden`, and placed before or after the label according to the canonical position.
Invalid, missing, or unsupported icon metadata will render no icon and will not affect action availability.

An internal icon-name map was rejected because it would be incomplete and would duplicate the pinned Font Awesome distribution.
A CDN stylesheet was rejected because it would violate offline, CSP, and external-request constraints.

### Effective application menus retain layout position

Effective-menu parsing and immutable projections will preserve `cssClassFaPosition` alongside the existing action icon hint.
Native menu rendering and Vaadin Menu Bar materialization will use the same bounded icon helper and composed tooltip helper.
Menu labels and sections remain unchanged, and raw Vaadin menu item state remains private.

## Risks / Trade-offs

- [Font Awesome WebJar increases shell CSS and font assets] → Pin the existing repository version, serve it lazily through same-origin WebJar paths, and cover packaging, CSP, and external-request behavior.
- [Arbitrary facet strings could become unsafe classes] → Bound length, tokenize rather than interpolate markup, accept only conservative Font Awesome class tokens, and escape all attributes.
- [Disabled controls are normally removed from keyboard order] → Put explanation focus on a non-invoking wrapper while leaving the disabled control itself disabled.
- [Shared metadata fields appear on non-action wrappers] → Return null outside applicable static action facets and document the narrow action-only semantics.
- [Prompt metadata could become stale during recomputation] → Freeze presentation from the current prepared capabilities and retire it with the existing prompt generation.
- [Native and Vaadin menu tooltip behavior can diverge] → Project one composed descriptor and test both default and explicit-native browser modes.

## Migration Plan

1. Add additive GraphQL metadata and tolerant browser discovery.
2. Add shared bounded tooltip and icon projection helpers.
3. Update ordinary action controls and parameterized prompts.
4. Update effective-menu parsing, projection, and native and Vaadin rendering.
5. Add the pinned same-origin stylesheet and Petclinic demonstrations.
6. Validate GraphQL approvals, foundation tests, HTMX rendering, Petclinic integration and Playwright modes, RAT, CSP, and strict OpenSpec consistency.

Rollback removes the additive metadata fields, shell stylesheet, and browser presentation use without changing persisted data or action execution contracts.

## Open Questions

None.
