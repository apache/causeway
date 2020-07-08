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

import javax.inject.Inject;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.facets.value.string.StringValueFacet;
import org.apache.isis.incubator.viewer.javafx.model.form.FormField;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.form.SimpleFormField;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.ComponentRequest;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import lombok.RequiredArgsConstructor;
import lombok.val;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class TextFieldFactory implements UiComponentHandlerFx {

    @Override
    public boolean isHandling(ComponentRequest request) {
        return request.hasFeatureFacet(StringValueFacet.class);
    }

    @Override
    public FormField handle(ComponentRequest request) {

        val uiLabel = new Label(request.getFeatureLabel());

        //TODO 1) move all the logic that is in the request to the underlying ManagedProperty
        // 2) pass the ManagedProperty over with the request object
        // 3) design for an API to bind a ManagedProperty to a FormField, also make sure this works
        // with Vaadin's FormLayout/Field API
        val textValue = request.getFeatureValue(String.class)
                .orElse("");

        val uiComponent = new TextArea(textValue);

        val labelPosition = request.getFeatureFacet(LabelAtFacet.class)
                .map(LabelAtFacet::label)
                .orElse(LabelPosition.NOT_SPECIFIED);

        return new SimpleFormField(labelPosition, uiLabel, uiComponent);
    }


}
