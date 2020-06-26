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
package org.apache.isis.incubator.viewer.javafx.ui.components.markup;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.Request;

import lombok.val;

import javafx.scene.Node;
import javafx.scene.control.TextArea;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.MIDPOINT)
public class MarkupFieldFactory implements UiComponentHandlerFx {
    
    @Override
    public boolean isHandling(Request request) {
        return request.isFeatureTypeInstanceOf(Markup.class);
    }

    @Override
    public Node handle(Request request) {
        
        val markupHtml = request.getFeatureValue(Markup.class)
                .map(Markup::asString)
                .orElse("");

//XXX Unfortunately we have no simple means of auto-fitting a WebView        
//        val uiComponent = new WebView();
//        uiComponent.getEngine().loadContent(markupHtml, "text/html");
        
        val uiComponent = new TextArea(markupHtml);
        uiComponent.setPrefHeight(40);
        return uiComponent;
    }


}
