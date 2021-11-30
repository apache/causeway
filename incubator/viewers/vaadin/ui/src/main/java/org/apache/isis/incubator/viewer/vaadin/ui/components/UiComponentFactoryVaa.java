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

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;

import org.springframework.stereotype.Service;

import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.incubator.viewer.vaadin.model.util._vaa;
import org.apache.isis.viewer.common.model.components.UiComponentFactory;

import lombok.Getter;
import lombok.val;

@Service
public class UiComponentFactoryVaa implements UiComponentFactory<Component, Component> {

    private final ChainOfResponsibility<ComponentRequest, Component> chainOfHandlers;

    /** handlers in order of precedence (debug info)*/
    @Getter
    private final List<Class<?>> registeredHandlers;

    @Inject
    private UiComponentFactoryVaa(final List<Handler<Component>> handlers) {
        this.chainOfHandlers = ChainOfResponsibility.named("UiComponentFactoryVaa", handlers);
        this.registeredHandlers = handlers.stream()
                .map(Handler::getClass)
                .collect(Collectors.toList());
    }

    @Override
    public Component buttonFor(final ButtonRequest request) {

        val managedAction = request.getManagedAction();
        val disablingUiModelIfAny = request.getDisablingUiModelIfAny();
        val actionEventHandler = request.getActionEventHandler();

        val uiButton = _vaa.newButton(managedAction.getFriendlyName());

        disablingUiModelIfAny.ifPresent(disablingUiModel->{
//            uiContext.getDisablingDecoratorForButton()
//                .decorate(uiButton, disablingUiModel);
            uiButton.setEnabled(false);
        });

        if(!disablingUiModelIfAny.isPresent()) {
            uiButton.addClickListener(event->actionEventHandler.accept(managedAction));
        }

        return uiButton;

    }

    @Override
    public Component componentFor(final ComponentRequest request) {
        return chainOfHandlers.handle(request);
    }

    @Override
    public Component parameterFor(final ComponentRequest request) {
        return chainOfHandlers.handle(request);
    }

    @Override
    public LabelAndPosition<Component> labelFor(final ComponentRequest request) {
        throw _Exceptions.unsupportedOperation("unlikely to be needed for Vaadin, "
                + "since Field components already have their own label");
    }


}
