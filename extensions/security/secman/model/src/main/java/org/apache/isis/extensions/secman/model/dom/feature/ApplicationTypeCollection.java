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

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;

@DomainObject(
        objectType = "isis.ext.secman.ApplicationClassCollection"
        )
@DomainObjectLayout(paged=100)
public class ApplicationTypeCollection extends ApplicationTypeMember {

    public static abstract class PropertyDomainEvent<T> extends ApplicationTypeMember.PropertyDomainEvent<ApplicationTypeCollection, T> {}

    public static abstract class CollectionDomainEvent<T> extends ApplicationTypeMember.CollectionDomainEvent<ApplicationTypeCollection, T> {}

    public static abstract class ActionDomainEvent extends ApplicationTypeMember.ActionDomainEvent<ApplicationTypeCollection> {}

    // -- constructors

    public ApplicationTypeCollection() {}

    public ApplicationTypeCollection(final ApplicationFeatureId featureId) {
        super(featureId);
    }

    // -- returnType

    public static class ElementTypeDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = ElementTypeDomainEvent.class
            )
    @PropertyLayout(group="Data Type", sequence = "2.6")
    public String getElementType() {
        return getFeature().getActionReturnType()
                .map(Class::getSimpleName)
                .orElse("<none>");
    }

    // -- derived

    public static class DerivedDomainEvent extends PropertyDomainEvent<Boolean> {}

    @Property(
            domainEvent = DerivedDomainEvent.class
            )
    @PropertyLayout(group="Detail", sequence = "2.7")
    public boolean isDerived() {
        return getFeature().isPropertyOrCollectionDerived();
    }


}
