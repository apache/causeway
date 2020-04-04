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

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.action.ActionUiModel;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableView;
import org.apache.isis.incubator.viewer.vaadin.ui.components.object.ObjectFormView;

import lombok.val;

/**
 * top-level view
 */
@Route("main")
@RouteAlias("")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@PWA(name = "Example Project", shortName = "Example Project")
//@Theme(value = Material.class, variant = Material.DARK)
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class MainView extends AppLayout {

    private static final long serialVersionUID = 1L;
    
    private Div pageContent = new Div();
    
    /**
     * Constructs the main view of the web-application, with the menu-bar and page content. 
     */
    @Inject
    public MainView(
            final MetaModelContext metaModelContext,
            final MenuBarsServiceBS3 menuBarsService) {

        val commonContext = IsisWebAppCommonContext.of(metaModelContext);
        
        setPrimarySection(Section.NAVBAR);
        
        val menuBarContainer = MenuUtil.createMenu(commonContext, menuBarsService, this::onMenuAction);
        
        addToNavbar(menuBarContainer);
        
        setContent(pageContent = new Div());
        
        setDrawerOpened(false);
    }

    private void onMenuAction(ActionUiModel saModel) {
        
        pageContent.removeAll();

        val objectAction = saModel.getObjectAction();
        val actionOwner = saModel.getEntityUiModel().getManagedObject();

        val result = objectAction
                .execute(
                        actionOwner,
                        null,
                        Collections.emptyList(),
                        InteractionInitiatedBy.USER
                        );

        if (result.getSpecification().isParentedOrFreeCollection()) {
            pageContent.add(TableView.fromCollection(result));
        } else {
            pageContent.add(new ObjectFormView(result));
        };
    }

    
}
