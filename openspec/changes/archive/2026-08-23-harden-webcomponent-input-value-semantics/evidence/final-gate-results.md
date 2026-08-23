# Final gate results

## Passed

- Foundation Node suite: 119 tests passed with zero failures.
- GraphQL model reactor and all model tests: `BUILD SUCCESS`.
- GraphQL resource, protected-value, unsupported-value, and structural-resource integration tests: `BUILD SUCCESS`.
- Web-component foundation and HTMX Maven tests: `BUILD SUCCESS`.
- Petclinic Playwright suite: 4 tests passed with zero failures, errors, or skips.
- Reference Application ordinary clean package: `BUILD SUCCESS`.
- Reference Application Playwright suite: 9 tests passed with zero failures, errors, or skips.
- Reference Application capability inventory: 4,286 items, 3,421 `SUPPORTED`, 210 `GRACEFUL_UNSUPPORTED`, zero `GRAPHQL_GAP`, zero `VIEWER_DEFECT`, one `VIEWER_SPECIFIC`, and 654 reasoned `NOT_EXERCISED`.
- Vaadin production-like pilot: 5 scenarios with zero axe, CSP, console, page, external-request, or overflow failures.
- GraphQL model and Reference Application RAT checks: `BUILD SUCCESS`.
- Strict OpenSpec validation and `git diff --check`: passed.

## Security and compatibility observations

Protected input did not appear in rendered value attributes, semantic prompt event values, bounded errors, or captured output.
Malformed exact numeric, temporal, URL, resource, and unsupported custom input remained bounded and did not expose submitted values or implementation exceptions.
No dependency file, operation name, route, public Causeway element, semantic event name, browser asset URL, Vaadin bundle, CSP hash, or default-selection policy changed.
The direct standalone foundation RAT goal continues to report the pre-existing generated Vaadin bundle and its README as unknown-license files; neither is introduced or changed by this implementation, and all newly added source files carry the ASF header.
