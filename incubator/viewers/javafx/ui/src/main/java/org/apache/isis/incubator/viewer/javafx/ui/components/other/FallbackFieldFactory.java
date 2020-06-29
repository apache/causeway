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
package org.apache.isis.incubator.viewer.javafx.ui.components.other;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentHandlerFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.debug.DebugField;
import org.apache.isis.incubator.viewer.javafx.ui.components.form.FormField;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.Request;
import org.apache.isis.viewer.common.model.debug.DebugUiModel;

import lombok.val;

import javafx.scene.Node;
import javafx.scene.control.Label;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.LAST)
public class FallbackFieldFactory implements UiComponentHandlerFx {
    
    @Inject private Provider<UiComponentFactoryFx> uiComponentFactory;

    @Override
    public boolean isHandling(Request request) {
        return true; // the last handler in the chain
    }

    @Override
    public FormField handle(Request request) {
        
        val spec = request.getObjectFeature().getSpecification();
        val debugUiModel = DebugUiModel.of(spec.getCorrespondingClass().getSimpleName() + " type not handled")
        .withProperty("ObjectFeature.specification.fullIdentifier",  spec.getFullIdentifier())
        .withProperty("ObjectFeature.identifier",  request.getObjectFeature().getIdentifier().toString());
        
        val handlerInfo = uiComponentFactory.get().getRegisteredHandlers()
        .stream()
        .map(Class::getSimpleName)
        .map(handlerName->" • " + handlerName)
        .collect(Collectors.joining("\n"));
        
        debugUiModel.withProperty("Handlers", handlerInfo);
        
        spec.streamFacets()
        .forEach(facet -> {
            debugUiModel.withProperty(
                    facet.facetType().getSimpleName(), 
                    summarize(facet));
        });
        
        
        val debugField = new DebugField(request.getObjectFeature().getName());
        debugField.setValue(debugUiModel);
        
        val uiLabel = new Label(request.getFeatureLabel());
        
        return new FormField() {
            
            @Override
            public Node getUiLabel() {
                return uiLabel;
            }
            
            @Override
            public Node getUiField() {
                return debugField;
            }
            
            @Override
            public LabelPosition getLabelPosition() {
                return LabelPosition.TOP;
            }
        };
        
    }
    
    private String summarize(Facet facet) {
        val sb = new StringBuilder();
        sb.append(facet.getClass().getSimpleName());
        if(facet instanceof FacetAbstract) {
            val attributeMap = _Maps.<String, Object>newTreeMap();
            ((FacetAbstract)facet).appendAttributesTo(attributeMap);
            attributeMap.forEach((k, v)->{
                sb.append("\n • ").append(k).append(": ").append(v);    
            });
        }
        return sb.toString();
    }


}
