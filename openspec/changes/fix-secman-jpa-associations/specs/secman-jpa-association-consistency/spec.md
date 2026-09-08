## ADDED Requirements

### Requirement: Consistent JPA user-role assignment
The system SHALL update both `ApplicationUser.roles` and `ApplicationRole.users` when a Secman role is assigned to or removed from a user, and the owning user-side change SHALL be preserved after the persistence context is flushed and reloaded.

#### Scenario: Assign a role to a user
- **WHEN** a role is assigned to a user through the supported Secman relationship operation
- **THEN** the managed user contains the role, the managed role contains the user, and both associations remain present after flush and reload

#### Scenario: Remove a role from a user
- **WHEN** an existing role assignment is removed through the supported Secman relationship operation
- **THEN** the managed user no longer contains the role, the managed role no longer contains the user, and the association remains absent after flush and reload

### Requirement: Safe deletion of an assigned JPA role
The system SHALL remove every user assignment from the owning `ApplicationUser.roles` collections before deleting an application role.

#### Scenario: Delete a role assigned to multiple users
- **WHEN** an application role assigned to multiple users is deleted
- **THEN** the role is deleted without a join-table constraint failure and none of the reloaded users contains the deleted role

### Requirement: Consistent JPA tenancy hierarchy assignment
The system SHALL update both the owning child parent reference and the inverse parent children collection when a Secman tenancy parent is assigned, cleared, or changed.

#### Scenario: Assign a parent to a root tenancy
- **WHEN** a parent is assigned to a root tenancy
- **THEN** the managed child references the parent, the managed parent contains the child, and both associations remain present after flush and reload

#### Scenario: Clear a tenancy parent
- **WHEN** the parent of a tenancy is cleared
- **THEN** the managed child has no parent, the former parent no longer contains the child, and that state remains after flush and reload

#### Scenario: Reassign a tenancy to a different parent
- **WHEN** a tenancy with an existing parent is assigned to a different parent
- **THEN** the child references the new parent, the former parent does not contain the child, the new parent contains the child, and that state remains after flush and reload

#### Scenario: Repeat an existing tenancy assignment
- **WHEN** a tenancy is assigned again to its current parent or an already-root tenancy is cleared again
- **THEN** the operation completes without duplicate children or inconsistent parent references

### Requirement: Compatibility-preserving association correction
The system SHALL preserve the existing Secman association mappings and public collection getter contracts while correcting the supported JPA mutation paths.

#### Scenario: Use existing Secman persistence mappings
- **WHEN** the corrected Secman JPA association operations are deployed
- **THEN** no database schema migration, new dependency, or intentional breaking public API change is required
