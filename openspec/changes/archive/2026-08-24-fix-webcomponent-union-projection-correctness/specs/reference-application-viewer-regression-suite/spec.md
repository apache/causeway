## ADDED Requirements

### Requirement: Polymorphic projection regression coverage
The pinned Reference Application suite SHALL distinguish valid concrete and union projection from a runtime object that is not a member of its advertised union.
It MUST preserve deterministic concrete-row evidence, bounded schema mismatch behavior, and the separate opaque-route gap.

#### Scenario: Declared type-of collection is activated
- **WHEN** the browser activates `demo.CollectionTypeOfPage.children`, whose `typeOf` metadata supplies the concrete row type
- **THEN** concrete `demo.CollectionTypeOfChildVm` rows render their semantic identities through the ordinary concrete projection
- **AND** union probing is not used for that collection

#### Scenario: Raw collection exposes incompatible runtime rows
- **WHEN** the companion raw collection is advertised as `rich__demo_ValueHolder__gqlv_union` but returns `demo.CollectionTypeOfChildVm`, which does not implement `demo.ValueHolder`
- **THEN** GraphQL and the viewer retain a bounded local schema/runtime mismatch
- **AND** the viewer does not manufacture union membership or an application-specific fragment

#### Scenario: Generated union membership is completed incrementally
- **WHEN** the Reference Application schema repeatedly discovers concrete `demo.ValueHolder` implementations
- **THEN** introspection advertises the deterministic merged set rather than only the first registration
- **AND** every advertised fragment name is a valid member of the completed union

#### Scenario: Invalid direct union metadata is rejected
- **WHEN** a reproduction selects `_meta` directly from `rich__demo_ValueHolder__gqlv_union`
- **THEN** GraphQL rejects that operation as invalid
- **AND** valid viewer planning uses typename and concrete fragments only

#### Scenario: Existing collection behavior remains stable
- **WHEN** the full collection journey also reaches concrete versionless rows, configured collections, lazy tabs, stale windows, partial errors, and route replacement
- **THEN** those established behaviors retain their passing assertions
- **AND** polymorphic planning does not make inactive collections eager

#### Scenario: Opaque composite route remains separate
- **WHEN** a projected concrete result carries the long opaque identifier retained by the corpus
- **THEN** the suite continues to assert the focused `invalid-route` gap
- **AND** does not attribute route rejection to union projection
