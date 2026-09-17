## 1. Role and User Associations

- [ ] 1.1 Add v4 Secman JPA integration-test bootstrapping and reusable fixtures for application users and roles.
- [ ] 1.2 Add an integration test for role assignment and removal that asserts both managed sides and persisted state after flush and reload.
- [ ] 1.3 Update the JPA role-side helper so assigning a user also updates the owning user roles collection without recursive coordination.
- [ ] 1.4 Add an integration test that deletes a role assigned to multiple users and verifies that reloaded users have no residual assignment.
- [ ] 1.5 Update role deletion to snapshot assigned users and remove each assignment through the existing owning-side relationship operation before deleting the role.

## 2. Tenancy Parent and Child Associations

- [ ] 2.1 Add v4 Secman JPA integration-test bootstrapping and reusable fixtures for application tenancies.
- [ ] 2.2 Add integration coverage for parent assignment and clearing across a flush-and-reload boundary.
- [ ] 2.3 Update parent assignment and clearing so the child reference and parent children collection remain synchronized.
- [ ] 2.4 Add integration coverage for reassignment and repeated idempotent operations, including removal from the former parent's children collection.
- [ ] 2.5 Update the remaining JPA tenancy association helpers so they cannot leave an inverse-only change or introduce recursive calls.

## 3. Validation

- [ ] 3.1 Run the targeted Secman JPA integration tests and resolve any v4 bootstrap or persistence-context differences.
- [ ] 3.2 Build the affected Secman applib and persistence-jpa modules to verify source and test compatibility.
