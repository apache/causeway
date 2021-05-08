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
package org.apache.isis.extensions.secman.api.feature.dom;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;

@DomainObject(
        objectType = "isis.ext.secman.ApplicationClassAction"
        )
@DomainObjectLayout(paged=100)
public class ApplicationTypeAction extends ApplicationTypeMember {

    public static abstract class PropertyDomainEvent<T> extends ApplicationTypeMember.PropertyDomainEvent<ApplicationTypeAction, T> {}

    public static abstract class CollectionDomainEvent<T> extends ApplicationTypeMember.CollectionDomainEvent<ApplicationTypeAction, T> {}

    public static abstract class ActionDomainEvent extends ApplicationTypeMember.ActionDomainEvent<ApplicationTypeAction> {}

    // -- constructors

    public ApplicationTypeAction() {
    }

    public ApplicationTypeAction(final ApplicationFeatureId featureId) {
        super(featureId);
    }


    // -- returnTypeName (property)

    public static class ReturnTypeDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = ReturnTypeDomainEvent.class
            )
    @PropertyLayout(fieldSetId="Data Type", sequence = "2.6")
    public String getReturnType() {
        return getFeature().getActionReturnType()
                .map(Class::getSimpleName)
                .orElse("<none>");
    }

    // -- actionSemantics (property)
    public static class ActionSemanticsDomainEvent extends PropertyDomainEvent<SemanticsOf> {}

    @Property(
            domainEvent = ActionSemanticsDomainEvent.class
            )
    @PropertyLayout(fieldSetId="Detail", sequence = "2.8")
    public SemanticsOf getActionSemantics() {
        return getFeature().getActionSemantics()
                .orElse(null);
    }


}
