## 1. Core semantic metadata

- [x] 1.1 Add the core member-name formatter and tests for casing, full-name retention, namespace shortening, truncation and canonical-name collisions; omit Wicket-only helpers.
- [x] 1.2 Resolve mixed-in associations from invocation facet and actual metamodel linkage in ActionExecutor; cover property, collection, multiple contributions and missing lookup.
- [x] 1.3 Select semantic observation category/context and full category-specific identifier in MemberExecutorServiceDefault, reusing domain action identity while retaining main action operation and existing generic metadata.

## 2. Behavior and export validation

- [x] 2.1 Add runtime tests for ordinary/mixed-in actions and associations, complete action parameter signatures, fallback, unchanged invocation/DTO behavior and error cleanup.
- [x] 2.2 Extend real exported Boot/agent fixtures for act/prop/coll names, exact identifier attributes, uppercase and long/colliding names, one span per invocation and HTTP/framework/JDBC ancestry.
- [x] 2.3 Verify observation inactive, missing exporter, failure-followed-by-success and custom registry/tracer behavior; confirm existing handlers preserve naming before considering any scoped correction.

## 3. Operator guidance and delivery checks

- [x] 3.1 Merge the new M3 capability into the existing otel-howto and observability guide with verified examples, full-identifier Jaeger searches and operation/context distinctions; omit before/after comparisons and migration tables, retaining the Boot-first then agent structure.
- [x] 3.2 Run focused naming/metamodel/runtime checks, relevant interact regressions, exported-trace compatibility suite and documentation rendering; record commands, baseline versions, results and remaining limits in validation.md.
