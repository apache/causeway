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
package org.apache.isis.incubator.viewer.vaadin.ui.components.text.uuid;

import java.util.UUID;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.incubator.viewer.vaadin.ui.binding.BinderUtil;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentHandlerVaa;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.ComponentRequest;

import lombok.val;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
public class UuidFieldFactory implements UiComponentHandlerVaa {

    @Override
    public boolean isHandling(ComponentRequest request) {
        return request.isFeatureTypeEqualTo(UUID.class);
    }

    @Override
    public Component handle(ComponentRequest request) {

        val uiField = new TextField(request.getDisplayLabel());
        
        val binder = BinderUtil.requestBinderWithConverter(uiField, UUID.class, new StringToUuidConverter());
        binder.setBean(request);
        
        return uiField;
    }
    
    private static class StringToUuidConverter implements Converter<String, UUID> {

        private static final long serialVersionUID = 1L;

        @Override
        public Result<UUID> convertToModel(String value, ValueContext context) {
            try {
                return Result.ok(UUID.fromString(value));
            } catch (IllegalArgumentException e) {
                return Result.error(e.getMessage());    
            }
        }

        @Override
        public String convertToPresentation(UUID value, ValueContext context) {
            return value.toString();
        }
        
    }
    
}