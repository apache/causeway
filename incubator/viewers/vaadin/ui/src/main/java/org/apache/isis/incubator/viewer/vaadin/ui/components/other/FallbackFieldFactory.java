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
package org.apache.isis.incubator.viewer.vaadin.ui.components.other;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;

import com.vaadin.flow.component.Component;

import org.springframework.core.annotation.Order;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentHandlerVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.debug.DebugField;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.Request;
import org.apache.isis.viewer.common.model.debug.DebugUiModel;

import lombok.val;

@org.springframework.stereotype.Component
@Order(OrderPrecedence.LAST)
public class FallbackFieldFactory implements UiComponentHandlerVaa {
    
    @Inject private Provider<UiComponentFactoryVaa> uiComponentFactory;

    @Override
    public boolean isHandling(Request request) {
        return true; // the last handler in the chain
    }

    @Override
    public Component handle(Request request) {
        
        val spec = request.getObjectFeature().getSpecification();
        
        val debugUiModel = DebugUiModel.of("type not handled")
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
        
        
        val uiField = new DebugField(request.getObjectFeature().getName());
        uiField.setValue(debugUiModel);
        return uiField;
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
