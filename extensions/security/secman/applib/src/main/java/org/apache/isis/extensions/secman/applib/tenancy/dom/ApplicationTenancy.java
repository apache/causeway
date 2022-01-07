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
package org.apache.isis.extensions.secman.applib.tenancy.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Comparator;

import org.apache.isis.applib.annotations.CollectionLayout;
import org.apache.isis.applib.annotations.DomainObject;
import org.apache.isis.applib.annotations.Editing;
import org.apache.isis.applib.annotations.ObjectSupport;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.Property;
import org.apache.isis.applib.annotations.PropertyLayout;
import org.apache.isis.applib.annotations.Where;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;

/**
 * @since 2.0 {@index}
 */
@DomainObject(
        logicalTypeName = ApplicationTenancy.LOGICAL_TYPE_NAME
)
public abstract class ApplicationTenancy implements Comparable<ApplicationTenancy> {

    public static final String LOGICAL_TYPE_NAME = IsisModuleExtSecmanApplib.NAMESPACE + ".ApplicationTenancy";

    public static final String NAMED_QUERY_FIND_BY_NAME = "ApplicationTenancy.findByName";
    public static final String NAMED_QUERY_FIND_BY_PATH = "ApplicationTenancy.findByPath";
    public static final String NAMED_QUERY_FIND_BY_NAME_OR_PATH_MATCHING = "ApplicationTenancy.findByNameOrPathMatching";


    // -- DOMAIN EVENTS

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtSecmanApplib.PropertyDomainEvent<ApplicationTenancy, T> {}
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtSecmanApplib.CollectionDomainEvent<ApplicationTenancy, T> {}
    public static abstract class ActionDomainEvent extends IsisModuleExtSecmanApplib.ActionDomainEvent<ApplicationTenancy> {}


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
            fieldSetId = "details",
            sequence = "1",
            typicalLength = Name.TYPICAL_LENGTH
    )
    @Parameter(
            maxLength = Name.MAX_LENGTH
    )
    @ParameterLayout(
            named = "Name",
            typicalLength= Name.TYPICAL_LENGTH
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Name {
        int MAX_LENGTH = 120;
        int TYPICAL_LENGTH = 20;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Name
    public abstract String getName();
    public abstract void setName(String name);


    // -- PATH

    @Property(
            domainEvent = Parent.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "identity",
            hidden = Where.PARENTED_TABLES
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Path {
        int MAX_LENGTH = 255;

        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @Path
    public abstract String getPath();
    public abstract void setPath(String path);


    // -- PARENT

    @Property(
            domainEvent = Parent.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "details",
            hidden = Where.PARENTED_TABLES,
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Parent {
        class DomainEvent extends PropertyDomainEvent<ApplicationTenancy> {}
    }

    @Parent
    public abstract ApplicationTenancy getParent();
    public abstract void setParent(ApplicationTenancy parent);



    // -- CHILDREN

    @org.apache.isis.applib.annotations.Collection(
            domainEvent = Children.DomainEvent.class
    )
    @CollectionLayout(
            defaultView="table"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Children {
        class DomainEvent extends CollectionDomainEvent<ApplicationTenancy> {}
    }

    @Children
    public abstract Collection<ApplicationTenancy> getChildren();



    // -- CONTRACT

    private static final Equality<ApplicationTenancy> equality =
            ObjectContracts.checkEquals(ApplicationTenancy::getPath);

    private static final Hashing<ApplicationTenancy> hashing =
            ObjectContracts.hashing(ApplicationTenancy::getPath);

    private static final ToString<ApplicationTenancy> toString =
            ObjectContracts.toString("path", ApplicationTenancy::getPath)
                    .thenToString("name", ApplicationTenancy::getName);

    private static final Comparator<ApplicationTenancy> comparator =
            Comparator.comparing(ApplicationTenancy::getPath);

    @Override
    public int compareTo(final org.apache.isis.extensions.secman.applib.tenancy.dom.ApplicationTenancy other) {
        return comparator.compare(this, other);
    }

    @Override
    public boolean equals(final Object other) {
        return equality.equals(this, other);
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
