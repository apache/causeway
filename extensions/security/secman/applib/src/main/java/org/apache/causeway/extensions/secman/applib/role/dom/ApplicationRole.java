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
package org.apache.causeway.extensions.secman.applib.role.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.util.Equality;
import org.apache.causeway.applib.util.Hashing;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.applib.util.ToString;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@Named(ApplicationRole.LOGICAL_TYPE_NAME)
@DomainObject(
        autoCompleteRepository = ApplicationRoleRepository.class,
        autoCompleteMethod = "findMatching"
        )
@DomainObjectLayout(
        titleUiEvent = ApplicationRole.TitleUiEvent.class,
        iconUiEvent = ApplicationRole.IconUiEvent.class,
        cssClassUiEvent = ApplicationRole.CssClassUiEvent.class,
        layoutUiEvent = ApplicationRole.LayoutUiEvent.class
)
public abstract class ApplicationRole implements Comparable<ApplicationRole> {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".ApplicationRole";
    public static final String SCHEMA = CausewayModuleExtSecmanApplib.SCHEMA;
    public static final String TABLE = "ApplicationRole";

    @UtilityClass
    public static class Nq {
        public static final String FIND_BY_NAME = LOGICAL_TYPE_NAME + ".findByName";
        public static final String FIND_BY_NAME_CONTAINING = LOGICAL_TYPE_NAME + ".findByNameContaining";
    }

    @Inject transient private ApplicationPermissionRepository applicationPermissionRepository;


    // -- UI & DOMAIN EVENTS

    public static class TitleUiEvent extends CausewayModuleExtSecmanApplib.TitleUiEvent<ApplicationRole> { }
    public static class IconUiEvent extends CausewayModuleExtSecmanApplib.IconUiEvent<ApplicationRole> { }
    public static class CssClassUiEvent extends CausewayModuleExtSecmanApplib.CssClassUiEvent<ApplicationRole> { }
    public static class LayoutUiEvent extends CausewayModuleExtSecmanApplib.LayoutUiEvent<ApplicationRole> { }

    public static abstract class PropertyDomainEvent<T> extends CausewayModuleExtSecmanApplib.PropertyDomainEvent<ApplicationRole, T> {}
    public static abstract class CollectionDomainEvent<T> extends CausewayModuleExtSecmanApplib.CollectionDomainEvent<ApplicationRole, T> {}


    // -- MODEL

    @ObjectSupport public String title() {
        return getName();
    }


    // -- NAME

    @Property(
            domainEvent = Name.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Name.MAX_LENGTH
    )
    @PropertyLayout(
            fieldSetId = "identity",
            sequence = "1",
            typicalLength= Name.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = Name.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Name",
            typicalLength = Name.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Name {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 120;
        int TYPICAL_LENGTH = 30;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @Name
    public abstract String getName();
    public abstract void setName(String name);


    // -- DESCRIPTION

    @Property(
            domainEvent = Description.DomainEvent.class,
            editing = Editing.DISABLED,
            maxLength = Description.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @PropertyLayout(
            fieldSetId = "details",
            sequence = "1",
            typicalLength = Description.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = Description.MAX_LENGTH,
            optionality = Optionality.OPTIONAL
    )
    @ParameterLayout(
            multiLine = 5,
            named = "Description",
            typicalLength = Description.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Description {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 254;
        int TYPICAL_LENGTH = 50;
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @Description
    public abstract String getDescription();
    public abstract void setDescription(String description);


    // -- USERS

    @Collection(
            domainEvent = Users.DomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "20"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Users {
        class DomainEvent extends CollectionDomainEvent<ApplicationUser> {}
        String MAPPED_BY = "roles";
    }
    @Users
    public abstract Set<ApplicationUser> getUsers();


    // -- PERMISSIONS

    @Collection(
            domainEvent = Permissions.DomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "10",
            sortedBy = ApplicationPermission.DefaultComparator.class
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Permissions {
        class DomainEvent extends CollectionDomainEvent<ApplicationPermission> {}
    }


    // -- PERMISSIONS

    @Permissions
    public List<org.apache.causeway.extensions.secman.applib.permission.dom.ApplicationPermission> getPermissions() {
        return applicationPermissionRepository.findByRole(this);
    }


    // -- equals, hashCode, compareTo, toString

    private static final Comparator<ApplicationRole> comparator =
            Comparator.comparing(ApplicationRole::getName);

    private static final Equality<ApplicationRole> equality =
            ObjectContracts.checkEquals(ApplicationRole::getName);

    private static final Hashing<ApplicationRole> hashing =
            ObjectContracts.hashing(ApplicationRole::getName);

    private static final ToString<ApplicationRole> toString =
            ObjectContracts.toString("name", ApplicationRole::getName);


    @Override
    public int compareTo(final org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole other) {
        return comparator.compare(this, other);
    }

    @Override
    public boolean equals(final Object obj) {
        return equality.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return hashing.hashCode(this);
    }

    @Override
    public String toString() {
        return toString.toString(this);
    }

}
