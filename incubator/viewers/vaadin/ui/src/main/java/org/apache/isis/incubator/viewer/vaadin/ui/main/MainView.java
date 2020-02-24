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
package org.apache.isis.incubator.viewer.vaadin.ui.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.menubars.MenuSection;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.services.menu.MenuBarsService.Type;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.runtimeservices.menubars.bootstrap3.MenuBarsServiceBS3;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.incubator.viewer.vaadin.model.entity.EntityUiModel;
import org.apache.isis.incubator.viewer.vaadin.model.menu.MenuSectionUiModel;
import org.apache.isis.incubator.viewer.vaadin.model.menu.ServiceAndActionUiModel;
import org.apache.isis.incubator.viewer.vaadin.ui.collection.TableView;
import org.apache.isis.incubator.viewer.vaadin.ui.object.ObjectFormView;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Route()
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
//@Theme(value = Material.class, variant = Material.DARK)
@Theme(value = Lumo.class)
@Log4j2
public class MainView extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    public MainView(
            @Autowired final IsisSessionFactory isisSessionFactory,
            @Autowired final SpecificationLoader specificationLoader,
            @Autowired final MetaModelContext metaModelContext,
            @Autowired final IsisConfiguration isisConfiguration
    ) {
        final IsisWebAppCommonContext isisWebAppCommonContext = IsisWebAppCommonContext.of(metaModelContext);

        final MenuBarsServiceBS3 menuBarsService = metaModelContext.getServiceRegistry()
                .lookupServiceElseFail(MenuBarsServiceBS3.class);
        final BS3MenuBars bs3MenuBars = menuBarsService.menuBars(Type.DEFAULT);

        final MenuBar menuBar = new MenuBar();
        final Text selectedMenuItem = new Text("");
        final VerticalLayout actionResult = new VerticalLayout();
        final Div message = new Div(new Text("Selected: "), selectedMenuItem);

        add(menuBar);
        add(message);
        add(actionResult);

        final List<MenuSectionUiModel> menuSectionUiModels = buildMenuModel(log, isisWebAppCommonContext, bs3MenuBars);
        log.warn("menu model:\n ");
        menuSectionUiModels.forEach(m -> log.warn("\t{}", m));

        menuSectionUiModels.forEach(sectionUiModel -> {
                    final MenuItem menuItem = menuBar.addItem(sectionUiModel.getName());
                    final SubMenu subMenu = menuItem.getSubMenu();
                    sectionUiModel.getServiceAndActionUiModels().forEach(a ->
                            createActionOverviewAndBindRunAction(selectedMenuItem, actionResult, subMenu, a));
                }
        );
        setWidthFull();
    }

    private void createActionOverviewAndBindRunAction(
            final Text selected,
            final VerticalLayout actionResultDiv,
            final SubMenu subMenu,
            final ServiceAndActionUiModel a
    ) {
        final ObjectAction objectAction = a.getObjectAction();
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
                        actionResultDiv.add(new Button("run", executeAndHandleResultAction(a, objectAction, actionResult)));
                        actionResultDiv.add(actionResult);
                    }
                    actionResultDiv.setWidthFull();
                }
        );
    }

    private ComponentEventListener<ClickEvent<Button>> executeAndHandleResultAction(
            final ServiceAndActionUiModel a,
            final ObjectAction objectAction,
            final Div actionResult
    ) {
        return buttonClickEvent -> {
            final ManagedObject actionOwner = a.getEntityUiModel().getManagedObject();
            final ManagedObject result = objectAction
                    .execute(
                            actionOwner,
                            null,
                            Collections.emptyList(),
                            InteractionInitiatedBy.USER
                    );
            actionResult.removeAll();
            if (result.getSpecification().isParentedOrFreeCollection()) {
                actionResult.add(new TableView(result));
            } else {
                actionResult.add(new ObjectFormView(result));
            }
        };
    }

    // copied from org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions.ServiceActionUtil.buildMenu
    public static List<MenuSectionUiModel> buildMenuModel(
            final Logger log,
            final IsisWebAppCommonContext commonContext,
            final BS3MenuBars menuBars
    ) {

        // TODO handle menuBars.getSecondary(), menuBars.getTertiary()
        final BS3MenuBar menuBar = menuBars.getPrimary();

        // we no longer use ServiceActionsModel#getObject() because the model only holds the services for the
        // menuBar in question, whereas the "Other" menu may reference a service which is defined for some other menubar

        final List<MenuSectionUiModel> menuSections = new ArrayList<>();
        for (final BS3Menu menu : menuBar.getMenus()) {

            final MenuSectionUiModel menuSectionUiModel = new MenuSectionUiModel(menu.getNamed());

            for (final MenuSection menuSection : menu.getSections()) {

                boolean isFirstSection = true;

                for (final ServiceActionLayoutData actionLayoutData : menuSection.getServiceActions()) {
                    val serviceSpecId = actionLayoutData.getObjectType();

                    final ManagedObject serviceAdapter = commonContext.lookupServiceAdapterById(serviceSpecId);
                    if (serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync
                        // with actual configured modules
                        continue;
                    }
                    // TODO Wicket final EntityModel entityModel = EntityModel.ofAdapter(commonContext, serviceAdapter);
                    final EntityUiModel entityUiModel =
                            new EntityUiModel(commonContext, serviceAdapter);

                    final ObjectAction objectAction =
                            serviceAdapter
                                    .getSpecification()
                                    .getObjectAction(actionLayoutData.getId())
                                    .orElse(null);
                    if (objectAction == null) {
                        log.warn("No such action {}", actionLayoutData.getId());
                        continue;
                    }
                    final ServiceAndActionUiModel serviceAndActionUiModel =
                            new ServiceAndActionUiModel(
                                    entityUiModel,
                                    actionLayoutData.getNamed(),
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
                menuSections.add(menuSectionUiModel);
            }
        }
        return menuSections;
    }

}