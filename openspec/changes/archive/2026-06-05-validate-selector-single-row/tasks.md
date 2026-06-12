## 1. Matching and Validation Support

- [x] 1.1 Extract selector row matching into shared helper logic usable by validation and invocation.
- [x] 1.2 Add an action validation facet for synthetic parented collection selector actions.
- [x] 1.3 Return clear validation messages when the parent argument is missing, no collection row matches, or multiple collection rows match.

## 2. Synthetic Action Wiring

- [x] 2.1 Install the selector action validation facet when synthetic selector actions are created.
- [x] 2.2 Keep invocation-time no-match and multiple-match checks as defensive safeguards for callers that bypass validation.

## 3. Tests and Verification

- [x] 3.1 Add tests that validation allows invocation when selector parameters identify exactly one row.
- [x] 3.2 Add tests that validation rejects no-row matches and prevents invocation.
- [x] 3.3 Add tests that validation rejects multiple-row matches and prevents invocation.
- [x] 3.4 Add tests that direct invocation still fails clearly when validation is bypassed for no-row and multiple-row matches.
- [x] 3.5 Run the focused metamodel test suite for parented collection selector actions.
- [x] 3.6 Run `openspec validate validate-selector-single-row --strict`.
