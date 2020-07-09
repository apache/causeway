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
package org.apache.isis.incubator.viewer.javafx.ui.components.objectref;

import javax.inject.Inject;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedParameter;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.incubator.viewer.javafx.model.form.FormFieldFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.form.SimpleFormField;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.ComponentRequest;

import lombok.RequiredArgsConstructor;
import lombok.val;

import javafx.scene.control.Label;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ObjectReferenceFieldFactory implements UiComponentHandlerFx {

    @Override
    public boolean isHandling(ComponentRequest request) {
        //return request.hasFeatureFacet(StringValueFacet.class);
        return request.getFeatureSpec().isEntityOrViewModel();
    }

    @Override
    public FormFieldFx<?> handle(ComponentRequest request) {

        //TODO 1) move all the logic that is in the request to the underlying ManagedProperty
        // 2) pass the ManagedProperty over with the request object
        // 3) design for an API to bind a ManagedProperty to a FormField, also make sure this works
        // with Vaadin's FormLayout/Field API
//        val textValue = request.getFeatureValue(String.class)
//                .orElse("");

        val uiComponent = new Label(request.getManagedFeature().getIdentifier().toString());
        
        if(request.getManagedFeature() instanceof ManagedParameter) {
            
            val managedParameter = (ManagedParameter)request.getManagedFeature();
            
//            uiComponent.textProperty().
//            
//            managedParameter.validate(proposedValue)
            
            //TODO bind to parameter model
            
        } else if(request.getManagedFeature() instanceof ManagedProperty) {
            
            val managedProperty = (ManagedProperty)request.getManagedFeature();
            //TODO bind to property model
        }

        val labelPosition = request.getFeatureFacet(LabelAtFacet.class)
                .map(LabelAtFacet::label)
                .orElse(LabelPosition.NOT_SPECIFIED);

        val formField = new SimpleFormField<String>(labelPosition, uiComponent);
        formField.setLabel(request.getDisplayLabel());
        return formField;
    }


}
