## ADDED Requirements

### Requirement: Polymorphic projection regression coverage
The pinned Reference Application suite SHALL exercise representative abstract property and collection values through valid concrete GraphQL fragments.
It MUST preserve deterministic concrete-row identity and member evidence, bounded partial behavior, and the separate opaque-route gap.

#### Scenario: Type-of collection is activated
- **WHEN** the browser activates the representative `demo.CollectionTypeOfPage` collection exposed through `rich__demo_ValueHolder__gqlv_union`
- **THEN** the viewer reaches a supported ready or documented partial state without selecting `_meta` directly on the union
- **AND** concrete `demo.CollectionTypeOfChildVm` rows render their semantic identities and advertised values

#### Scenario: Raw collection lacks a declared Java generic
- **WHEN** the companion collection relies on runtime union typenames rather than a Java generic signature
- **THEN** the same bounded probe and fragment policy applies
- **AND** no application-specific concrete-type list is embedded in the viewer

#### Scenario: Polymorphic operation remains bounded
- **WHEN** the Reference Application union advertises more possible types than are present in one collection window
- **THEN** evidence records the advertised count, observed concrete types, introspection operations, and final fragment operation
- **AND** the viewer describes and selects only the bounded required concrete closure

#### Scenario: Existing collection behavior remains stable
- **WHEN** the full collection journey also reaches concrete versionless rows, configured columns, lazy tabs, stale windows, partial errors, and route replacement
- **THEN** those established behaviors retain their passing assertions
- **AND** polymorphic probing does not make inactive collections eager

#### Scenario: Opaque composite route remains separate
- **WHEN** a projected concrete result carries the long opaque identifier retained by the corpus
- **THEN** the suite continues to assert the focused `invalid-route` gap
- **AND** does not attribute route rejection to union projection
