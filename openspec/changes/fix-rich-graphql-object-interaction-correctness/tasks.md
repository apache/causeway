## 1. Pin Correctness Fixtures

- [ ] 1.1 Translate matrix entries `REF-OBJECT-02`, `REF-PROP-02`, `REF-ACTION-03`, `REF-ACTION-04`, and `REF-COLLECTION-03` into reduced deterministic test fixtures.
- [ ] 1.2 Record existing successful concrete-object and scalar-interaction documents as compatibility fixtures.

## 2. Correct Identity and Polymorphic Output

- [ ] 2.1 Include every supported assignable concrete public logical type in generated object-input choices.
- [ ] 2.2 Validate logical type and identifier pairs without exposing implementation classes or unauthorized state.
- [ ] 2.3 Generate and resolve a polymorphic rich output for abstract declared object types.
- [ ] 2.4 Restore typed reads for abstract-element collections and action results.

## 3. Unify Argument Conversion

- [ ] 3.1 Implement one recursive declared-type converter for nullable, scalar, object-reference, and collection-valued arguments.
- [ ] 3.2 Use converted arguments for defaults, choices, autocomplete, per-parameter validation, all-arguments validation, and invocation.
- [ ] 3.3 Preserve omitted-versus-null semantics and prefix-argument negotiation.
- [ ] 3.4 Replace assertions and null-map failures with bounded GraphQL coercion or validation errors.

## 4. Correct Property Mutation Results

- [ ] 4.1 Return the mutated pojo in the output form expected by rich member fetchers.
- [ ] 4.2 Regenerate memento view-model identity from authoritative updated state.
- [ ] 4.3 Preserve persistent entity identity and existing authorization, usability, and validation enforcement.

## 5. Verify and Document

- [ ] 5.1 Add schema, query, mutation, negotiation, invalid-reference, stale-reference, and authorization tests.
- [ ] 5.2 Measure generated schema and startup impact for the polymorphic additions.
- [ ] 5.3 Document identity inputs, polymorphic selection, collection arguments, mutation results, errors, and compatibility.
- [ ] 5.4 Run GraphQL model and viewer tests, reference-derived fixtures, documentation checks, formatting, and strict OpenSpec validation.
