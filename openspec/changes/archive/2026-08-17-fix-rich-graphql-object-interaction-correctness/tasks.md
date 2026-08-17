## 1. Pin Correctness Fixtures

- [x] 1.1 Translate matrix entries `REF-OBJECT-02`, `REF-PROP-02`, `REF-ACTION-03`, `REF-ACTION-04`, and `REF-COLLECTION-03` into reduced deterministic test fixtures.
- [x] 1.2 Record existing successful concrete-object and scalar-interaction documents as compatibility fixtures.

## 2. Correct Identity and Polymorphic Output

- [x] 2.1 Include every supported assignable concrete public logical type in generated object-input choices.
- [x] 2.2 Validate logical type and identifier pairs without exposing implementation classes or unauthorized state.
- [x] 2.3 Generate and resolve a polymorphic rich output for abstract declared object types.
- [x] 2.4 Restore typed reads for abstract-element collections and action results.

## 3. Unify Argument Conversion

- [x] 3.1 Implement one recursive declared-type converter for nullable, scalar, object-reference, and collection-valued arguments.
- [x] 3.2 Use converted arguments for defaults, choices, autocomplete, per-parameter validation, all-arguments validation, and invocation.
- [x] 3.3 Preserve omitted-versus-null semantics and prefix-argument negotiation.
- [x] 3.4 Replace assertions and null-map failures with bounded GraphQL coercion or validation errors.

## 4. Correct Property Mutation Results

- [x] 4.1 Return the mutated pojo in the output form expected by rich member fetchers.
- [x] 4.2 Regenerate memento view-model identity from authoritative updated state.
- [x] 4.3 Preserve persistent entity identity and existing authorization, usability, and validation enforcement.

## 5. Verify and Document

- [x] 5.1 Add schema, query, mutation, negotiation, invalid-reference, stale-reference, and authorization tests.
- [x] 5.2 Measure generated schema and startup impact for the polymorphic additions.
- [x] 5.3 Document identity inputs, polymorphic selection, collection arguments, mutation results, errors, and compatibility.
- [x] 5.4 Run GraphQL model and viewer tests, reference-derived fixtures, documentation checks, formatting, and strict OpenSpec validation.
