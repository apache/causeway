## 1. Pin the Application-Entry Contract

- [ ] 1.1 Translate matrix entries `REF-MENU-01` and `REF-HOME-01` into reduced explicit-layout, fallback-layout, and home-object fixtures.
- [ ] 1.2 Define the targeted application-entry field, menu resource metadata, home kinds, localization, caching, and diagnostics.
- [ ] 1.3 Verify object-interaction and structural-resource prerequisites are complete.

## 2. Implement Menu-Bar Discovery

- [ ] 2.1 Add targeted root capability discovery and secured effective menu-bars resource retrieval.
- [ ] 2.2 Preserve primary, secondary, tertiary, menu, section, service-action, and ordering semantics in the canonical resource.
- [ ] 2.3 Map entries to logical service type and semantic action ID without another invocation endpoint.
- [ ] 2.4 Handle generated fallback, absent, forbidden, malformed, stale, hidden, and partially invalid layout data.

## 3. Implement Home-Page Discovery

- [ ] 3.1 Detect whether the configured home entry is a domain object or supported service action.
- [ ] 3.2 Resolve an object home page through the framework home-page service and corrected polymorphic rich output.
- [ ] 3.3 Resolve an action home page through the existing service-action contract.
- [ ] 3.4 Define absence for missing, hidden, invalid, parameterized, or unresolvable home entries.

## 4. Security, Verification, and Documentation

- [ ] 4.1 Scope resources and caches by authorization, locale, layout generation, and other menu-affecting context.
- [ ] 4.2 Test three bars, ordering, empty bars, hidden and disabled actions, localization, malformed references, fallback menus, home objects, and supported home actions.
- [ ] 4.3 Add schema compatibility, standard targeted-introspection, and resource-link tests.
- [ ] 4.4 Document the application-entry contract, canonical resource, service-action relationship, home kinds, caching, authorization, and viewer-policy boundary.
- [ ] 4.5 Run GraphQL, security, compatibility, documentation, formatting, and strict OpenSpec validation checks.
