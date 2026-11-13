## Why

The executable reference-application analysis found one `@HomePage` view model and a three-level menu-bars layout containing 14 menus, 58 sections, and 197 service-action entries.
The rich root exposes 58 service-menu types but no menu-bar entry point and no indication that `demo.Homepage` is the configured home object.
The normal home object lookup still requires identity and fails when called without it.

The evidence is recorded in `coverage-matrix.yaml` entries `REF-MENU-01` and `REF-HOME-01`.

## What Changes

- Add a targeted rich GraphQL application-entry root capability.
- Return an authorized reference to the effective menu-bars layout as the canonical structural source.
- Preserve primary, secondary, tertiary, menu, section, ordering, and service-action references in that resource rather than duplicating them as a second GraphQL tree.
- Resolve each menu entry to the existing rich service-action contract.
- Identify and resolve the domain object returned by the configured `HomePageResolverService` without requiring a client-supplied identifier.
- Define authorization filtering, empty-menu behavior, private no-store delivery, localization, bounded malformed-layout diagnostics, and generated fallback behavior.
- Avoid prescribing frontend rendering, automatic home invocation, routing, or result navigation.

## Capabilities

### New Capabilities

- `rich-graphql-application-entry-points`: Defines framework-neutral rich GraphQL discovery of Causeway menu bars and configured home-page semantics.

### Modified Capabilities

None.

## Impact

- Affects rich GraphQL root metadata, secured menu-layout resource exposure, object home-page resolution, service-action references, tests, and documentation.
- Depends on completed reference-app analysis, corrected object interaction and polymorphic output, and corrected structural resource links.
- Is a prerequisite for `<causeway-menubars>` and the generic application shell.
- Does not depend on broad rich member metadata and does not implement menus, routes, authentication screens, a viewer shell, or a speculative home-action configuration API.
