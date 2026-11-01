## Why

Visible property and collection descriptions consume scarce page space even when users only need them occasionally.
Authors need a compact presentation option that preserves accessible descriptions and still explains disabled members without changing the established action-tooltip contract.

## What Changes

- Add a `description-as` attribute to `<cw-property>` and `<cw-collection>` with supported `label` and `tooltip` values.
- Preserve the existing visible description presentation as the default and as the explicit `label` mode.
- In `tooltip` mode, remove the description from visible layout while exposing it from the property label or collection heading by pointer and keyboard.
- Compose a disabled reason beneath the description as a distinct tooltip section when a property or collection is disabled.
- Keep description and disabled-reason text available to assistive technology independently of visual presentation.
- Leave `<cw-action>` behavior unchanged because action descriptions already use their tooltip-oriented presentation.
- Document and test precedence, invalid-value fallback, reactivity, accessibility, bounded tooltip content, and disabled-state composition.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Add authored description presentation modes and accessible combined description/disabled-reason tooltips for properties and collections.

## Impact

The change affects the framework-neutral property and collection elements, shared component styling, generated stylesheet parity, usage documentation, and foundation component/style tests.
It does not change GraphQL selections, domain metadata, action presentation, toolkit internals, or application routing.
