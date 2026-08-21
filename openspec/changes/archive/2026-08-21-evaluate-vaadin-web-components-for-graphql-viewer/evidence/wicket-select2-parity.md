# Wicket Select2 behavior baseline

## Current dependency and integration

The Wicket UI depends on `org.wicketstuff:wicketstuff-select2` and the Select2 WebJar.
Causeway wraps the library with `SingleChoice`, `MultiChoice`, `ChoiceProvider`, `Select2`, and `OnSelectBehavior` rather than exposing raw Select2 state as domain state.

## Widget selection

`Select2.create()` chooses `Select2Choice` for singular attributes and `Select2MultiChoice` for plural attributes.
The component is configured from Causeway attribute metadata, including required state and a configurable AJAX delay.
The configured delay is the debounce parity requirement for a browser-side replacement.

## Choice sources

`ChoiceProvider` supports three Causeway choice-provider modes:

- `CHOICES` loads the declared choices and filters them by the entered term.
- `AUTO_COMPLETE` invokes the member autocomplete behavior with the entered term.
- `OBJECT_AUTO_COMPLETE` invokes type-level object autocomplete.

The WicketStuff provider receives `term` and `page`, but the current Causeway delegate does not page its matching results.
Consequently, Vaadin page-aware loading would improve scalability, but matching current Wicket behavior requires debounced remote filtering and stable identity rather than true paging.

Required selectors omit the null choice.
Optional selectors add a clearable null representation.
Choice labels and ordering are supplied by Causeway metadata rather than Select2.

## Stable identity and conversion

Selected objects are represented by `ObjectMemento` values.
`getIdValue()` uses an encoded object display representation rather than list index or display text, and selection templates preserve the title and bookmark identity needed to recover domain objects.
The provider converts selected ids back to application objects before validation or invocation.

A replacement must use canonical logical type and opaque identifier or the corresponding encoded GraphQL choice identity.
It must not treat labels as identity.

## Single and multi-selection events

`OnSelectBehavior` listens to `select2:select`, `select2:unselect`, and `select2:clear`.
For multi-selection it copies the current ordered collection, adds or removes the identified memento, updates the receiving attribute, and triggers the Causeway change dispatcher.
For single selection it sets or clears the receiving value and resets component input when necessary.

The semantic parity target is one deterministic Causeway-owned change event per accepted add, remove, select, or clear operation.
Applications must not need to consume Vaadin-specific events.

## Validation and disabled behavior

Select2 participates in Wicket required checks and Causeway conversion before the pending attribute value is accepted.
The surrounding attribute model owns editability, disabled reasons, validation, and dependent member refresh.
The replacement control must remain subordinate to the existing Causeway interaction state in the same way.

## Breadcrumb autocomplete

The Wicket breadcrumb selector also uses `Select2Choice` with a custom provider.
It searches known breadcrumb objects, identifies them through the object identifier, resolves the selected breadcrumb model, and navigates to the canonical domain object page.
This demonstrates that the widget is also a navigation affordance, not only an action-parameter editor.

## Parity checklist

- Singular and plural selection.
- Fixed choices, member autocomplete, and type-level autocomplete.
- Configurable debounce.
- Required and optional clearing behavior.
- Stable encoded domain identity.
- Causeway-owned labels and ordering.
- Deterministic select, unselect, and clear reconciliation.
- Causeway conversion, validation, disabled state, and dependent refresh.
- Canonical object navigation for breadcrumb-like use.
- No requirement for true server paging in the current Wicket baseline.
