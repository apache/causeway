## Why

Child change 9 of the maintenance-branch → main final reconciliation
(`openspec/reconciliation/maintenance-branch/final-reconciliation-plan.md`, discrepancy **MA-12**; third-opinion
F10, fourth-opinion G12 — LOW). Implements product decision **D-B**: re-add the capability the v4 way.

Maintenance disables the Wicket BigDecimal collection "summary view" when an environment variable
`causeway.viewer.wicket.summary-view-disabled` equals (case-insensitively) `true` — an explicitly `[v2]`-tagged
`System.getenv(...)` stop-gap, not a real configuration property. `main` has no equivalent: its
`CollectionContentsAsSummaryFactory.appliesTo(...)` always applies whenever the element type has a `BigDecimal`
property (`viewers/wicket/ui/.../collection/present/summary/CollectionContentsAsSummaryFactory.java:61-69`), and
the property name has zero hits repo-wide.

Rather than copy the v2 env-var mechanism, this change introduces a proper v4 configuration property so operators
can disable the summary view through the normal configuration system.

## What Changes

- Add a boolean configuration property `causeway.viewer.wicket.summary-view-disabled` (default `false`) to the
  Wicket viewer configuration (`CausewayConfiguration.Viewer.Wicket`), following the existing boolean-property
  style there.
- In `CollectionContentsAsSummaryFactory.appliesTo(...)`, short-circuit to `ApplicationAdvice.DOES_NOT_APPLY`
  when the property is set, before the existing BigDecimal-column check. The factory reads the property via
  `elementType.getMetaModelContext().getConfiguration().viewer().wicket()`.
- Do not use an environment-variable lookup; do not change the BigDecimal-column behaviour when the property is
  unset.

## Capabilities

### New Capabilities

- `collection-summary-view-config`: defines a configuration property that disables the Wicket collection summary
  view for BigDecimal-bearing collections when set.

## Impact

- Affects `core/config` `CausewayConfiguration.Viewer.Wicket` (one boolean property) and
  `viewers/wicket/ui` `CollectionContentsAsSummaryFactory` (one short-circuit in `appliesTo`).
- v4-idiomatic replacement for the v2 `getenv` escape hatch; no env-var lookup is introduced.
- Requires coverage: with the property unset the summary view still applies for a BigDecimal column; with it set
  the factory returns `DOES_NOT_APPLY`.
