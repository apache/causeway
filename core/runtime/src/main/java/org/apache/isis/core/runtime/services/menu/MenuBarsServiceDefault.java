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
package org.apache.isis.core.runtime.services.menu;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.menus.ActionLayoutData;
import org.apache.isis.applib.layout.menus.Menu;
import org.apache.isis.applib.layout.menus.MenuBar;
import org.apache.isis.applib.layout.menus.MenuBars;
import org.apache.isis.applib.layout.menus.MenuSection;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.NotInServiceMenuFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

@DomainService(nature = NatureOfService.DOMAIN)
public class MenuBarsServiceDefault implements MenuBarsService {

    MenuBars menuBars;

    @Override @Programmatic
    public MenuBars menuBars() {

        if(menuBars == null) {

            MenuBars menuBars = new MenuBars();

            final List<ObjectAdapter> serviceAdapters =
                    isisSessionFactory.getCurrentSession().getPersistenceSession().getServices();

            append(serviceAdapters, menuBars.getPrimary(), DomainServiceLayout.MenuBar.PRIMARY);
            append(serviceAdapters, menuBars.getSecondary(), DomainServiceLayout.MenuBar.SECONDARY);
            append(serviceAdapters, menuBars.getTertiary(), DomainServiceLayout.MenuBar.TERTIARY);

            this.menuBars = menuBars;
        }

        return menuBars;
    }

    private void append(
            final List<ObjectAdapter> serviceAdapters,
            final MenuBar menuBar,
            final DomainServiceLayout.MenuBar menuBarPos) {

        List<ServiceAndAction> serviceActions = Lists.newArrayList();

        // cf ServiceActionsModel & ServiceActionUtil#buildMenu in Wicket viewer
        for (final ObjectAdapter serviceAdapter : FluentIterable.from(serviceAdapters).filter(with(menuBarPos))) {
            collateServiceActions(serviceAdapter, ActionType.USER, serviceActions);
            collateServiceActions(serviceAdapter, ActionType.PROTOTYPE, serviceActions);
        }

        final Set<String> serviceNamesInOrder = serviceNamesInOrder(serviceAdapters, serviceActions);
        final Map<String, List<ServiceAndAction>> serviceActionsByName = groupByServiceName(serviceActions);

        // prune any service names that have no service actions
        serviceNamesInOrder.retainAll(serviceActionsByName.keySet());

        List<Menu> menus = buildMenuItems(serviceNamesInOrder, serviceActionsByName);
        menuBar.getMenus().addAll(menus);
    }

    private static List<Menu> buildMenuItems(
            final Set<String> serviceNamesInOrder,
            final Map<String, List<ServiceAndAction>> serviceActionsByName) {

        final List<Menu> menus = Lists.newArrayList();
        for (String serviceName : serviceNamesInOrder) {

            Menu menu = new Menu(serviceName);
            menus.add(menu);

            MenuSection menuSection = new MenuSection();
            final List<ServiceAndAction> serviceActionsForName = serviceActionsByName.get(serviceName);
            for (ServiceAndAction serviceAndAction : serviceActionsForName) {

                if(serviceAndAction.separator && !menuSection.getActions().isEmpty()) {
                    menu.getSections().add(menuSection);
                    menuSection = new MenuSection();
                }

                ObjectAction objectAction = serviceAndAction.objectAction;
                final String serviceOid = serviceAndAction.serviceAdapter.getOid().enString();
                ActionLayoutData action = new ActionLayoutData(serviceOid, objectAction.getId());
                action.setNamed(objectAction.getName());
                menuSection.getActions().add(action);
            }
            if(!menuSection.getActions().isEmpty()) {
                menu.getSections().add(menuSection);
            }
        }
        return menus;
    }


    /**
     * The unique service names, as they appear in order of the provided List of {@link ServiceAndAction}s.
     *
     * straight copy from Wicket UI
     */
    private static Set<String> serviceNamesInOrder(
            final List<ObjectAdapter> serviceAdapters,
            final List<ServiceAndAction> serviceActions) {
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
     *
     * straight copy from Wicket UI
     */
    private static Map<String, List<ServiceAndAction>> groupByServiceName(
            final List<ServiceAndAction> serviceActions) {
        final Map<String, List<ServiceAndAction>> serviceActionsByName = Maps.newTreeMap();

        // map available services
        ObjectAdapter lastServiceAdapter = null;

        for (ServiceAndAction serviceAction : serviceActions) {
            List<ServiceAndAction> serviceActionsForName = serviceActionsByName.get(serviceAction.serviceName);

            final ObjectAdapter serviceAdapter = serviceAction.serviceAdapter;

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


    private void collateServiceActions(
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
                actionType, Contributed.INCLUDED, Predicates.<ObjectAction>alwaysTrue())) {

            // skip if annotated to not be included in repository menu using legacy mechanism
            if (objectAction.getFacet(NotInServiceMenuFacet.class) != null) {
                continue;
            }

            final MemberOrderFacet memberOrderFacet = objectAction.getFacet(MemberOrderFacet.class);
            String serviceName = memberOrderFacet != null? memberOrderFacet.name(): null;
            if(Strings.isNullOrEmpty(serviceName)){
                serviceName = serviceSpec.getFacet(NamedFacet.class).value();
            }
            serviceActions.add(new ServiceAndAction(serviceName, serviceAdapter, objectAction));
        }
    }

    private static Predicate<ObjectAdapter> with(final DomainServiceLayout.MenuBar menuBar) {
        return new Predicate<ObjectAdapter>() {
            @Override
            public boolean apply(ObjectAdapter input) {
                final DomainServiceLayoutFacet facet =
                        input.getSpecification().getFacet(DomainServiceLayoutFacet.class);
                return facet != null && facet.getMenuBar() == menuBar;
            }
        };
    }

    @Inject
    IsisSessionFactory isisSessionFactory;


}

