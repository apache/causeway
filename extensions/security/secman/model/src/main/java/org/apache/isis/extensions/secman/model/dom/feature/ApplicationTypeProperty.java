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
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;

import lombok.val;

@DomainObject(
        objectType = "isis.ext.secman.ApplicationClassProperty"
        )
@DomainObjectLayout(paged=100)
public class ApplicationTypeProperty extends ApplicationTypeMember {

    public static abstract class PropertyDomainEvent<T> extends ApplicationTypeMember.PropertyDomainEvent<ApplicationTypeProperty, T> {}

    public static abstract class CollectionDomainEvent<T> extends ApplicationTypeMember.CollectionDomainEvent<ApplicationTypeProperty, T> {}

    public static abstract class ActionDomainEvent extends ApplicationTypeMember.ActionDomainEvent<ApplicationTypeProperty> {}



    // -- constructors
    public ApplicationTypeProperty() {
    }

    public ApplicationTypeProperty(final ApplicationFeatureId featureId) {
        super(featureId);
    }


    // -- returnType

    public static class ReturnTypeDomainEvent extends PropertyDomainEvent<String> {}

    @Property(
            domainEvent = ReturnTypeDomainEvent.class
            )
    @MemberOrder(name="Data Type", sequence = "2.6")
    public String getReturnType() {
        return getFeature().getActionReturnType()
                .map(Class::getSimpleName)
                .orElse("<none>");
    }


    // -- derived

    public static class DerivedDomainEvent extends PropertyDomainEvent<Boolean> {}

    @Property(
            domainEvent = DerivedDomainEvent.class
            )
    @MemberOrder(name="Detail", sequence = "2.7")
    public boolean isDerived() {
        return getFeature().isPropertyOrCollectionDerived();
    }



    // -- maxLength
    public static class MaxLengthDomainEvent extends PropertyDomainEvent<Integer> {}

    @Property(
            domainEvent = MaxLengthDomainEvent.class,
            optionality = Optionality.OPTIONAL
            )
    @MemberOrder(name="Detail", sequence = "2.8")
    public Integer getMaxLength() {
        val maxLen = getFeature().getPropertyMaxLength();
        return maxLen.isPresent() 
                ? maxLen.getAsInt()
                : null; // unexpected code path, as this case should be hidden 
    }

    public boolean hideMaxLength() {
        if(!getFeature().getPropertyMaxLength().isPresent()) {
            return true;
        }
        return !String.class.getSimpleName().equals(getReturnType());
    }




    // -- typicalLength
    public static class TypicalLengthDomainEvent extends PropertyDomainEvent<Integer> {}

    @Property(
            domainEvent = TypicalLengthDomainEvent.class,
            optionality = Optionality.OPTIONAL
            )
    @MemberOrder(name="Detail", sequence = "2.9")
    public Integer getTypicalLength() {
        val maxLen = getFeature().getPropertyTypicalLength();
        return maxLen.isPresent() 
                ? maxLen.getAsInt()
                : null; // unexpected code path, as this case should be hidden 
    }

    public boolean hideTypicalLength() {
        if(!getFeature().getPropertyTypicalLength().isPresent()) {
            return true;
        }
        return !String.class.getSimpleName().equals(getReturnType());
    }



}

