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
package org.apache.isis.incubator.viewer.javafx.ui.components.text;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.core.metamodel.facets.value.string.StringValueFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.incubator.viewer.javafx.model.binding.BindingsFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.viewer.common.model.binding.BindingConverter;
import org.apache.isis.viewer.common.model.components.UiComponentFactory.ComponentRequest;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@org.springframework.stereotype.Component
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TextFieldFactory implements UiComponentHandlerFx {

    @Override
    public boolean isHandling(ComponentRequest request) {
        return request.hasFeatureTypeFacet(StringValueFacet.class);
    }

    @Override
    public Node handle(ComponentRequest request) {

        val uiComponent = new TextField();
        val valueSpec = request.getFeatureTypeSpec();
        val converter = StringConverter.of(valueSpec);

        if(request.getManagedFeature() instanceof ManagedParameter) {

            val managedParameter = (ManagedParameter)request.getManagedFeature();

            BindingsFx.bindBidirectional(
                    uiComponent.textProperty(),
                    managedParameter.getValue(),
                    converter);

            //TODO bind parameter validation feedback

        } else if(request.getManagedFeature() instanceof ManagedProperty) {

            val managedProperty = (ManagedProperty)request.getManagedFeature();

            // readonly binding
            BindingsFx.bind(
                    uiComponent.textProperty(),
                    managedProperty.getValue(),
                    converter);

            //TODO allow property editing
            //TODO bind property validation feedback
        }

        return uiComponent;
    }

    // -- HELPER

    @RequiredArgsConstructor(staticName = "of")
    private static final class StringConverter implements BindingConverter<String> {

        @Getter(onMethod_ = {@Override})
        private final ObjectSpecification valueSpecification;

        @Override
        public String toString(String value) {
            return value; // identity
        }

        @Override
        public String fromString(String stringifiedValue) {
            return stringifiedValue; // identity
        }

        @Override
        public Optional<String> tryParse(String stringifiedValue) {
            return Optional.empty(); // always ok
        }

    }


}
