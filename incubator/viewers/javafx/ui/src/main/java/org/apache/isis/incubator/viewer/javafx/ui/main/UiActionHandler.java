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
package org.apache.isis.incubator.viewer.javafx.ui.main;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.collections.TableViewFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.object.ObjectViewFx;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.scene.Node;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiActionHandler {

    private final IsisInteractionFactory isisInteractionFactory;
    private final UiComponentFactoryFx uiComponentFactory;

    public void handleActionLinkClicked(ManagedAction managedAction, Consumer<Node> onNewPageContent) {

        log.info("about to build an action prompt for {}", managedAction.getIdentifier());
        
        // TODO get an ActionPrompt, then on invocation show the result in the content view
        

        isisInteractionFactory.runAnonymous(()->{

            //Thread.sleep(1000); // simulate long running

            val actionResultOrVeto = managedAction.invoke(Can.empty());
            
            actionResultOrVeto.left()
            .ifPresent(actionResult->
                    onNewPageContent.accept(uiComponentForActionResult(actionResult, onNewPageContent)));

        });

    }
    
    private Node uiComponentForActionResult(ManagedObject actionResult, Consumer<Node> onNewPageContent) {
        if (actionResult.getSpecification().isParentedOrFreeCollection()) {
            return TableViewFx.fromCollection(actionResult);
        } else {
            return ObjectViewFx.fromObject(
                    uiComponentFactory, 
                    action->handleActionLinkClicked(action, onNewPageContent), 
                    actionResult);
        }
    }


}
