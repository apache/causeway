# Baseline and boundaries

## Reproduced geometry

The supplied Petclinic screenshot captures the described multiline Notes property before the fix.
The Notes label begins near x=58 while its value wraps beneath it near x=58 instead of occupying the normal value column.
The description begins in the value column near x=283, above the actual value.
The Edit Notes button begins near x=283 and stretches to approximately x=846, consuming almost the full value-column width.

This geometry follows from auto-placement in the existing three-column property grid: the explicitly placed description claims columns two through three, the value falls into the next available first-column cell, and the edit button stretches in the remaining cell.

## Preserved boundaries

- The existing `<causeway-property multiline>` host attribute remains the semantic layout signal.
- The label, description, output, and edit button remain connected light-DOM nodes with unchanged accessible relationships.
- GraphQL remains authoritative for property metadata, value, visibility, usability, preparation, validation, and mutation.
- The public property element, events, test hooks, renderer selection, and native/Vaadin editor policies remain unchanged.
- Direct associated actions remain independently composed and are not styled as the built-in edit affordance.
- Petclinic HTML and domain metadata require no change.
- Strict CSP remains unchanged and no inline style or JavaScript measurement is introduced.
- The unrelated local `default0RemovePet()` modification in `PetOwner.java` is outside this change and remains unstaged.
