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
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContext;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.collections.TableViewFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.object.ObjectViewFx;
import org.apache.isis.viewer.common.model.binding.UiComponentFactory.ComponentRequest;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiActionHandler {

    private final UiContext uiContext;
    private final UiComponentFactoryFx uiComponentFactory;

    public void handleActionLinkClicked(ManagedAction managedAction, Consumer<Node> onNewPageContent) {

        log.info("about to build an action prompt for {}", managedAction.getIdentifier());
        
        final int paramCount = managedAction.getAction().getParameterCount();
        
        if(paramCount>0) {
            // TODO get an ActionPrompt, then on invocation show the result in the content view
            
            //Dialogs.message("Warn", "ActionPrompt not supported yet!", null);
            
            val pendingArgs = managedAction.startParameterNegotiation();
            
            
            Dialog<ParameterNegotiationModel> dialog = new Dialog<>();
            dialog.setTitle("<Title>");
            dialog.setHeaderText("<HeaderText>");
           
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            val grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            dialog.getDialogPane().setContent(grid);
            
            pendingArgs.getParamModels().forEach(paramModel->{
                
                val paramNr = paramModel.getParamNr(); // zero based
                
                val request = ComponentRequest.of(paramModel);
                
                val labelAndPosition = uiComponentFactory.labelFor(request);
                val uiField = uiComponentFactory.parameterFor(request);
                
                grid.add(labelAndPosition.getUiLabel(), 0, paramNr);
                grid.add(uiField, 1, paramNr);
                
            });
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return pendingArgs;
                }
                return null;
            });
            
            dialog.showAndWait().ifPresent(params->{
                System.out.println("param negotiation result");
            });
            
            return;
        }
        
        uiContext.getIsisInteractionFactory().runAnonymous(()->{

            //Thread.sleep(1000); // simulate long running

            val actionResultOrVeto = managedAction.invoke(Can.empty());
            
            actionResultOrVeto.left()
            .ifPresent(actionResult->handleActionResult(actionResult, onNewPageContent));

        });

    }
    
    public void handleActionResult(ManagedObject actionResult, Consumer<Node> onNewPageContent) {
        onNewPageContent.accept(uiComponentForActionResult(actionResult, onNewPageContent));
    }
    
    private Node uiComponentForActionResult(ManagedObject actionResult, Consumer<Node> onNewPageContent) {
        if (actionResult.getSpecification().isParentedOrFreeCollection()) {
            return TableViewFx.fromCollection(uiContext, actionResult);
        } else {
            return ObjectViewFx.fromObject(
                    uiContext,
                    uiComponentFactory, 
                    action->handleActionLinkClicked(action, onNewPageContent), 
                    actionResult);
        }
    }


}
