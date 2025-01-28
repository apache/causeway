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
package org.apache.causeway.viewer.commons.model.components;

import java.util.Optional;
import java.util.function.Consumer;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.commons.handler.ChainOfResponsibility;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedFeature;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedValue;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.decorators.DisablingDecorator.DisablingDecorationModel;

import org.jspecify.annotations.NonNull;
import lombok.Value;

public interface UiComponentFactory<B, C> {

    B buttonFor(UiComponentFactory.ButtonRequest request);
    C componentFor(UiComponentFactory.ComponentRequest request);
    C parameterFor(UiComponentFactory.ComponentRequest request);
    LabelAndPosition<C> labelFor(UiComponentFactory.ComponentRequest request);

    @Value(staticConstructor = "of")
    public static class LabelAndPosition<T> {
        @NonNull private final LabelPosition labelPosition;
        @NonNull private final T uiLabel;
    }

    @Value(staticConstructor = "of")
    public static class ButtonRequest {
        @NonNull private final ManagedAction managedAction;
        @NonNull private final Optional<DisablingDecorationModel> disablingUiModelIfAny;
        @NonNull private final Consumer<ManagedAction> actionEventHandler;
    }

    @Value(staticConstructor = "of")
    public static class ComponentRequest {

        @NonNull private final ManagedValue managedValue;
        @NonNull private final ManagedFeature managedFeature;
        @NonNull private final Optional<DisablingDecorationModel> disablingUiModelIfAny;

        public static ComponentRequest of(final ManagedParameter managedParameter) {
            return of(managedParameter, managedParameter, Optional.empty());
        }

        // -- SHORTCUTS

        public String getFriendlyName() {
            return managedFeature.getFriendlyName();
        }

        public ObjectSpecification getFeatureTypeSpec() {
            return managedFeature.getElementType();
        }

        public Class<?> getFeatureType() {
            return managedFeature.getElementClass();
        }

        public boolean isFeatureTypeEqualTo(final @Nullable Class<?> type) {
            return getFeatureType() == type;
        }

        public boolean isFeatureTypeAssignableFrom(final @Nullable Class<?> type) {
            return type!=null
                    ? getFeatureType().isAssignableFrom(type)
                    : false;
        }

        public boolean isFeatureTypeInstanceOf(final @Nullable Class<?> type) {
            return type!=null
                    ? type.isAssignableFrom(getFeatureType())
                    : false;
        }

        /**
         * @param facetType
         * @return Whether there exists a facet for this feature, that is of the
         * specified {@code facetType} (as per the type it reports from {@link Facet#facetType()}).
         */
        public <T extends Facet> boolean hasFeatureTypeFacet(final @Nullable Class<T> facetType) {
            return facetType!=null
                    ? getFeatureTypeSpec().containsFacet(facetType)
                    : false;
        }

        public boolean hasFacetForValueType(final @Nullable Class<?> valueType) {
            return valueType!=null
                    ? Facets.valueTypeMatches(valueType::equals)
                            .test(getFeatureTypeSpec())
                    : false;
        }

        //TODO are there ever parameters that might render readonly?
        public boolean isReadOnly() {
            return ((ManagedProperty)managedFeature).checkUsability().isPresent();
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
