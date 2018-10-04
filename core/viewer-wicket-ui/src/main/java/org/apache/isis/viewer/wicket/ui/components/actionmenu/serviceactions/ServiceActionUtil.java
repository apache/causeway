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

package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.applib.layout.menubars.MenuSection;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.SystemConstants;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ServiceActionsModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;

public final class ServiceActionUtil {

    private final static Logger LOG = LoggerFactory.getLogger(ServiceActionUtil.class);

    private ServiceActionUtil(){}

    static void addLeafItem(
            final CssMenuItem menuItem,
            final ListItem<CssMenuItem> listItem,
            final MarkupContainer parent) {

        Fragment leafItem;
        if (!menuItem.isSeparator()) {
            leafItem = new Fragment("content", "leafItem", parent);

            AbstractLink subMenuItemLink = menuItem.getLink();

            Label menuItemLabel = new Label("menuLinkLabel", menuItem.getName());
            subMenuItemLink.addOrReplace(menuItemLabel);

            listItem.add(new CssClassAppender("isis-" + CssClassAppender.asCssStyle(menuItem.getActionIdentifier())));
            if (!menuItem.isEnabled()) {
                listItem.add(new CssClassAppender("disabled"));
                subMenuItemLink.setEnabled(false);
                
                Tooltips.addTooltip(listItem, menuItem.getDisabledReason());
                
                
            } else {

                if(!_Strings.isNullOrEmpty(menuItem.getDescription())) {
                    Tooltips.addTooltip(listItem, menuItem.getDescription());
                }

                //XXX ISIS-1626, confirmation dialog for no-parameter menu actions
                if (menuItem.requiresImmediateConfirmation()) {
                    addConfirmationDialog(
                            subMenuItemLink,
                            menuItem.getPersistenceSession().getServicesInjector());
                }

            }
            if (menuItem.isPrototyping()) {
                subMenuItemLink.add(new CssClassAppender("prototype"));
            }
            leafItem.add(subMenuItemLink);

            String cssClassFa = menuItem.getCssClassFa();
            if (_Strings.isNullOrEmpty(cssClassFa)) {
                subMenuItemLink.add(new CssClassAppender("menuLinkSpacer"));
            } else {
                menuItemLabel.add(new CssClassFaBehavior(cssClassFa, menuItem.getCssClassFaPosition()));
            }

            String cssClass = menuItem.getCssClass();
            if (!_Strings.isNullOrEmpty(cssClass)) {
                subMenuItemLink.add(new CssClassAppender(cssClass));
            }
        } else {
            leafItem = new Fragment("content", "empty", parent);
            listItem.add(new CssClassAppender("divider"));
        }
        listItem.add(leafItem);
    }


    enum SeparatorStrategy {
        WITH_SEPARATORS {
            @Override
            List<CssMenuItem> applySeparatorStrategy(final CssMenuItem subMenuItem) {
                return withSeparators(subMenuItem);
            }

        },
        WITHOUT_SEPARATORS {
            @Override
            List<CssMenuItem> applySeparatorStrategy(final CssMenuItem subMenuItem) {
                final List<CssMenuItem> subMenuItems = subMenuItem.getSubMenuItems();
                return subMenuItems;
            }
        };

        abstract List<CssMenuItem> applySeparatorStrategy(final CssMenuItem subMenuItem);
    }

    static List<CssMenuItem> withSeparators(CssMenuItem subMenuItem) {
        final List<CssMenuItem> subMenuItems = subMenuItem.getSubMenuItems();
        final List<CssMenuItem> cssMenuItemsWithSeparators = withSeparators(subMenuItems);
        subMenuItem.replaceSubMenuItems(cssMenuItemsWithSeparators);
        return cssMenuItemsWithSeparators;
    }

    static List<CssMenuItem> withSeparators(List<CssMenuItem> subMenuItems) {
        final List<CssMenuItem> itemsWithSeparators = _Lists.newArrayList();
        for (CssMenuItem menuItem : subMenuItems) {
            if(menuItem.requiresSeparator()) {
                if(!itemsWithSeparators.isEmpty()) {
                    // bit nasty... we add a new separator item
                    final CssMenuItem separatorItem = CssMenuItem.newMenuItem(menuItem.getName() + "-separator")
                            .prototyping(menuItem.isPrototyping())
                            .build();
                    separatorItem.setSeparator(true);
                    itemsWithSeparators.add(separatorItem);
                }
                menuItem.setRequiresSeparator(false);
            }
            itemsWithSeparators.add(menuItem);
        }
        return itemsWithSeparators;
    }

    static void addFolderItem(
            final CssMenuItem subMenuItem,
            final ListItem<CssMenuItem> listItem,
            final MarkupContainer parent,
            final SeparatorStrategy separatorStrategy) {

        listItem.add(new CssClassAppender("dropdown-submenu"));

        Fragment folderItem = new Fragment("content", "folderItem", parent);
        listItem.add(folderItem);

        folderItem.add(new Label("folderName", subMenuItem.getName()));
        final List<CssMenuItem> menuItems = separatorStrategy.applySeparatorStrategy(subMenuItem);
        ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems",
                menuItems) {
             private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<CssMenuItem> listItem) {
                CssMenuItem subMenuItem = listItem.getModelObject();

                if (subMenuItem.hasSubMenuItems()) {
                    addFolderItem(subMenuItem, listItem, parent, SeparatorStrategy.WITHOUT_SEPARATORS);
                } else {
                    addLeafItem(subMenuItem, listItem, parent);
                }
            }
        };
        folderItem.add(subMenuItemsView);
    }

    public static List<CssMenuItem> buildMenu(
            final MenuBars menuBars,
            final ServiceActionsModel serviceActionsModel) {

        // TODO: remove hard-coded dependency on BS3
        final BS3MenuBar menuBar = (BS3MenuBar) menuBars.menuBarFor(serviceActionsModel.getMenuBar());

        // we no longer use ServiceActionsModel#getObject() because the model only holds the services for the
        // menuBar in question, whereas the "Other" menu may reference a service which is defined for some other menubar
        final Stream<ObjectAdapter> serviceAdapters = 
                IsisContext.getSessionFactory().getCurrentSession().getPersistenceSession().streamServices();

        final Map<String, ObjectAdapter> serviceAdapterBySpecId = 
                serviceAdapters
                .collect(Collectors.toMap(a->a.getSpecification().getSpecId().asString(), a->a, (o,n)->n, HashMap::new));

        final List<CssMenuItem> menuItems = _Lists.newArrayList();
        for (final BS3Menu menu : menuBar.getMenus()) {

            final CssMenuItem serviceMenu = CssMenuItem.newMenuItem(menu.getNamed()).build();

            for (final MenuSection menuSection : menu.getSections()) {

                boolean firstSection = true;

                for (final ServiceActionLayoutData actionLayoutData : menuSection.getServiceActions()) {
                    final String objectTypeLiteral = actionLayoutData.getObjectType();
                    
                    final ObjectAdapter serviceAdapter = serviceAdapterBySpecId.get(objectTypeLiteral);
                    if(serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync with actual configured modules
                        continue;
                    }
                    final EntityModel entityModel = new EntityModel(serviceAdapter);
                    final ObjectAction objectAction = serviceAdapter.getSpecification()
                            .getObjectAction(actionLayoutData.getId());
                    if(objectAction == null) {
                        LOG.warn("No such action {}", actionLayoutData.getId());
                        continue;
                    }
                    final ServiceAndAction serviceAndAction =
                            new ServiceAndAction(actionLayoutData.getNamed(), entityModel, objectAction);

                    if(firstSection) {
                        serviceAndAction.separator = true;
                        firstSection = false;
                    }

                    final CssMenuItem.Builder subMenuItemBuilder = serviceMenu.newSubMenuItem(serviceAndAction);
                    if (subMenuItemBuilder == null) {
                        // either service or this action is not visible
                        continue;
                    }
                    subMenuItemBuilder.build();
                }
            }
            if (serviceMenu.hasSubMenuItems()) {
                menuItems.add(serviceMenu);
            }
        }
        return menuItems;
    }


    private static void addConfirmationDialog(
            final Component component,
            final ServicesInjector servicesInjector) {

        final TranslationService translationService =
                servicesInjector.lookupServiceElseFail(TranslationService.class);

        ConfirmationConfig confirmationConfig = new ConfirmationConfig();

        final String context = IsisSessionFactoryBuilder.class.getName();
        final String areYouSure = translationService.translate(context, SystemConstants.MSG_ARE_YOU_SURE);
        final String confirm = translationService.translate(context, SystemConstants.MSG_CONFIRM);
        final String cancel = translationService.translate(context, SystemConstants.MSG_CANCEL);

        confirmationConfig
        .withTitle(areYouSure)
        .withBtnOkLabel(confirm)
        .withBtnCancelLabel(cancel)
        .withPlacement(TooltipConfig.Placement.bottom)
        .withBtnOkClass("btn btn-danger")
        .withBtnCancelClass("btn btn-default");
        
        component.add(new ConfirmationBehavior(null, confirmationConfig));
    }

}
