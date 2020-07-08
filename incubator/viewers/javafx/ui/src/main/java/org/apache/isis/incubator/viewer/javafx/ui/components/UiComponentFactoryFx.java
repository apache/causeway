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
import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContext;
import org.apache.isis.incubator.viewer.javafx.model.form.FormField;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory;
import org.apache.isis.viewer.common.model.decorator.prototyping.PrototypingUiModel;

import lombok.Getter;
import lombok.val;

import javafx.scene.Node;
import javafx.scene.control.Button;

@Service
public class UiComponentFactoryFx implements UiComponentFactory<Node, FormField> {

    private final boolean isPrototyping;
    private final UiContext uiContext;
    private final ChainOfResponsibility<ComponentRequest, FormField> chainOfHandlers;
    
    /** handlers in order of precedence (debug info)*/
    @Getter 
    private final List<Class<?>> registeredHandlers; 
    
    @Inject
    private UiComponentFactoryFx(
            IsisSystemEnvironment isisSystemEnvironment,
            UiContext uiContext,
            List<UiComponentHandlerFx> handlers) {
        
        this.isPrototyping = isisSystemEnvironment.isPrototyping();
        this.uiContext = uiContext;
        this.chainOfHandlers = ChainOfResponsibility.of(handlers);
        this.registeredHandlers = handlers.stream()
                .map(Handler::getClass)
                .collect(Collectors.toList());
    }
    
    @Override
    public FormField componentFor(ComponentRequest request) {
        
        val formField = chainOfHandlers
                .handle(request)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "Component Mapper failed to handle request %s", request));
        
        val managedMember = (ManagedMember) request.getManagedFeature(); 
        
        request.getDisablingUiModelIfAny().ifPresent(disablingUiModel->{
            uiContext.getDisablingDecoratorForFormField()
            .decorate(formField, disablingUiModel);
        });
        
        return isPrototyping
                ? uiContext.getPrototypingDecoratorForFormField()
                        .decorate(formField, PrototypingUiModel.of(managedMember))
                : formField;
    }
    
    @Override
    public Node buttonFor(ButtonRequest request) {

        val managedAction = request.getManagedAction();
        val disablingUiModelIfAny = request.getDisablingUiModelIfAny();
        val actionEventHandler = request.getActionEventHandler();
        
        val uiButton = new Button(managedAction.getName());
        uiButton.setOnAction(event->actionEventHandler.accept(managedAction));

        disablingUiModelIfAny.ifPresent(disablingUiModel->{
            uiContext.getDisablingDecoratorForButton()
            .decorate(uiButton, disablingUiModel);
        });
        
        return isPrototyping
                ? uiContext.getPrototypingDecoratorForButton()
                        .decorate(uiButton, PrototypingUiModel.of(managedAction))
                : uiButton;
    }

    @Override
    public FormField parameterFor(ComponentRequest request) {
        val formField = chainOfHandlers
                .handle(request)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "Component Mapper failed to handle request %s", request));
        return formField;
    }
    
    
}
