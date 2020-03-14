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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.inject.Inject;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.applib.services.menu.MenuBarsService.Type;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.entity.EntityUiModel;
import org.apache.isis.incubator.viewer.vaadin.model.menu.MenuSectionUiModel;
import org.apache.isis.incubator.viewer.vaadin.model.menu.ServiceAndActionUiModel;
import org.apache.isis.incubator.viewer.vaadin.ui.auth.VaadinAuthenticationHandler;
import org.apache.isis.incubator.viewer.vaadin.ui.components.collection.TableView;
import org.apache.isis.incubator.viewer.vaadin.ui.components.object.ObjectFormView;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Route()
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
//@Theme(value = Material.class, variant = Material.DARK)
@Theme(value = Lumo.class)
@Log4j2
public class MainView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private final transient VaadinAuthenticationHandler vaadinAuthenticationHandler;
    
    /**
     * Constructs the main view of the web-application, with the menu-bar and page content. 
     */
    @Inject
    public MainView(
            final MetaModelContext metaModelContext,
            final VaadinAuthenticationHandler vaadinAuthenticationHandler,
            final MenuBarsServiceBS3 menuBarsService
    ) {
        this.vaadinAuthenticationHandler = vaadinAuthenticationHandler;
        
        val commonContext = IsisWebAppCommonContext.of(metaModelContext);

        val titleOrLogo = createTitleOrLogo(commonContext);
        val leftMenuBar = new MenuBar();
        val horizontalSpacer = new Div();
        horizontalSpacer.setWidthFull();
        val rightMenuBar = new MenuBar();
        
        // holds the top level left and right aligned menu parts
        // TODO does not honor small displays yet, overflow is just not visible
        val menuBarContainer = new HorizontalLayout(titleOrLogo, leftMenuBar, horizontalSpacer, rightMenuBar);
        add(menuBarContainer);
        //menuBarContainer.setJustifyContentMode(JustifyContentMode.BETWEEN);
        menuBarContainer.setFlexGrow(0, titleOrLogo, leftMenuBar, rightMenuBar);
        menuBarContainer.setFlexGrow(1, horizontalSpacer);
        menuBarContainer.setWidthFull();
        
        val selectedMenuItem = new Text("");
        val actionResult = new VerticalLayout();
        val message = new Div(new Text("Selected: "), selectedMenuItem);
        
        add(message);
        add(actionResult);

        // menu section handler, that creates and adds sub-menus to their parent top level menu   
        final BiConsumer<MenuBar, MenuSectionUiModel> menuSectionBuilder = (parentMenu, menuSectionUiModel) -> {
            val menuItem = parentMenu.addItem(menuSectionUiModel.getName());
            val subMenu = menuItem.getSubMenu();
            menuSectionUiModel.getServiceAndActionUiModels().forEach(saModel ->
                    createActionOverviewAndBindRunAction(selectedMenuItem, actionResult, subMenu, saModel));
        };
        
        val bs3MenuBars = menuBarsService.menuBars(Type.DEFAULT);
        
        // top level left aligned ...
        buildMenuModel(commonContext, bs3MenuBars.getPrimary(), menuSectionUiModel->
            menuSectionBuilder.accept(leftMenuBar, menuSectionUiModel));
        
        // top level right aligned ...
        buildMenuModel(commonContext, bs3MenuBars.getSecondary(), menuSectionUiModel->
            menuSectionBuilder.accept(rightMenuBar, menuSectionUiModel));
        // TODO tertiary menu items should get collected under a top level menu labeled with the current user's name 
        buildMenuModel(commonContext, bs3MenuBars.getTertiary(), menuSectionUiModel->
            menuSectionBuilder.accept(rightMenuBar, menuSectionUiModel));
        
        setWidthFull();
    }
    
    private Component createTitleOrLogo(IsisWebAppCommonContext commonContext) {
        
        val isisConfiguration = commonContext.getConfiguration(); 
        val webAppContextPath = commonContext.getWebAppContextPath();
        
        //TODO application name/logo borrowed from Wicket's configuration, we might generalize this config option to all viewers
        val applicationName = isisConfiguration.getViewer().getWicket().getApplication().getName();
        val applicationLogo = isisConfiguration.getViewer().getWicket().getApplication().getBrandLogoHeader();
        
        if(applicationLogo.isPresent()) {
            return new Image(webAppContextPath.prependContextPathIfLocal(applicationLogo.get()), "logo");
        }
        
        return new Text(applicationName);
        
    }

    private void createActionOverviewAndBindRunAction(
            final Text selected,
            final VerticalLayout actionResultDiv,
            final SubMenu subMenu,
            final ServiceAndActionUiModel saModel
    ) {
        val objectAction = saModel.getObjectAction();
        subMenu.addItem(objectAction.getName(),
                e -> {
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
        );
    }

    private ComponentEventListener<ClickEvent<Button>> executeAndHandleResultAction(
            final ServiceAndActionUiModel saModel,
            final ObjectAction objectAction,
            final Div actionResult
    ) {
        return buttonClickEvent -> {
            
            actionResult.removeAll();
            
            val actionOwner = saModel.getEntityUiModel().getManagedObject();
            
            vaadinAuthenticationHandler.runAuthenticated(()->{ 
                val result = objectAction
                        .execute(
                                actionOwner,
                                null,
                                Collections.emptyList(),
                                InteractionInitiatedBy.USER
                                );

                if (result.getSpecification().isParentedOrFreeCollection()) {
                    actionResult.add(new TableView(result));
                } else {
                    actionResult.add(new ObjectFormView(result));
                }
                
            });
            
        };
    }
    
    // initially copied from org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions.ServiceActionUtil.buildMenu
    private void buildMenuModel(
            final IsisWebAppCommonContext commonContext,
            final BS3MenuBar menuBar,
            Consumer<MenuSectionUiModel> onMenuSection
    ) {

        // we no longer use ServiceActionsModel#getObject() because the model only holds the services for the
        // menuBar in question, whereas the "Other" menu may reference a service which is defined for some other menubar

        for (val menu : menuBar.getMenus()) {

            val menuSectionUiModel = new MenuSectionUiModel(menu.getNamed());

            for (val menuSection : menu.getSections()) {

                boolean isFirstSection = true;

                for (val serviceActionLayoutData : menuSection.getServiceActions()) {
                    val serviceSpecId = serviceActionLayoutData.getObjectType();

                    val serviceAdapter = commonContext.lookupServiceAdapterById(serviceSpecId);
                    if (serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync
                        // with actual configured modules
                        continue;
                    }
                    // TODO Wicket final EntityModel entityModel = EntityModel.ofAdapter(commonContext, serviceAdapter);
                    val entityUiModel =
                            new EntityUiModel(commonContext, serviceAdapter);

                    val objectAction =
                            serviceAdapter
                                    .getSpecification()
                                    .getObjectAction(serviceActionLayoutData.getId())
                                    .orElse(null);
                    if (objectAction == null) {
                        log.warn("No such action {}", serviceActionLayoutData.getId());
                        continue;
                    }
                    val serviceAndActionUiModel =
                            new ServiceAndActionUiModel(
                                    entityUiModel,
                                    serviceActionLayoutData.getNamed(),
                                    objectAction,
                                    isFirstSection);

                    menuSectionUiModel.addAction(serviceAndActionUiModel);
                    isFirstSection = false;

                    // TODO Wicket
                    //                    final CssMenuItem.Builder subMenuItemBuilder = menuSectionModel.newSubMenuItem(serviceAndAction);
                    //                    if (subMenuItemBuilder == null) {
                    //                        // either service or this action is not visible
                    //                        continue;
                    //                    }
                    //                    subMenuItemBuilder.build();
                }
            }
            if (menuSectionUiModel.hasSubMenuItems()) {
                onMenuSection.accept(menuSectionUiModel);
            }
        }
    }

}