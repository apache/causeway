## 1. JPA Association Regression Coverage

- [ ] 1.1 Add Secman JPA integration-test bootstrapping and reusable fixture setup for users, roles, and tenancies.
- [ ] 1.2 Add integration tests for role assignment and removal that assert both managed sides and then flush, clear, reload, and assert persisted state.
- [ ] 1.3 Add an integration test that deletes a role assigned to multiple users and verifies that deletion succeeds and reloaded users have no residual assignment.
- [ ] 1.4 Add integration tests for tenancy parent assignment and clearing that assert both managed sides and persisted state after flush and reload.
- [ ] 1.5 Add integration tests for tenancy reassignment and repeated idempotent operations, including removal from the former parent's children collection.

## 2. Role Association Corrections

- [ ] 2.1 Update the JPA role-side helper so assigning a user also updates the owning user roles collection without recursive coordination.
- [ ] 2.2 Update role deletion to snapshot assigned users and remove each assignment through the existing owning-side relationship operation before deleting permissions and the role.

## 3. Tenancy Association Corrections

- [ ] 3.1 Update tenancy parent assignment orchestration to detach from a different former parent and synchronize the child reference with the new parent's children collection.
- [ ] 3.2 Update tenancy parent clearing and remaining JPA tenancy association helpers so they cannot leave an inverse-only in-memory change while the owning foreign key remains unchanged.
- [ ] 3.3 Verify that same-parent assignment and repeated clearing remain idempotent and do not introduce recursive helper calls.

## 4. Validation

- [ ] 4.1 Run the focused Secman JPA integration tests and resolve any failures.
- [ ] 4.2 Build the affected Secman applib and JPA persistence modules with their tests to verify JDO-compatible shared API compilation and absence of regressions.
