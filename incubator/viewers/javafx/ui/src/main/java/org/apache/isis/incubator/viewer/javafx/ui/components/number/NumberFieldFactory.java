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
package org.apache.isis.incubator.viewer.javafx.ui.components.number;

import javax.inject.Inject;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.incubator.viewer.javafx.model.binding.BindingsFx;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.viewer.common.model.binding.NumberConverterForStringComponent;
import org.apache.isis.viewer.common.model.components.UiComponentFactory.ComponentRequest;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class NumberFieldFactory implements UiComponentHandlerFx {

    @Override
    public boolean isHandling(ComponentRequest request) {
        return request.hasFeatureTypeFacetAnyOf(NumberConverterForStringComponent.getSupportedFacets());
    }

    @Override
    public Node handle(ComponentRequest request) {

        val uiComponent = new VBox();
        val uiField = _fx.add(uiComponent, new TextField());
        val uiValidationFeedback = _fx.newValidationFeedback(uiComponent);
        val valueSpec = request.getFeatureTypeSpec();
        val converter = new NumberConverterForStringComponent(valueSpec);

        // ensure user can only type text that is also parse-able by the value facet (parser)
        // however, not every phase of text entering produces parse-able text
        uiField.setTextFormatter(new TextFormatter<String>(change->{
            val input = change.getText();

            val parsingError = converter.tryParse(_Strings.suffix(input, "0"));
            if (parsingError.isPresent()) {
                log.warn("Failed to parse UI input '{}': {}", input, parsingError.get());
                return null; // veto change
            }
            return change; // allow change
        }));

        if(request.getManagedFeature() instanceof ManagedParameter) {

            val managedParameter = (ManagedParameter)request.getManagedFeature();

            BindingsFx.bindBidirectional(
                    uiField.textProperty(),
                    managedParameter.getValue(),
                    converter);

            BindingsFx.bindValidationFeeback(
                    uiValidationFeedback.textProperty(),
                    uiValidationFeedback.visibleProperty(),
                    managedParameter.getValidationMessage());

        } else if(request.getManagedFeature() instanceof ManagedProperty) {

            val managedProperty = (ManagedProperty)request.getManagedFeature();

            // readonly binding
            BindingsFx.bind(
                    uiField.textProperty(),
                    managedProperty.getValue(),
                    converter);

            //TODO allow property editing
            uiField.editableProperty().set(false);

            //TODO bind property validation feedback

        }

        return uiComponent;
    }

    // -- HELPER



}
