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
package org.apache.isis.extensions.secman.applib.role.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Collection;
import org.apache.isis.applib.annotations.CollectionLayout;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Editing;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermissionRepository;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;

/**
 * @since 2.0 {@index}
 */
@DomainObject(
        logicalTypeName = ApplicationRole.LOGICAL_TYPE_NAME
)
public abstract class ApplicationRole implements Comparable<ApplicationRole> {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".ApplicationRole";

    public static final String NAMED_QUERY_FIND_BY_NAME = "ApplicationRole.findByName";
    public static final String NAMED_QUERY_FIND_BY_NAME_CONTAINING = "ApplicationRole.findByNameContaining";

    @Inject transient private ApplicationPermissionRepository applicationPermissionRepository;


    // -- EVENTS

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApplib.PropertyDomainEvent<ApplicationRole, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApplib.CollectionDomainEvent<ApplicationRole, T> {}


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
        int MAX_LENGTH = 120;
        int TYPICAL_LENGTH = 30;

        public static class DomainEvent extends PropertyDomainEvent<String> {}
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
        int MAX_LENGTH = 254;
        int TYPICAL_LENGTH = 50;

        public class DomainEvent extends PropertyDomainEvent<String> {}
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
        public static class DomainEvent extends CollectionDomainEvent<ApplicationUser> {}
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
        public static class DomainEvent extends CollectionDomainEvent<ApplicationPermission> {}
    }


    // -- PERMISSIONS

    @Permissions
    public List<org.apache.isis.extensions.secman.applib.permission.dom.ApplicationPermission> getPermissions() {
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
    public int compareTo(final org.apache.isis.extensions.secman.applib.role.dom.ApplicationRole other) {
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
