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
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.Model;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.NotInServiceMenuFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ServiceActionsModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.CssClassFaBehavior;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;

public final class ServiceActionUtil {

    private ServiceActionUtil(){}

    static void addLeafItem(final CssMenuItem menuItem, final ListItem<CssMenuItem> listItem, final MarkupContainer parent) {
        
    	Fragment leafItem;
        if (!menuItem.isSeparator()) {
            leafItem = new Fragment("content", "leafItem", parent);

            AbstractLink subMenuItemLink = menuItem.getLink();

            Label menuItemLabel = new Label("menuLinkLabel", menuItem.getName());
            subMenuItemLink.addOrReplace(menuItemLabel);

            if (!menuItem.isEnabled()) {
                listItem.add(new CssClassAppender("disabled"));
                subMenuItemLink.setEnabled(false);
                TooltipBehavior tooltipBehavior = new TooltipBehavior(Model.of(menuItem.getDisabledReason()));
                listItem.add(tooltipBehavior);
            } else {
            	
            	if(!Strings.isNullOrEmpty(menuItem.getDescription())) {
                	//XXX ISIS-1625, tooltips for menu actions 
                	listItem.add(new AttributeModifier("title", Model.of(menuItem.getDescription())));
                	
                	// ISIS-1615, prevent bootstrap from changing the HTML link's 'title' attribute on client-side;
                    // bootstrap will not touch the 'title' attribute once the HTML link has a 'data-original-title' attribute
                	subMenuItemLink.add(new AttributeModifier("data-original-title", ""));
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
            if (Strings.isNullOrEmpty(cssClassFa)) {
                subMenuItemLink.add(new CssClassAppender("menuLinkSpacer"));
            } else {
                menuItemLabel.add(new CssClassFaBehavior(cssClassFa, menuItem.getCssClassFaPosition()));
            }

            String cssClass = menuItem.getCssClass();
            if (!Strings.isNullOrEmpty(cssClass)) {
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
            List<CssMenuItem> applySeparatorStrategy(final CssMenuItem subMenuItem) {
                return withSeparators(subMenuItem);
            }

        },
        WITHOUT_SEPARATORS {
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
        final List<CssMenuItem> itemsWithSeparators = Lists.newArrayList();
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

    static void addFolderItem(final CssMenuItem subMenuItem, final ListItem<CssMenuItem> listItem, final MarkupContainer parent, final SeparatorStrategy separatorStrategy) {
        listItem.add(new CssClassAppender("dropdown-submenu"));

        Fragment folderItem = new Fragment("content", "folderItem", parent);
        listItem.add(folderItem);

        folderItem.add(new Label("folderName", subMenuItem.getName()));
        final List<CssMenuItem> menuItems = separatorStrategy.applySeparatorStrategy(subMenuItem);
        ListView<CssMenuItem> subMenuItemsView = new ListView<CssMenuItem>("subMenuItems",
                menuItems) {
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

    public static List<CssMenuItem> buildMenu(final ServiceActionsModel appActionsModel) {

        final List<ObjectAdapter> serviceAdapters = appActionsModel.getObject();

        final List<ServiceAndAction> serviceActions = Lists.newArrayList();
        for (final ObjectAdapter serviceAdapter : serviceAdapters) {
            collateServiceActions(serviceAdapter, ActionType.USER, serviceActions);
            collateServiceActions(serviceAdapter, ActionType.PROTOTYPE, serviceActions);
        }

        final Set<String> serviceNamesInOrder = serviceNamesInOrder(serviceAdapters, serviceActions);
        final Map<String, List<ServiceAndAction>> serviceActionsByName = groupByServiceName(serviceActions);

        // prune any service names that have no service actions
        serviceNamesInOrder.retainAll(serviceActionsByName.keySet());

        return buildMenuItems(serviceNamesInOrder, serviceActionsByName);
    }

    /**
     * Builds a hierarchy of {@link CssMenuItem}s, following the provided map of {@link ServiceAndAction}s (keyed by their service Name).
     */
    private static List<CssMenuItem> buildMenuItems(
            final Set<String> serviceNamesInOrder,
            final Map<String, List<ServiceAndAction>> serviceActionsByName) {

        final List<CssMenuItem> menuItems = Lists.newArrayList();
        for (String serviceName : serviceNamesInOrder) {
            final CssMenuItem serviceMenuItem = CssMenuItem.newMenuItem(serviceName).build();
            final List<ServiceAndAction> serviceActionsForName = serviceActionsByName.get(serviceName);
            for (ServiceAndAction serviceAndAction : serviceActionsForName) {

                final CssMenuItem.Builder subMenuItemBuilder = serviceMenuItem.newSubMenuItem(serviceAndAction);
                if (subMenuItemBuilder == null) {
                    // either service or this action is not visible
                    continue;
                }
                subMenuItemBuilder.build();
            }
            if (serviceMenuItem.hasSubMenuItems()) {
                menuItems.add(serviceMenuItem);
            }
        }
        return menuItems;
    }


    // //////////////////////////////////////

    /**
     * Spin through all object actions of the service adapter, and add to the provided List of {@link ServiceAndAction}s.
     */
    private static void collateServiceActions(
            final ObjectAdapter serviceAdapter,
            final ActionType actionType,
            final List<ServiceAndAction> serviceActions) {

        final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();

        // skip if annotated to not be included in repository menu using @DomainService
        final DomainServiceFacet domainServiceFacet = serviceSpec.getFacet(DomainServiceFacet.class);
        if (domainServiceFacet != null) {
            final NatureOfService natureOfService = domainServiceFacet.getNatureOfService();
            if (natureOfService == NatureOfService.VIEW_REST_ONLY ||
                    natureOfService == NatureOfService.VIEW_CONTRIBUTIONS_ONLY ||
                    natureOfService == NatureOfService.DOMAIN) {
                return;
            }
        }

        for (final ObjectAction objectAction : serviceSpec.getObjectActions(
                actionType, Contributed.INCLUDED, Filters.<ObjectAction>any())) {

            // skip if annotated to not be included in repository menu using legacy mechanism
            if (objectAction.getFacet(NotInServiceMenuFacet.class) != null) {
                continue;
            }

            final MemberOrderFacet memberOrderFacet = objectAction.getFacet(MemberOrderFacet.class);
            String serviceName = memberOrderFacet != null? memberOrderFacet.name(): null;
            if(Strings.isNullOrEmpty(serviceName)){
                serviceName = serviceSpec.getFacet(NamedFacet.class).value();
            }
            final EntityModel serviceModel = new EntityModel(serviceAdapter);
            serviceActions.add(new ServiceAndAction(serviceName, serviceModel, objectAction));
        }
    }

    /**
     * The unique service names, as they appear in order of the provided List of {@link ServiceAndAction}s.
     * @param serviceAdapters
     */
    private static Set<String> serviceNamesInOrder(
            final List<ObjectAdapter> serviceAdapters, final List<ServiceAndAction> serviceActions) {
        final Set<String> serviceNameOrder = Sets.newLinkedHashSet();

        // first, order as defined in isis.properties
        for (ObjectAdapter serviceAdapter : serviceAdapters) {
            final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
            String serviceName = serviceSpec.getFacet(NamedFacet.class).value();
            serviceNameOrder.add(serviceName);
        }
        // then, any other services (eg due to misspellings, at the end)
        for (ServiceAndAction serviceAction : serviceActions) {
            if(!serviceNameOrder.contains(serviceAction.serviceName)) {
                serviceNameOrder.add(serviceAction.serviceName);
            }
        }
        return serviceNameOrder;
    }

    /**
     * Group the provided {@link ServiceAndAction}s by their service name.
     */
    private static Map<String, List<ServiceAndAction>> groupByServiceName(final List<ServiceAndAction> serviceActions) {
        final Map<String, List<ServiceAndAction>> serviceActionsByName = Maps.newTreeMap();

        // map available services
        ObjectAdapter lastServiceAdapter = null;

        for (ServiceAndAction serviceAction : serviceActions) {
            List<ServiceAndAction> serviceActionsForName = serviceActionsByName.get(serviceAction.serviceName);

            final ObjectAdapter serviceAdapter =
                    serviceAction.serviceEntityModel.load(AdapterManager.ConcurrencyChecking.NO_CHECK);

            if(serviceActionsForName == null) {
                serviceActionsForName = Lists.newArrayList();
                serviceActionsByName.put(serviceAction.serviceName, serviceActionsForName);
            } else {
                // capture whether this action is from a different service; if so, add a separator before it
                serviceAction.separator = lastServiceAdapter != serviceAdapter;
            }
            serviceActionsForName.add(serviceAction);
            lastServiceAdapter = serviceAdapter;
        }

        return serviceActionsByName;
    }
    
    private static void addConfirmationDialog(Component component, final ServicesInjector servicesInjector) {
        
        final TranslationService translationService =
                servicesInjector.lookupService(TranslationService.class);
    	
    	ConfirmationConfig confirmationConfig = new ConfirmationConfig();

        final String context = IsisSessionFactoryBuilder.class.getName();
        final String areYouSure = translationService.translate(context, IsisSystem.MSG_ARE_YOU_SURE);
        final String confirm = translationService.translate(context, IsisSystem.MSG_CONFIRM);
        final String cancel = translationService.translate(context, IsisSystem.MSG_CANCEL);

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
