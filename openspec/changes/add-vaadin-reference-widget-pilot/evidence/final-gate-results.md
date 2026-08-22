# Final pilot gate results

| Gate | Result | Evidence |
|---|---|---|
| Strict CSP | PASS | Four exact `style-src-elem` hashes, explicit `style-src-attr 'none'`, zero matrix and real-viewer violations, and no `unsafe-inline`. |
| GraphQL authority | PASS | Existing public prepare, choices, autocomplete, validate, update, and invoke operations remain authoritative; no endpoint or operation shape was added. |
| Stable Causeway contract | PASS | Semantic elements and events remain application-facing; Vaadin tags and events remain internal. |
| Single reference | PASS | Fixed and searched keyboard selection preserve public logical type and bookmark identity. |
| Multi reference | PASS for supported list inputs | Deterministic selected identity order, token add and keyboard removal, validation, and fallback are covered; unsupported shapes retain the existing editor. |
| Autocomplete honesty | PASS | Search is debounced and cancellable, stale results are rejected, complete responses are bounded, and no server paging is claimed. |
| Route-lazy delivery | PASS | Zero candidate requests before an eligible editor and exactly one shared candidate request afterward. |
| Free-core licensing | PASS | 19 runtime packages have pinned integrity and packaged licenses; all 12 Vaadin runtime packages are Apache-2.0 and no Pro package is present. |
| Vulnerabilities | PASS | Production npm audit reports zero vulnerabilities at every severity. |
| Telemetry and external requests | PASS | Collector import is aliased to Vaadin's no-op module, prohibited markers are absent, and browser journeys make zero external request. |
| Maven packaging | PASS | Normal builds verify deterministic assets and package same-origin resources, licenses, and metadata in the foundation JAR. |
| Bundle budget | PASS | 48,684 bytes gzip against a 66,560-byte limit. |
| Accessibility | PASS | Zero axe violations in five retained modes, successful keyboard and focus journeys, and only forced-color contrast remains manually incomplete. |
| Responsive and themes | PASS | Zero overflow across desktop, narrow, light, dark, reduced-motion, and forced-color evidence. |
| Lifecycle | PASS | Aborted stale search, disconnect release, route replacement, overlay cleanup, Escape cancellation, and focus restoration pass. |
| Existing foundation and HTMX tests | PASS | 111 foundation Node tests and HTMX route tests pass through Maven. |
| Petclinic regression | PASS | Existing integration and headless Playwright suites pass with the pilot enabled. |
| Vanilla sample | PASS | Integration test and live headless reference selection pass without GraphQL or browser failure. |
| Rollback | PASS | One configuration flag restores native editors, original CSP, and zero candidate requests without API or data migration. |

## Decision

Retain the implementation as an optional supported pilot and sample qualification path.
Do not make it the viewer-wide default in this change.
Promotion requires a later compatibility review after real application experience and must repeat every gate above.
