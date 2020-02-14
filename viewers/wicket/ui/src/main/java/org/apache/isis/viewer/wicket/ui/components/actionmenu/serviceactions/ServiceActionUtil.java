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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;

import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.applib.layout.menubars.MenuSection;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.config.messages.MessageRegistry;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ServiceActionsModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;

@Log4j2
public final class ServiceActionUtil {

    private ServiceActionUtil(){}

    static void addLeafItem(
            IsisWebAppCommonContext commonContext, 
            CssMenuItem menuItem,
            ListItem<CssMenuItem> listItem,
            MarkupContainer parent) {

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
                    addConfirmationDialog(commonContext, subMenuItemLink);
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
            IsisWebAppCommonContext commonContext,
            CssMenuItem subMenuItem,
            ListItem<CssMenuItem> listItem,
            MarkupContainer parent,
            SeparatorStrategy separatorStrategy) {

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
                    addFolderItem(commonContext, subMenuItem, listItem, parent, SeparatorStrategy.WITHOUT_SEPARATORS);
                } else {
                    addLeafItem(commonContext, subMenuItem, listItem, parent);
                }
            }
        };
        folderItem.add(subMenuItemsView);
    }

    public static List<CssMenuItem> buildMenu(
            IsisWebAppCommonContext commonContext,
            MenuBars menuBars,
            ServiceActionsModel serviceActionsModel) {

        // TODO: remove hard-coded dependency on BS3
        final BS3MenuBar menuBar = (BS3MenuBar) menuBars.menuBarFor(serviceActionsModel.getMenuBar());

        // we no longer use ServiceActionsModel#getObject() because the model only holds the services for the
        // menuBar in question, whereas the "Other" menu may reference a service which is defined for some other menubar

        final List<CssMenuItem> menuItems = _Lists.newArrayList();
        for (final BS3Menu menu : menuBar.getMenus()) {

            final CssMenuItem serviceMenu = CssMenuItem.newMenuItem(menu.getNamed()).build();

            for (final MenuSection menuSection : menu.getSections()) {

                boolean isFirstSection = true;

                for (final ServiceActionLayoutData actionLayoutData : menuSection.getServiceActions()) {
                    val serviceSpecId = actionLayoutData.getObjectType();

                    val serviceAdapter = commonContext.lookupServiceAdapterById(serviceSpecId);
                    if(serviceAdapter == null) {
                        // service not recognized, presumably the menu layout is out of sync with actual configured modules
                        continue;
                    }
                    final EntityModel entityModel = EntityModel.ofAdapter(commonContext, serviceAdapter);
                    final ObjectAction objectAction = serviceAdapter.getSpecification()
                            .getObjectAction(actionLayoutData.getId()).orElse(null);
                    if(objectAction == null) {
                        log.warn("No such action {}", actionLayoutData.getId());
                        continue;
                    }
                    final ServiceAndAction serviceAndAction =
                            new ServiceAndAction(actionLayoutData.getNamed(), entityModel, objectAction, isFirstSection);

                    isFirstSection = false;

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


    private static void addConfirmationDialog(IsisWebAppCommonContext commonContext, Component component) {

        val translationService =
                commonContext.lookupServiceElseFail(TranslationService.class);

        ConfirmationConfig confirmationConfig = new ConfirmationConfig();

        final String context = MessageRegistry.class.getName();
        final String areYouSure = translationService.translate(context, MessageRegistry.MSG_ARE_YOU_SURE);
        final String confirm = translationService.translate(context, MessageRegistry.MSG_CONFIRM);
        final String cancel = translationService.translate(context, MessageRegistry.MSG_CANCEL);

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
