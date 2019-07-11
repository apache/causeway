/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.extensions.secman.jdo.dom.role;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.InheritanceStrategy;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Bounding;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.types.DescriptionType;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.extensions.secman.api.SecurityModule;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionMode;
import org.apache.isis.extensions.secman.api.permission.ApplicationPermissionRule;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission;
import org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUser;
import org.apache.isis.extensions.secman.jdo.dom.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.jdo.seed.scripts.IsisModuleSecurityAdminRoleAndPermissions;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeature;
import org.apache.isis.metamodel.services.appfeat.ApplicationFeatureType;

import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("UnusedDeclaration")
@javax.jdo.annotations.PersistenceCapable(
        identityType = IdentityType.DATASTORE,
        schema = "isissecurity",
        table = "ApplicationRole")
@javax.jdo.annotations.Inheritance(
        strategy = InheritanceStrategy.NEW_TABLE)
@javax.jdo.annotations.DatastoreIdentity(
        strategy = IdGeneratorStrategy.NATIVE, column = "id")
@javax.jdo.annotations.Uniques({
        @javax.jdo.annotations.Unique(
                name = "ApplicationRole_name_UNQ", members = { "name" })
})
@javax.jdo.annotations.Queries({
        @javax.jdo.annotations.Query(
                name = "findByName", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole "
                        + "WHERE name == :name"),
        @javax.jdo.annotations.Query(
                name = "findByNameContaining", language = "JDOQL",
                value = "SELECT "
                        + "FROM org.apache.isis.extensions.secman.jdo.dom.role.ApplicationRole "
                        + "WHERE name.matches(:nameRegex) ")
})
@DomainObject(
		bounding = Bounding.BOUNDED,
//		bounded = true,
        objectType = "isissecurity.ApplicationRole",
        autoCompleteRepository = ApplicationRoleRepository.class,
        autoCompleteAction = "findMatching"
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT
)
public class ApplicationRole implements Comparable<ApplicationRole> {

    public static abstract class PropertyDomainEvent<T> extends SecurityModule.PropertyDomainEvent<ApplicationRole, T> {
		private static final long serialVersionUID = 1L;}

    public static abstract class CollectionDomainEvent<T> extends SecurityModule.CollectionDomainEvent<ApplicationRole, T> {
		private static final long serialVersionUID = 1L;}

    public static abstract class ActionDomainEvent extends SecurityModule.ActionDomainEvent<ApplicationRole> {
		private static final long serialVersionUID = 1L;}

    

    // -- constants

    public static final int MAX_LENGTH_NAME = 50;
    public static final int TYPICAL_LENGTH_NAME = 30;
    public static final int TYPICAL_LENGTH_DESCRIPTION = 50;
    

    // -- identification
    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    public String title() {
        return getName();
    }
    

    // -- name (property)

    public static class NameDomainEvent extends PropertyDomainEvent<String> {
		private static final long serialVersionUID = 1L;}


    @javax.jdo.annotations.Column(allowsNull="false", length = MAX_LENGTH_NAME)
    @Property(
            domainEvent = NameDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(typicalLength=TYPICAL_LENGTH_NAME)
    @MemberOrder(sequence = "1")
    @Getter @Setter
    private String name;

    

    // -- updateName (action)

    public static class UpdateNameDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = UpdateNameDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="name", sequence = "1")
    public ApplicationRole updateName(
            @Parameter(maxLength = MAX_LENGTH_NAME) @ParameterLayout(named="Name", typicalLength = TYPICAL_LENGTH_NAME)
            final String name) {
        setName(name);
        return this;
    }

    public String default0UpdateName() {
        return getName();
    }

    

    // -- description (property)

    public static class DescriptionDomainEvent extends PropertyDomainEvent<String> {
		private static final long serialVersionUID = 1L;}


    @javax.jdo.annotations.Column(allowsNull="true", length = DescriptionType.Meta.MAX_LEN)
    @Property(
            domainEvent = DescriptionDomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            typicalLength=TYPICAL_LENGTH_DESCRIPTION
    )
    @MemberOrder(sequence = "2")
    @Getter @Setter
    private String description;

    

    // -- updateDescription (action)

    public static class UpdateDescriptionDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = UpdateDescriptionDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name="description", sequence = "1")
    public ApplicationRole updateDescription(
            @Parameter(
                    maxLength = DescriptionType.Meta.MAX_LEN,
                    optionality = Optionality.OPTIONAL
            )
            @ParameterLayout(named="Description", typicalLength=TYPICAL_LENGTH_DESCRIPTION)
            final String description) {
        setDescription(description);
        return this;
    }

    public String default0UpdateDescription() {
        return getDescription();
    }

    

    // -- permissions (derived collection)
    public static class PermissionsCollectionDomainEvent extends CollectionDomainEvent<ApplicationPermission> {
		private static final long serialVersionUID = 1L;}

    @Collection(
            domainEvent = PermissionsCollectionDomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sortedBy = ApplicationPermission.DefaultComparator.class
    )
    @MemberOrder(sequence = "10")
    public List<ApplicationPermission> getPermissions() {
        return applicationPermissionRepository.findByRole(this);
    }
    

    // -- addPackage (action)

    public static class AddPackageDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    /**
     * Adds a {@link org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission permission} for this role to a
     * {@link ApplicationFeatureType#PACKAGE package}
     * {@link ApplicationFeature feature}.
     */
    @Action(
            domainEvent = AddPackageDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(name = "Permissions", sequence = "1")
    public ApplicationRole addPackage(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            @ParameterLayout(named="Package", typicalLength= ApplicationFeature.TYPICAL_LENGTH_PKG_FQN)
            final String packageFqn) {
        applicationPermissionRepository.newPermission(this, rule, mode, ApplicationFeatureType.PACKAGE, packageFqn);
        return this;
    }

    public ApplicationPermissionRule default0AddPackage() {
        return ApplicationPermissionRule.ALLOW;
    }

    public ApplicationPermissionMode default1AddPackage() {
        return ApplicationPermissionMode.CHANGING;
    }

    public java.util.Collection<String> choices2AddPackage() {
        return applicationFeatureRepository.packageNames();
    }
    

    // -- addClass (action)
    public static class AddClassDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    /**
     * Adds a {@link org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission permission} for this role to a
     * {@link ApplicationFeatureType#MEMBER member}
     * {@link ApplicationFeature feature}.
     */
    @Action(
            domainEvent = AddClassDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(name = "Permissions", sequence = "2")
    public ApplicationRole addClass(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            @ParameterLayout(named="Package", typicalLength=ApplicationFeature.TYPICAL_LENGTH_PKG_FQN)
            final String packageFqn,
            @ParameterLayout(named="Class", typicalLength=ApplicationFeature.TYPICAL_LENGTH_CLS_NAME)
            final String className) {
        applicationPermissionRepository.newPermission(this, rule, mode, ApplicationFeatureType.CLASS,
                packageFqn + "." + className);
        return this;
    }

    public ApplicationPermissionRule default0AddClass() {
        return ApplicationPermissionRule.ALLOW;
    }

    public ApplicationPermissionMode default1AddClass() {
        return ApplicationPermissionMode.CHANGING;
    }

    /**
     * Package names that have classes in them.
     */
    public java.util.Collection<String> choices2AddClass() {
        return applicationFeatureRepository.packageNamesContainingClasses(null);
    }

    /**
     * Class names for selected package.
     */
    public java.util.Collection<String> choices3AddClass(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn) {
        return applicationFeatureRepository.classNamesContainedIn(packageFqn, null);
    }

    

    // -- addAction (action)
    public static class AddActionDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    /**
     * Adds a {@link org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission permission} for this role to a
     * {@link ApplicationMemberType#ACTION action}
     * {@link ApplicationFeatureType#MEMBER member}
     * {@link ApplicationFeature feature}.
     */
    @Action(
            domainEvent = AddActionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(name = "Permissions", sequence = "3")
    public ApplicationRole addAction(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            @ParameterLayout(named="Package", typicalLength=ApplicationFeature.TYPICAL_LENGTH_PKG_FQN)
            final String packageFqn,
            @ParameterLayout(named="Class", typicalLength=ApplicationFeature.TYPICAL_LENGTH_CLS_NAME)
            final String className,
            @ParameterLayout(named="Action", typicalLength = ApplicationFeature.TYPICAL_LENGTH_MEMBER_NAME)
            final String memberName) {
        applicationPermissionRepository.newPermission(this, rule, mode, packageFqn, className, memberName);
        return this;
    }

    public ApplicationPermissionRule default0AddAction() {
        return ApplicationPermissionRule.ALLOW;
    }

    public ApplicationPermissionMode default1AddAction() {
        return ApplicationPermissionMode.CHANGING;
    }

    public java.util.Collection<String> choices2AddAction() {
        return applicationFeatureRepository.packageNamesContainingClasses(ApplicationMemberType.ACTION);
    }

    public java.util.Collection<String> choices3AddAction(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn) {
        return applicationFeatureRepository.classNamesContainedIn(packageFqn, ApplicationMemberType.ACTION);
    }

    public java.util.Collection<String> choices4AddAction(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn,
            final String className) {
        return applicationFeatureRepository.memberNamesOf(packageFqn, className, ApplicationMemberType.ACTION);
    }

    

    // -- addProperty (action)
    public static class AddPropertyDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}
    /**
     * Adds a {@link org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission permission} for this role to a
     * {@link ApplicationMemberType#PROPERTY property}
     * {@link ApplicationFeatureType#MEMBER member}
     * {@link ApplicationFeature feature}.
     */
    @Action(
            domainEvent = AddPropertyDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(name = "Permissions", sequence = "4")
    public ApplicationRole addProperty(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            @ParameterLayout(named="Package", typicalLength=ApplicationFeature.TYPICAL_LENGTH_PKG_FQN)
            final String packageFqn,
            @ParameterLayout(named="Class", typicalLength=ApplicationFeature.TYPICAL_LENGTH_CLS_NAME)
            final String className,
            @ParameterLayout(named="Property", typicalLength=ApplicationFeature.TYPICAL_LENGTH_MEMBER_NAME)
            final String memberName) {
        applicationPermissionRepository.newPermission(this, rule, mode, packageFqn, className, memberName);
        return this;
    }

    public ApplicationPermissionRule default0AddProperty() {
        return ApplicationPermissionRule.ALLOW;
    }

    public ApplicationPermissionMode default1AddProperty() {
        return ApplicationPermissionMode.CHANGING;
    }

    /**
     * Package names that have classes in them.
     */
    public java.util.Collection<String> choices2AddProperty() {
        return applicationFeatureRepository.packageNamesContainingClasses(ApplicationMemberType.PROPERTY);
    }

    /**
     * Class names for selected package.
     */
    public java.util.Collection<String> choices3AddProperty(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn) {
        return applicationFeatureRepository.classNamesContainedIn(packageFqn, ApplicationMemberType.PROPERTY);
    }

    /**
     * Member names for selected class.
     */
    public java.util.Collection<String> choices4AddProperty(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn,
            final String className) {
        return applicationFeatureRepository.memberNamesOf(packageFqn, className, ApplicationMemberType.PROPERTY);
    }
    

    // -- addCollection (action)
    public static class AddCollectionDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    /**
     * Adds a {@link org.apache.isis.extensions.secman.jdo.dom.permission.ApplicationPermission permission} for this role to a
     * {@link ApplicationMemberType#COLLECTION collection}
     * {@link ApplicationFeatureType#MEMBER member}
     * {@link ApplicationFeature feature}.
     */
    @Action(
            domainEvent = AddCollectionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT
    )
    @MemberOrder(name = "Permissions", sequence = "5")
    public ApplicationRole addCollection(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Mode")
            final ApplicationPermissionMode mode,
            @ParameterLayout(named="Package", typicalLength=ApplicationFeature.TYPICAL_LENGTH_PKG_FQN)
            final String packageFqn,
            @ParameterLayout(named="Class", typicalLength=ApplicationFeature.TYPICAL_LENGTH_CLS_NAME)
            final String className,
            @ParameterLayout(named="Collection", typicalLength=ApplicationFeature.TYPICAL_LENGTH_MEMBER_NAME)
            final String memberName) {
        applicationPermissionRepository.newPermission(this, rule, mode, packageFqn, className, memberName);
        return this;
    }

    public ApplicationPermissionRule default0AddCollection() {
        return ApplicationPermissionRule.ALLOW;
    }

    public ApplicationPermissionMode default1AddCollection() {
        return ApplicationPermissionMode.CHANGING;
    }

    public java.util.Collection<String> choices2AddCollection() {
        return applicationFeatureRepository.packageNamesContainingClasses(ApplicationMemberType.COLLECTION);
    }

    public java.util.Collection<String> choices3AddCollection(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn) {
        return applicationFeatureRepository.classNamesContainedIn(packageFqn, ApplicationMemberType.COLLECTION);
    }

    public java.util.Collection<String> choices4AddCollection(
            final ApplicationPermissionRule rule,
            final ApplicationPermissionMode mode,
            final String packageFqn,
            final String className) {
        return applicationFeatureRepository.memberNamesOf(packageFqn, className, ApplicationMemberType.COLLECTION);
    }

    

    // -- removePermission (action)
    public static class RemovePermissionDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent= RemovePermissionDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @MemberOrder(name = "Permissions", sequence = "9")
    public ApplicationRole removePermission(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Type")
            final ApplicationFeatureType type,
            @ParameterLayout(named="Feature", typicalLength=ApplicationFeature.TYPICAL_LENGTH_MEMBER_NAME)
            final String featureFqn) {
        final ApplicationPermission permission = applicationPermissionRepository.findByRoleAndRuleAndFeature(this,
                rule, type, featureFqn);
        if(permission != null) {
        	repository.remove(permission);
        }
        return this;
    }

    public String validateRemovePermission(
            @ParameterLayout(named="Rule")
            final ApplicationPermissionRule rule,
            @ParameterLayout(named="Type")
            final ApplicationFeatureType type,
            @ParameterLayout(named="Feature", typicalLength=ApplicationFeature.TYPICAL_LENGTH_MEMBER_NAME)
            final String featureFqn) {
        if(isAdminRole() && IsisModuleSecurityAdminRoleAndPermissions.oneOf(featureFqn)) {
            return "Cannot remove top-level package permissions for the admin role.";
        }
        return null;
    }
    public ApplicationPermissionRule default0RemovePermission() {
        return ApplicationPermissionRule.ALLOW;
    }
    public ApplicationFeatureType default1RemovePermission() {
        return ApplicationFeatureType.PACKAGE;
    }

    public java.util.Collection<String> choices2RemovePermission(
            final ApplicationPermissionRule rule,
            final ApplicationFeatureType type) {
        final List<ApplicationPermission> permissions = applicationPermissionRepository.findByRoleAndRuleAndFeatureTypeCached(
                this, rule, type);
        return Lists.newArrayList(
                Iterables.transform(
                        permissions,
                        ApplicationPermission.Functions.GET_FQN));
    }

    

    // -- users (collection)

    public static class UsersDomainEvent extends CollectionDomainEvent<ApplicationUser> {
		private static final long serialVersionUID = 1L;}

    @javax.jdo.annotations.Persistent(mappedBy = "roles")
    @Collection(
            domainEvent = UsersDomainEvent.class,
            editing = Editing.DISABLED
    )
    @CollectionLayout(
            defaultView="table"
    )
    @MemberOrder(sequence = "20")
    @Getter @Setter
    private SortedSet<ApplicationUser> users = new TreeSet<>();


    // necessary for integration tests
    public void addToUsers(final ApplicationUser applicationUser) {
        getUsers().add(applicationUser);
    }
    // necessary for integration tests
    public void removeFromUsers(final ApplicationUser applicationUser) {
        getUsers().remove(applicationUser);
    }
    

    // -- addUser (action)

    public static class AddUserDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = AddUserDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
        named="Add")
    @MemberOrder(name="Users", sequence = "1")
    public ApplicationRole addUser(final ApplicationUser applicationUser) {
        applicationUser.addRole(this);
        // no need to add to users set, since will be done by JDO/DN.
        return this;
    }

    public List<ApplicationUser> autoComplete0AddUser(final String search) {
        final List<ApplicationUser> matchingSearch = applicationUserRepository.find(search);
        final List<ApplicationUser> list = Lists.newArrayList(matchingSearch);
        list.removeAll(getUsers());
        return list;
    }

    

    // -- removeUser (action)

    public static class RemoveUserDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = RemoveUserDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT
    )
    @ActionLayout(
            named="Remove"
    )
    @MemberOrder(name="Users", sequence = "2")
    public ApplicationRole removeUser(final ApplicationUser applicationUser) {
        applicationUser.removeRole(this);
        // no need to remove from users set, since will be done by JDO/DN.
        return this;
    }

    public java.util.Collection<ApplicationUser> choices0RemoveUser() {
        return getUsers();
    }

    public String validateRemoveUser(
            final ApplicationUser applicationUser) {
        return applicationUser.validateRemoveRole(this);
    }

    

    // -- delete (action)
    public static class DeleteDomainEvent extends ActionDomainEvent {
		private static final long serialVersionUID = 1L;}

    @Action(
            domainEvent = DeleteDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT_ARE_YOU_SURE
    )
    @MemberOrder(sequence = "1")
    public List<ApplicationRole> delete() {
        getUsers().clear();
        final List<ApplicationPermission> permissions = getPermissions();
        for (final ApplicationPermission permission : permissions) {
            permission.delete();
        }
        repository.removeAndFlush(this);
        return applicationRoleRepository.allRoles();
    }

    public String disableDelete() {
        return isAdminRole() ? "Cannot delete the admin role" : null;
    }

    

    // -- isAdminRole (programmatic)
    @Programmatic
    public boolean isAdminRole() {
        final ApplicationRole adminRole = applicationRoleRepository.findByNameCached(
                IsisModuleSecurityAdminRoleAndPermissions.ROLE_NAME);
        return this == adminRole;
    }
    

    // -- Functions

    public static class Functions {
        private Functions(){}

        public static Function<ApplicationRole, String> GET_NAME = new Function<ApplicationRole, String>() {
            @Override
            public String apply(final ApplicationRole input) {
                return input.getName();
            }
        };
    }
    

    // -- equals, hashCode, compareTo, toString
    private final static String propertyNames = "name";

    @Override
    public int compareTo(final ApplicationRole o) {
        return ObjectContracts.compare(this, o, propertyNames);
    }

    @Override
    public boolean equals(final Object obj) {
        return ObjectContracts.equals(this, obj, propertyNames);
    }

    @Override
    public int hashCode() {
        return ObjectContracts.hashCode(this, propertyNames);
    }

    @Override
    public String toString() {
        return ObjectContracts.toString(this, propertyNames);
    }

    

    //region  >  (injected)
    @javax.inject.Inject
    RepositoryService repository;
    @javax.inject.Inject
    ApplicationFeatureRepository applicationFeatureRepository;
    @javax.inject.Inject
    ApplicationPermissionRepository applicationPermissionRepository;
    @javax.inject.Inject
    ApplicationUserRepository applicationUserRepository;
    @javax.inject.Inject
    ApplicationRoleRepository applicationRoleRepository;
    

}
