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
package org.apache.isis.extensions.secman.model.app.feature;

import java.util.List;
import java.util.SortedSet;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureId;
import org.apache.isis.core.metamodel.services.appfeat.ApplicationFeatureType;

@DomainObject(
        objectType = "secman.ApplicationPackage"
        )
@DomainObjectLayout(paged=100)
public class ApplicationPackage extends ApplicationFeatureViewModel {

    public static abstract class PropertyDomainEvent<T> extends ApplicationFeatureViewModel.PropertyDomainEvent<ApplicationClass, T> {}

    public static abstract class CollectionDomainEvent<T> extends ApplicationFeatureViewModel.CollectionDomainEvent<ApplicationClass, T> {}

    public static abstract class ActionDomainEvent extends ApplicationFeatureViewModel.ActionDomainEvent<ApplicationClass> {}



    // -- constructors

    public ApplicationPackage() {
    }

    public ApplicationPackage(final ApplicationFeatureId featureId) {
        super(featureId);
    }


    // -- contents (collection, for packages only)

    public static class ContentsDomainEvent extends CollectionDomainEvent<ApplicationPackage> {}

    @Collection(
            domainEvent = ContentsDomainEvent.class
            )
    @CollectionLayout(
            defaultView="table"
            )
    @MemberOrder(sequence = "4")
    public List<ApplicationFeatureViewModel> getContents() {
        final SortedSet<ApplicationFeatureId> contents = getFeature().getContents();
        return asViewModels(contents);
    }
    public boolean hideContents() {
        return getType() != ApplicationFeatureType.PACKAGE;
    }


}
