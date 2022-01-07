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
package org.apache.isis.incubator.viewer.vaadin.ui.components.text;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.incubator.viewer.vaadin.ui.binding.BindingsVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentHandlerVaa;
import org.apache.isis.viewer.common.model.components.UiComponentFactory.ComponentRequest;

import lombok.val;

@org.springframework.stereotype.Component
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
public class TextFieldFactory implements UiComponentHandlerVaa {

    @Override
    public boolean isHandling(final ComponentRequest request) {
        return request.hasFacetForValueType(String.class);
    }

    @Override
    public Component handle(final ComponentRequest request) {

        val uiField = new TextField(request.getFriendlyName());

        val managedFeature = request.getManagedFeature();

        BindingsVaa.bindFeature(uiField, managedFeature);

        return uiField;
    }

}