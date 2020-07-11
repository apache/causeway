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
package org.apache.isis.viewer.common.model.binding;

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.handler.ChainOfResponsibility;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.internal.functions._Predicates;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedFeature;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

public interface UiComponentFactory<B, C> {
    
    B buttonFor(UiComponentFactory.ButtonRequest request);
    C componentFor(UiComponentFactory.ComponentRequest request);
    C parameterFor(UiComponentFactory.ComponentRequest request);
    
    @Value(staticConstructor = "of")
    public static class ButtonRequest {
        @NonNull private final ManagedAction managedAction;
        @NonNull private final Optional<DisablingUiModel> disablingUiModelIfAny;
        @NonNull private final Consumer<ManagedAction> actionEventHandler;
    }
    
    @Value(staticConstructor = "of")
    public static class ComponentRequest {
        @NonNull private final ManagedFeature managedFeature;
        @NonNull private final Optional<DisablingUiModel> disablingUiModelIfAny;
        @NonNull private final Where where;
        
        public static ComponentRequest of(ManagedParameter managedParameter) {
            return of(managedParameter, Optional.empty(), Where.ANYWHERE);
        }
        
        // -- SHORTCUTS
        
        public String getDisplayLabel() {
            return managedFeature.getDisplayLabel();   
        }
        
        public ObjectSpecification getFeatureSpec() {
            return managedFeature.getSpecification();    
        }
        
        public Class<?> getFeatureType() {
            return managedFeature.getCorrespondingClass();    
        }
        
        public boolean isFeatureTypeEqualTo(@Nullable Class<?> type) {
            return getFeatureType() == type;
        }
        
        public boolean isFeatureTypeAssignableFrom(@Nullable Class<?> type) {
            return type!=null
                    ? getFeatureType().isAssignableFrom(type)
                    : false;
        }
        
        public boolean isFeatureTypeInstanceOf(@Nullable Class<?> type) {
            return type!=null
                    ? type.isAssignableFrom(getFeatureType())
                    : false;
        }
        
        /**
         * @param facetType
         * @return Whether there exists a facet for this feature, that is of the 
         * specified {@code facetType} (as per the type it reports from {@link Facet#facetType()}).
         */
        public <T extends Facet> boolean hasFeatureFacet(@Nullable Class<T> facetType) {
            return facetType!=null
                    ? getFeatureSpec().getFacet(facetType)!=null
                    : false;
        }

        /**
         * @param facetType
         * @return Optionally the feature's facet of the specified {@code facetType} 
         * (as per the type it reports from {@link Facet#facetType()}), based on existence.
         */
        public <T extends Facet> Optional<T> getFeatureFacet(@Nullable Class<T> facetType) {
            return facetType!=null
                    ? Optional.ofNullable(getFeatureSpec().getFacet(facetType))
                    : Optional.empty();
        }
        
        public <T extends Facet> T getFeatureFacetElseFail(@Nullable Class<T> facetType) {
            return getFeatureFacet(facetType)
                    .orElseThrow(()->_Exceptions
                            .noSuchElement("Feature %s has no such facet %s",
                                    managedFeature.getIdentifier(),
                                    facetType.getName()));    
        }
        
        public <T> Optional<T> getFeatureValue(@Nullable Class<T> type) {
            val managedProperty = (ManagedProperty)managedFeature;
            //TODO do a type check before the cast, so we can throw a more detailed exception
            // that is, given type must be assignable from the actual pojo type 
            return Optional.ofNullable(managedProperty.getPropertyValue(where))
                    .filter(_Predicates.not(ManagedObjects::isNullOrUnspecifiedOrEmpty))
                    .map(ManagedObject::getPojo)
                    .map(type::cast);
        }

        public boolean isReadOnly() {
            return ((ManagedProperty)managedFeature).checkUsability(where).isPresent();
        }

        public Optional<InteractionVeto> setFeatureValue(Object proposedNewValuePojo) {
            //TODO we are loosing any fields that are cached within ManagedObject
            val proposedNewValue = ManagedObject.of(getFeatureSpec(), proposedNewValuePojo);
            return ((ManagedProperty)managedFeature).modifyProperty(proposedNewValue);
        }
        
    }
    
    
    // -- HANDLER
    
    /**
     * @param <T> - the Handler's Response type
     */
    static interface Handler<T> 
    extends ChainOfResponsibility.Handler<UiComponentFactory.ComponentRequest, T> {
    }
    
}
