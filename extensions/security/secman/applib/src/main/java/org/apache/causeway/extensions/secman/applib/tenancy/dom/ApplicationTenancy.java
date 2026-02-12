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
package org.apache.causeway.extensions.secman.applib.tenancy.dom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Comparator;

import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.util.Equality;
import org.apache.causeway.applib.util.Hashing;
import org.apache.causeway.applib.util.ObjectContracts;
import org.apache.causeway.applib.util.ToString;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@Named(ApplicationTenancy.LOGICAL_TYPE_NAME) // required for permission mapping
@DomainObject(
        autoCompleteRepository = ApplicationTenancyRepository.class,
        autoCompleteMethod = "findMatching"
)
@DomainObjectLayout(
        titleUiEvent = ApplicationTenancy.TitleUiEvent.class,
        iconUiEvent = ApplicationTenancy.IconUiEvent.class,
        cssClassUiEvent = ApplicationTenancy.CssClassUiEvent.class,
        layoutUiEvent = ApplicationTenancy.LayoutUiEvent.class)
public interface ApplicationTenancy extends Comparable<ApplicationTenancy> {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleExtSecmanApplib.NAMESPACE + ".ApplicationTenancy";
    public static final String SCHEMA = CausewayModuleExtSecmanApplib.SCHEMA;
    public static final String TABLE = "ApplicationTenancy";

    @UtilityClass
    public static class Nq {
        public static final String FIND_BY_NAME = LOGICAL_TYPE_NAME + ".findByName";
        public static final String FIND_BY_PATH = LOGICAL_TYPE_NAME + ".findByPath";
        public static final String FIND_BY_NAME_OR_PATH_MATCHING = LOGICAL_TYPE_NAME + ".findByNameOrPathMatching";
    }

    // -- UI & DOMAIN EVENTS

    public static class TitleUiEvent extends CausewayModuleExtSecmanApplib.TitleUiEvent<ApplicationTenancy> { }
    public static class IconUiEvent extends CausewayModuleExtSecmanApplib.IconUiEvent<ApplicationTenancy> { }
    public static class CssClassUiEvent extends CausewayModuleExtSecmanApplib.CssClassUiEvent<ApplicationTenancy> { }
    public static class LayoutUiEvent extends CausewayModuleExtSecmanApplib.LayoutUiEvent<ApplicationTenancy> { }

    public static abstract class PropertyDomainEvent<T> extends CausewayModuleExtSecmanApplib.PropertyDomainEvent<ApplicationTenancy, T> {}
    public static abstract class CollectionDomainEvent<T> extends CausewayModuleExtSecmanApplib.CollectionDomainEvent<ApplicationTenancy, T> {}
    public static abstract class ActionDomainEvent extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationTenancy> {}

    // -- MODEL

    @ObjectSupport default String title() {
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
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = 120;
        int TYPICAL_LENGTH = 20;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @Name
    String getName();
    void setName(String name);

    // -- PATH

    @Property(
            domainEvent = Parent.DomainEvent.class,
            optionality = Optionality.MANDATORY
    )
    @PropertyLayout(
            fieldSetId = "identity"
    )
    @HasAtPath.AtPath
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Path {
        class DomainEvent extends PropertyDomainEvent<String> {}
        int MAX_LENGTH = HasAtPath.AtPath.MAX_LENGTH;
        boolean NULLABLE = false;
        String ALLOWS_NULL = "false";
    }
    @Path
    String getPath();
    void setPath(String path);

    // -- PARENT

    @Property(
            domainEvent = Parent.DomainEvent.class,
            editing = Editing.DISABLED
    )
    @PropertyLayout(
            fieldSetId = "details",
            hidden = Where.REFERENCES_PARENT,
            sequence = "2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Parent {
        class DomainEvent extends PropertyDomainEvent<ApplicationTenancy> {}
        String NAME = "parentPath";
        boolean NULLABLE = true;
        String ALLOWS_NULL = "true";
    }
    @Parent
    ApplicationTenancy getParent();
    void setParent(ApplicationTenancy parent);

    @Programmatic
    default boolean isRoot() {
        return getParent()==null;
    }

    // -- CHILDREN

    @org.apache.causeway.applib.annotation.Collection(
            domainEvent = Children.DomainEvent.class
    )
    @CollectionLayout(
            defaultView="table"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Children {
        class DomainEvent extends CollectionDomainEvent<ApplicationTenancy> {}
        String MAPPED_BY = "parent";
    }
    @Children
    Collection<ApplicationTenancy> getChildren();

    // -- CONTRACT

    static final Equality<ApplicationTenancy> EQUALITY =
            ObjectContracts.checkEquals(ApplicationTenancy::getPath);

    static final Hashing<ApplicationTenancy> HASHING =
            ObjectContracts.hashing(ApplicationTenancy::getPath);

    static final ToString<ApplicationTenancy> TOSTRING =
            ObjectContracts.toString("path", ApplicationTenancy::getPath)
                    .thenToString("name", ApplicationTenancy::getName);

    static final Comparator<ApplicationTenancy> COMPARATOR =
            Comparator.comparing(ApplicationTenancy::getPath);

    @Override
    default int compareTo(final org.apache.causeway.extensions.secman.applib.tenancy.dom.ApplicationTenancy other) {
        return COMPARATOR.compare(this, other);
    }

}
