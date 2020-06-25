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
package org.apache.isis.incubator.viewer.javafx.ui.components;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.handler.ChainOfResponsibility;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory;

import lombok.Getter;

import javafx.scene.Node;

@Service
public class UiComponentFactoryFx implements UiComponentFactory<Node> {

    private final ChainOfResponsibility<Request, Node> chainOfHandlers;
    
    /** handlers in order of precedence (debug info)*/
    @Getter 
    private final List<Class<?>> registeredHandlers; 
    
    @Inject
    private UiComponentFactoryFx(List<Handler<Node>> handlers) {
        this.chainOfHandlers = ChainOfResponsibility.of(handlers);
        this.registeredHandlers = handlers.stream()
                .map(Handler::getClass)
                .collect(Collectors.toList());
    }
    
    @Override
    public Node componentFor(Request request) {
        return chainOfHandlers
                .handle(request)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "Component Mapper failed to handle request %s", request));
    }
    
    
}
