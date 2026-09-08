## 1. Role and User Associations

- [x] 1.1 Add JPA integration-test bootstrapping and reusable fixture setup for application users and roles.
- [x] 1.2 Add integration tests for role assignment and removal that assert both managed sides and then flush, clear, reload, and assert persisted state.
- [x] 1.3 Update the JPA role-side helper so assigning a user also updates the owning user roles collection without recursive coordination.
- [x] 1.4 Add an integration test that deletes a role assigned to multiple users and verifies that deletion succeeds and reloaded users have no residual assignment.
- [x] 1.5 Update role deletion to snapshot assigned users and remove each assignment through the existing owning-side relationship operation before deleting permissions and the role.

## 2. Tenancy Parent and Child Associations

- [x] 2.1 Add JPA integration-test bootstrapping and reusable fixture setup for application tenancies.
- [x] 2.2 Add integration tests for parent assignment and clearing that assert both managed sides and persisted state after flush and reload.
- [x] 2.3 Update parent assignment and clearing so the child reference and the parent's children collection remain synchronized.
- [x] 2.4 Add integration tests for reassignment and repeated idempotent operations, including removal from the former parent's children collection.
- [x] 2.5 Update reassignment and the remaining JPA tenancy association helpers so they cannot leave an inverse-only change or introduce recursive calls.

## 3. Validation

- [x] 3.1 Run the focused Secman JPA integration tests and resolve any failures.
- [x] 3.2 Build the affected Secman applib and JPA persistence modules with their tests to verify JDO-compatible shared API compilation and absence of regressions.
