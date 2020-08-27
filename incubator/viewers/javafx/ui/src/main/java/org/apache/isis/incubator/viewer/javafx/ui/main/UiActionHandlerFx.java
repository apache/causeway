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

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContextFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.viewer.common.model.components.UiComponentFactory.ComponentRequest;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiActionHandlerFx {

    private final UiContextFx uiContext;
    private final IsisInteractionFactory isisInteractionFactory;
    private final UiComponentFactoryFx uiComponentFactory;

    public void handleActionLinkClicked(ManagedAction managedAction) {

        log.info("about to build an action prompt for {}", managedAction.getIdentifier());
        
        final int paramCount = managedAction.getAction().getParameterCount();
        
        if(paramCount==0) {
            invoke(managedAction, Can.empty());     
        } else {
            // get an ActionPrompt, then on invocation show the result in the content view
            
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
                log.info("param negotiation done");
                invoke(managedAction, params.getParamValues()); //TODO handle vetoes
            });
            
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
