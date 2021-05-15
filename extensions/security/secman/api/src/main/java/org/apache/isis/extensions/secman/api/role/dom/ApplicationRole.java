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
package org.apache.isis.extensions.secman.api.role.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.SortedSet;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.extensions.secman.api.IsisModuleExtSecmanApi;
import org.apache.isis.extensions.secman.api.permission.dom.ApplicationPermission;
import org.apache.isis.extensions.secman.api.user.dom.ApplicationUser;

/**
 * @since 2.0 {@index}
 */
@DomainObject(
        objectType = ApplicationRole.OBJECT_TYPE
)
public interface ApplicationRole extends Comparable<ApplicationRole> {

    String OBJECT_TYPE = IsisModuleExtSecmanApi.NAMESPACE + ".ApplicationRole";

    String NAMED_QUERY_FIND_BY_NAME = "ApplicationRole.findByName";
    String NAMED_QUERY_FIND_BY_NAME_CONTAINING = "ApplicationRole.findByNameContaining";


    // -- EVENTS

    abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApi.PropertyDomainEvent<ApplicationRole, T> {}
    abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApi.CollectionDomainEvent<ApplicationRole, T> {}


    // -- MODEL

    /**
     * having a title() method (rather than using @Title annotation) is necessary as a workaround to be able to use
     * wrapperFactory#unwrap(...) method, which is otherwise broken in Isis 1.6.0
     */
    default String title() {
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
    @interface Name {
        int MAX_LENGTH = 120;
        int TYPICAL_LENGTH = 30;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Name
    String getName();
    void setName(String name);


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
    @interface Description {
        int MAX_LENGTH = 254;
        int TYPICAL_LENGTH = 50;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Description
    String getDescription();
    void setDescription(String description);


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
    @interface Users {
        class DomainEvent extends CollectionDomainEvent<ApplicationUser> {}
    }

    @Users
    SortedSet<ApplicationUser> getUsers();


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
    @interface Permissions {
        class DomainEvent extends CollectionDomainEvent<ApplicationPermission> {}
    }

    @Permissions
    List<ApplicationPermission> getPermissions();

}
