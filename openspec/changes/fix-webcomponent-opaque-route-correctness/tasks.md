## 1. Establish route-bound evidence

- [ ] 1.1 Capture the authoritative `demo.CompositeValuesPage` identifier length and canonical encoded length without recording its protected content.
- [ ] 1.2 Record the current browser and Java route validation units, accepted bound, and failure boundary.
- [ ] 1.3 Define shared valid, boundary, overflow, Unicode, reserved-character, and malformed route vectors.

## 2. Correct the Java route codec

- [ ] 2.1 Replace the decoded UTF-16 character limit with bounded UTF-8 and canonical encoded-size validation.
- [ ] 2.2 Preserve exact UTF-8 percent encoding and decode-then-reencode canonicality.
- [ ] 2.3 Preserve rejection of empty values, dot segments, separators, controls, malformed UTF-8, malformed escapes, and non-canonical encodings.
- [ ] 2.4 Preserve the bounded complete-path check before segment decoding.
- [ ] 2.5 Keep invalid-route exceptions generic and free of submitted route content.

## 3. Correct the browser route codec

- [ ] 3.1 Apply the same UTF-8 and final encoded-size bounds before returning a canonical segment.
- [ ] 3.2 Detect unpaired UTF-16 surrogates and convert native encoding failures into the bounded route error.
- [ ] 3.3 Preserve the existing percent-encoding grammar for reserved punctuation.
- [ ] 3.4 Keep canonical object paths independent of identity meaning and route producer.

## 4. Add codec and controller verification

- [ ] 4.1 Add Java round-trip coverage for long ASCII-oriented opaque identifiers within the encoded bound.
- [ ] 4.2 Add Java exact-bound and one-character-overflow coverage.
- [ ] 4.3 Add Java multibyte, malformed Unicode, canonical escape, separator, control, and whole-path bound coverage.
- [ ] 4.4 Add browser vectors equivalent to the Java accepted and rejected cases.
- [ ] 4.5 Add browser exact-bound and encoded-expansion overflow coverage.
- [ ] 4.6 Verify full-page, HTMX fragment, context-path, and history-restore responses preserve a valid long identifier exactly.
- [ ] 4.7 Verify invalid long or malformed routes still return non-disclosing `invalid-route` and canonical root history behavior.

## 5. Qualify the pinned Reference Application

- [ ] 5.1 Add focused integration evidence for the composite-values action result metadata and identifier bounds.
- [ ] 5.2 Verify the server codec round-trips the exact GraphQL-returned composite identifier.
- [ ] 5.3 Replace the browser `invalid-route` expectation with a ready or documented partial object-state assertion.
- [ ] 5.4 Assert the rendered object context retains the exact returned logical type and identifier.
- [ ] 5.5 Assert representative composite-value content remains visible after reconstruction.
- [ ] 5.6 Exercise semantic route replacement to the long identifier rather than only a manually assembled URL.
- [ ] 5.7 Exercise back and forward restoration around the long route and reject stale replacement.
- [ ] 5.8 Retain focused malformed-route and unrelated raw-union mismatch assertions.
- [ ] 5.9 Verify clean and incremental capability inventory generation remain byte-identical.

## 6. Preserve compatibility and isolation

- [ ] 6.1 Verify ordinary Petclinic direct, semantic, result, home, custom, and generic routes remain canonical.
- [ ] 6.2 Verify configured non-root base paths and context paths remain correct.
- [ ] 6.3 Verify public semantic navigation and result event payloads remain unchanged.
- [ ] 6.4 Verify no GraphQL, metamodel bookmark, dependency, asset, CSP, Vaadin, or native fallback policy changes.
- [ ] 6.5 Verify rejected route values do not enter HTML, headers, errors, diagnostics, or test evidence.

## 7. Document the corrected contract

- [ ] 7.1 Update HTMX route documentation with byte-oriented and encoded-segment bounds.
- [ ] 7.2 Add a route-bound and canonicalization matrix with safe measured Reference Application evidence.
- [ ] 7.3 Add reproducible focused and full qualification commands.
- [ ] 7.4 Add implementation, compatibility, rollback, and retained-gap evidence.
- [ ] 7.5 Record final gate results and the next recommended change.

## 8. Run final qualification gates

- [ ] 8.1 Run HTMX Java, browser route-policy, foundation, and web-component Maven suites.
- [ ] 8.2 Run the full Petclinic integration and Playwright suites.
- [ ] 8.3 Run the Reference Application clean package, integration, inventory, and Playwright suites.
- [ ] 8.4 Run strict CSP, accessibility, keyboard, responsive, theme, external-request, console-error, page-error, and overflow gates.
- [ ] 8.5 Run applicable RAT checks, strict OpenSpec validation, `git diff --check`, and production-isolation verification.
