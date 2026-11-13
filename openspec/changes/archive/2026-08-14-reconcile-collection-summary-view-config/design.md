## Context

`CollectionContentsAsSummaryFactory` decides applicability by checking for a `BigDecimal` property on the element
type:

```
// viewers/wicket/ui/.../collection/present/summary/CollectionContentsAsSummaryFactory.java:61-69
public ApplicationAdvice appliesTo(final IModel<?> model) {
    final boolean hasAnyBigDecProperty = …streamAssociations(MixedIn.EXCLUDED).anyMatch(OF_TYPE_BIGDECIMAL)…;
    return appliesIf(hasAnyBigDecProperty);
}
```

There is no summary-view-disabled short-circuit and no env-var lookup. The factory has access to configuration
via `MetaModelContext` (inherited from `ComponentFactoryAbstract`); other components read Wicket config as
`elementType.getMetaModelContext().getConfiguration().viewer().wicket()` (e.g. `ActionColumn.java`).

The Wicket viewer configuration is the record `CausewayConfiguration.Viewer.Wicket`
(`core/config/.../CausewayConfiguration.java`), which already declares many boolean properties with
`@DefaultValue("false") boolean …` fields (e.g. `ajaxDebugMode`, `redirectEvenIfSameObject`). A field
`summaryViewDisabled` maps to the property name `causeway.viewer.wicket.summary-view-disabled` by Spring Boot's
kebab-case relaxed binding.

Maintenance used `System.getenv("causeway.viewer.wicket.summary-view-disabled")`. Product decision D-B rejects
copying that mechanism in favour of a real configuration property.

## Goals / Non-Goals

**Goals:**

- Operators can disable the Wicket collection summary view through a normal `causeway.viewer.wicket.*`
  configuration property.

**Non-Goals:**

- No environment-variable lookup.
- No change to summary-view behaviour when the property is unset (default `false` = current behaviour).
- No change to other collection presentations.

## Decisions

### Add a boolean Wicket config property

Add `@DefaultValue("false") boolean summaryViewDisabled` to `CausewayConfiguration.Viewer.Wicket`, in the same
style as the existing boolean properties, mapping to `causeway.viewer.wicket.summary-view-disabled`.

### Short-circuit in `appliesTo`

At the top of `CollectionContentsAsSummaryFactory.appliesTo(...)`, return `ApplicationAdvice.DOES_NOT_APPLY` when
`getMetaModelContext().getConfiguration().viewer().wicket().summaryViewDisabled()` is true, before the BigDecimal
check. Obtain `MetaModelContext` from the element type / model, consistent with how sibling components read
Wicket config.

Rejected — port the v2 `System.getenv(...)`: violates v4 configuration conventions (D-B).

## Acceptance evidence

- With `causeway.viewer.wicket.summary-view-disabled` unset/false, a collection whose element type has a
  `BigDecimal` property still gets the summary view (`appliesTo` returns apply).
- With the property `true`, `appliesTo` returns `DOES_NOT_APPLY` for the same collection.
- The property appears in the generated configuration metadata under `causeway.viewer.wicket.*`.
