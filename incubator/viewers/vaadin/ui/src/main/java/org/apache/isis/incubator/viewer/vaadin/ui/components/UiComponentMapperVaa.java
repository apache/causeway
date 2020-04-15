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
package org.apache.isis.incubator.viewer.vaadin.ui.components;

import com.vaadin.flow.component.Component;

import org.apache.isis.core.commons.handler.ChainOfResponsibility;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

public interface UiComponentMapperVaa {
    
    Component componentFor(UiComponentMapperVaa.Request request);
    
    // -- REQUEST (VALUE) TYPE
    
    @Value(staticConstructor = "of")
    public static class Request {
        /** not null but the wrapped pojo is allowed to be null*/
        @NonNull private final ManagedObject managedObject; 
        @NonNull private final ObjectFeature objectFeature;
    }
    
    // -- HANDLER
    
    static interface Handler 
    extends ChainOfResponsibility.Handler<UiComponentMapperVaa.Request, Component> {
    }

    // -- FACTORY
    
    public static UiComponentMapperVaa createDefault() {
        
        val chainOfHandlers = _Lists.of(
                UiComponentMapperVaa_builtinHandlers.getClob(),
                UiComponentMapperVaa_builtinHandlers.getBlob(),
                UiComponentMapperVaa_builtinHandlers.getText(),
                UiComponentMapperVaa_builtinHandlers.getOther());
        
        val chainOfRespo = ChainOfResponsibility.of(chainOfHandlers);
        
        return request -> chainOfRespo
                .handle(request)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "Component Mapper failed to handle request %s", request));
        
    }
    

    
}
