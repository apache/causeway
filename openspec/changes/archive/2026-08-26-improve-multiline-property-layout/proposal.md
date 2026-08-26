## Why

Read-only multiline string properties currently waste horizontal space because a description occupies the value column, forcing the actual value below the label and stretching the edit action across the remaining row.
The property should retain the established label/value alignment while using its label column for supporting description text and presenting editing as a compact secondary action.

## What Changes

- Keep a multiline property's read-only value in the normal value column beside its label.
- Place the property's description directly beneath its label in the label column.
- Render the built-in property edit action as a compact control rather than a column-filling button.
- Preserve accessible label and description relationships, editing behavior, GraphQL authority, associated actions, responsive layout, and native/Vaadin editor policies.
- Add structural and Petclinic browser regression coverage for the Notes property layout.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `domain-web-components`: Define the read-only presentation layout for described multiline properties and their built-in edit control.

## Impact

The change affects foundation property presentation styles, synchronized installable styles where applicable, style-contract tests, and Petclinic browser coverage.
It does not change GraphQL schemas or operations, semantic events, public element names, routes, CSP, dependencies, or application-owned page composition.
