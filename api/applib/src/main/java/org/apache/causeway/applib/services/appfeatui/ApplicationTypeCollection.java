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
package org.apache.causeway.applib.services.appfeatui;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;

/**
 * @since 2.x  {@index}
 */
@Named(ApplicationTypeCollection.LOGICAL_TYPE_NAME)
@DomainObject
@DomainObjectLayout(
        paged = 100
)
public class ApplicationTypeCollection extends ApplicationTypeMember {

    static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE_FEAT + ".ApplicationTypeCollection";

    public static abstract class PropertyDomainEvent<T> extends ApplicationTypeMember.PropertyDomainEvent<ApplicationTypeCollection, T> {}

    // -- constructors
    public ApplicationTypeCollection() {}
    public ApplicationTypeCollection(final ApplicationFeatureId featureId) {
        super(featureId);
    }
    @Inject
    public ApplicationTypeCollection(final String memento) {
        super(memento);
    }


    // -- elementType

    @ApplicationFeatureViewModel.TypeSimpleName
    @Property(
            domainEvent = ElementType.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "dataType",
            sequence = "2.6"
    )
    @Target({ java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ElementType {
        class DomainEvent extends PropertyDomainEvent<String> {}
    }

    @PropertyLayout(
            fieldSetId = "dataType", // TODO: shouldn't be necessary??
            sequence = "2.6"
    )
    @ElementType
    public String getElementType() {
        return getFeature().getActionReturnType()
                .map(Class::getSimpleName)
                .orElse("<none>");
    }


    // -- derived

    @Property(
            domainEvent = Derived.DomainEvent.class
    )
    @PropertyLayout(
            fieldSetId = "detail",
            sequence = "2.7"
    )
    @Target({ java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.ANNOTATION_TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Derived {
        class DomainEvent extends PropertyDomainEvent<Boolean> {}
    }

    @Derived
    public boolean isDerived() {
        return getFeature().isPropertyOrCollectionDerived();
    }


}
