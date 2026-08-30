## Why

Associated and object-level action buttons currently sit on separate rows that weaken their visual relationship to the field or object they operate on.
The Petclinic reference page should demonstrate a more compact, intentional action layout while preserving semantic component ownership.

## What Changes

- Align property-associated actions with the property field/value column rather than below the complete property row.
- Allow property-associated action placement to collapse responsively without overlap or horizontal overflow.
- Move the Petclinic owner object-level delete action into the title row immediately after the object header.
- Keep object-title/action composition application-owned while ensuring semantic web components do not force unnecessary full-line placement.
- Add focused foundation and Petclinic regression coverage for the revised placements.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Refine associated-action and component flow presentation requirements for responsive member composition.
- `generic-htmx-web-component-viewer`: Refine Petclinic custom-page and visual acceptance requirements for application-owned inline title actions.

## Impact

- Foundation structural styles and synchronized installable styles.
- Foundation component-style and property-associated-action regression tests.
- Petclinic `petclinic.PetOwner.html` application layout and application stylesheet.
- Petclinic browser and/or resource-page regression assertions.
- No GraphQL, semantic event, action invocation, or public Java API changes.
