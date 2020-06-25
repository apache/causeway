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

import org.springframework.stereotype.Component;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.iactn.IsisInteractionFactory;
import org.apache.isis.incubator.viewer.javafx.ui.components.UiComponentFactoryFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.collections.TableViewFx;
import org.apache.isis.incubator.viewer.javafx.ui.components.object.ObjectViewFx;
import org.apache.isis.viewer.common.model.header.HeaderUiModelProvider;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class UiController {
    
    private final UiComponentFactoryFx uiComponentFactory;
    private final MetaModelContext metaModelContext;
    private final HeaderUiModelProvider headerUiModelProvider;
    private final IsisInteractionFactory isisInteractionFactory;

    @FXML private MenuBar menuBarLeft;
    @FXML private MenuBar menuBarRight;
    @FXML private ScrollPane contentView;
    @FXML private AnchorPane pageContent;
    @FXML private TextArea sampleTextArea;
    
    @FXML
    public void initialize() {
        log.info("about to initialize");
        isisInteractionFactory.runAnonymous(this::buildMenu);
    }
    
    private void buildMenu() {
        val header = headerUiModelProvider.getHeader();
        
        val commonContext = IsisAppCommonContext.of(metaModelContext);
        
        val leftMenuBuilder = MenuBuilderFx.of(menuBarLeft, this::onMenuAction);
        val rightMenuBuilder = MenuBuilderFx.of(menuBarRight, this::onMenuAction);
        
        header.getPrimary().buildMenuItems(commonContext, leftMenuBuilder);
        header.getSecondary().buildMenuItems(commonContext, rightMenuBuilder);
        header.getTertiary().buildMenuItems(commonContext, rightMenuBuilder);
    }

    private void onMenuAction(ManagedAction managedAction) {
        log.info("about to invoke action {}", managedAction.getIdentifier());
        
        val resultOrVeto = managedAction.invoke(Can.empty());
        
        val result = resultOrVeto.leftIfAny();

        if (result.getSpecification().isParentedOrFreeCollection()) {
            replaceContent(TableViewFx.fromCollection(result));
        } else {
            replaceContent(ObjectViewFx.fromObject(uiComponentFactory, result));
        }
        
        // TODO get an ActionPrompt, then on invocation show the result in the contentView
        sampleTextArea.setText(String.format("action result to be rendered here\nfor %s", managedAction.getIdentifier()));
    }
    
    private void replaceContent(Node node) {
        pageContent.getChildren().clear();
        pageContent.getChildren().add(node);
    }
    
}
