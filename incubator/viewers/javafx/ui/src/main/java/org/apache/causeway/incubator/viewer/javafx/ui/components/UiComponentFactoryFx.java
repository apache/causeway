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
package org.apache.causeway.incubator.viewer.javafx.ui.components;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.commons.handler.ChainOfResponsibility;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.incubator.viewer.javafx.model.context.UiContextFx;
import org.apache.causeway.viewer.commons.model.components.UiComponentFactory;
import org.apache.causeway.viewer.commons.model.decorators.PrototypingDecorator.PrototypingDecorationModel;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.val;

@Service
public class UiComponentFactoryFx implements UiComponentFactory<Node, Node> {

    private final boolean isPrototyping;
    private final UiContextFx uiContext;
    private final ChainOfResponsibility<ComponentRequest, Node> chainOfHandlers;

    /** handlers in order of precedence (debug info)*/
    @Getter
    private final List<Class<?>> registeredHandlers;

    @Inject
    private UiComponentFactoryFx(
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final UiContextFx uiContext,
            final List<UiComponentHandlerFx> handlers) {

        this.isPrototyping = causewaySystemEnvironment.isPrototyping();
        this.uiContext = uiContext;
        this.chainOfHandlers = ChainOfResponsibility.named("Component Mapper", handlers);
        this.registeredHandlers = handlers.stream()
                .map(Handler::getClass)
                .collect(Collectors.toList());
    }

    @Override
    public Node componentFor(final ComponentRequest request) {

        val formField = chainOfHandlers.handle(request);
        val managedMember = (ManagedMember) request.getManagedFeature();

        request.getDisablingUiModelIfAny().ifPresent(disablingUiModel->{
            uiContext.getDisablingDecoratorForFormField()
            .decorate(formField, disablingUiModel);
        });

        return isPrototyping
                ? uiContext.getPrototypingDecoratorForFormField()
                        .decorate(formField, PrototypingDecorationModel.of(managedMember))
                : formField;
    }

    @Override
    public Node buttonFor(final ButtonRequest request) {

        val managedAction = request.getManagedAction();
        val disablingUiModelIfAny = request.getDisablingUiModelIfAny();
        val actionEventHandler = request.getActionEventHandler();

        val uiButton = new Button(managedAction.getFriendlyName());
        uiButton.setOnAction(event->actionEventHandler.accept(managedAction));

        disablingUiModelIfAny.ifPresent(disablingUiModel->{
            uiContext.getDisablingDecoratorForButton()
            .decorate(uiButton, disablingUiModel);
        });

        return isPrototyping
                ? uiContext.getPrototypingDecoratorForButton()
                        .decorate(uiButton, PrototypingDecorationModel.of(managedAction))
                : uiButton;
    }

    @Override
    public Node parameterFor(final ComponentRequest request) {
        val formField = chainOfHandlers.handle(request);
        return formField;
    }

    @Override
    public LabelAndPosition<Node> labelFor(final ComponentRequest request) {
        val labelPosition = Facets.labelAt(request.getManagedFeature().getMetaModel())
                .orElse(LabelPosition.LEFT);
        val uiLabel = new Label(request.getFriendlyName());
        return LabelAndPosition.of(labelPosition, uiLabel);
    }


}
