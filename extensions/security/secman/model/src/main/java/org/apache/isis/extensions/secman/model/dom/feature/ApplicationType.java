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
package org.apache.isis.extensions.secman.model.dom.feature;

import java.util.List;
import java.util.SortedSet;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;

@DomainObject(
        objectType = "isis.ext.secman.ApplicationType"
        )
@DomainObjectLayout(
        paged=100,
        bookmarking = BookmarkPolicy.AS_ROOT
        )
public class ApplicationType extends ApplicationFeatureViewModel {

    public static abstract class PropertyDomainEvent<T> 
    extends ApplicationFeatureViewModel.PropertyDomainEvent<ApplicationType, T> {}

    public static abstract class CollectionDomainEvent<T> 
    extends ApplicationFeatureViewModel.CollectionDomainEvent<ApplicationType, T> {}

    public static abstract class ActionDomainEvent 
    extends ApplicationFeatureViewModel.ActionDomainEvent<ApplicationType> {}



    // -- constructors

    public ApplicationType() {
    }

    public ApplicationType(final ApplicationFeatureId featureId) {
        super(featureId);
    }




    // -- actions (collection)

    public static class ActionsDomainEvent 
    extends CollectionDomainEvent<ApplicationTypeAction> {}

    @Collection(
            domainEvent = ActionsDomainEvent.class
            )
    @CollectionLayout(
            defaultView="table",
            sequence = "20.1")
    public List<ApplicationTypeAction> getActions() {
        final SortedSet<ApplicationFeatureId> members = getFeature().getActions();
        return asViewModels(members, ApplicationTypeAction.class);
    }


    // -- properties (collection)

    public static class PropertiesCollectionDomainEvent 
    extends CollectionDomainEvent<ApplicationTypeAction> {}


    @Collection(
            domainEvent = PropertiesCollectionDomainEvent.class
            )
    @CollectionLayout(
            defaultView="table",
            sequence = "20.2")
    public List<ApplicationTypeProperty> getProperties() {
        final SortedSet<ApplicationFeatureId> members = getFeature().getProperties();
        return asViewModels(members, ApplicationTypeProperty.class);
    }


    // -- collections (collection)
    public static class CollectionsCollectionDomainEvent 
    extends CollectionDomainEvent<ApplicationTypeAction> {}

    @Collection(
            domainEvent = CollectionsCollectionDomainEvent.class
            )
    @CollectionLayout(
            defaultView="table",
            sequence = "20.3")
    public List<ApplicationTypeCollection> getCollections() {
        final SortedSet<ApplicationFeatureId> members = getFeature().getCollections();
        return asViewModels(members, ApplicationTypeCollection.class);
    }


}
