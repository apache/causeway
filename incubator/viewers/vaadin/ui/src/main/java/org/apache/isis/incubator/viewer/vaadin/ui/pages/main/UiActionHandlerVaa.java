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
package org.apache.isis.incubator.viewer.vaadin.ui.pages.main;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.InteractionFactory;
import org.apache.isis.incubator.viewer.vaadin.model.context.UiContextVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.action.ActionDialog;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiActionHandlerVaa {

    private final UiContextVaa uiContext;
    private final InteractionFactory isisInteractionFactory;
    private final UiComponentFactoryVaa uiComponentFactory;

    public void handleActionLinkClicked(ManagedAction managedAction) {

        log.info("about to build an action prompt for {}", managedAction.getIdentifier());
        
        final int paramCount = managedAction.getAction().getParameterCount();
        
        if(paramCount==0) {
            invoke(managedAction, Can.empty());     
        } else {
            // get an ActionPrompt, then on invocation show the result in the content view
            
            val actionDialog = ActionDialog.forManagedAction(
                    uiComponentFactory, 
                    managedAction,
                    params->{
                        log.info("param negotiation done");
                        invoke(managedAction, params);
                        return true; //TODO handle vetoes
                    });
            actionDialog.open();

            
            return;
        }

    }
    
    private void invoke(
            ManagedAction managedAction, 
            Can<ManagedObject> params) {
        
        isisInteractionFactory.runAnonymous(()->{

            //Thread.sleep(1000); // simulate long running

            val actionResultOrVeto = managedAction.invoke(params);
            
            actionResultOrVeto.left()
            .ifPresent(actionResult->uiContext.route(actionResult));

        });
    }

}
