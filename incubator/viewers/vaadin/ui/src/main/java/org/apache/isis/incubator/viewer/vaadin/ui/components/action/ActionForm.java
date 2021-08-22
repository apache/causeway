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
package org.apache.isis.incubator.viewer.vaadin.ui.components.action;

import com.vaadin.flow.component.formlayout.FormLayout;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.viewer.common.model.components.UiComponentFactory.ComponentRequest;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ActionForm extends FormLayout {

    private static final long serialVersionUID = 1L;

    @Getter
    private final ParameterNegotiationModel pendingArgs;

    public static ActionForm forManagedAction(
            final @NonNull UiComponentFactoryVaa uiComponentFactory,
            final @NonNull ManagedAction managedAction) {

        val actionForm = new ActionForm(uiComponentFactory, managedAction);
        return actionForm;
    }

    protected ActionForm(
            final UiComponentFactoryVaa uiComponentFactory,
            final ManagedAction managedAction) {

        pendingArgs = managedAction.startParameterNegotiation();

        pendingArgs.getParamModels().forEach(paramModel->{

            //val paramNr = paramModel.getParamNr(); // zero based

            val request = ComponentRequest.of(paramModel);

            //val labelAndPosition = uiComponentFactory.labelFor(request);
            val uiField = uiComponentFactory.parameterFor(request);
            super.add(uiField);

        });

    }

}
