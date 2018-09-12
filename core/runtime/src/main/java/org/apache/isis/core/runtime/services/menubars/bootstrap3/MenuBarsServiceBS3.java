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
package org.apache.isis.core.runtime.services.menubars.bootstrap3;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuSection;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.menu.MenuBarsLoaderService;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.NotInServiceMenuFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.isis.core.metamodel.services.grid.GridServiceDefault;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

@DomainService(nature = NatureOfService.DOMAIN)
public class MenuBarsServiceBS3 implements MenuBarsService {

    private static final Logger LOG = LoggerFactory.getLogger(MenuBarsServiceBS3.class);

    public static final String MB3_TNS = "http://isis.apache.org/applib/layout/menubars/bootstrap3";
    public static final String MB3_SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/menubars/bootstrap3/menubars.xsd";

    public static final String COMPONENT_TNS = GridServiceDefault.COMPONENT_TNS;
    public static final String COMPONENT_SCHEMA_LOCATION = GridServiceDefault.COMPONENT_SCHEMA_LOCATION;

    public static final String LINKS_TNS = GridServiceDefault.LINKS_TNS;
    public static final String LINKS_SCHEMA_LOCATION = GridServiceDefault.LINKS_SCHEMA_LOCATION;

    BS3MenuBars menuBars;

    @Override
    @Programmatic
    public BS3MenuBars menuBars() {
        return menuBars(Type.DEFAULT);
    }

    @Override
    @Programmatic
    public BS3MenuBars menuBars(final Type type) {

        final BS3MenuBars fallbackMenuBars = deriveMenuBarsFromMetaModelFacets();

        if(type == Type.FALLBACK) {
            return fallbackMenuBars;
        }

        // else load (and only fallback if nothing could be loaded)...
        if(menuBars == null || menuBarsLoaderService.supportsReloading()) {

            BS3MenuBars menuBars = menuBarsLoaderService.menuBars();
            if(menuBars == null) {
                menuBars = fallbackMenuBars;
            }

            menuBars.setTnsAndSchemaLocation(tnsAndSchemaLocation());

            final BS3Menu unreferencedActionsMenu = validate(menuBars);

            if (unreferencedActionsMenu != null) {
                // add in any missing actions from the fallback
                final LinkedHashMap<String, ServiceActionLayoutData> referencedActionsByObjectTypeAndId =
                        menuBars.getAllServiceActionsByObjectTypeAndId();

                fallbackMenuBars.visit(new BS3MenuBars.VisitorAdapter(){

                    @Override
                    public void visit(final BS3MenuSection menuSection) {
                        BS3MenuSection section = null;
                        for (ServiceActionLayoutData serviceAction : menuSection.getServiceActions()) {
                            final String objectTypeAndId = serviceAction.getObjectTypeAndId();
                            if (!referencedActionsByObjectTypeAndId.containsKey(objectTypeAndId)) {
                                if(section == null) {
                                    section = new BS3MenuSection();
                                    unreferencedActionsMenu.getSections().add(section);
                                }
                                // detach from fallback, attach to this
                                serviceAction.setOwner(section);
                                section.getServiceActions().add(serviceAction);
                            }
                        }
                    }
                });

            } else {
                // just use fallback
                menuBars = fallbackMenuBars;
            }

            this.menuBars = menuBars;
        }

        return menuBars;
    }

    BS3Menu validate(final BS3MenuBars menuBars) {

        if (menuBars == null) {
            return null;
        }

        final List<BS3Menu> menusWithUnreferencedActionsFlagSet = Lists.newArrayList();
        menuBars.visit(new BS3MenuBars.VisitorAdapter(){
            @Override public void visit(final BS3Menu menu) {
                if(isSet(menu.isUnreferencedActions())) {
                    menusWithUnreferencedActionsFlagSet.add(menu);
                }
            }
            private Boolean isSet(final Boolean flag) {
                return flag != null && flag;
            }

        });

        final int size = menusWithUnreferencedActionsFlagSet.size();
        if (size == 1) {
            return menusWithUnreferencedActionsFlagSet.get(0);
        }

        menuBars.setMetadataError(
                "Exactly one menu must have 'unreferencedActions' flag set; found " + size + " such menus");
        if(!deploymentCategoryProvider.getDeploymentCategory().isProduction()) {
            messageService.warnUser("Menubars metadata errors; check the error log");
        }
        LOG.error("Menubar layout metadata errors:\n\n{}\n\n", jaxbService.toXml(menuBars));

        return null;
    }

    private BS3MenuBars deriveMenuBarsFromMetaModelFacets() {
        final BS3MenuBars menuBars = new BS3MenuBars();

        final List<ObjectAdapter> serviceAdapters =
                isisSessionFactory.getCurrentSession().getPersistenceSession().getServices();

        final List<ObjectAdapter> visibleServiceAdapters =
                FluentIterable.from(
                        serviceAdapters)
                .filter(new Predicate<ObjectAdapter>() {
                    @Override public boolean apply(final ObjectAdapter objectAdapter) {
                        if (objectAdapter == null) {
                            return false;
                        }
                        if (objectAdapter.getSpecification() == null) {
                            return false;
                        }
                        final ObjectSpecification spec = objectAdapter.getSpecification();
                        if (spec.isHidden()) {
                            // however, this isn't the same as HiddenObjectFacet, so doesn't filter out
                            // services that have an imperative hidden() method.
                            return false;
                        }
                        final DomainServiceFacet facet = spec.getFacet(DomainServiceFacet.class);
                        if (facet == null) {
                            return true;
                        }
                        final NatureOfService natureOfService = facet.getNatureOfService();
                        return natureOfService == null || natureOfService != NatureOfService.DOMAIN;
                    }
                }).toList();

        append(visibleServiceAdapters, menuBars.getPrimary(), DomainServiceLayout.MenuBar.PRIMARY);
        append(visibleServiceAdapters, menuBars.getSecondary(), DomainServiceLayout.MenuBar.SECONDARY);
        append(visibleServiceAdapters, menuBars.getTertiary(), DomainServiceLayout.MenuBar.TERTIARY);

        menuBars.setTnsAndSchemaLocation(tnsAndSchemaLocation());

        final BS3Menu otherMenu = new BS3Menu();
        otherMenu.setNamed("Other");
        otherMenu.setUnreferencedActions(true);
        menuBars.getPrimary().getMenus().add(otherMenu);

        return menuBars;
    }


    private void append(
            final List<ObjectAdapter> serviceAdapters,
            final BS3MenuBar menuBar,
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

        List<BS3Menu> menus = buildMenuItems(serviceNamesInOrder, serviceActionsByName);
        menuBar.getMenus().addAll(menus);
    }

    private static List<BS3Menu> buildMenuItems(
            final Set<String> serviceNamesInOrder,
            final Map<String, List<ServiceAndAction>> serviceActionsByName) {

        final List<BS3Menu> menus = Lists.newArrayList();
        for (String serviceName : serviceNamesInOrder) {

            BS3Menu menu = new BS3Menu(serviceName);
            menus.add(menu);

            BS3MenuSection menuSection = new BS3MenuSection();
            final List<ServiceAndAction> serviceActionsForName = serviceActionsByName.get(serviceName);
            for (ServiceAndAction serviceAndAction : serviceActionsForName) {

                if(serviceAndAction.separator && !menuSection.getServiceActions().isEmpty()) {
                    menu.getSections().add(menuSection);
                    menuSection = new BS3MenuSection();
                }

                ObjectAction objectAction = serviceAndAction.objectAction;
                final String objectType = serviceAndAction.serviceAdapter.getSpecification().getSpecId().asString();
                ServiceActionLayoutData action = new ServiceActionLayoutData(objectType, objectAction.getId());
                action.setNamed(objectAction.getName());
                menuSection.getServiceActions().add(action);
            }
            if(!menuSection.getServiceActions().isEmpty()) {
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

        final Stream<ObjectAction> objectActions = serviceSpec.streamObjectActions(actionType, Contributed.INCLUDED);
        
        objectActions
        // skip if annotated to not be included in repository menu using legacy mechanism
        .filter(objectAction->objectAction.getFacet(NotInServiceMenuFacet.class) == null)
        .forEach(objectAction->{
            final MemberOrderFacet memberOrderFacet = objectAction.getFacet(MemberOrderFacet.class);
            String serviceName = memberOrderFacet != null? memberOrderFacet.name(): null;
            if(Strings.isNullOrEmpty(serviceName)){
                serviceName = serviceSpec.getFacet(NamedFacet.class).value();
            }
            serviceActions.add(new ServiceAndAction(serviceName, serviceAdapter, objectAction));
        });
       
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


    private String tnsAndSchemaLocation() {
        final List<String> parts = Lists.newArrayList();

        parts.add(MB3_TNS);
        parts.add(MB3_SCHEMA_LOCATION);

        parts.add(COMPONENT_TNS);
        parts.add(COMPONENT_SCHEMA_LOCATION);

        parts.add(LINKS_TNS);
        parts.add(LINKS_SCHEMA_LOCATION);

        return Joiner.on(" ").join(parts);
    }


    @Inject
    IsisSessionFactory isisSessionFactory;

    @Inject
    MenuBarsLoaderService menuBarsLoaderService;

    @javax.inject.Inject
    DeploymentCategoryProvider deploymentCategoryProvider;

    @javax.inject.Inject
    MessageService messageService;

    @javax.inject.Inject
    JaxbService jaxbService;

}

