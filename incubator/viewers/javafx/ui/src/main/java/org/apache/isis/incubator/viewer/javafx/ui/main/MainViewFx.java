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

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.incubator.viewer.javafx.model.context.UiContextFx;
import org.apache.isis.incubator.viewer.javafx.model.events.JavaFxViewerConfig;
import org.apache.isis.incubator.viewer.javafx.model.util._fx;
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
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class MainViewFx {

    private final JavaFxViewerConfig viewerConfig;
    private final MetaModelContext metaModelContext;
    private final HeaderUiModelProvider headerUiModelProvider;
    private final InteractionService interactionService;
    private final UiContextFx uiContext;
    private final UiActionHandlerFx uiActionHandler;
    private final UiComponentFactoryFx uiComponentFactory;

    @FXML private MenuBar menuBarLeft;
    @FXML private MenuBar menuBarRight;
    @FXML private ScrollPane contentView;
    @FXML private HBox topPane;
    @FXML private VBox contentPane;
    @FXML private TextArea sampleTextArea;

    @FXML
    public void initialize() {
        log.info("about to initialize");

        uiContext.setNewPageHandler(this::replaceContent);
        uiContext.setPageFactory(this::uiComponentForActionResult);

        contentView.setFitToWidth(true);
        contentView.setFitToHeight(true);
        contentView.setHbarPolicy(ScrollBarPolicy.NEVER);
        contentView.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        contentPane.setFillWidth(true);
        //_fx.borderDashed(contentPane, Color.CRIMSON); //debug
        interactionService.runAnonymous(this::buildMenu);

        renderHomepage();
    }

    private void buildMenu() {
        val header = headerUiModelProvider.getHeader();

        val commonContext = IsisAppCommonContext.of(metaModelContext);

        // adding a top level menu 'Home' decorated with a branding-icon ...

        val brandingIcon = new ImageView(viewerConfig.getBrandingIcon());
        brandingIcon.setPreserveRatio(true);

        val menu = _fx.newMenu(menuBarLeft, "Home");
        menu.setGraphic(brandingIcon);
        brandingIcon.fitHeightProperty().set(16);
        _fx.setMenuOnAction(menu, e->renderHomepage());

        // let the MenuBuilderFx populate the menu-bars ...

        val leftMenuBuilder = MenuBuilderFx.of(uiContext, menuBarLeft, uiActionHandler::handleActionLinkClicked);
        val rightMenuBuilder = MenuBuilderFx.of(uiContext, menuBarRight, uiActionHandler::handleActionLinkClicked);

        header.getPrimary().buildMenuItems(commonContext, leftMenuBuilder);
        header.getSecondary().buildMenuItems(commonContext, rightMenuBuilder);
        header.getTertiary().buildMenuItems(commonContext, rightMenuBuilder);
    }

    private void replaceContent(final Node node) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(node);
    }

    private void renderHomepage() {
        log.info("about to render homepage");
        uiContext.route(metaModelContext::getHomePageAdapter);
    }

    private Node uiComponentForActionResult(final ManagedObject actionResult) {
        if (ManagedObjects.isSpecified(actionResult)
                && actionResult.getSpecification().isNonScalar()) {
            return TableViewFx.fromCollection(uiContext, actionResult, Where.STANDALONE_TABLES);
        } else {
            return ObjectViewFx.fromObject(
                    uiContext,
                    uiComponentFactory,
                    uiActionHandler::handleActionLinkClicked,
                    actionResult);
        }
    }


}
