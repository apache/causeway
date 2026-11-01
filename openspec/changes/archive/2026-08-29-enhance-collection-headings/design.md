## Context

`<cw-collection>` already renders a semantic heading shell and has styling for a smaller collection description, but it derives its heading from the legacy HTML `label` attribute or the member id and does not select canonical collection metadata.
The component also turns the collection wrapper's disabled reason into visible prose, producing messages such as “Cannot edit a mixed-in collection” even though collection contents remain useful and readable.
Rich GraphQL wrappers already expose canonical `metadata.friendlyName` and `metadata.description`, so no schema expansion is required.

## Goals / Non-Goals

**Goals:**

- Support application-authored collection names and descriptions with `named` and `described-as`.
- Use canonical collection friendly names and descriptions when HTML does not override them.
- Present descriptions below headings with quiet, smaller typography and accessible association.
- Remove visible collection-level unmodifiable noise without changing read-only semantics.
- Demonstrate selective combinations in Petclinic.

**Non-Goals:**

- Make collections editable.
- Hide disabled reasons for properties, actions, parameters, or associated collection actions.
- Change Rich GraphQL metadata fields or metamodel facet semantics.
- Add toolkit-specific collection heading behavior.

## Decisions

### Resolve names and descriptions with explicit precedence

The name precedence is `named`, legacy `label`, canonical `metadata.friendlyName`, then a humanized member id.
The description precedence is `described-as`, canonical `metadata.description`, then no description.
Blank explicit values are treated as absent, and a description equal to the resolved name after trimmed case-insensitive comparison is suppressed.

Keeping `label` as a compatibility alias avoids breaking existing pages while making `named` the preferred domain-language attribute.
Using light-DOM heading slots was rejected because the existing generated shell owns loading, error, empty, table, and Grid states consistently.

### Select collection metadata through the existing requirement

Collection requirement translation will request only supported `friendlyName` and `description` fields under the member's `metadata` wrapper.
The component reads accepted values from the current response data rather than from schema introspection descriptors.
This follows property metadata behavior and preserves targeted, additive GraphQL reads.

### Keep disabled state but omit collection-level explanation markup

The collection continues receiving hidden and disabled wrapper state and remains read-only.
It will not render the collection wrapper's disabled boolean or reason as text, title, or tooltip.
Member cells and associated actions retain their own authorization and usability semantics.

### Retain one accessible heading shell

The existing `<h2>` and optional following description remain the collection's accessible name and description through `aria-labelledby` and `aria-describedby`.
CSS will give the description explicit inline padding, subdued color, smaller text, and spacing directly below the label before collection content.

## Risks / Trade-offs

- [Risk] Existing pages rely on visible collection disabled reasons. → Limit suppression to the collection wrapper; actionable controls retain their own reasons.
- [Risk] Additional metadata broadens every selected collection query. → Select only fields supported by introspection and reuse the current bounded member read.
- [Risk] Two HTML name attributes could conflict. → Define deterministic `named` precedence and retain `label` only for compatibility.
- [Risk] Duplicate descriptions add noise. → Suppress descriptions equal to the resolved name.

## Migration Plan

No migration is required.
Existing `label` attributes continue to work, while pages can adopt `named` and `described-as` incrementally.
Rollback removes the two attributes, metadata selection, Petclinic examples, and collection-specific presentation changes.

## Open Questions

None.
