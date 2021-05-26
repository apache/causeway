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
package org.apache.isis.applib.services.appfeatui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.SortedSet;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;

/**
 * @since 2.x  {@index}
 */
@DomainObject(
        logicalTypeName = ApplicationType.LOGICAL_TYPE_NAME
)
@DomainObjectLayout(
        bookmarking = BookmarkPolicy.AS_ROOT,
        paged=100
)
public class ApplicationType extends ApplicationFeatureViewModel {

    public static final String LOGICAL_TYPE_NAME = IsisModuleApplib.NAMESPACE_FEAT + ".ApplicationType";

    public static abstract class CollectionDomainEvent<T>
            extends ApplicationFeatureViewModel.CollectionDomainEvent<ApplicationType, T> {}



    // -- constructors

    public ApplicationType() { }
    public ApplicationType(final ApplicationFeatureId featureId) {
        super(featureId);
    }



    // -- actions (collection)

    @Collection(
            domainEvent = Actions.DomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "20.1"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Actions {
        class DomainEvent extends CollectionDomainEvent<ApplicationTypeAction> {}
    }

    @Actions
    public List<ApplicationTypeAction> getActions() {
        final SortedSet<ApplicationFeatureId> members = getFeature().getActions();
        return asViewModels(members, ApplicationTypeAction.class);
    }


    // -- properties (collection)

    @Collection(
            domainEvent = Properties.CollectionDomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "20.2"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Properties {
        class CollectionDomainEvent extends ApplicationType.CollectionDomainEvent<ApplicationTypeProperty> {}
    }

    @Properties
    public List<ApplicationTypeProperty> getProperties() {
        final SortedSet<ApplicationFeatureId> members = getFeature().getProperties();
        return asViewModels(members, ApplicationTypeProperty.class);
    }



    // -- collections (collection)

    @Collection(
            domainEvent = Collections.DomainEvent.class
    )
    @CollectionLayout(
            defaultView="table",
            sequence = "20.3"
    )
    @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Collections {
        class DomainEvent extends CollectionDomainEvent<ApplicationTypeCollection> {}
    }

    @Collections
    public List<ApplicationTypeCollection> getCollections() {
        final SortedSet<ApplicationFeatureId> members = getFeature().getCollections();
        return asViewModels(members, ApplicationTypeCollection.class);
    }


}
