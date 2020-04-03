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

import java.util.Collections;

import javax.inject.Inject;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.menu.ServiceAndActionUiModel;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableView;
import org.apache.isis.incubator.viewer.vaadin.ui.components.object.ObjectFormView;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Route("simple")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
//@Theme(value = Material.class, variant = Material.DARK)
@Theme(value = Lumo.class)
@Log4j2
public class SimpleMainView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs the main view of the web-application, with the menu-bar and page content. 
     */
    @Inject
    public SimpleMainView(
            final MetaModelContext metaModelContext,
            final MenuBarsServiceBS3 menuBarsService) {

        val commonContext = IsisWebAppCommonContext.of(metaModelContext);

        val selectedMenuItem = new Text("");
        val actionResult = new VerticalLayout();
        val message = new Div(new Text("Selected: "), selectedMenuItem);

        val menuBarContainer = MenuUtil.createMenu(commonContext, menuBarsService, saModel ->
            onMenuAction(saModel, selectedMenuItem, actionResult));

        add(menuBarContainer);
        add(message);
        add(actionResult);

        setWidthFull();
    }


    private void onMenuAction(
            final ServiceAndActionUiModel saModel,
            final Text selected,
            final VerticalLayout actionResultDiv) {

        val objectAction = saModel.getObjectAction();
        
        actionResultDiv.removeAll();

        selected.setText(objectAction.toString());
        objectAction.getParameters();
        actionResultDiv.add(new Div(new Text("Name: " + objectAction.getName())));
        actionResultDiv.add(new Div(new Text("Description: " + objectAction.getDescription())));
        actionResultDiv.add(new Div(new Text("Parameters: " + objectAction.getParameters())));
        final Div actionResult = new Div();
        actionResult.setWidthFull();

        if (objectAction.isAction() && objectAction.getParameters().isEmpty()) {
            actionResultDiv.add(new Button("run", executeAndHandleResultAction(saModel, objectAction, actionResult)));
            actionResultDiv.add(actionResult);
        }
        actionResultDiv.setWidthFull();

    }

    private ComponentEventListener<ClickEvent<Button>> executeAndHandleResultAction(
            final ServiceAndActionUiModel saModel,
            final ObjectAction objectAction,
            final Div actionResult) {

        return buttonClickEvent -> {

            actionResult.removeAll();

            val actionOwner = saModel.getEntityUiModel().getManagedObject();

            val result = objectAction
                    .execute(
                            actionOwner,
                            null,
                            Collections.emptyList(),
                            InteractionInitiatedBy.USER
                            );

            if (result.getSpecification().isParentedOrFreeCollection()) {
                actionResult.add(TableView.fromCollection(result));
            } else {
                actionResult.add(new ObjectFormView(result));
            }

        };
    }



}