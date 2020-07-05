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

import java.util.function.Consumer;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.action.ActionDialog;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableViewVaa;
import org.apache.isis.incubator.viewer.vaadin.ui.components.object.ObjectViewVaa;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiActionHandler {

    private final UiComponentFactoryVaa uiComponentFactory;
    private final IsisInteractionFactory isisInteractionFactory;

    public void handleActionLinkClicked(ManagedAction managedAction, Consumer<Component> onNewPageContent) {

        log.info("about to build an action prompt for {}", managedAction.getIdentifier());
        
        if(managedAction.getAction().getParameterCount()>0) {
            // TODO get an ActionPrompt, then on invocation show the result in the content view
            
            val actionDialog = ActionDialog.forManagedAction(uiComponentFactory, managedAction);
            actionDialog.open();
            
           // Dialogs.message("Warn", "ActionPrompt not supported yet!", null);
            
            return;
        }
        
        val actionResultOrVeto = managedAction.invoke(Can.empty());
        
        actionResultOrVeto.left()
        .ifPresent(actionResult->
                onNewPageContent.accept(uiComponentForActionResult(actionResult, onNewPageContent)));
    }

    private Component uiComponentForActionResult(ManagedObject actionResult, Consumer<Component> onNewPageContent) {
        if (actionResult.getSpecification().isParentedOrFreeCollection()) {
            return TableViewVaa.fromCollection(actionResult);
        } else {
            return ObjectViewVaa.from(
                    uiComponentFactory, 
                    action->handleActionLinkClicked(action, onNewPageContent), 
                    actionResult);
        }
    }

}
