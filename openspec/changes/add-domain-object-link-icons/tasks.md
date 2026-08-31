## 1. Rich GraphQL breadcrumb metadata

- [ ] 1.1 Add policy-aware nullable icon metadata to the shared navigable breadcrumb entry and derive ancestor URLs through the existing configured metadata resource path.
- [ ] 1.2 Extend breadcrumb resolver, schema, introspection, and traversal tests for enabled, unavailable, and forbidden icon metadata.

## 2. Semantic object-link presentation

- [ ] 2.1 Extend `<cw-object-link>` with optional decorative icon rendering, bounded failure fallback, and unchanged semantic navigation payload behavior.
- [ ] 2.2 Add shared responsive icon sizing and alignment styles and document the public attribute, classes, and theme variables.
- [ ] 2.3 Render `<cw-object-header>` titles as same-object semantic links while preserving one heading and the existing secondary identity.

## 3. Targeted icon propagation

- [ ] 3.1 Include advertised icon fields in header and reusable object-result metadata selections without breaking older or restricted schemas.
- [ ] 3.2 Propagate returned icon metadata through property-reference, collection list/grid, hydrated-row, and breadcrumb object links.
- [ ] 3.3 Add foundation tests for targeted selections, current-object self-links, property and collection links, breadcrumb links, decorative accessibility, and missing or failed icon fallback.

## 4. Application verification

- [ ] 4.1 Extend Petclinic browser coverage for icon-bearing current-object, property-reference, collection-row, and breadcrumb links plus canonical self-navigation.
- [ ] 4.2 Run focused GraphQL tests, foundation Node and Maven tests, Petclinic browser tests, and strict OpenSpec validation; resolve any regressions.
